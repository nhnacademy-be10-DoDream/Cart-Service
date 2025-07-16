package shop.dodream.cart.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.dodream.cart.client.BookClient;
import shop.dodream.cart.dto.*;
import shop.dodream.cart.entity.Cart;
import shop.dodream.cart.entity.CartItem;
import shop.dodream.cart.exception.DataNotFoundException;
import shop.dodream.cart.repository.CartItemRepository;
import shop.dodream.cart.repository.CartRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartItemService {
	private final CartItemRepository cartItemRepository;
	private final BookClient bookClient;
	private final CartRepository cartRepository;
	
	@Cacheable(value = "cart",key = "#cartId")
	@Transactional(readOnly = true)
	public List<CartItemResponse> getCartItems(Long cartId) {
		List<CartItem> items = cartItemRepository.findByCart_CartId(cartId);
		
		if (items.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<Long> bookIds = items.stream()
				                     .map(CartItem::getBookId)
				                     .distinct()
				                     .toList();
		
		List<BookListResponseRecord> books = bookClient.getBooksByIds(bookIds);
		
		Map<Long, BookListResponseRecord> bookMap = books.stream()
				                                            .collect(Collectors.toMap(BookListResponseRecord::getBookId, Function.identity()));
		
		return items.stream()
				       .map(item -> {
					       BookListResponseRecord book = bookMap.get(item.getBookId());
					       if (book == null) {
						       throw new DataNotFoundException("도서 목록 정보를 찾을 수 없습니다: id=" + item.getBookId());
					       }
					       return CartItemResponse.of(item, book);
				       })
				       .toList();
	}
	
	@CacheEvict(value = "cart",key="#request.getCartId()")
	@Transactional
	public CartItemResponse addCartItem(CartItemRequest request) {
		Cart cart = cartRepository.findById(request.getCartId())
				            .orElseThrow(() -> new DataNotFoundException("Cart not found with id: " + request.getCartId()));
		
		CartItem existingItem = cartItemRepository.findByCart_CartIdAndBookId(request.getCartId(), request.getBookId());
		
		CartItem cartItemToProcess;
		if (existingItem != null) {
			existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
			cartItemToProcess = existingItem;
		} else {
			cartItemToProcess = new CartItem();
			cartItemToProcess.setBookId(request.getBookId());
			cartItemToProcess.setCart(cart);
			cartItemToProcess.setQuantity(request.getQuantity());
		}
		
		// 도서 정보 일괄 조회 (단 건이어도 동일한 로직 사용)
		Map<Long, BookListResponseRecord> bookMap = fetchBooksInBulk(List.of(cartItemToProcess.getBookId()));
		BookListResponseRecord book = bookMap.get(cartItemToProcess.getBookId());
		
		if (book == null) {
			throw new DataNotFoundException("도서 정보를 찾을 수 없습니다: id=" + cartItemToProcess.getBookId());
		}
		
		// 판매 가격 업데이트 후 저장
		cartItemToProcess.setSalePrice(book.getSalePrice());
		CartItem savedItem = cartItemRepository.save(cartItemToProcess);
		
		return CartItemResponse.of(savedItem, book);
	}
	
	@CacheEvict(value = "cart", key = "#cartId")
	@Transactional
	public CartItemResponse updateCartItemQuantity(Long cartId,Long cartItemId, Long quantity) {
		CartItem item = cartItemRepository.findById(cartItemId)
				                .orElseThrow(() -> new DataNotFoundException("Cart item to update not found"));
		
		// 도서 정보 일괄 조회
		Map<Long, BookListResponseRecord> bookMap = fetchBooksInBulk(List.of(item.getBookId()));
		BookListResponseRecord book = bookMap.get(item.getBookId());
		
		if (book == null) {
			throw new DataNotFoundException("도서를 찾을 수 없습니다: id=" + item.getBookId());
		}
		
		item.setQuantity(quantity);
		item.setSalePrice(book.getSalePrice()); // 가격 정보도 최신 데이터로 업데이트
		CartItem updated = cartItemRepository.save(item);
		
		return CartItemResponse.of(updated, book);
	}
	
	@CacheEvict(value = "cart",key = "#cartId")
	@Transactional
	public void removeAllCartItems(Long cartId) {
		List<CartItem> items = cartItemRepository.findByCart_CartId(cartId);
		if (items.isEmpty()) {
			return;
		}
		cartItemRepository.deleteByCart_CartId(cartId);
	}
	
	@CacheEvict(value = "cart", key = "#cartId")
	@Transactional
	public void removeCartItemByBookId(Long cartId, Long bookId) {
		CartItem item = cartItemRepository.findByCart_CartIdAndBookId(cartId, bookId);
		if (item == null) {
			throw new DataNotFoundException("No cart item found for cartId " + cartId + " and bookId " + bookId);
		}
		cartItemRepository.deleteByCart_CartIdAndBookId(cartId, bookId);
	}
	
	@CacheEvict(value = "cart", key = "#memberCart.getCartId()")
	@Transactional
	public void mergeGuestItemsIntoMemberCart(List<GuestCartItem> guestItems, Cart memberCart) {
		// 1. 일괄 Book 조회
		List<Long> bookIds = guestItems.stream()
				                     .map(GuestCartItem::getBookId)
				                     .distinct()
				                     .collect(Collectors.toList());
		
		List<BookListResponseRecord> books = bookClient.getBooksByIds(bookIds);
		Map<Long, BookListResponseRecord> bookMap = books.stream()
				                                            .collect(Collectors.toMap(BookListResponseRecord::getBookId, Function.identity()));
		
		// 2. 병합
		for (GuestCartItem guestItem : guestItems) {
			Long bookId = guestItem.getBookId();
			BookListResponseRecord book = bookMap.get(bookId);
			if (book == null) {
				throw new DataNotFoundException("Book not found for ID: " + bookId);
			}
			
			CartItem existing = cartItemRepository.findByCart_CartIdAndBookId(memberCart.getCartId(), bookId);
			if (existing != null) {
				existing.setQuantity(existing.getQuantity() + guestItem.getQuantity());
				cartItemRepository.save(existing);
			} else {
				CartItem newItem = new CartItem();
				newItem.setCart(memberCart);
				newItem.setBookId(bookId);
				newItem.setQuantity(guestItem.getQuantity());
				newItem.setSalePrice(book.getSalePrice());
				cartItemRepository.save(newItem);
			}
		}
	}
	
	private Map<Long, BookListResponseRecord> fetchBooksInBulk(List<Long> bookIds) {
		if (bookIds == null || bookIds.isEmpty()) {
			return Collections.emptyMap();
		}
		// 제공된 BookClient의 메소드를 직접 호출합니다.
		List<BookListResponseRecord> books = bookClient.getBooksByIds(bookIds);
		return books.stream()
				       .collect(Collectors.toMap(BookListResponseRecord::getBookId, java.util.function.Function.identity()));
	}
}