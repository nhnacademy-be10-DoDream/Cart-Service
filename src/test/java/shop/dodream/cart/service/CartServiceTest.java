package shop.dodream.cart.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
	
	@Test
	void testSaveCartWithUserId() {
		Cart savedCart = new Cart();
		savedCart.setCartId(1L);
		savedCart.setUserId(userId);
		
		given(cartRepository.save(any())).willReturn(savedCart);
		
		CartResponse response = cartService.saveCart(userId, null);
		assertThat(response.getCartId()).isEqualTo(1L);
		assertThat(response.getUserId()).isEqualTo(userId);
	}
	
	@Test
	void testSaveCartWithGuestId() {
		Cart savedCart = new Cart();
		savedCart.setCartId(2L);
		savedCart.setGuestId(guestId);
		
		given(cartRepository.save(any())).willReturn(savedCart);
		
		CartResponse response = cartService.saveCart(null, guestId);
		assertThat(response.getCartId()).isEqualTo(2L);
		assertThat(response.getGuestId()).isEqualTo(guestId);
	}
	
	@Test
	void testSaveCartWithEmptyStrings() {
		assertThatThrownBy(() -> cartService.saveCart("", ""))
				.isInstanceOf(MissingIdentifierException.class);
		assertThatThrownBy(() -> cartService.saveCart(" ", null))
				.isInstanceOf(MissingIdentifierException.class);
	}
	
	@Test
	void testGetCartByUserIdReturnsCart() {
		Cart cart = new Cart();
		cart.setCartId(10L);
		cart.setUserId(userId);
		
		given(cartRepository.findByUserId(userId)).willReturn(Optional.of(cart));
		given(cartItemService.getCartItems(10L)).willReturn(List.of());
		
		Optional<CartResponse> result = cartService.getCartByUserId(userId);
		assertThat(result).isPresent();
		assertThat(result.get().getUserId()).isEqualTo(userId);
	}
	
	@Test
	void testGetCartByUserIdWithInvalidInput() {
		assertThatThrownBy(() -> cartService.getCartByUserId(null))
				.isInstanceOf(MissingIdentifierException.class);
		assertThatThrownBy(() -> cartService.getCartByUserId(""))
				.isInstanceOf(MissingIdentifierException.class);
	}
	
	@Test
	void testGetCartByUserIdReturnsEmpty() {
		given(cartRepository.findByUserId("nonexistent")).willReturn(Optional.empty());
		Optional<CartResponse> result = cartService.getCartByUserId("nonexistent");
		assertThat(result).isEmpty();
	}
	
	@Test
	void testGetCartByGuestIdReturnsCart() {
		Cart cart = new Cart();
		cart.setCartId(20L);
		cart.setGuestId(guestId);
		
		given(cartRepository.findByGuestId(guestId)).willReturn(Optional.of(cart));
		given(cartItemService.getCartItems(20L)).willReturn(List.of());
		
		Optional<CartResponse> result = cartService.getCartByGuestId(guestId);
		assertThat(result).isPresent();
		assertThat(result.get().getGuestId()).isEqualTo(guestId);
	}
	
	@Test
	void testGetCartByGuestIdWithInvalidInput() {
		assertThatThrownBy(() -> cartService.getCartByGuestId(null))
				.isInstanceOf(MissingIdentifierException.class);
		assertThatThrownBy(() -> cartService.getCartByGuestId(" "))
				.isInstanceOf(MissingIdentifierException.class);
	}
	
	@Test
	void testGetCartByGuestIdReturnsEmpty() {
		given(cartRepository.findByGuestId("nonexistent")).willReturn(Optional.empty());
		Optional<CartResponse> result = cartService.getCartByGuestId("nonexistent");
		assertThat(result).isEmpty();
	}
	
	@Test
	void testDeleteCartDeletesWhenExists() {
		given(cartRepository.existsById(1L)).willReturn(true);
		doNothing().when(cartRepository).deleteById(1L);
		
		cartService.deleteCart(1L);
		
		verify(cartRepository).deleteById(1L);
	}
	
	@Test
	void testDeleteCartThrowsExceptionIfNotExists() {
		given(cartRepository.existsById(999L)).willReturn(false);
		
		assertThatThrownBy(() -> cartService.deleteCart(999L))
				.isInstanceOf(DataNotFoundException.class)
				.hasMessageContaining("999");
	}
	
	@Test
	void testMergeCartOnLoginWithValidGuestCartAndNoMemberCart() {
		GuestCart guestCart = new GuestCart();
		guestCart.setGuestId(guestId);
		guestCart.setItems(List.of(new GuestCartItem(1L, 2L)));
		
		given(guestCartService.getRawCart(guestId)).willReturn(guestCart);
		given(cartRepository.findByUserId(userId)).willReturn(Optional.empty());
		
		Cart newCart = new Cart();
		newCart.setCartId(2L);
		newCart.setUserId(userId);
		
		given(cartRepository.save(any())).willReturn(newCart);
		
		doNothing().when(cartItemService).mergeGuestItemsIntoMemberCart(anyList(), eq(newCart));
		doNothing().when(guestCartService).deleteGuestCartWithRetry(guestId);  // 변경된 부분
		
		cartService.mergeCartOnLogin(userId, guestId);
		
		verify(cartItemService).mergeGuestItemsIntoMemberCart(guestCart.getItems(), newCart);
		verify(guestCartService).deleteGuestCartWithRetry(guestId);  // 변경된 부분
	}
	
	
	@Test
	void testMergeCartOnLoginWithExistingMemberCart() {
		GuestCart guestCart = new GuestCart();
		guestCart.setGuestId(guestId);
		guestCart.setItems(List.of(new GuestCartItem(1L, 2L)));
		
		Cart existingCart = new Cart();
		existingCart.setCartId(5L);
		existingCart.setUserId(userId);
		
		given(guestCartService.getRawCart(guestId)).willReturn(guestCart);
		given(cartRepository.findByUserId(userId)).willReturn(Optional.of(existingCart));
		
		doNothing().when(cartItemService).mergeGuestItemsIntoMemberCart(anyList(), eq(existingCart));
		doNothing().when(guestCartService).deleteGuestCartWithRetry(guestId);  // 변경된 부분
		
		cartService.mergeCartOnLogin(userId, guestId);
		
		verify(cartItemService).mergeGuestItemsIntoMemberCart(guestCart.getItems(), existingCart);
		verify(guestCartService).deleteGuestCartWithRetry(guestId);  // 변경된 부분
	}
	
	
	@Test
	void testMergeCartOnLoginWithEmptyGuestCart() {
		GuestCart emptyGuestCart = new GuestCart();
		emptyGuestCart.setGuestId(guestId);
		emptyGuestCart.setItems(List.of());
		
		given(guestCartService.getRawCart(guestId)).willReturn(emptyGuestCart);
		
		// 아무 작업도 수행하지 않음 (병합 안함)
		cartService.mergeCartOnLogin(userId, guestId);
		
		verify(cartItemService, never()).mergeGuestItemsIntoMemberCart(anyList(), any());
		verify(guestCartService, never()).deleteCart(anyString());
	}
	
	@Test
	void testMergeCartOnLoginWithNullGuestCart() {
		given(guestCartService.getRawCart(guestId)).willReturn(null);
		
		cartService.mergeCartOnLogin(userId, guestId);
		
		verify(cartItemService, never()).mergeGuestItemsIntoMemberCart(anyList(), any());
		verify(guestCartService, never()).deleteCart(anyString());
	}
	
	@Test
	void testMergeCartOnLoginThrowsExceptionWhenIdsMissing() {
		assertThatThrownBy(() -> cartService.mergeCartOnLogin(null, guestId))
				.isInstanceOf(MissingIdentifierException.class);
		assertThatThrownBy(() -> cartService.mergeCartOnLogin(userId, ""))
				.isInstanceOf(MissingIdentifierException.class);
	}
}

