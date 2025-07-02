package shop.dodream.cart.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.dodream.cart.client.BookClient;
import shop.dodream.cart.dto.*;
import shop.dodream.cart.entity.Cart;
import shop.dodream.cart.entity.CartItem;
import shop.dodream.cart.exception.DataNotFoundException;
import shop.dodream.cart.repository.CartItemRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartItemService {
	private final CartItemRepository cartItemRepository;
	private final BookClient bookClient;
	
	@Transactional(readOnly = true)
	public List<CartItemResponse> getCartItems(Long cartId) {
		List<CartItem> items = cartItemRepository.findByCartId(cartId);
		
		List<Long> bookIds = items.stream()
				                     .map(CartItem::getBookId)
				                     .distinct()
				                     .toList();
		
		List<BookDto> books = bookClient.getBooksByIds(bookIds);
		
		Map<Long, BookDto> bookMap = books.stream()
				                             .collect(Collectors.toMap(BookDto::getBookId, Function.identity()));
		
		return items.stream()
				       .map(item -> {
					       BookDto book = bookMap.get(item.getBookId());
					       if (book == null) {
						       throw new DataNotFoundException("도서 정보를 찾을 수 없습니다: id=" + item.getBookId());
					       }
					       return CartItemResponse.of(item, book);
				       })
				       .toList();
	}
	
	
	@Transactional
	public CartItemResponse addCartItem(CartItemRequest request) {
		CartItem existing = cartItemRepository.findByCartIdAndBookId(request.getCartId(), request.getBookId());
		if (existing != null) {
			existing.setQuantity(existing.getQuantity() + request.getQuantity());
			CartItem updated = cartItemRepository.save(existing);
			BookDto book = getBookByIdForItem(updated);
			return CartItemResponse.of(updated, book);
		}
		
		CartItem item = new CartItem();
		item.setBookId(request.getBookId());
		
		BookDto book = getBookByIdForItem(item);
		
		
		item.setCartId(request.getCartId());
		item.setQuantity(request.getQuantity());
		item.setSalePrice(book.getSalePrice());
		
		CartItem saved = cartItemRepository.save(item);
		return CartItemResponse.of(saved, book);
	}
	
	@Transactional
	public CartItemResponse updateCartItemQuantity(Long  cartItemId, Long quantity) {
		CartItem item = cartItemRepository.findById(cartItemId)
				                .orElseThrow(() -> new DataNotFoundException("Cart item to update not found"));
		
		BookDto book = getBookByIdForItem(item);
		
		item.setQuantity(quantity);
		item.setSalePrice(book.getSalePrice());
		CartItem updated = cartItemRepository.save(item);
		return CartItemResponse.of(updated, book);
	}
	
	@Transactional
	public void removeCartItem(Long cartItemId) {
		if(!cartItemRepository.existsById(cartItemId)) {
			throw new DataNotFoundException("CartItem to remove not found");
		}
		cartItemRepository.deleteById(cartItemId);
	}
	
	@Transactional
	public void removeAllCartItems(Long cartId) {
		List<CartItem> items = cartItemRepository.findByCartId(cartId);
		if (items.isEmpty()) {
			return;
		}
		cartItemRepository.deleteByCartId(cartId);
	}
	
	@Transactional
	public void removeCartItemByBookId(Long cartId, Long bookId) {
		CartItem item = cartItemRepository.findByCartIdAndBookId(cartId, bookId);
		if (item == null) {
			throw new DataNotFoundException("No cart item found for cartId " + cartId + " and bookId " + bookId);
		}
		cartItemRepository.deleteByCartIdAndBookId(cartId, bookId);
	}
	
	@Transactional(readOnly = true)
	public CartItem getCartItemByBookId(Long cartId, Long bookId) {
		CartItem item = cartItemRepository.findByCartIdAndBookId(cartId, bookId);
		if (item == null) {
			throw new DataNotFoundException("CartItem not found for cartId " + cartId + " and bookId " + bookId);
		}
		return item;
	}
	
	public void mergeGuestItemsIntoMemberCart(List<GuestCartItem> guestItems, Cart memberCart) {
		// 1. 일괄 Book 조회
		List<Long> bookIds = guestItems.stream()
				                     .map(GuestCartItem::getBookId)
				                     .distinct()
				                     .collect(Collectors.toList());
		
		List<BookDto> books = bookClient.getBooksByIds(bookIds);
		Map<Long, BookDto> bookMap = books.stream()
				                             .collect(Collectors.toMap(BookDto::getBookId, Function.identity()));
		
		// 2. 병합
		for (GuestCartItem guestItem : guestItems) {
			Long bookId = guestItem.getBookId();
			BookDto book = bookMap.get(bookId);
			if (book == null) {
				throw new DataNotFoundException("Book not found for ID: " + bookId);
			}
			
			CartItem existing = cartItemRepository.findByCartIdAndBookId(memberCart.getCartId(), bookId);
			if (existing != null) {
				existing.setQuantity(existing.getQuantity() + guestItem.getQuantity());
				cartItemRepository.save(existing);
			} else {
				CartItem newItem = new CartItem();
				newItem.setCartId(memberCart.getCartId());
				newItem.setBookId(bookId);
				newItem.setQuantity(guestItem.getQuantity());
				newItem.setSalePrice(book.getSalePrice());
				cartItemRepository.save(newItem);
			}
		}
	}
	
	public CartItem getCartItemById(Long cartItemId) {
		return cartItemRepository.findById(cartItemId).orElseThrow(() -> new DataNotFoundException("CartItem to get not found"));
	}
	
	public BookDto getBookByIdForItem(CartItem item) {
		BookDto bookDto = bookClient.getBookById(item.getBookId());
		if (bookDto == null) {
			throw new DataNotFoundException("도서 정보를 찾을 수 없습니다: " + item.getBookId());
		}
		return bookDto;
	}
}
