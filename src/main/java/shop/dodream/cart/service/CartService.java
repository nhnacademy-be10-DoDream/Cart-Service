package shop.dodream.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import shop.dodream.cart.client.BookClient;
import shop.dodream.cart.dto.CartItemResponse;
import shop.dodream.cart.dto.CartResponse;
import shop.dodream.cart.entity.Cart;
import shop.dodream.cart.exception.DataNotFoundException;
import shop.dodream.cart.exception.MissingIdentifierException;
import shop.dodream.cart.repository.CartItemRepository;
import shop.dodream.cart.repository.CartRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
	
	private final CartRepository cartRepository;
	private final CartItemService cartItemService;
	private final CartItemRepository cartItemRepository;
	private final BookClient bookClient;
	
	@Transactional
	public CartResponse saveCart(String memberId, String sessionId) {
		if (memberId == null && !StringUtils.hasText(sessionId)) {
			throw new MissingIdentifierException("memberId or sessionId must be provided.");
		}
		Cart cart = new Cart();
		cart.setMemberId(memberId);
		cart.setSessionId(sessionId);
		return CartResponse.of(cartRepository.save(cart));
	}
	
	@Transactional(readOnly = true)
	public Optional<CartResponse> getCartByMemberId(String memberId) {
		if (memberId == null) {
			throw new MissingIdentifierException("memberId must be provided.");
		}
		Optional<Cart> cartOpt = cartRepository.findByMemberId(memberId);
		if (cartOpt.isEmpty()) {
			return Optional.empty();
		}
		Cart cart = cartOpt.get();
		List<CartItemResponse> itemResponses = cartItemService.getCartItems(cart.getCartId());
		return Optional.of(CartResponse.of(cart, itemResponses));
	}
	
	@Transactional(readOnly = true)
	public Optional<CartResponse> getCartBySessionId(String sessionId) {
		if(!StringUtils.hasText(sessionId)) {
			throw new MissingIdentifierException("sessionId must be provided.");
		}
		Optional<Cart> cartOpt = cartRepository.findBySessionId(sessionId);
		if (cartOpt.isEmpty()) {
			return Optional.empty();
		}
		Cart cart = cartOpt.get();
		List<CartItemResponse> itemResponses = cartItemService.getCartItems(cart.getCartId());
		return Optional.of(CartResponse.of(cart, itemResponses));
	}
	
	
	
	@Transactional
	public void deleteCart(Long cartId) {
		if(!cartRepository.existsById(cartId)) {
			throw new DataNotFoundException("cart id " + cartId + " not exist.");
		}
		cartRepository.deleteById(cartId);
	}
	
	@Transactional
	public void mergeCartOnLogin(String memberId, String sessionId) {
		if (memberId == null || !StringUtils.hasText(sessionId)) {
			throw new MissingIdentifierException("Both memberId and sessionId must be provided.");
		}
		Optional<Cart> guestCartOpt = cartRepository.findBySessionId(sessionId);
		Optional<Cart> memberCartOpt = cartRepository.findByMemberId(memberId);
		
		if (guestCartOpt.isEmpty()) {
			// 비회원 장바구니 없으면 종료
			return;
		}
		
		Cart guestCart = guestCartOpt.get();
		
		Cart memberCart = memberCartOpt.orElseGet(() -> {
			// 회원 장바구니가 없으면 새로 생성
			Cart newCart = new Cart();
			newCart.setMemberId(memberId);
			newCart.setSessionId(null);
			return cartRepository.save(newCart);
		});
		cartItemService.mergeCartItems(guestCart.getCartId(), memberCart.getCartId());
		
		cartRepository.delete(guestCart);
	}
}