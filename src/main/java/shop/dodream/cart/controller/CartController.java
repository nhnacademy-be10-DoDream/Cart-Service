package shop.dodream.cart.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shop.dodream.cart.dto.CartRequest;
import shop.dodream.cart.dto.CartResponse;
import shop.dodream.cart.service.CartService;

import java.util.Optional;

@RestController("/carts")
@RequiredArgsConstructor
public class CartController {
	
	private final CartService cartService;
	
	
	@GetMapping("/member/{memberId}")
	public ResponseEntity<CartResponse> getMemberCart(@PathVariable String memberId) {
		Optional<CartResponse> response = cartService.getCartByMemberId(memberId);
		return ResponseEntity.ok(response.get());
	}
	
	@GetMapping("/session/{sessionId")
	public ResponseEntity<CartResponse> getSessionCart(@PathVariable String sessionId) {
		Optional<CartResponse> response = cartService.getCartBySessionId(sessionId);
		return ResponseEntity.ok(response.get());
	}
	
	@PostMapping
	public ResponseEntity<CartResponse> createCart(@RequestBody @Valid CartRequest request) {
		CartResponse response = cartService.saveCart(request.getMemberId(), request.getSessionId());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	@DeleteMapping("/{cartId}")
	public ResponseEntity<Void> deleteCart(@PathVariable Long cartId) {
		cartService.deleteCart(cartId);
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("/merge")
	public ResponseEntity<Void> mergeCart(@RequestParam String memberId,
	                                      @RequestParam String sessionId) {
		cartService.mergeCartOnLogin(memberId, sessionId);
		return ResponseEntity.ok().build();
	}
}
