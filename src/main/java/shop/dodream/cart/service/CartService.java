package shop.dodream.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import shop.dodream.cart.dto.*;
import shop.dodream.cart.entity.Cart;
import shop.dodream.cart.exception.DataNotFoundException;
import shop.dodream.cart.exception.MissingIdentifierException;
import shop.dodream.cart.repository.CartRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
	
	private final CartRepository cartRepository;
	private final CartItemService cartItemService;
	private final GuestCartService guestCartService;
	
	@Transactional
	public CartResponse saveCart(String userId, String guestId) {
		if (!StringUtils.hasText(userId) && !StringUtils.hasText(guestId)) {
			throw new MissingIdentifierException("userId or guestId must be provided.");
		}
		Cart cart = new Cart();
		cart.setUserId(userId);
		cart.setGuestId(guestId);
		return CartResponse.of(cartRepository.save(cart));
	}
	
	@Transactional(readOnly = true)
	public Optional<CartResponse> getCartByUserId(String userId) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingIdentifierException("userId must be provided.");
		}
		return cartRepository.findByUserId(userId)
				       .map(cart -> {
					       List<CartItemResponse> itemResponses = cartItemService.getCartItems(cart.getCartId());
					       return CartResponse.of(cart, itemResponses);
				       });
	}
	
	@Transactional(readOnly = true)
	public Optional<CartResponse> getCartByGuestId(String guestId) {
		if(!StringUtils.hasText(guestId)) {
			throw new MissingIdentifierException("guestId must be provided.");
		}
		return cartRepository.findByGuestId(guestId)
				       .map(cart -> {
					       List<CartItemResponse> itemResponses = cartItemService.getCartItems(cart.getCartId());
					       return CartResponse.of(cart, itemResponses);
				       });
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
		guestCartService.deleteCart(guestId);
	}
}