package shop.dodream.cart.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import shop.dodream.cart.client.BookClient;
import shop.dodream.cart.dto.BookDto;
import shop.dodream.cart.dto.CartResponse;
import shop.dodream.cart.dto.GuestCart;
import shop.dodream.cart.dto.GuestCartItem;
import shop.dodream.cart.entity.Cart;
import shop.dodream.cart.exception.DataNotFoundException;
import shop.dodream.cart.exception.MissingIdentifierException;
import shop.dodream.cart.repository.CartRepository;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
public class CartServiceTest {
	
	@Autowired
	private CartService cartService;
	
	@MockBean
	private CartRepository cartRepository;
	
	@MockBean
	private BookClient bookClient;
	
	@MockBean
	private GuestCartService guestCartService;
	
	@MockBean
	private CartItemService cartItemService;
	
	private final String userId = "testUserId";
	private final String guestId = "testGuestId";

	@BeforeEach
	void setup() {
		BookDto mockBook = new BookDto(1L,"testbook",3000L,3000L,10L,"test");
		given(bookClient.getBookById(anyLong())).willReturn(mockBook);
	}
	
	@Test
	void testSaveCartByUserId() {
		CartResponse cartResponse = cartService.saveCart(userId,null);
		
		assertThat(cartResponse).isNotNull();
		assertThat(cartResponse.getUserId()).isEqualTo(userId);
		assertThat(cartResponse.getGuestId()).isNull();
	}
	
	@Test
	void testSaveCartByGuestId() {
		CartResponse cartResponse = cartService.saveCart(null,guestId);
		
		assertThat(cartResponse).isNotNull();
		assertThat(cartResponse.getUserId()).isEqualTo(null);
		assertThat(cartResponse.getGuestId()).isEqualTo(guestId);
	}
	
	@Test
	void testSaveCartFail(){
		assertThatThrownBy(()-> cartService.saveCart(null,null))
				.isInstanceOf(MissingIdentifierException.class);
	}
	
	@Test
	void testGetCartByUserId(){
		cartService.saveCart(userId,null);
		Optional<CartResponse> cartResponse = cartService.getCartByUserId(userId);
		
		assertThat(cartResponse).isPresent();
		assertThat(cartResponse.get().getUserId()).isEqualTo(userId);
		assertThat(cartResponse.get().getItems()).isEmpty();
	}
	
	@Test
	void testGetCartByGuestId(){
		cartService.saveCart(null,guestId);
		Optional<CartResponse> cartResponse = cartService.getCartByGuestId(guestId);
		
		assertThat(cartResponse).isPresent();
		assertThat(cartResponse.get().getGuestId()).isEqualTo(guestId);
		assertThat(cartResponse.get().getItems()).isEmpty();
	}
	
	@Test
	void testDeleteCartByCartId(){
		Cart cart = new Cart();
		cart.setUserId(userId);
		cart.setGuestId(guestId);
		cart = cartRepository.save(cart);
		cartService.deleteCart(cart.getCartId());
		assertThat(cartRepository.findById(cart.getCartId())).isEmpty();
	}
	
	@Test
	void testDeleteCartByCartIdFail(){
		assertThatThrownBy(() -> cartService.deleteCart(999L))
				.isInstanceOf(DataNotFoundException.class);
	}
	
	@Test
	void testMergeCartOnLoginSuccess() {
		GuestCart guestCart = new GuestCart(guestId, new ArrayList<>());
		guestCart.getItems().add(new GuestCartItem(1L, 2L));
		
		given(guestCartService.getRawCart(guestId)).willReturn(guestCart);
		
		Cart newCart = new Cart();
		newCart.setCartId(1L);
		newCart.setUserId(userId);
		
		// findByUserId 첫 호출은 empty, 두번째부터는 새 Cart 반환
		given(cartRepository.findByUserId(userId)).willReturn(Optional.empty(), Optional.of(newCart));
		
		given(cartRepository.save(any(Cart.class))).willAnswer(invocation -> {
			Cart cart = invocation.getArgument(0);
			cart.setCartId(1L); // 저장 시 id 세팅
			return cart;
		});
		
		doNothing().when(cartItemService).mergeGuestItemsIntoMemberCart(anyList(), any(Cart.class));
		doNothing().when(guestCartService).deleteCart(guestId);
		
		cartService.mergeCartOnLogin(userId, guestId);
		
		verify(cartItemService, times(1)).mergeGuestItemsIntoMemberCart(eq(guestCart.getItems()), any(Cart.class));
		verify(guestCartService, times(1)).deleteCart(guestId);
	}
	
	@Test
	void testMergeCartOnLoginNoGuestCart() {
		given(guestCartService.getRawCart(guestId)).willReturn(null);
		
		cartService.mergeCartOnLogin(userId, guestId);
		
		verify(cartItemService, never()).mergeGuestItemsIntoMemberCart(anyList(), any(Cart.class));
		verify(guestCartService, never()).deleteCart(anyString());
	}
	
	@Test
	void testMergeCartOnLoginEmptyGuestCartItems() {
		GuestCart guestCart = new GuestCart(guestId, new ArrayList<>()); // guestId 포함, 빈 리스트
		given(guestCartService.getRawCart(guestId)).willReturn(guestCart);
		
		cartService.mergeCartOnLogin(userId, guestId);
		
		verify(cartItemService, never()).mergeGuestItemsIntoMemberCart(anyList(), any(Cart.class));
		verify(guestCartService, never()).deleteCart(anyString());
	}
	
	@Test
	void testMergeCartOnLoginMissingUserIdOrGuestId() {
		assertThatThrownBy(() -> cartService.mergeCartOnLogin(null, guestId))
				.isInstanceOf(MissingIdentifierException.class);
		
		assertThatThrownBy(() -> cartService.mergeCartOnLogin(userId, ""))
				.isInstanceOf(MissingIdentifierException.class);
	}
}
