package shop.dodream.cart.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import shop.dodream.cart.dto.CartResponse;
import shop.dodream.cart.dto.GuestCartResponse;
import shop.dodream.cart.service.CartService;
import shop.dodream.cart.service.GuestCartService;
import shop.dodream.cart.util.GuestIdUtil;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private CartService cartService;
	
	@MockBean
	private GuestCartService guestCartService;
	
	@MockBean
	private GuestIdUtil guestIdUtil;
	
	// 공통 테스트 데이터
	private final String USER_ID = "user-123";
	private final String GUEST_ID = "guest-abc-789";
	private final Long CART_ID = 1L;
	
	private CartResponse cartResponse;
	private GuestCartResponse guestCartResponse;
	
	@BeforeEach
	void setUp() {
		// 공통 응답 객체 초기화
		cartResponse = new CartResponse(CART_ID, USER_ID, new ArrayList<>());
		guestCartResponse = new GuestCartResponse(GUEST_ID, new ArrayList<>());
	}
	
	@Test
	@DisplayName("[GET] 회원 장바구니 조회/생성 - 성공")
	void getUserCart_Success() throws Exception {
		// given
		given(cartService.getOrCreateUserCart(USER_ID)).willReturn(cartResponse);
		
		// when
		ResultActions resultActions = mockMvc.perform(get("/carts/users")
				                                              .header("X-USER-ID", USER_ID));
		
		// then
		resultActions.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.cartId").value(CART_ID))
				.andExpect(jsonPath("$.userId").value(USER_ID))
				.andDo(print());
		verify(cartService).getOrCreateUserCart(USER_ID);
	}
	
	@Test
	@DisplayName("[DELETE] 회원 장바구니 삭제 - 성공")
	void deleteCart_Success() throws Exception {
		// given
		willDoNothing().given(cartService).deleteCart(CART_ID);
		
		// when
		ResultActions resultActions = mockMvc.perform(delete("/carts/{cartId}", CART_ID));
		
		// then
		resultActions.andExpect(status().isNoContent())
				.andDo(print());
		verify(cartService).deleteCart(CART_ID);
	}
	
	@Test
	@DisplayName("[GET] 비회원 장바구니 조회/생성 (guestId 없음) - 성공")
	void getGuestCart_WithoutGuestId_Success() throws Exception {
		// given
		// guestIdUtil이 특정 guestId를 반환하도록 설정
		given(guestIdUtil.getOrCreateGuestId(any(HttpServletRequest.class), any(HttpServletResponse.class))).willReturn(GUEST_ID);
		given(guestCartService.getCart(GUEST_ID)).willReturn(guestCartResponse);
		
		// when
		ResultActions resultActions = mockMvc.perform(get("/public/carts"));
		
		// then
		resultActions.andExpect(status().isOk())
				.andExpect(jsonPath("$.guestId").value(GUEST_ID))
				.andDo(print());
		verify(guestIdUtil).getOrCreateGuestId(any(HttpServletRequest.class), any(HttpServletResponse.class));
		verify(guestCartService).getCart(GUEST_ID);
	}
	
	@Test
	@DisplayName("[GET] 비회원 장바구니 조회 (guestId 있음) - 성공")
	void getGuestCart_WithGuestId_Success() throws Exception {
		// given
		given(guestCartService.getCart(GUEST_ID)).willReturn(guestCartResponse);
		
		// when
		ResultActions resultActions = mockMvc.perform(get("/public/carts/{guestId}", GUEST_ID));
		
		// then
		resultActions.andExpect(status().isOk())
				.andExpect(jsonPath("$.guestId").value(GUEST_ID))
				.andDo(print());
		verify(guestCartService).getCart(GUEST_ID);
	}
	
	@Test
	@DisplayName("[DELETE] 비회원 장바구니 삭제 - 성공")
	void deleteGuestCart_Success() throws Exception {
		// given
		willDoNothing().given(guestCartService).deleteCart(GUEST_ID);
		
		// when
		ResultActions resultActions = mockMvc.perform(delete("/public/carts/{guestId}", GUEST_ID));
		
		// then
		resultActions.andExpect(status().isNoContent())
				.andDo(print());
		verify(guestCartService).deleteCart(GUEST_ID);
	}
	
	@Test
	@DisplayName("[POST] 비회원 장바구니를 회원 장바구니로 병합 - 성공")
	void mergeCart_Success() throws Exception {
		// given
		willDoNothing().given(cartService).mergeCartOnLogin(USER_ID, GUEST_ID);
		
		// when
		ResultActions resultActions = mockMvc.perform(post("/carts/merge/{guestId}", GUEST_ID)
				                                              .header("X-USER-ID", USER_ID));
		
		// then
		resultActions.andExpect(status().isOk()) // 본문이 없는 200 OK
				.andDo(print());
		verify(cartService).mergeCartOnLogin(USER_ID, GUEST_ID);
	}
	
}

