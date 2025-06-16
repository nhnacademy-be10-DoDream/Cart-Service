package shop.dodream.cart.controller;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import shop.dodream.cart.dto.CartRequest;
import shop.dodream.cart.dto.CartResponse;
import shop.dodream.cart.dto.GuestCartResponse;
import shop.dodream.cart.service.CartService;
import shop.dodream.cart.service.GuestCartService;
import shop.dodream.cart.util.GuestIdUtil;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private CartService cartService;
	
	@MockBean
	private GuestCartService guestCartService;
	
	@MockBean
	private GuestIdUtil guestIdUtil;
	
	private final String guestId = "guest-123";
	private final String userId = "user-abc";
	
	
	@Test
	void getUserCartReturnCart() throws Exception {
		CartResponse response = new CartResponse(1L, userId, null, List.of());
		
		when(cartService.getCartByUserId(userId)).thenReturn(Optional.of(response));
		
		mockMvc.perform(get("/carts/user")
				                .header("X-USER-ID", userId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(userId));
	}
	
	@Test
	void getUserCartReturnNoContentIfEmpty() throws Exception {
		when(cartService.getCartByUserId(userId)).thenReturn(Optional.empty());
		
		mockMvc.perform(get("/carts/user")
				                .header("X-USER-ID", userId))
				.andExpect(status().isNoContent());
	}
	
	@Test
	void filterSetGuestIdCookieIfNotExists() throws Exception {
		when(guestIdUtil.getOrCreateGuestId(any(), any())).thenAnswer(invocation -> {
			HttpServletResponse response = invocation.getArgument(1);
			Cookie cookie = new Cookie("guestId", guestId);
			response.addCookie(cookie);
			return guestId;
		});
		
		GuestCartResponse response = new GuestCartResponse(guestId, List.of());
		when(guestCartService.getCart(guestId)).thenReturn(response);
		
		mockMvc.perform(get("/carts/guest"))
				.andExpect(status().isOk())
				.andExpect(cookie().exists("guestId"));
	}
	
	@Test
	void getGuestCartReturnCart() throws Exception {
		GuestCartResponse response = new GuestCartResponse(guestId, List.of());
		
		when(guestIdUtil.getOrCreateGuestId(any(), any())).thenReturn(guestId);
		when(guestCartService.getCart(guestId)).thenReturn(response);
		
		mockMvc.perform(get("/carts/guest")
				                .cookie(new Cookie("guestId", guestId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.guestId").value(guestId));
	}
	

	@Test
	void getGuestCartByGuestIdReturnCart() throws Exception {
		CartResponse response = new CartResponse(2L, null,guestId, List.of());
		
		when(cartService.getCartByGuestId(guestId)).thenReturn(Optional.of(response));
		
		mockMvc.perform(get("/carts/guest/{guestId}", guestId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cartId").value(2L));
	}
	
	@Test
	void getGuestCartByGuestIdReturnNullIfEmpty() throws Exception {
		when(cartService.getCartByGuestId(guestId)).thenReturn(Optional.empty());
		
		mockMvc.perform(get("/carts/guest/{guestId}", guestId))
				.andExpect(status().isOk())
				.andExpect(content().string("")); // null 본문
	}
	
	
	@Test
	void createCartReturnCreatedCart() throws Exception {
		CartRequest request = new CartRequest(userId, null);
		CartResponse response = new CartResponse(10L, userId, null, List.of());
		
		when(cartService.saveCart(userId, null)).thenReturn(response);
		
		mockMvc.perform(post("/carts")
				                .contentType(MediaType.APPLICATION_JSON)
				                .content("""
                            {
                              "userId": "user-abc",
                              "guestId": null
                            }
                        """))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.cartId").value(10L));
	}
	

	@Test
	void deleteCartReturnNoContent() throws Exception {
		mockMvc.perform(delete("/carts/{cartId}", 1L))
				.andExpect(status().isNoContent());
		
		verify(cartService).deleteCart(1L);
	}
	
	
	@Test
	void deleteGuestCartSucceed() throws Exception {
		when(guestIdUtil.getOrCreateGuestId(any(), any())).thenReturn(guestId);
		
		mockMvc.perform(delete("/carts/guest")
				                .cookie(new Cookie("guestId", guestId)))
				.andExpect(status().isNoContent());
		
		verify(guestCartService).deleteCart(guestId);
	}
	

	@Test
	void mergeCartMergeSuccessfully() throws Exception {
		when(guestIdUtil.getOrCreateGuestId(any(), any())).thenReturn(guestId);
		
		mockMvc.perform(post("/carts/merge")
				                .header("X-USER-ID", userId)
				                .cookie(new Cookie("guestId", guestId)))
				.andExpect(status().isOk());
		
		verify(cartService).mergeCartOnLogin(userId, guestId);
	}
}
