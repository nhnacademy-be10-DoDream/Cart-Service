package shop.dodream.cart.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.dodream.cart.client.BookClient;
import shop.dodream.cart.dto.BookDto;
import shop.dodream.cart.dto.CartItemRequest;
import shop.dodream.cart.dto.CartItemResponse;
import shop.dodream.cart.entity.CartItem;
import shop.dodream.cart.exception.DataNotFoundException;
import shop.dodream.cart.repository.CartItemRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartItemService {
	private final CartItemRepository cartItemRepository;
	private final BookClient bookClient;
	
	@Transactional(readOnly = true)
	public List<CartItemResponse> getCartItems(Long cartId) {
		List<CartItem> items = cartItemRepository.findByCartId(cartId);
		return items.stream().map(item -> {
			BookDto book = getBookByIdForItem(item);
			return CartItemResponse.of(item, book);
		}).collect(Collectors.toList());
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
		item.setPrice(book.getDiscountPrice());
		
		CartItem saved = cartItemRepository.save(item);
		return CartItemResponse.of(saved, book);
	}
	
	@Transactional
	public CartItemResponse updateCartItemQuantity(Long  cartItemId, Long quantity) {
		CartItem item = cartItemRepository.findById(cartItemId)
				                .orElseThrow(() -> new DataNotFoundException("CartItem not found"));
		
		BookDto book = getBookByIdForItem(item);
		
		item.setQuantity(quantity);
		item.setPrice(book.getDiscountPrice());
		CartItem updated = cartItemRepository.save(item);
		return CartItemResponse.of(updated, book);
	}
	
	@Transactional
	public void removeCartItem(Long cartItemId) {
		if(!cartItemRepository.existsById(cartItemId)) {
			throw new DataNotFoundException("CartItem not found");
		}
		cartItemRepository.deleteById(cartItemId);
	}
	
	@Transactional
	public void removeAllCartItems(Long cartId) {
		List<CartItem> items = cartItemRepository.findByCartId(cartId);
		if (items.isEmpty()) {
			throw new DataNotFoundException("No cart items to remove for cartId " + cartId);
		}
		cartItemRepository.deleteByCartId(cartId);
	}
	
	@Transactional
	public void removeCartItemsByBookId(Long cartId, Long bookId) {
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
		return cartItemRepository.findByCartIdAndBookId(cartId, bookId);
	}
	
	@Transactional(readOnly = true)
	public BookDto getBookByIdForItem(CartItem item) {
		return bookClient.getBookById(item.getBookId());
	}
}
