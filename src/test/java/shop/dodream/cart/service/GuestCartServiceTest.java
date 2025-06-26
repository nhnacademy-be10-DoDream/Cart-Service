package shop.dodream.cart.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
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
	private BookClient bookClient;
	
	@Mock
	private ValueOperations<String, GuestCart> valueOperations;
	
	@InjectMocks
	private GuestCartService guestCartService;
	
	private final String guestId = "guest123";
	private final String redisKey = "guest_cart:" + guestId;
	
	@BeforeEach
	void setUp() {
		lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
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
		BookDto bookDto = new BookDto(1L, "Test Book", 900L, 3000L, 10L, "test");
		
		when(valueOperations.get(redisKey)).thenReturn(guestCart);
		when(bookClient.getBookById(1L)).thenReturn(bookDto);
		
		GuestCartResponse response = guestCartService.getCart(guestId);
		
		assertEquals(1, response.getItems().size());
		GuestCartItemResponse item = response.getItems().get(0);
		assertEquals(1L, item.getBookId());
		assertEquals("Test Book", item.getTitle());
		assertEquals(2L, item.getQuantity());
		assertEquals(10L, item.getStockQuantity());
		assertEquals(900L, item.getOriginalPrice());
		assertEquals(3000L, item.getDiscountPrice());
		assertEquals("test", item.getImageUrl());
	}
	
	@Test
	void addCartItemCreateNewCartIfNotExists() {
		GuestCartItemRequest request = new GuestCartItemRequest(1L, 2L);
		BookDto bookDto = new BookDto(1L, "New Book", 800L, 800L, 5L, "test");
		
		when(valueOperations.get(redisKey)).thenReturn(null);
		when(bookClient.getBookById(1L)).thenReturn(bookDto);
		
		// set 검증
		doNothing().when(valueOperations).set(eq(redisKey), any(GuestCart.class), any(Duration.class));
		
		GuestCartResponse response = guestCartService.addCartItem(guestId, request);
		
		assertEquals(guestId, response.getGuestId());
		assertEquals(1, response.getItems().size());
		assertEquals("New Book", response.getItems().get(0).getTitle());
		
		verify(valueOperations).set(eq(redisKey), any(GuestCart.class), eq(Duration.ofDays(7)));
	}
	
	@Test
	void addCartItemUpdateQuantityIfBookExists() {
		GuestCartItem existingItem = new GuestCartItem(1L, 2L);
		GuestCart existingCart = new GuestCart(guestId, new ArrayList<>(List.of(existingItem)));
		GuestCartItemRequest request = new GuestCartItemRequest(1L, 3L);
		BookDto bookDto = new BookDto(1L, "Same Book", 700L, 700L, 15L, "test");
		
		when(valueOperations.get(redisKey)).thenReturn(existingCart);
		when(bookClient.getBookById(1L)).thenReturn(bookDto);
		
		GuestCartResponse response = guestCartService.addCartItem(guestId, request);
		
		assertEquals(guestId, response.getGuestId());
		assertEquals(1, response.getItems().size());
		assertEquals(5L, response.getItems().get(0).getQuantity());
		assertEquals("Same Book", response.getItems().get(0).getTitle());
		
		verify(valueOperations).set(eq(redisKey), any(GuestCart.class), eq(Duration.ofDays(7)));
	}
	
	@Test
	void removeItem_removesBookFromCart() {
		Long bookIdToRemove = 1L;
		GuestCartItem itemToRemove = new GuestCartItem(bookIdToRemove, 2L);
		GuestCartItem remainingItem = new GuestCartItem(2L, 1L);
		GuestCart cart = new GuestCart(guestId, new ArrayList<>(List.of(itemToRemove, remainingItem)));
		
		when(valueOperations.get(redisKey)).thenReturn(cart);
		
		guestCartService.removeItem(guestId, bookIdToRemove);
		
		assertEquals(1, cart.getItems().size());
		assertEquals(2L, cart.getItems().get(0).getBookId());
		
		verify(valueOperations).set(eq(redisKey), eq(cart), eq(Duration.ofDays(7)));
	}
	
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
	
	@Test
	void removeItem_whenCartIsNull_shouldReturnWithoutException() {
		when(valueOperations.get(redisKey)).thenReturn(null);
		
		assertDoesNotThrow(() -> guestCartService.removeItem(guestId, 1L));
		
		verify(valueOperations).set(anyString(), any(GuestCart.class), any(Duration.class));
	}
	
	@Test
	void addCartItem_shouldCapQuantityAtMaxItemCount() {
		GuestCartItem existingItem = new GuestCartItem(1L, 98L); // 98
		GuestCart cart = new GuestCart(guestId, new ArrayList<>(List.of(existingItem)));
		GuestCartItemRequest request = new GuestCartItemRequest(1L, 5L);
		BookDto bookDto = new BookDto(1L, "Book", 1000L, 1000L, 10L, "url");
		
		when(valueOperations.get(redisKey)).thenReturn(cart);
		when(bookClient.getBookById(1L)).thenReturn(bookDto);
		
		GuestCartResponse response = guestCartService.addCartItem(guestId, request);
		
		assertEquals(20L, response.getItems().get(0).getQuantity()); // capped
		
		verify(valueOperations).set(eq(redisKey), any(), eq(Duration.ofDays(7)));
	}
	
	@Test
	void removeItem_whenBookIdNotFound_shouldDoNothing() {
		GuestCartItem item = new GuestCartItem(1L, 2L);
		GuestCart cart = new GuestCart(guestId, new ArrayList<>(List.of(item)));
		
		when(valueOperations.get(redisKey)).thenReturn(cart);
		
		guestCartService.removeItem(guestId, 99L); // 없는 ID
		
		assertEquals(1, cart.getItems().size()); // 변경 없음
		verify(valueOperations).set(eq(redisKey), eq(cart), eq(Duration.ofDays(7)));
	}
	
	@Test
	void addCartItem_shouldAddNewItem_whenItemNotExists() {
		GuestCart cart = new GuestCart(guestId, new ArrayList<>());
		GuestCartItemRequest request = new GuestCartItemRequest(2L, 10L); // 새로운 아이템
		BookDto bookDto = new BookDto(2L, "New Book", 1000L, 1000L, 10L, "url");
		
		when(valueOperations.get(redisKey)).thenReturn(cart);
		when(bookClient.getBookById(2L)).thenReturn(bookDto);
		
		GuestCartResponse response = guestCartService.addCartItem(guestId, request);
		
		assertEquals(1, response.getItems().size());
		assertEquals(10L, response.getItems().get(0).getQuantity());
		assertEquals(2L, response.getItems().get(0).getBookId());
		
		verify(valueOperations).set(eq(redisKey), any(), eq(Duration.ofDays(7)));
	}
	
	@Test
	void safeGetBook_shouldReturnNull_whenBookClientThrows() {
		GuestCart cart = new GuestCart(guestId, new ArrayList<>(List.of(new GuestCartItem(1L, 1L))));
		
		when(valueOperations.get(redisKey)).thenReturn(cart);
		when(bookClient.getBookById(1L)).thenThrow(new RuntimeException("Fail"));
		
		GuestCartResponse response = guestCartService.getCart(guestId);
		
		assertNotNull(response);
		assertEquals(1, response.getItems().size());
		assertNull(response.getItems().get(0).getTitle());
		assertNull(response.getItems().get(0).getOriginalPrice());
		assertNull(response.getItems().get(0).getDiscountPrice());
		assertNull(response.getItems().get(0).getImageUrl());
		assertNull(response.getItems().get(0).getStockQuantity());
	}
	
	@Test
	void addCartItem_shouldThrow_whenQuantityIsZeroOrNegative() {
		GuestCartItemRequest invalidRequest = new GuestCartItemRequest(1L, 0L);
		
		Exception e = assertThrows(IllegalArgumentException.class, () -> {
			guestCartService.addCartItem(guestId, invalidRequest);
		});
		assertEquals("수량은 1개 이상이어야 합니다.", e.getMessage());
	}
	
	@Test
	void addCartItem_shouldThrow_whenBookIdIsNull() {
		GuestCartItemRequest invalidRequest = new GuestCartItemRequest(null, 1L);
		
		Exception e = assertThrows(IllegalArgumentException.class, () -> {
			guestCartService.addCartItem(guestId, invalidRequest);
		});
		assertEquals("bookId는 null이 될 수 없습니다.", e.getMessage());
	}
	
	@Test
	void addCartItem_shouldThrowException_whenCartItemsExceedMax() {
		// given
		List<GuestCartItem> maxItems = new ArrayList<>();
		for (long i = 1; i <= 20; i++) {
			maxItems.add(new GuestCartItem(i, 1L)); // 이미 20개 아이템이 담긴 장바구니
		}
		GuestCart cart = new GuestCart(guestId, maxItems);
		
		when(valueOperations.get(redisKey)).thenReturn(cart);
		
		GuestCartItemRequest request = new GuestCartItemRequest(21L, 1L); // 새로운 아이템 추가 시도
		
		// when & then
		assertThrows(IllegalStateException.class, () -> guestCartService.addCartItem(guestId, request));
	}
	
	@Test
	void addCartItem_shouldLimitQuantity_whenQuantityExceedsMax() {
		// given
		GuestCart cart = new GuestCart(guestId, new ArrayList<>());
		when(valueOperations.get(redisKey)).thenReturn(cart);
		
		GuestCartItemRequest request = new GuestCartItemRequest(1L, 100L); // 수량 100개 요청 (20개 제한)
		
		// when
		GuestCartResponse response = guestCartService.addCartItem(guestId, request);
		
		// then
		assertNotNull(response);
		assertEquals(1, response.getItems().size());
		assertEquals(20, response.getItems().get(0).getQuantity()); // 수량이 20으로 제한됐는지 확인
	}
	
}

