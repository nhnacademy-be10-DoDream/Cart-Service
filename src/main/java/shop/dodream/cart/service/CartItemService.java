package shop.dodream.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.dodream.cart.client.BookClient;
import shop.dodream.cart.dto.BookDto;
import shop.dodream.cart.dto.CartItemRequest;
import shop.dodream.cart.dto.CartItemResponse;
import shop.dodream.cart.entity.CartItem;
import shop.dodream.cart.exception.DataNotFoundException;
import shop.dodream.cart.repository.CartItemRepository;
import shop.dodream.cart.util.BookAvailabilityChecker;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartItemService {
	private final CartItemRepository cartItemRepository;
	private final BookClient bookClient;
	
	
	public List<CartItemResponse> getCartItems(Long cartId) {
		List<CartItem> items = cartItemRepository.findByCartId(cartId);
		return items.stream().map(item -> {
			BookDto book = bookClient.getBookById(item.getBookId());
			return CartItemResponse.of(item, book);
		}).collect(Collectors.toList());
	}
	
	
	public CartItemResponse addCartItem(CartItemRequest request) {
		CartItem existing = cartItemRepository.findByCartIdAndBookId(request.getCartId(), request.getBookId());
		if (existing != null) {
			existing.setQuantity(existing.getQuantity() + request.getQuantity());
			CartItem updated = cartItemRepository.save(existing);
			BookDto book = bookClient.getBookById(updated.getBookId());
			return CartItemResponse.of(updated, book);
		}
		
		BookDto book = bookClient.getBookById(request.getBookId());
		boolean isAvailable = BookAvailabilityChecker.isAvailable(book, request.getQuantity());
		
		CartItem item = new CartItem();
		item.setBookId(request.getBookId());
		item.setCartId(request.getCartId());
		item.setQuantity(request.getQuantity());
		item.setPrice(book.getDiscountPrice());
		item.setAvailable(isAvailable);
		
		CartItem saved = cartItemRepository.save(item);
		return CartItemResponse.of(saved, book);
	}
	
	
	public CartItemResponse updateCartItemQuantity(Long  cartItemId, Long quantity) {
		CartItem item = cartItemRepository.findById(cartItemId)
				                .orElseThrow(() -> new DataNotFoundException("CartItem not found"));
		
		BookDto book = bookClient.getBookById(item.getBookId());
		boolean isAvailable = BookAvailabilityChecker.isAvailable(book, quantity);
		
		item.setQuantity(quantity);
		item.setAvailable(isAvailable);
		item.setPrice(book.getDiscountPrice());
		CartItem updated = cartItemRepository.save(item);
		return CartItemResponse.of(updated, book);
	}
	
	
	public void removeCartItem(Long cartItemId) {
		if(!cartItemRepository.existsById(cartItemId)) {
			throw new DataNotFoundException("CartItem not found");
		}
		cartItemRepository.deleteById(cartItemId);
	}
	
	
	public void removeAllCartItems(Long cartId) {
		List<CartItem> items = cartItemRepository.findByCartId(cartId);
		if (items.isEmpty()) {
			throw new DataNotFoundException("No cart items to remove for cartId " + cartId);
		}
		cartItemRepository.deleteByCartId(cartId);
	}
	
	public void removeCartItemsByBookId(Long cartId, Long bookId) {
		CartItem item = cartItemRepository.findByCartIdAndBookId(cartId, bookId);
		if (item == null) {
			throw new DataNotFoundException("No cart item found for cartId " + cartId + " and bookId " + bookId);
		}
		cartItemRepository.deleteByCartIdAndBookId(cartId, bookId);
	}
	
	
	public boolean validateOrderable(Long cartItemId) {
		CartItem item = cartItemRepository.findById(cartItemId)
				                .orElseThrow(() -> new DataNotFoundException("CartItem not found"));
		BookDto book = bookClient.getBookById(item.getBookId());
		return BookAvailabilityChecker.isAvailable(book, item.getQuantity());
	}
	
	
	public CartItem getCartItemByBookId(Long cartId, Long bookId) {
		CartItem item = cartItemRepository.findByCartIdAndBookId(cartId, bookId);
		if (item == null) {
			throw new DataNotFoundException("CartItem not found for cartId " + cartId + " and bookId " + bookId);
		}
		return cartItemRepository.findByCartIdAndBookId(cartId, bookId);
	}
	
	
	public BookDto getBookByIdForItem(CartItem item) {
		return bookClient.getBookById(item.getBookId());
	}
}
