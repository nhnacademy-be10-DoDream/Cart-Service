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
	void getCartItemsReturnListOfItems() throws Exception {
		Long cartId = 1L;
		List<CartItemResponse> mockItems = List.of(
				new CartItemResponse(1L, 10L, "Book A", 8000L, 8000L, 2L,3L,"test")
		);
		
		given(cartItemService.getCartItems(cartId)).willReturn(mockItems);
		
		mockMvc.perform(get("/carts/{cartId}/cart-items", cartId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].bookTitle").value("Book A"));
	}
	
	@Test
	void addCartItemReturnCreatedItem() throws Exception {
		CartItemRequest request = new CartItemRequest(1L, 10L, 2L);
		CartItemResponse response = new CartItemResponse(1L, 10L, "Book A", 8000L, 8000L, 2L,3L,"test");
		
		given(cartItemService.addCartItem(any(CartItemRequest.class))).willReturn(response);
		
		mockMvc.perform(post("/carts/{cartId}/cart-items", 1L)
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.bookTitle").value("Book A"));
	}
	
	@Test
	void updateCartItemQuantityReturnUpdatedItem() throws Exception {
		Long cartItemId = 1L;
		Long quantity = 2L;
		CartItemResponse response = new CartItemResponse(cartItemId, 10L, "Book A", 8000L, 8000L, 2L,3L,"test");
		
		given(cartItemService.updateCartItemQuantity(cartItemId, quantity)).willReturn(response);
		
		CartItemRequest request = new CartItemRequest(null, null, quantity);
		String json = new ObjectMapper().writeValueAsString(request);
		
		mockMvc.perform(patch("/carts/{cartId}/cart-items/{cartItemId}/quantity", 1L, cartItemId)
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(json)) // 바디에 JSON 전달
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.quantity").value(quantity));
	}
	
	@Test
	void removeCartItemReturnNoContent() throws Exception {
		mockMvc.perform(delete("/cart-items/{cartItemId}", 1L, 1L))
				.andExpect(status().isNoContent());
	}
	
	@Test
	void removeAllCartItemsReturnNoContent() throws Exception {
		mockMvc.perform(delete("/carts/{cartId}/cart-items", 1L))
				.andExpect(status().isNoContent());
	}
	
	@Test
	void addGuestCartItemReturnCreatedGuestCartResponse() throws Exception {
		String guestId = "guest123";
		GuestCartItemRequest request = new GuestCartItemRequest(10L, 1L);
		
		GuestCartItem guestItem = new GuestCartItem(10L, 1L);
		BookDto book = new BookDto(10L, "Book A", 8000L, 8000L, 30L,"test");
		GuestCartItemResponse guestResponse = GuestCartItemResponse.of(guestItem, book);
		
		GuestCartResponse response = new GuestCartResponse(guestId, List.of(guestResponse));
		
		given(guestCartService.addCartItem(eq(guestId), any(GuestCartItemRequest.class))).willReturn(response);
		
		mockMvc.perform(post("/carts/guests/{guestId}/cart-items", guestId)
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.guestId").value(guestId))
				.andExpect(jsonPath("$.items[0].title").value("Book A"));
	}
	
	
	@Test
	void removeCartItemsByBookIdReturnNoContent() throws Exception {
		mockMvc.perform(delete("/carts/{cartId}/cart-items/books/{bookId}", 1L, 10L))
				.andExpect(status().isNoContent());
	}
	
	@Test
	void removeGuestCartItemReturnNoContent() throws Exception {
		mockMvc.perform(delete("/carts/guests/{guestId}/cart-items/books/{bookId}", "guest123", 10L))
				.andExpect(status().isNoContent());
	}
	
	@Test
	void getCartItemByBookIdReturnItem() throws Exception {
		Long cartId = 1L;
		Long bookId = 10L;
		
		CartItem item = new CartItem();
		item.setCartItemId(1L);
		item.setCartId(cartId);
		item.setBookId(bookId);
		item.setQuantity(2L);
		item.setOriginalPrice(8000L);
		item.setDiscountPrice(8000L);
		
		BookDto book = new BookDto(bookId, "Book A", 8000L, 8000L, 30L,"test");
		
		given(cartItemService.getCartItemByBookId(cartId, bookId)).willReturn(item);
		given(cartItemService.getBookByIdForItem(item)).willReturn(book);
		
		mockMvc.perform(get("/carts/{cartId}/cart-items/books/{bookId}", cartId, bookId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.bookTitle").value("Book A"))
				.andExpect(jsonPath("$.quantity").value(2L));
	}
	
	@Test
	void getCartItemReturnItem() throws Exception {
		Long cartItemId = 1L;
		
		CartItem item = new CartItem();
		item.setCartItemId(cartItemId);
		item.setCartId(1L);
		item.setBookId(10L);
		item.setQuantity(2L);
		item.setOriginalPrice(8000L);
		item.setDiscountPrice(8000L);
		
		BookDto book = new BookDto(10L, "Book A", 8000L, 8000L, 30L,"test");
		
		given(cartItemService.getCartItemById(cartItemId)).willReturn(item);
		given(cartItemService.getBookByIdForItem(item)).willReturn(book);
		
		mockMvc.perform(get("/cart-items/{cartItemId}", cartItemId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.bookTitle").value("Book A"))
				.andExpect(jsonPath("$.quantity").value(2));
	}
}
