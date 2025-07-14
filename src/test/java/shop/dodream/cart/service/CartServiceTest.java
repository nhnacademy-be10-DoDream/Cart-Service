package shop.dodream.cart.service;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import shop.dodream.cart.dto.CartResponse;
import shop.dodream.cart.dto.GuestCart;
import shop.dodream.cart.dto.GuestCartItem;
import shop.dodream.cart.entity.Cart;
import shop.dodream.cart.exception.DataNotFoundException;
import shop.dodream.cart.exception.MissingIdentifierException;
import shop.dodream.cart.repository.CartRepository;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

	@Mock
	CartRepository cartRepository;

	@Mock
	CartItemService cartItemService;

	@Mock
	GuestCartService guestCartService;

	@InjectMocks
	CartService cartService;

	private final String userId = "user123";
	private final String guestId = "guest456";
	private final Long cartId = 1L;
	
	@Test
	@DisplayName("기존 장바구니가 존재할 경우 해당 장바구니를 반환한다")
	void whenCartExists_returnsExistingCart() {
		// given
		Cart existingCart = new Cart(cartId, userId);
		given(cartRepository.findByUserId(userId)).willReturn(Optional.of(existingCart));
		
		// when
		CartResponse response = cartService.getOrCreateUserCart(userId);
		
		// then
		assertThat(response.getCartId()).isEqualTo(cartId);
		assertThat(response.getUserId()).isEqualTo(userId);
		verify(cartRepository, never()).save(any(Cart.class));
	}
	
	@Test
	@DisplayName("기존 장바구니가 없을 경우 새로운 장바구니를 생성하여 반환한다")
	void whenCartNotExists_createsAndReturnsNewCart() {
		// given
		Cart newCart = new Cart(cartId, userId);
		given(cartRepository.findByUserId(userId)).willReturn(Optional.empty());
		given(cartRepository.save(any(Cart.class))).willReturn(newCart);
		
		// when
		CartResponse response = cartService.getOrCreateUserCart(userId);
		
		// then
		assertThat(response.getCartId()).isEqualTo(cartId);
		assertThat(response.getUserId()).isEqualTo(userId);
		verify(cartRepository).save(any(Cart.class));
	}
	
	@Test
	@DisplayName("장바구니 생성 시 동시성 문제(race condition)가 발생하면, 다시 조회를 시도하여 장바구니를 반환한다")
	void whenCreationFailsDueToRaceCondition_retriesAndFindsCart() {
		// given
		Cart raceConditionCart = new Cart(cartId, userId);
		// 처음 조회 시에는 없고, DB 제약 조건 위반 후 다시 조회 시에는 있음
		given(cartRepository.findByUserId(userId))
				.willReturn(Optional.empty())
				.willReturn(Optional.of(raceConditionCart));
		given(cartRepository.save(any(Cart.class))).willThrow(new DataIntegrityViolationException("Unique constraint violation"));
		
		// when
		CartResponse response = cartService.getOrCreateUserCart(userId);
		
		// then
		assertThat(response.getCartId()).isEqualTo(raceConditionCart.getCartId());
		verify(cartRepository, times(2)).findByUserId(userId);
		verify(cartRepository, times(1)).save(any(Cart.class));
	}

	@Test
	@DisplayName("삭제할 장바구니가 존재하면 성공적으로 삭제한다")
	void whenCartExists_deletesSuccessfully() {
		// given
		given(cartRepository.existsById(cartId)).willReturn(true);
		doNothing().when(cartRepository).deleteById(cartId);
		
		// when
		cartService.deleteCart(cartId);
		
		// then
		verify(cartRepository).deleteById(cartId);
	}
	
	@Test
	@DisplayName("삭제할 장바구니가 없으면 DataNotFoundException을 던진다")
	void whenCartDoesNotExist_throwsDataNotFoundException() {
		// given
		Long nonExistentCartId = 999L;
		given(cartRepository.existsById(nonExistentCartId)).willReturn(false);
		
		// when & then
		assertThatThrownBy(() -> cartService.deleteCart(nonExistentCartId))
				.isInstanceOf(DataNotFoundException.class)
				.hasMessage("cart id " + nonExistentCartId + " not exist.");
	}
	
	private GuestCart createGuestCart(List<GuestCartItem> items) {
		GuestCart guestCart = new GuestCart();
		guestCart.setGuestId(guestId);
		guestCart.setItems(items);
		return guestCart;
	}
	
	@Test
	@DisplayName("비회원 장바구니는 있고 회원 장바구니는 없을 때, 회원 장바구니를 생성하고 병합한다")
	void withGuestCartAndNoMemberCart_createsMemberCartAndMerges() {
		// given
		GuestCart guestCart = createGuestCart(List.of(new GuestCartItem(1L, 2L)));
		Cart newMemberCart = new Cart(2L, userId);
		
		given(guestCartService.getRawCart(guestId)).willReturn(guestCart);
		given(cartRepository.findByUserId(userId)).willReturn(Optional.empty());
		given(cartRepository.save(any(Cart.class))).willReturn(newMemberCart);
		
		// when
		cartService.mergeCartOnLogin(userId, guestId);
		
		// then
		verify(cartRepository).save(any(Cart.class));
		verify(cartItemService).mergeGuestItemsIntoMemberCart(guestCart.getItems(), newMemberCart);
		verify(guestCartService).deleteGuestCartWithRetry(guestId);
	}
	
	@Test
	@DisplayName("비회원 장바구니와 회원 장바구니가 모두 있을 때, 기존 회원 장바구니에 병합한다")
	void withGuestCartAndExistingMemberCart_mergesIntoExistingCart() {
		// given
		GuestCart guestCart = createGuestCart(List.of(new GuestCartItem(1L, 2L)));
		Cart existingMemberCart = new Cart(5L, userId);
		
		given(guestCartService.getRawCart(guestId)).willReturn(guestCart);
		given(cartRepository.findByUserId(userId)).willReturn(Optional.of(existingMemberCart));
		
		// when
		cartService.mergeCartOnLogin(userId, guestId);
		
		// then
		verify(cartRepository, never()).save(any(Cart.class));
		verify(cartItemService).mergeGuestItemsIntoMemberCart(guestCart.getItems(), existingMemberCart);
		verify(guestCartService).deleteGuestCartWithRetry(guestId);
	}
	
	@Test
	@DisplayName("비회원 장바구니가 비어있을 경우, 아무 작업도 수행하지 않는다")
	void withEmptyGuestCart_doesNothing() {
		// given
		GuestCart emptyGuestCart = createGuestCart(List.of());
		given(guestCartService.getRawCart(guestId)).willReturn(emptyGuestCart);
		
		// when
		cartService.mergeCartOnLogin(userId, guestId);
		
		// then
		verify(cartRepository, never()).findByUserId(anyString());
		verify(cartItemService, never()).mergeGuestItemsIntoMemberCart(any(), any());
		verify(guestCartService, never()).deleteGuestCartWithRetry(anyString());
	}
	
	@Test
	@DisplayName("비회원 장바구니가 null일 경우, 아무 작업도 수행하지 않는다")
	void withNullGuestCart_doesNothing() {
		// given
		given(guestCartService.getRawCart(guestId)).willReturn(null);
		
		// when
		cartService.mergeCartOnLogin(userId, guestId);
		
		// then
		verify(cartRepository, never()).findByUserId(anyString());
		verify(cartItemService, never()).mergeGuestItemsIntoMemberCart(any(), any());
		verify(guestCartService, never()).deleteGuestCartWithRetry(anyString());
	}
	
	@Test
	@DisplayName("userId 또는 guestId가 비어있으면 MissingIdentifierException을 던진다")
	void whenIdsAreMissing_throwsException() {
		// when & then
		assertThatThrownBy(() -> cartService.mergeCartOnLogin(null, guestId))
				.isInstanceOf(MissingIdentifierException.class)
				.hasMessage("Both userId and guestId must be provided.");
		
		assertThatThrownBy(() -> cartService.mergeCartOnLogin(userId, ""))
				.isInstanceOf(MissingIdentifierException.class)
				.hasMessage("Both userId and guestId must be provided.");
	}
}

