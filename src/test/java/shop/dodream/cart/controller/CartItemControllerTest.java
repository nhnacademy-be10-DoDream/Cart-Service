package shop.dodream.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import shop.dodream.cart.dto.*;
import shop.dodream.cart.service.CartItemService;
import shop.dodream.cart.service.GuestCartService;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartItemController 단위 테스트")
class CartItemControllerTest {
	
	@Mock
	private CartItemService cartItemService;
	@Mock
	private GuestCartService guestCartService;
	
	@InjectMocks
	private CartItemController cartItemController;
	
	private MockMvc mockMvc;
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	private final Long cartId = 1L;
	private final Long cartItemId = 10L;
	private final Long bookId = 100L;
	private final String guestId = "guest-123";
	
	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(cartItemController).build();
	}
	
	@Test
	@DisplayName("GET /carts/{cartId}/cart-items: 아이템 목록을 성공적으로 조회한다")
	void getCartItems_success() throws Exception {
		// given
		List<CartItemResponse> items = List.of(new CartItemResponse(cartItemId, bookId, "Test Book", 10000L, 2L, "url"));
		given(cartItemService.getCartItems(cartId)).willReturn(items);
		
		// when & then
		mockMvc.perform(get("/carts/{cartId}/cart-items", cartId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("Test Book"));
		verify(cartItemService).getCartItems(cartId);
	}
	
	@Test
	@DisplayName("POST /carts/{cartId}/cart-items: 아이템을 성공적으로 추가하고 201 Created를 반환한다")
	void addCartItem_success() throws Exception {
		// given
		CartItemRequest request = new CartItemRequest(null, bookId, 2L); // cartId는 controller에서 설정됨
		CartItemResponse response = new CartItemResponse(cartItemId, bookId, "New Book", 12000L, 2L, "url");
		given(cartItemService.addCartItem(any(CartItemRequest.class))).willReturn(response);
		
		// when & then
		mockMvc.perform(post("/carts/{cartId}/cart-items", cartId)
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("New Book"));
		verify(cartItemService).addCartItem(any(CartItemRequest.class));
	}
	
	@Test
	@DisplayName("PUT /carts/{cartId}/cart-items/{cartItemId}/quantity: 아이템 수량을 성공적으로 변경한다")
	void updateCartItemQuantity_success() throws Exception {
		// given
		CartItemRequest request = new CartItemRequest(null, null, 5L);
		CartItemResponse response = new CartItemResponse(cartItemId, bookId, "Updated Book", 10000L, 5L, "url");
		given(cartItemService.updateCartItemQuantity(cartItemId, 5L)).willReturn(response);
		
		// when & then
		mockMvc.perform(put("/carts/{cartId}/cart-items/{cartItemId}/quantity", cartId, cartItemId)
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.quantity").value(5));
		verify(cartItemService).updateCartItemQuantity(cartItemId, 5L);
	}
	
	@Test
	@DisplayName("DELETE /carts/{cartId}/cart-items/{cartItemId}: 특정 아이템을 성공적으로 삭제한다")
	void removeCartItem_success() throws Exception {
		// given
		doNothing().when(cartItemService).removeCartItem(cartItemId);
		// when & then
		mockMvc.perform(delete("/carts/{cartId}/cart-items/{cartItemId}", cartId, cartItemId))
				.andExpect(status().isNoContent());
		verify(cartItemService).removeCartItem(cartItemId);
	}
	
	@Test
	@DisplayName("DELETE /carts/{cartId}/cart-items: 모든 아이템을 성공적으로 삭제한다")
	void removeAllCartItems_success() throws Exception {
		// given
		doNothing().when(cartItemService).removeAllCartItems(cartId);
		// when & then
		mockMvc.perform(delete("/carts/{cartId}/cart-items", cartId))
				.andExpect(status().isNoContent());
		verify(cartItemService).removeAllCartItems(cartId);
	}
	
	@Test
	@DisplayName("DELETE /carts/{cartId}/cart-items/books/{bookId}: 특정 도서 아이템을 성공적으로 삭제한다")
	void removeCartItemByBookId_success() throws Exception {
		// given
		doNothing().when(cartItemService).removeCartItemByBookId(cartId, bookId);
		// when & then
		mockMvc.perform(delete("/carts/{cartId}/cart-items/books/{bookId}", cartId, bookId))
				.andExpect(status().isNoContent());
		verify(cartItemService).removeCartItemByBookId(cartId, bookId);
	}
	
	@Test
	@DisplayName("POST /public/carts/{guestId}/cart-items: 아이템을 성공적으로 추가하고 201 Created를 반환한다")
	void addGuestCartItem_success() throws Exception {
		// given
		GuestCartItemRequest request = new GuestCartItemRequest(bookId, 1L);
		GuestCartResponse response = new GuestCartResponse(guestId, List.of(new GuestCartItemResponse(bookId, "Guest Book", 1L, 5000L, "url")));
		given(guestCartService.addCartItem(eq(guestId), any(GuestCartItemRequest.class))).willReturn(response);
		
		// when & then
		mockMvc.perform(post("/public/carts/{guestId}/cart-items", guestId)
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.guestId").value(guestId))
				.andExpect(jsonPath("$.items[0].title").value("Guest Book"));
		verify(guestCartService).addCartItem(eq(guestId), any(GuestCartItemRequest.class));
	}
	
	@Test
	@DisplayName("PUT /public/carts/{guestId}/quantity: 아이템 수량을 성공적으로 변경한다")
	void updateGuestCartItemQuantity_success() throws Exception {
		// given
		GuestCartItemRequest request = new GuestCartItemRequest(bookId, 3L);
		GuestCartResponse response = new GuestCartResponse(guestId, List.of(new GuestCartItemResponse(bookId, "Updated Guest Book", 3L, 5000L, "url")));
		given(guestCartService.updateQuantity(guestId, bookId, 3L)).willReturn(response);
		
		// when & then
		mockMvc.perform(put("/public/carts/{guestId}/quantity", guestId)
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].quantity").value(3));
		verify(guestCartService).updateQuantity(guestId, bookId, 3L);
	}
	
	@Test
	@DisplayName("DELETE /public/carts/{guestId}/cart-items/books/{bookId}: 특정 도서 아이템을 성공적으로 삭제한다")
	void removeGuestCartItemByBookId_success() throws Exception {
		// given
		doNothing().when(guestCartService).removeItem(guestId, bookId);
		// when & then
		mockMvc.perform(delete("/public/carts/{guestId}/cart-items/books/{bookId}", guestId, bookId))
				.andExpect(status().isNoContent());
		verify(guestCartService).removeItem(guestId, bookId);
	}
	
}
