package shop.dodream.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import shop.dodream.cart.dto.*;
import shop.dodream.cart.entity.Cart;
import shop.dodream.cart.exception.DataNotFoundException;
import shop.dodream.cart.exception.MissingIdentifierException;
import shop.dodream.cart.repository.CartRepository;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
	
	private final CartRepository cartRepository;
	private final CartItemService cartItemService;
	private final GuestCartService guestCartService;
	
	@Transactional
	public CartResponse getOrCreateUserCart(String userId) {
		Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
		if (cartOpt.isPresent()) return CartResponse.of(cartOpt.get());
		try {
			Cart newCart = new Cart();
			newCart.setUserId(userId);
			return CartResponse.of(cartRepository.save(newCart));
		} catch (DataIntegrityViolationException e) {
			return cartRepository.findByUserId(userId)
					       .map(CartResponse::of)
					       .orElseThrow(() -> new IllegalStateException("Cart creation failed and cart not found."));
		}
	}
	
	@Transactional
	public void deleteCart(Long cartId) {
		if(!cartRepository.existsById(cartId)) {
			throw new DataNotFoundException("cart id " + cartId + " not exist.");
		}
		cartRepository.deleteById(cartId);
	}
	
	
	public void mergeCartOnLogin(String userId, String guestId) {
		if (!StringUtils.hasText(userId) || !StringUtils.hasText(guestId)) {
			throw new MissingIdentifierException("Both userId and guestId must be provided.");
		}
		
		// 1. Redis에서 비회원 장바구니 가져오기
		GuestCart guestCart = guestCartService.getRawCart(guestId);
		if (guestCart == null || guestCart.getItems().isEmpty()) {
			return;
		}
		
		// 2. 회원 장바구니 조회 또는 생성
		Cart memberCart = cartRepository.findByUserId(userId).orElseGet(() -> {
			Cart newCart = new Cart();
			newCart.setUserId(userId);
			return cartRepository.save(newCart);
		});
		
		// 3. 병합 로직 분리
		cartItemService.mergeGuestItemsIntoMemberCart(guestCart.getItems(), memberCart);
		
		// 4. Redis 비회원 장바구니 삭제
		guestCartService.deleteGuestCartWithRetry(guestId);
	}
	
	
}