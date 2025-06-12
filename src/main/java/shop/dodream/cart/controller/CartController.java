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


@RestController
@RequiredArgsConstructor
@RequestMapping("/carts")
public class CartController {
	
	private final CartService cartService;
	private final GuestCartService guestCartService;
	private final GuestIdUtil guestIdUtil;
	
	
	@GetMapping("/user/{userId}")
	public ResponseEntity<CartResponse> getUserCart(@PathVariable String userId) {
		return cartService.getCartByUserId(userId)
				       .map(ResponseEntity::ok)
				       .orElse(ResponseEntity.noContent().build()); // 204 No Content
	}
	
	@GetMapping("/guest")
	public ResponseEntity<GuestCartResponse> getGuestCart(HttpServletRequest request,
	                                                      HttpServletResponse response) {
		String guestId = guestIdUtil.getOrCreateGuestId(request, response);
		GuestCartResponse cartResponse = guestCartService.getCart(guestId);
		return ResponseEntity.ok(cartResponse);
	}
	
	@PostMapping
	public ResponseEntity<CartResponse> createCart(@RequestBody @Valid CartRequest request) {
		CartResponse response = cartService.saveCart(request.getUserId(), request.getGuestId());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	@DeleteMapping("/{cartId}")
	public ResponseEntity<Void> deleteCart(@PathVariable Long cartId) {
		cartService.deleteCart(cartId);
		return ResponseEntity.noContent().build();
	}
	
	
	@DeleteMapping("/guest")
	public ResponseEntity<Void> deleteGuestCart(HttpServletRequest request,
	                                            HttpServletResponse response) {
		String guestId = guestIdUtil.getOrCreateGuestId(request, response);
		guestCartService.deleteCart(guestId);
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("/merge")
	public ResponseEntity<Void> mergeCart(@RequestParam String userId,
	                                      HttpServletRequest request,
	                                      HttpServletResponse response) {
		String guestId = guestIdUtil.getOrCreateGuestId(request, response);
		cartService.mergeCartOnLogin(userId, guestId);
		return ResponseEntity.ok().build();
	}
}

