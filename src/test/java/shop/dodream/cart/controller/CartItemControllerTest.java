package shop.dodream.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import shop.dodream.cart.dto.*;
import shop.dodream.cart.service.CartItemService;
import shop.dodream.cart.service.GuestCartService;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartItemController.class)
class CartItemControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean
	private CartItemService cartItemService;
	
	@MockBean
	private GuestCartService guestCartService; // Controller에 의존성이 있으므로 MockBean으로 등록
	
	// 공통 테스트 데이터
	private CartItemResponse cartItemResponse;
	private GuestCartResponse guestCartResponse;
	private final String GUEST_ID = "guest-uuid-12345";
	private final Long CART_ID = 1L;
	private final Long CART_ITEM_ID = 10L;
	private final Long BOOK_ID = 101L;
	
	@BeforeEach
	void setUp() {
		// 회원 장바구니 응답 객체
		cartItemResponse = new CartItemResponse(CART_ITEM_ID, BOOK_ID, "JPA 프로그래밍", 15000L, 2L, "/books/101.jpg");
		// 비회원 장바구니 응답 객체 (필요한 경우 DTO를 정의해야 하지만, 예시로 생성)
		guestCartResponse = new GuestCartResponse(GUEST_ID, List.of(new GuestCartItemResponse(BOOK_ID, "JPA 프로그래밍", 15000L, 1L, "/books/101.jpg")));
	}
	
	// --- 회원 장바구니 API 테스트 ---
	
	@Test
	@DisplayName("[GET] 장바구니 아이템 목록 조회 - 성공")
	void getCartItems_Success() throws Exception {
		// given
		given(cartItemService.getCartItems(CART_ID)).willReturn(List.of(cartItemResponse));
		
		// when
		ResultActions resultActions = mockMvc.perform(get("/carts/{cartId}/cart-items", CART_ID));
		
		// then
		resultActions.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].bookId").value(BOOK_ID))
				.andDo(print());
		verify(cartItemService).getCartItems(CART_ID);
	}
	
	@Test
	@DisplayName("[POST] 장바구니 아이템 추가 - 성공")
	void addCartItem_Success() throws Exception {
		// given
		CartItemRequest request = new CartItemRequest(null, BOOK_ID, 1L);
		String jsonRequest = objectMapper.writeValueAsString(request);
		given(cartItemService.addCartItem(any(CartItemRequest.class))).willReturn(cartItemResponse);
		
		// when
		ResultActions resultActions = mockMvc.perform(post("/carts/{cartId}/cart-items", CART_ID)
				                                              .contentType(MediaType.APPLICATION_JSON)
				                                              .content(jsonRequest));
		
		// then
		resultActions.andExpect(status().isCreated())
				.andExpect(jsonPath("$.bookId").value(BOOK_ID))
				.andDo(print());
		
		ArgumentCaptor<CartItemRequest> captor = ArgumentCaptor.forClass(CartItemRequest.class);
		verify(cartItemService).addCartItem(captor.capture());
		assertThat(captor.getValue().getCartId()).isEqualTo(CART_ID);
	}
	
	@Test
	@DisplayName("[PUT] 장바구니 아이템 수량 수정 - 성공")
	void updateCartItemQuantity_Success() throws Exception {
		// given
		Long newQuantity = 5L;
		CartItemRequest request = new CartItemRequest(null, null, newQuantity);
		String jsonRequest = objectMapper.writeValueAsString(request);
		given(cartItemService.updateCartItemQuantity(CART_ID, CART_ITEM_ID, newQuantity)).willReturn(cartItemResponse);
		
		// when
		ResultActions resultActions = mockMvc.perform(put("/carts/{cartId}/cart-items/{cartItemId}/quantity", CART_ID, CART_ITEM_ID)
				                                              .contentType(MediaType.APPLICATION_JSON)
				                                              .content(jsonRequest));
		
		// then
		resultActions.andExpect(status().isOk())
				.andExpect(jsonPath("$.cartItemId").value(CART_ITEM_ID))
				.andDo(print());
		verify(cartItemService).updateCartItemQuantity(CART_ID, CART_ITEM_ID, newQuantity);
	}
	
	@Test
	@DisplayName("[DELETE] 장바구니 전체 아이템 삭제 - 성공")
	void removeAllCartItems_Success() throws Exception {
		// given
		willDoNothing().given(cartItemService).removeAllCartItems(CART_ID);
		
		// when
		ResultActions resultActions = mockMvc.perform(delete("/carts/{cartId}/cart-items", CART_ID));
		
		// then
		resultActions.andExpect(status().isNoContent()).andDo(print());
		verify(cartItemService).removeAllCartItems(CART_ID);
	}
	
	@Test
	@DisplayName("[DELETE] 장바구니 특정 책 아이템 삭제 - 성공")
	void removeCartItemByBookId_Success() throws Exception {
		// given
		willDoNothing().given(cartItemService).removeCartItemByBookId(CART_ID, BOOK_ID);
		
		// when
		ResultActions resultActions = mockMvc.perform(delete("/carts/{cartId}/cart-items/books/{bookId}", CART_ID, BOOK_ID));
		
		// then
		resultActions.andExpect(status().isNoContent()).andDo(print());
		verify(cartItemService).removeCartItemByBookId(CART_ID, BOOK_ID);
	}
	
	
	// --- 비회원 장바구니 API 테스트 ---
	@Test
	@DisplayName("[POST] 비회원 장바구니 아이템 추가 - 성공")
	void addGuestCartItem_Success() throws Exception {
		// given
		GuestCartItemRequest request = new GuestCartItemRequest(BOOK_ID, 1L);
		String jsonRequest = objectMapper.writeValueAsString(request);
		given(guestCartService.addCartItem(eq(GUEST_ID), any(GuestCartItemRequest.class))).willReturn(guestCartResponse);
		
		// when
		ResultActions resultActions = mockMvc.perform(post("/public/carts/{guestId}/cart-items", GUEST_ID)
				                                              .contentType(MediaType.APPLICATION_JSON)
				                                              .content(jsonRequest));
		
		// then
		resultActions.andExpect(status().isCreated())
				.andExpect(jsonPath("$.guestId").value(GUEST_ID))
				.andExpect(jsonPath("$.items[0].bookId").value(BOOK_ID))
				.andDo(print());
		
		ArgumentCaptor<GuestCartItemRequest> captor = ArgumentCaptor.forClass(GuestCartItemRequest.class);
		verify(guestCartService).addCartItem(eq(GUEST_ID), captor.capture());
		assertThat(captor.getValue().getBookId()).isEqualTo(BOOK_ID);
	}
	
	@Test
	@DisplayName("[PUT] 비회원 장바구니 아이템 수량 수정 - 성공")
	void updateGuestCartItemQuantity_Success() throws Exception {
		// given
		Long newQuantity = 3L;
		GuestCartItemRequest request = new GuestCartItemRequest(BOOK_ID, newQuantity);
		String jsonRequest = objectMapper.writeValueAsString(request);
		given(guestCartService.updateQuantity(GUEST_ID, BOOK_ID, newQuantity)).willReturn(guestCartResponse);
		
		// when
		ResultActions resultActions = mockMvc.perform(put("/public/carts/{guestId}/quantity", GUEST_ID)
				                                              .contentType(MediaType.APPLICATION_JSON)
				                                              .content(jsonRequest));
		
		// then
		resultActions.andExpect(status().isOk())
				.andExpect(jsonPath("$.guestId").value(GUEST_ID))
				.andDo(print());
		verify(guestCartService).updateQuantity(GUEST_ID, BOOK_ID, newQuantity);
	}
	
	@Test
	@DisplayName("[DELETE] 비회원 장바구니 특정 책 아이템 삭제 - 성공")
	void removeGuestCartItem_Success() throws Exception {
		// given
		willDoNothing().given(guestCartService).removeItem(GUEST_ID, BOOK_ID);
		
		// when
		ResultActions resultActions = mockMvc.perform(delete("/public/carts/{guestId}/cart-items/books/{bookId}", GUEST_ID, BOOK_ID));
		
		// then
		resultActions.andExpect(status().isNoContent()).andDo(print());
		verify(guestCartService).removeItem(GUEST_ID, BOOK_ID);
	}
	
	@Test
	@DisplayName("[POST] 회원 장바구니 추가 - quantity가 0이면 400 Bad Request")
	void addCartItem_WithInvalidQuantity_ShouldReturnBadRequest() throws Exception {
		// given
		CartItemRequest invalidRequest = new CartItemRequest(null, BOOK_ID, 0L); // @Min(1) 위반
		String jsonRequest = objectMapper.writeValueAsString(invalidRequest);
		
		// when
		ResultActions resultActions = mockMvc.perform(post("/carts/{cartId}/cart-items", CART_ID)
				                                              .contentType(MediaType.APPLICATION_JSON)
				                                              .content(jsonRequest));
		
		// then
		resultActions.andExpect(status().isBadRequest()).andDo(print());
	}
	
	@Test
	@DisplayName("[POST] 비회원 장바구니 추가 - quantity가 null이면 400 Bad Request")
	void addGuestCartItem_WithNullQuantity_ShouldReturnBadRequest() throws Exception {
		// given
		// GuestCartItemRequest DTO에 @NotNull, @Min(1)이 있다고 가정
		GuestCartItemRequest invalidRequest = new GuestCartItemRequest(BOOK_ID, null);
		String jsonRequest = objectMapper.writeValueAsString(invalidRequest);
		
		// when
		ResultActions resultActions = mockMvc.perform(post("/public/carts/{guestId}/cart-items", GUEST_ID)
				                                              .contentType(MediaType.APPLICATION_JSON)
				                                              .content(jsonRequest));
		
		// then
		resultActions.andExpect(status().isBadRequest()).andDo(print());
	}
	
}