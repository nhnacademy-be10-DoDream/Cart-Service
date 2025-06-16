package shop.dodream.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import shop.dodream.cart.client.BookClient;
import shop.dodream.cart.dto.*;
import shop.dodream.cart.entity.Cart;
import shop.dodream.cart.entity.CartItem;
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
	private final GuestCartService guestCartService;
	private final CartItemRepository cartItemRepository;
	private final BookClient bookClient;
	
	@Transactional
	public CartResponse saveCart(String userId, String guestId) {
		if (userId == null && !StringUtils.hasText(guestId)) {
			throw new MissingIdentifierException("userId or guestId must be provided.");
		}
		Cart cart = new Cart();
		cart.setUserId(userId);
		cart.setGuestId(guestId);
		return CartResponse.of(cartRepository.save(cart));
	}
	
	@Transactional(readOnly = true)
	public Optional<CartResponse> getCartByUserId(String userId) {
		if (userId == null) {
			throw new MissingIdentifierException("userId must be provided.");
		}
		Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
		if (cartOpt.isEmpty()) {
			return Optional.empty();
		}
		Cart cart = cartOpt.get();
		List<CartItemResponse> itemResponses = cartItemService.getCartItems(cart.getCartId());
		return Optional.of(CartResponse.of(cart, itemResponses));
	}
	
	@Transactional(readOnly = true)
	public Optional<CartResponse> getCartByGuestId(String guestId) {
		if(!StringUtils.hasText(guestId)) {
			throw new MissingIdentifierException("guestId must be provided.");
		}
		Optional<Cart> cartOpt = cartRepository.findByGuestId(guestId);
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
	public void mergeCartOnLogin(String userId, String guestId) {
		if (userId == null || !StringUtils.hasText(guestId)) {
			throw new MissingIdentifierException("Both userId and guestId must be provided.");
		}
		
		// 1. Redis에서 비회원 장바구니 가져오기
		GuestCart guestCart = guestCartService.getRawCart(guestId);
		if (guestCart == null || guestCart.getItems().isEmpty()) {
			return; // 비회원 장바구니가 비어있으면 병합 불필요
		}
		
		// 2. MySQL에서 회원 장바구니 조회 또는 생성
		Cart memberCart = cartRepository.findByUserId(userId).orElseGet(() -> {
			Cart newCart = new Cart();
			newCart.setUserId(userId);
			newCart.setGuestId(null);
			return cartRepository.save(newCart);
		});
		
		
		// 3. Redis 장바구니 아이템을 회원 장바구니로 병합
		for (GuestCartItem guestItem : guestCart.getItems()) {
			CartItem existing = cartItemRepository.findByCartIdAndBookId(memberCart.getCartId(), guestItem.getBookId());
			BookDto book = bookClient.getBookById(guestItem.getBookId());
			if (existing != null) {
				existing.setQuantity(existing.getQuantity() + guestItem.getQuantity());
				cartItemRepository.save(existing);
			} else {
				
				CartItem newItem = new CartItem();
				newItem.setCartId(memberCart.getCartId());
				newItem.setBookId(guestItem.getBookId());
				newItem.setQuantity(guestItem.getQuantity());
				newItem.setPrice(book.getDiscountPrice());
				cartItemRepository.save(newItem);
			}
		}
		
		// 4. Redis에서 비회원 장바구니 삭제
		guestCartService.deleteCart(guestId);
	}
}