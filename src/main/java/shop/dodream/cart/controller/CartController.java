package shop.dodream.cart.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shop.dodream.cart.dto.CartRequest;
import shop.dodream.cart.dto.CartResponse;
import shop.dodream.cart.dto.GuestCartResponse;
import shop.dodream.cart.service.CartService;
import shop.dodream.cart.service.GuestCartService;
import shop.dodream.cart.util.GuestIdUtil;

import java.util.Optional;


@RestController
@RequiredArgsConstructor
@RequestMapping("/carts")
public class CartController {
	
	private final CartService cartService;
	private final GuestCartService guestCartService;
	private final GuestIdUtil guestIdUtil;
	
	// 회원 장바구니 조회
	@GetMapping("/users")
	public ResponseEntity<CartResponse> getUserCart(@RequestHeader("X-USER-ID") String userId) {
		return cartService.getCartByUserId(userId)
				       .map(ResponseEntity::ok)
				       .orElse(ResponseEntity.notFound().build());
	}
	// 게스트Id가 없이 조회할 경우 생성 후 조회
	@GetMapping("/guests")
	public ResponseEntity<GuestCartResponse> getGuestCart(HttpServletRequest request,
	                                                      HttpServletResponse response) {
		String guestId = guestIdUtil.getOrCreateGuestId(request, response);
		GuestCartResponse cartResponse = guestCartService.getCart(guestId);
		return ResponseEntity.ok(cartResponse);
	}
	// 게스트Id가 있을경우 조회
	@GetMapping("/guests/{guestId}")
	public ResponseEntity<CartResponse> getGuestCart(@PathVariable String guestId) {
		Optional<CartResponse> cartResponse = cartService.getCartByGuestId(guestId);
		return cartResponse
				       .map(ResponseEntity::ok)
				       .orElse(ResponseEntity.notFound().build());
	}
	// 장바구니 생성
	@PostMapping
	public ResponseEntity<CartResponse> createCart(@RequestBody @Valid CartRequest request) {
		CartResponse response = cartService.saveCart(request.getUserId(), request.getGuestId());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	// 장바구니 삭제
	@DeleteMapping("/{cartId}")
	public ResponseEntity<Void> deleteCart(@PathVariable Long cartId) {
		cartService.deleteCart(cartId);
		return ResponseEntity.noContent().build();
	}
	// 비회원 장바구니 통합
	@PostMapping("/merge")
	public ResponseEntity<Void> mergeCart(@RequestHeader("X-USER-ID") String userId,
	                                      HttpServletRequest request,
	                                      HttpServletResponse response) {
		String guestId = guestIdUtil.getOrCreateGuestId(request, response);
		cartService.mergeCartOnLogin(userId, guestId);
		return ResponseEntity.ok().build();
	}
}

