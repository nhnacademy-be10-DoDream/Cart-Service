package shop.dodream.cart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shop.dodream.cart.dto.BookDto;
import shop.dodream.cart.dto.CartItemRequest;
import shop.dodream.cart.dto.CartItemResponse;
import shop.dodream.cart.entity.CartItem;
import shop.dodream.cart.exception.MissingIdentifierException;
import shop.dodream.cart.service.CartItemService;

import java.util.List;

@RestController("/cart-items")
@RequiredArgsConstructor
public class CartItemController {
	private final CartItemService cartItemService;
	
	// 장바구니 아이템 목록 조회
	@GetMapping("/{cartId}")
	public ResponseEntity<List<CartItemResponse>> getCartItems(@PathVariable Long cartId) {
		List<CartItemResponse> items = cartItemService.getCartItems(cartId);
		return ResponseEntity.ok(items);
	}
	
	// 장바구니에 아이템 추가
	@PostMapping
	public ResponseEntity<CartItemResponse> addCartItem(@RequestBody CartItemRequest request) {
		if (request.getQuantity() == null || request.getQuantity() <= 0) {
			throw new MissingIdentifierException("Quantity must be greater than zero");
		}
		CartItemResponse response = cartItemService.addCartItem(request);
		if (!response.isAvailable()) {
			throw new MissingIdentifierException("Book stock is insufficient");
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	// 장바구니 아이템 수량 수정
	@PatchMapping("/{cartItemId}/quantity")
	public ResponseEntity<CartItemResponse> updateCartItemQuantity(
			@PathVariable Long cartItemId,
			@RequestParam Long quantity) {
		if (quantity == null || quantity <= 0) {
			throw new MissingIdentifierException("Quantity must be greater than zero");
		}
		CartItemResponse response = cartItemService.updateCartItemQuantity(cartItemId, quantity);
		if (!response.isAvailable()) {
			throw new MissingIdentifierException("Book stock is insufficient");
		}
		return ResponseEntity.ok(response);
	}
	
	// 장바구니 아이템 하나 삭제
	@DeleteMapping("/{cartItemId}")
	public ResponseEntity<Void> removeCartItem(@PathVariable Long cartItemId) {
		cartItemService.removeCartItem(cartItemId);
		return ResponseEntity.noContent().build();
	}
	
	// 특정 장바구니의 전체 아이템 삭제
	@DeleteMapping("/cart/{cartId}")
	public ResponseEntity<Void> removeAllCartItems(@PathVariable Long cartId) {
		cartItemService.removeAllCartItems(cartId);
		return ResponseEntity.noContent().build();
	}
	
	// 특정 책 아이템만 삭제
	@DeleteMapping("/cart/{cartId}/book/{bookId}")
	public ResponseEntity<Void> removeCartItemsByBookId(
			@PathVariable Long cartId,
			@PathVariable Long bookId) {
		cartItemService.removeCartItemsByBookId(cartId, bookId);
		return ResponseEntity.noContent().build();
	}
	
	
	//  장바구니 ID + 책 ID 로 아이템 하나 조회
	@GetMapping("/cart/{cartId}/book/{bookId}")
	public ResponseEntity<CartItemResponse> getCartItemByBookId(
			@PathVariable Long cartId,
			@PathVariable Long bookId) {
		CartItem item = cartItemService.getCartItemByBookId(cartId, bookId);
		BookDto book = cartItemService.getBookByIdForItem(item);
		return ResponseEntity.ok(CartItemResponse.of(item, book));
	}
}
