package shop.dodream.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import shop.dodream.cart.dto.*;
import shop.dodream.cart.entity.CartItem;
import shop.dodream.cart.service.CartItemService;
import shop.dodream.cart.service.GuestCartService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartItemController.class)
@AutoConfigureMockMvc
class CartItemControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private CartItemService cartItemService;
	
	@MockBean
	private GuestCartService guestCartService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Test
	void getCartItems_ShouldReturnListOfItems() throws Exception {
		Long cartId = 1L;
		List<CartItemResponse> mockItems = List.of(
				new CartItemResponse(1L, 10L, "Book A", 8000L, 2L, 30L)
		);
		
		given(cartItemService.getCartItems(cartId)).willReturn(mockItems);
		
		mockMvc.perform(get("/carts/{cartId}/cart-items", cartId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].bookTitle").value("Book A"));
	}
	
	@Test
	void addCartItem_ShouldReturnCreatedItem() throws Exception {
		CartItemRequest request = new CartItemRequest(1L, 10L, 2L);
		CartItemResponse response = new CartItemResponse(1L, 10L, "Book A", 8000L, 2L, 30L);
		
		given(cartItemService.addCartItem(any(CartItemRequest.class))).willReturn(response);
		
		mockMvc.perform(post("/carts/{cartId}/cart-items", 1L)
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.bookTitle").value("Book A"));
	}
	
	@Test
	void updateCartItemQuantity_ShouldReturnUpdatedItem() throws Exception {
		Long cartItemId = 1L;
		Long quantity = 3L;
		CartItemResponse response = new CartItemResponse(cartItemId, 10L, "Book A", 8000L, quantity, 30L);
		
		given(cartItemService.updateCartItemQuantity(cartItemId, quantity)).willReturn(response);
		
		mockMvc.perform(patch("/carts/{cartId}/cart-items/{cartItemId}/quantity", 1L, cartItemId)
				                .param("quantity", quantity.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.quantity").value(quantity));
	}
	
	@Test
	void removeCartItem_ShouldReturnNoContent() throws Exception {
		mockMvc.perform(delete("/carts/{cartId}/cart-items/{cartItemId}", 1L, 1L))
				.andExpect(status().isNoContent());
	}
	
	@Test
	void removeAllCartItems_ShouldReturnNoContent() throws Exception {
		mockMvc.perform(delete("/carts/{cartId}/cart-items", 1L))
				.andExpect(status().isNoContent());
	}
	
	@Test
	void addGuestCartItem_ShouldReturnCreatedGuestCartResponse() throws Exception {
		String guestId = "guest123";
		GuestCartItemRequest request = new GuestCartItemRequest(10L, 1L);
		
		GuestCartItem guestItem = new GuestCartItem(10L, 1L);
		BookDto book = new BookDto(10L, "Book A", 8000L, 30L);
		GuestCartItemResponse guestResponse = GuestCartItemResponse.of(guestItem, book);
		
		GuestCartResponse response = new GuestCartResponse(guestId, List.of(guestResponse));
		
		given(guestCartService.addCartItem(eq(guestId), any(GuestCartItemRequest.class))).willReturn(response);
		
		mockMvc.perform(post("/carts/guest/{guestId}/cart-items", guestId)
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.guestId").value(guestId))
				.andExpect(jsonPath("$.items[0].title").value("Book A"));
	}
	
	@Test
	void removeCartItemsByBookId_ShouldReturnNoContent() throws Exception {
		mockMvc.perform(delete("/carts/{cartId}/book/{bookId}", 1L, 10L))
				.andExpect(status().isNoContent());
	}
	
	@Test
	void getCartItemByBookId_ShouldReturnItem() throws Exception {
		Long cartId = 1L;
		Long bookId = 10L;
		
		CartItem item = new CartItem();
		item.setCartItemId(1L);
		item.setCartId(cartId);
		item.setBookId(bookId);
		item.setQuantity(2L);
		item.setPrice(8000L);
		
		BookDto book = new BookDto(bookId, "Book A", 8000L, 30L);
		
		given(cartItemService.getCartItemByBookId(cartId, bookId)).willReturn(item);
		given(cartItemService.getBookByIdForItem(item)).willReturn(book);
		
		mockMvc.perform(get("/carts/{cartId}/book/{bookId}", cartId, bookId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.bookTitle").value("Book A"))
				.andExpect(jsonPath("$.quantity").value(2L));
	}
}
