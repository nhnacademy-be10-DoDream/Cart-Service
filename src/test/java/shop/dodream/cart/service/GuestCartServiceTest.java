package shop.dodream.cart.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import shop.dodream.cart.client.BookClient;
import shop.dodream.cart.dto.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuestCartServiceTest {
	
	@Mock
	private RedisTemplate<String, GuestCart> redisTemplate;
	
	@Mock
	private ValueOperations<String, GuestCart> valueOperations;
	
	@Mock
	private BookClient bookClient;
	
	@InjectMocks
	private GuestCartService guestCartService;
	
	private final String guestId = "guest123";
	private final String redisKey = "guest_cart:" + guestId;
	
	@BeforeEach
	void setUp() {
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}
	
	@Test
	void getCartReturnEmptyCartWhenNotExists() {
		when(valueOperations.get(redisKey)).thenReturn(null);
		
		GuestCartResponse response = guestCartService.getCart(guestId);
		
		assertEquals(guestId, response.getGuestId());
		assertTrue(response.getItems().isEmpty());
	}
	
	@Test
	void getCartReturnMappedItemResponse() {
		GuestCartItem cartItem = new GuestCartItem(1L, 2L);
		GuestCart guestCart = new GuestCart(guestId, List.of(cartItem));
		BookDto bookDto = new BookDto(1L, "Test Book", 900L, 10L);
		
		when(valueOperations.get(redisKey)).thenReturn(guestCart);
		when(bookClient.getBookById(1L)).thenReturn(bookDto);
		
		GuestCartResponse response = guestCartService.getCart(guestId);
		
		assertEquals(1, response.getItems().size());
		GuestCartItemResponse item = response.getItems().get(0);
		assertEquals(1L, item.getBookId());
		assertEquals("Test Book", item.getTitle());
		assertEquals(2L, item.getQuantity());
		assertEquals(10L, item.getStockQuantity());
		assertEquals(900L, item.getPrice());
	}
	
	@Test
	void addCartItemCreateNewCartIfNotExists() {
		GuestCartItemRequest request = new GuestCartItemRequest(1L, 2L);
		BookDto bookDto = new BookDto(1L, "New Book", 800L, 5L);
		
		when(valueOperations.get(redisKey))
				.thenReturn(null)
				.thenReturn(new GuestCart(guestId, List.of(new GuestCartItem(1L, 2L))));
		when(bookClient.getBookById(1L)).thenReturn(bookDto);
		
		// Stub set() to simulate Redis save
		doAnswer(invocation -> {
			GuestCart cartArg = invocation.getArgument(1);
			assertEquals(1, cartArg.getItems().size());
			assertEquals(1L, cartArg.getItems().get(0).getBookId());
			assertEquals(2L, cartArg.getItems().get(0).getQuantity());
			return null;
		}).when(valueOperations).set(eq(redisKey), any(GuestCart.class), any(Duration.class));
		
		GuestCartResponse response = guestCartService.addCartItem(guestId, request);
		
		assertEquals(guestId, response.getGuestId());
		assertEquals(1, response.getItems().size());
		assertEquals("New Book", response.getItems().get(0).getTitle());
	}
	
	@Test
	void addCartItemUpdateQuantityIfBookExists() {
		GuestCartItem existingItem = new GuestCartItem(1L, 2L);
		GuestCart existingCart = new GuestCart(guestId, new ArrayList<>(List.of(existingItem)));
		GuestCartItemRequest request = new GuestCartItemRequest(1L, 3L);
		BookDto bookDto = new BookDto(1L, "Same Book", 700L, 15L);
		
		when(valueOperations.get(redisKey)).thenReturn(existingCart);
		when(bookClient.getBookById(1L)).thenReturn(bookDto);
		
		GuestCartResponse response = guestCartService.addCartItem(guestId, request);
		
		assertEquals(guestId, response.getGuestId());
		assertEquals(1, response.getItems().size());
		assertEquals(5L, response.getItems().get(0).getQuantity());
		assertEquals("Same Book", response.getItems().get(0).getTitle());
	}
	
	@MockitoSettings(strictness = Strictness.LENIENT)
	@Test
	void deleteCartDeleteKeyFromRedis() {
		guestCartService.deleteCart(guestId);
		verify(redisTemplate).delete(redisKey);
	}
	
	@Test
	void getRawCartReturnCartObject() {
		GuestCartItem item = new GuestCartItem(1L, 2L);
		GuestCart cart = new GuestCart(guestId, List.of(item));
		
		when(valueOperations.get(redisKey)).thenReturn(cart);
		
		GuestCart result = guestCartService.getRawCart(guestId);
		
		assertNotNull(result);
		assertEquals(1, result.getItems().size());
		assertEquals(1L, result.getItems().get(0).getBookId());
	}
}

