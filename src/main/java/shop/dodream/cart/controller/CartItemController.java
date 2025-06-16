package shop.dodream.cart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shop.dodream.cart.dto.*;
import shop.dodream.cart.entity.CartItem;
import shop.dodream.cart.exception.MissingIdentifierException;
import shop.dodream.cart.service.CartItemService;
import shop.dodream.cart.service.GuestCartService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CartItemController {
	private final CartItemService cartItemService;
	private final GuestCartService guestCartService;
	
	// 장바구니 아이템 목록 조회
	@GetMapping("/carts/{cartId}/cart-items")
	public ResponseEntity<List<CartItemResponse>> getCartItems(@PathVariable Long cartId) {
		List<CartItemResponse> items = cartItemService.getCartItems(cartId);
		return ResponseEntity.ok(items);
	}
	
	// 장바구니에 아이템 추가
	@PostMapping("/carts/{cartId}/cart-items")
	public ResponseEntity<CartItemResponse> addCartItem(@RequestBody CartItemRequest request) {
		CartItemResponse response = cartItemService.addCartItem(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	// 장바구니 아이템 수량 수정
	@PatchMapping("/carts/{cartId}/cart-items/{cartItemId}/quantity")
	public ResponseEntity<CartItemResponse> updateCartItemQuantity(@PathVariable Long cartId,
			@PathVariable Long cartItemId,
			@RequestParam Long quantity) {
		CartItemResponse response = cartItemService.updateCartItemQuantity(cartItemId, quantity);
		return ResponseEntity.ok(response);
	}
	
	// 장바구니 아이템 하나 삭제
	@DeleteMapping("/carts/{cartId}/cart-items/{cartItemId}")
	public ResponseEntity<Void> removeCartItem(@PathVariable Long cartId, @PathVariable Long cartItemId) {
		cartItemService.removeCartItem(cartItemId);
		return ResponseEntity.noContent().build();
	}
	
	// 특정 장바구니의 전체 아이템 삭제
	@DeleteMapping("/carts/{cartId}/cart-items")
	public ResponseEntity<Void> removeAllCartItems(@PathVariable Long cartId) {
		cartItemService.removeAllCartItems(cartId);
		return ResponseEntity.noContent().build();
	}
	
	// 게스트 장바구니 아이템 추가
	@PostMapping("/carts/guest/{guestId}/cart-items")
	public ResponseEntity<GuestCartResponse> addGuestCartItem(@PathVariable String guestId,@RequestBody GuestCartItemRequest request) {
		GuestCartResponse response = guestCartService.addCartItem(guestId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	// 특정 책 아이템만 삭제
	@DeleteMapping("/carts/{cartId}/book/{bookId}")
	public ResponseEntity<Void> removeCartItemsByBookId(
			@PathVariable Long cartId,
			@PathVariable Long bookId) {
		cartItemService.removeCartItemsByBookId(cartId, bookId);
		return ResponseEntity.noContent().build();
	}
	
	
	//  장바구니 ID + 책 ID 로 아이템 하나 조회
	@GetMapping("/carts/{cartId}/book/{bookId}")
	public ResponseEntity<CartItemResponse> getCartItemByBookId(
			@PathVariable Long cartId,
			@PathVariable Long bookId) {
		CartItem item = cartItemService.getCartItemByBookId(cartId, bookId);
		BookDto book = cartItemService.getBookByIdForItem(item);
		return ResponseEntity.ok(CartItemResponse.of(item, book));
	}
}
