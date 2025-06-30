package shop.dodream.cart.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.ws.rs.PUT;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shop.dodream.cart.dto.*;
import shop.dodream.cart.entity.CartItem;
import shop.dodream.cart.service.CartItemService;
import shop.dodream.cart.service.GuestCartService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CartItemController {
	private final CartItemService cartItemService;
	private final GuestCartService guestCartService;
	
	// 장바구니 아이템 목록 조회
	@Operation(summary = "장바구니의 항목 조회", description = "장바구니 아이디로 장바구니에 들어있는 항목을 조회합니다.")
	@GetMapping("/carts/{cartId}/cart-items")
	public ResponseEntity<List<CartItemResponse>> getCartItems(@PathVariable Long cartId) {
		List<CartItemResponse> items = cartItemService.getCartItems(cartId);
		return ResponseEntity.ok(items);
	}
	
	// 장바구니에 아이템 추가
	@Operation(summary = "장바구니 항목 추가", description = "장바구니 아이디로 장바구니에 항목을 추가합니다.")
	@PostMapping("/carts/{cartId}/cart-items")
	public ResponseEntity<CartItemResponse> addCartItem(@PathVariable Long cartId,
	                                                    @RequestBody @Valid CartItemRequest request) {
		request.setCartId(cartId);
		CartItemResponse response = cartItemService.addCartItem(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	// 장바구니 아이템 수량 수정
	@Operation(summary = "장바구니 항목의 수량 변경", description = "장바구니 아이디로 장바구니에 항목의 수량을 수정합니다.")
	@PutMapping("/carts/{cartId}/cart-items/{cartItemId}/quantity")
	public ResponseEntity<CartItemResponse> updateCartItemQuantity(@RequestBody @Valid CartItemRequest request,
	                                                               @PathVariable Long cartItemId, @PathVariable Long cartId) {
		CartItemResponse response = cartItemService.updateCartItemQuantity(cartItemId, request.getQuantity());
		return ResponseEntity.ok(response);
	}
	
	// 장바구니 아이템 하나 삭제
	@Operation(summary = "장바구니 항목 삭제", description = "장바구니의 항목 단건 삭제합니다.")
	@DeleteMapping("/carts/cart-items/{cartItemId}")
	public ResponseEntity<Void> removeCartItem(@PathVariable Long cartItemId) {
		cartItemService.removeCartItem(cartItemId);
		return ResponseEntity.noContent().build();
	}
	
	// 특정 장바구니의 전체 아이템 삭제
	@Operation(summary = "특정 장바구니의 전체 아아템 삭제", description = "장바구니 아이디로 특정 장바구니를 조회 후 항목을 전체 삭제합니다.")
	@DeleteMapping("/carts/{cartId}/cart-items")
	public ResponseEntity<Void> removeAllCartItems(@PathVariable Long cartId) {
		cartItemService.removeAllCartItems(cartId);
		return ResponseEntity.noContent().build();
	}
	
	// 게스트 장바구니 아이템 추가
	@Operation(summary = "비회원 장바구니 항목 추가", description = "비회원 장바구니를 조회하고 장바구니 항목을 추가합니다.")
	@PostMapping("/carts/guests/{guestId}/cart-items")
	public ResponseEntity<GuestCartResponse> addGuestCartItem(@PathVariable String guestId,@RequestBody @Valid GuestCartItemRequest request) {
		GuestCartResponse response = guestCartService.addCartItem(guestId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	// 게스트 장바구니 아이템 도서 삭제
	@Operation(summary = "비회원 장바구니 도서 하나 삭제", description = "비회원 장바구니를 조회하고 도서아이디로 도서를 조회 후 도서를 삭제합니다.")
	@DeleteMapping("/carts/guests/{guestId}/cart-items/books/{bookId}")
	public ResponseEntity<Void> removeGuestCartItem(@PathVariable String guestId, @PathVariable Long bookId) {
		guestCartService.removeItem(guestId,bookId);
		return ResponseEntity.noContent().build();
	}
	
	
	// 특정 책 아이템만 삭제
	@Operation(summary = "특정 책 아이템만 삭제", description = "장바구니를 조회 후 도서 아이디로 장바구니 항목에서 조회 후 삭제합니다.")
	@DeleteMapping("/carts/{cartId}/cart-items/books/{bookId}")
	public ResponseEntity<Void> removeCartItemsByBookId(
			@PathVariable Long cartId,
			@PathVariable Long bookId) {
		cartItemService.removeCartItemByBookId(cartId, bookId);
		return ResponseEntity.noContent().build();
	}
	
	
	// 장바구니 ID + 책 ID 로 아이템 하나 조회
	@Operation(summary = "장바구니 항목 단건 조회(장바구니 아이디와 도서 아이디로)", description = "장바구니아이디와 도서아이디로 조회 후 장바구니 항목 단건 조회합니다.")
	@GetMapping("/carts/{cartId}/cart-items/books/{bookId}")
	public ResponseEntity<CartItemResponse> getCartItemByBookId(
			@PathVariable Long cartId,
			@PathVariable Long bookId) {
		CartItem item = cartItemService.getCartItemByBookId(cartId, bookId);
		BookDto book = cartItemService.getBookByIdForItem(item);
		return ResponseEntity.ok(CartItemResponse.of(item, book));
	}
	
	// 특정 장바구니 항목 단건 조회
	@Operation(summary = "특정 장바구니 항목 단건 조회", description = "장바구니에서 특정 장바구니 항목을 단건 조회합니다.")
	@GetMapping("/cart-items/{cartItemId}")
	public ResponseEntity<CartItemResponse> getCartItem(@PathVariable Long cartItemId) {
		CartItem item = cartItemService.getCartItemById(cartItemId);
		BookDto book = cartItemService.getBookByIdForItem(item);
		return ResponseEntity.ok(CartItemResponse.of(item, book));
	}
}
