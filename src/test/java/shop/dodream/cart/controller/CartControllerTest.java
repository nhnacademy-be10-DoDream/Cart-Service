package shop.dodream.cart.controller;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import shop.dodream.cart.dto.CartResponse;
import shop.dodream.cart.dto.GuestCartResponse;
import shop.dodream.cart.service.CartService;
import shop.dodream.cart.service.GuestCartService;
import shop.dodream.cart.util.GuestIdUtil;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartController 단위 테스트")
class CartControllerTest {
	
	@Mock
	private CartService cartService;
	
	@Mock
	private GuestCartService guestCartService;
	
	@Mock
	private GuestIdUtil guestIdUtil;
	
	@InjectMocks
	private CartController cartController;
	
	private MockMvc mockMvc;
	
	private final String guestId = "guest-123";
	private final String userId = "user-abc";
	private final Long cartId = 1L;
	
	@BeforeEach
	void setUp() {
		// 컨트롤러를 독립적으로 테스트하기 위해 standaloneSetup 사용
		mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
	}
	
	@Test
	@DisplayName("성공 시 200 OK와 함께 장바구니 정보를 반환한다")
	void getUserCart_success() throws Exception {
		// given
		CartResponse cartResponse = new CartResponse(cartId, userId, new ArrayList<>());
		given(cartService.getOrCreateUserCart(userId)).willReturn(cartResponse);
		
		// when & then
		mockMvc.perform(get("/carts/users")
				                .header("X-USER-ID", userId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(userId))
				.andExpect(jsonPath("$.cartId").value(cartId));
		
		verify(cartService).getOrCreateUserCart(userId);
	}
	
	@Test
	@DisplayName("GET /public/carts: 쿠키가 없을 경우, guestId를 생성하고 장바구니를 반환한다")
	void getGuestCart_whenNoCookie_createsAndReturnsCart() throws Exception {
		// given
		GuestCartResponse guestCartResponse = new GuestCartResponse(guestId, new ArrayList<>());
		
		given(guestIdUtil.getOrCreateGuestId(any(HttpServletRequest.class), any(HttpServletResponse.class)))
				.willReturn(guestId);
		given(guestCartService.getCart(guestId)).willReturn(guestCartResponse);
		
		// when & then
		mockMvc.perform(get("/public/carts"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.guestId").value(guestId));
		
		verify(guestIdUtil).getOrCreateGuestId(any(HttpServletRequest.class), any(HttpServletResponse.class));
		verify(guestCartService).getCart(guestId);
	}
	
	@Test
	@DisplayName("GET /public/carts: 쿠키가 있을 경우, 기존 guestId를 사용하여 장바구니를 반환한다")
	void getGuestCart_whenCookieExists_returnsCart() throws Exception {
		// given
		GuestCartResponse guestCartResponse = new GuestCartResponse(guestId, new ArrayList<>());
		given(guestIdUtil.getOrCreateGuestId(any(HttpServletRequest.class), any(HttpServletResponse.class)))
				.willReturn(guestId);
		given(guestCartService.getCart(guestId)).willReturn(guestCartResponse);
		
		// when & then
		mockMvc.perform(get("/public/carts")
				                .cookie(new Cookie("guestId", guestId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.guestId").value(guestId));
		
		verify(guestIdUtil).getOrCreateGuestId(any(HttpServletRequest.class), any(HttpServletResponse.class));
		verify(guestCartService).getCart(guestId);
	}
	
	@Test
	@DisplayName("GET /public/carts/{guestId}: 경로 변수로 주어진 guestId로 장바구니를 반환한다")
	void getGuestCart_withPathVariable_returnsCart() throws Exception {
		// given
		GuestCartResponse guestCartResponse = new GuestCartResponse(guestId, new ArrayList<>());
		given(guestCartService.getCart(guestId)).willReturn(guestCartResponse);
		
		// when & then
		mockMvc.perform(get("/public/carts/{guestId}", guestId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.guestId").value(guestId));
		
		verify(guestCartService).getCart(guestId);
	}
	
	@Test
	@DisplayName("DELETE /carts/{cartId}: 회원 장바구니를 성공적으로 삭제하고 204 No Content를 반환한다")
	void deleteCart_success() throws Exception {
		// given
		doNothing().when(cartService).deleteCart(cartId);
		
		// when & then
		mockMvc.perform(delete("/carts/{cartId}", cartId))
				.andExpect(status().isNoContent());
		
		verify(cartService).deleteCart(cartId);
	}
	
	@Test
	@DisplayName("DELETE /public/carts/{guestId}: 비회원 장바구니를 성공적으로 삭제하고 204 No Content를 반환한다")
	void deleteGuestCart_success() throws Exception {
		// given
		doNothing().when(guestCartService).deleteCart(guestId);
		
		// when & then
		mockMvc.perform(delete("/public/carts/{guestId}", guestId))
				.andExpect(status().isNoContent());
		
		verify(guestCartService).deleteCart(guestId);
	}
	
	@Test
	@DisplayName("성공적으로 장바구니를 병합하고 200 OK를 반환한다")
	void mergeCart_success() throws Exception {
		// given
		doNothing().when(cartService).mergeCartOnLogin(userId, guestId);
		
		// when & then
		mockMvc.perform(post("/carts/merge/{guestId}", guestId)
				                .header("X-USER-ID", userId))
				.andExpect(status().isOk());
		
		verify(cartService).mergeCartOnLogin(userId, guestId);
	}
	
}

