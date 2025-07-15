package shop.dodream.cart.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import shop.dodream.cart.client.BookClient;
import shop.dodream.cart.dto.*;
import shop.dodream.cart.exception.DataNotFoundException;
import shop.dodream.cart.exception.InvalidQuantityException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
@DisplayName("GuestCartService 단위 테스트")
class GuestCartServiceTest {
	
	@Mock
	private RedisTemplate<String, GuestCart> redisTemplate;
	@Mock
	private BookClient bookClient;
	@Mock
	private ValueOperations<String, GuestCart> valueOperations;
	
	@InjectMocks
	private GuestCartService guestCartService;
	
	@Captor
	private ArgumentCaptor<GuestCart> cartCaptor;
	
	private final String guestId = "guest123";
	private final String redisKey = "guest_cart:guest123";
	private final Duration CART_EXPIRATION = Duration.ofDays(30);
	
	@BeforeEach
	void setUp() {
		// Mockito.lenient()를 사용하여 불필요한 엄격한 stubbing 검사를 완화합니다.
		// 모든 테스트에서 redisTemplate.opsForValue()가 호출될 수 있기 때문에 공통으로 설정합니다.
		lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}
	
	@Test
	@DisplayName("Redis에 장바구니가 없으면 빈 장바구니 응답을 반환한다")
	void getCart_whenNotExists_returnsEmptyResponse() {
		// given
		given(valueOperations.get(redisKey)).willReturn(null);
		
		// when
		GuestCartResponse response = guestCartService.getCart(guestId);
		
		// then
		assertThat(response.getGuestId()).isEqualTo(guestId);
		assertThat(response.getItems()).isEmpty();
		verify(bookClient, never()).getBooksByIds(anyList());
	}
	
	@Test
	@DisplayName("장바구니가 존재하면 도서 정보를 포함한 응답을 반환한다")
	void getCart_whenExists_returnsResponseWithItems() {
		// given
		GuestCart cart = new GuestCart(guestId, List.of(new GuestCartItem(1L, 2L)));
		BookListResponseRecord book = new BookListResponseRecord(1L, "Test Book", 3000L, "url");
		given(valueOperations.get(redisKey)).willReturn(cart);
		given(bookClient.getBooksByIds(List.of(1L))).willReturn(List.of(book));
		
		// when
		GuestCartResponse response = guestCartService.getCart(guestId);
		
		// then
		assertThat(response.getItems()).hasSize(1);
		GuestCartItemResponse itemResponse = response.getItems().get(0);
		assertThat(itemResponse.getBookId()).isEqualTo(1L);
		assertThat(itemResponse.getTitle()).isEqualTo("Test Book");
	}
	
	@Test
	@DisplayName("BookClient에서 예외 발생 시 도서 정보를 null로 채운 응답을 반환한다 (try-catch 커버)")
	void getCart_whenBookClientFails_returnsResponseWithNullBookInfo() {
		// given
		GuestCart cart = new GuestCart(guestId, List.of(new GuestCartItem(1L, 2L)));
		given(valueOperations.get(redisKey)).willReturn(cart);
		given(bookClient.getBooksByIds(anyList())).willThrow(new RuntimeException("Feign client error"));
		
		// when
		GuestCartResponse response = guestCartService.getCart(guestId);
		
		// then
		assertThat(response.getItems()).hasSize(1);
		GuestCartItemResponse itemResponse = response.getItems().get(0);
		assertThat(itemResponse.getTitle()).isNull();
		assertThat(itemResponse.getSalePrice()).isNull();
	}
	
	@Test
	@DisplayName("getRawCart는 Redis의 원본 GuestCart 객체를 반환한다")
	void getRawCart_returnsRawCartObject() {
		// given
		GuestCart cart = new GuestCart(guestId, List.of(new GuestCartItem(1L, 2L)));
		given(valueOperations.get(redisKey)).willReturn(cart);
		
		// when
		GuestCart result = guestCartService.getRawCart(guestId);
		
		// then
		assertThat(result).isSameAs(cart);
	}
	
	
	@Test
	@DisplayName("유효성 검증 실패: 수량이 0이하이면 예외를 던진다")
	void addCartItem_whenQuantityIsZero_throwsException() {
		GuestCartItemRequest request = new GuestCartItemRequest(1L, 0L);
		assertThatThrownBy(() -> guestCartService.addCartItem(guestId, request))
				.isInstanceOf(InvalidQuantityException.class)
				.hasMessage("수량은 1개 이상이어야 합니다.");
	}
	
	@Test
	@DisplayName("유효성 검증 실패: bookId가 null이면 예외를 던진다")
	void addCartItem_whenBookIdIsNull_throwsException() {
		GuestCartItemRequest request = new GuestCartItemRequest(null, 1L);
		assertThatThrownBy(() -> guestCartService.addCartItem(guestId, request))
				.isInstanceOf(DataNotFoundException.class)
				.hasMessage("bookId는 null이 될 수 없습니다.");
	}
	
	@Test
	@DisplayName("새로운 아이템을 추가하면 수량이 20을 넘을 경우 20으로 제한된다")
	void addCartItem_whenNewAndQuantityOverMax_capsAtMax() {
		// given
		GuestCartItemRequest request = new GuestCartItemRequest(1L, 25L); // 20 초과
		given(valueOperations.get(redisKey)).willReturn(new GuestCart(guestId, new ArrayList<>()));
		given(bookClient.getBooksByIds(anyList())).willReturn(Collections.emptyList());
		
		// when
		guestCartService.addCartItem(guestId, request);
		
		// then
		verify(valueOperations).set(eq(redisKey), cartCaptor.capture(), eq(CART_EXPIRATION));
		GuestCart savedCart = cartCaptor.getValue();
		assertThat(savedCart.getItems().get(0).getQuantity()).isEqualTo(20L);
	}
	
	@Test
	@DisplayName("기존 아이템에 수량을 더할 때 20을 넘으면 20으로 제한된다")
	void addCartItem_whenExistingAndSumOverMax_capsAtMax() {
		// given
		GuestCartItem existingItem = new GuestCartItem(1L, 15L);
		GuestCart cart = new GuestCart(guestId, new ArrayList<>(List.of(existingItem)));
		GuestCartItemRequest request = new GuestCartItemRequest(1L, 10L); // 15 + 10 = 25
		given(valueOperations.get(redisKey)).willReturn(cart);
		given(bookClient.getBooksByIds(anyList())).willReturn(Collections.emptyList());
		
		// when
		guestCartService.addCartItem(guestId, request);
		
		// then
		verify(valueOperations).set(eq(redisKey), cartCaptor.capture(), eq(CART_EXPIRATION));
		GuestCart savedCart = cartCaptor.getValue();
		assertThat(savedCart.getItems().get(0).getQuantity()).isEqualTo(20L);
	}
	
	@Test
	@DisplayName("장바구니가 가득 찼을 때 새 아이템 추가 시 예외를 던진다")
	void addCartItem_whenCartIsFull_throwsException() {
		// given
		List<GuestCartItem> fullItems = LongStream.range(1, 21)
				                                .mapToObj(i -> new GuestCartItem(i, 1L))
				                                .collect(toList());
		GuestCart cart = new GuestCart(guestId, fullItems);
		given(valueOperations.get(redisKey)).willReturn(cart);
		
		GuestCartItemRequest request = new GuestCartItemRequest(21L, 1L); // 21번째 아이템
		
		// when & then
		assertThatThrownBy(() -> guestCartService.addCartItem(guestId, request))
				.isInstanceOf(InvalidQuantityException.class)
				.hasMessage("장바구니는 최대 20개까지만 담을 수 있습니다.");
	}
	
	@Test
	@DisplayName("유효하지 않은 수량으로 요청 시 예외를 던진다")
	void updateQuantity_withInvalidQuantity_throwsException() {
		assertThatThrownBy(() -> guestCartService.updateQuantity(guestId, 1L, 0L))
				.isInstanceOf(InvalidQuantityException.class);
		assertThatThrownBy(() -> guestCartService.updateQuantity(guestId, 1L, 21L))
				.isInstanceOf(InvalidQuantityException.class);
		assertThatThrownBy(() -> guestCartService.updateQuantity(guestId, 1L, null))
				.isInstanceOf(InvalidQuantityException.class);
	}
	
	@Test
	@DisplayName("변경할 아이템이 장바구니에 없으면 예외를 던진다")
	void updateQuantity_whenItemNotFound_throwsException() {
		// given
		given(valueOperations.get(redisKey)).willReturn(new GuestCart(guestId, new ArrayList<>()));
		
		// when & then
		assertThatThrownBy(() -> guestCartService.updateQuantity(guestId, 1L, 5L))
				.isInstanceOf(DataNotFoundException.class)
				.hasMessage("해당 도서가 장바구니에 존재하지 않습니다.");
	}
	
	@Test
	@DisplayName("성공적으로 아이템 수량을 변경한다")
	void updateQuantity_success() {
		// given
		GuestCart cart = new GuestCart(guestId, new ArrayList<>(List.of(new GuestCartItem(1L, 2L))));
		given(valueOperations.get(redisKey)).willReturn(cart);
		given(bookClient.getBooksByIds(anyList())).willReturn(Collections.emptyList());
		
		// when
		guestCartService.updateQuantity(guestId, 1L, 10L);
		
		// then
		verify(valueOperations).set(eq(redisKey), cartCaptor.capture(), eq(CART_EXPIRATION));
		GuestCart savedCart = cartCaptor.getValue();
		assertThat(savedCart.getItems().get(0).getQuantity()).isEqualTo(10L);
	}
	
	
	@Test
	@DisplayName("removeItem은 특정 아이템을 장바구니에서 제거한다")
	void removeItem_removesItemFromCart() {
		// given
		GuestCartItem itemToRemove = new GuestCartItem(1L, 2L);
		GuestCartItem itemToKeep = new GuestCartItem(2L, 1L);
		GuestCart cart = new GuestCart(guestId, new ArrayList<>(List.of(itemToRemove, itemToKeep)));
		given(valueOperations.get(redisKey)).willReturn(cart);
		
		// when
		guestCartService.removeItem(guestId, 1L);
		
		// then
		verify(valueOperations).set(eq(redisKey), cartCaptor.capture(), eq(CART_EXPIRATION));
		GuestCart savedCart = cartCaptor.getValue();
		assertThat(savedCart.getItems()).hasSize(1);
		assertThat(savedCart.getItems().get(0).getBookId()).isEqualTo(2L);
	}
	
	@Test
	@DisplayName("deleteCart는 Redis에서 해당 키를 삭제한다")
	void deleteCart_deletesKeyFromRedis() {
		// when
		guestCartService.deleteCart(guestId);
		// then
		verify(redisTemplate).delete(redisKey);
	}
	
	@Test
	@DisplayName("첫 시도에 성공하면 1번만 호출한다")
	void deleteWithRetry_succeedsOnFirstTry() {
		// given
		given(redisTemplate.delete(redisKey)).willReturn(true);
		// when
		guestCartService.deleteGuestCartWithRetry(guestId);
		// then
		verify(redisTemplate, times(1)).delete(redisKey);
	}
	
	@Test
	@DisplayName("재시도 후 성공하면 성공할 때까지 호출한다")
	void deleteWithRetry_succeedsOnRetry() {
		// given
		given(redisTemplate.delete(redisKey)).willReturn(false, true); // 첫 실패, 두 번째 성공
		// when
		guestCartService.deleteGuestCartWithRetry(guestId);
		// then
		verify(redisTemplate, times(2)).delete(redisKey);
	}
	
	@Test
	@DisplayName("최대 재시도 횟수까지 실패하면 3번 호출한다")
	void deleteWithRetry_failsAfterMaxRetries() {
		// given
		given(redisTemplate.delete(redisKey)).willReturn(false);
		// when
		guestCartService.deleteGuestCartWithRetry(guestId);
		// then
		verify(redisTemplate, times(3)).delete(redisKey);
	}
	
	@Test
	@DisplayName("삭제 중 예외가 발생하면 재시도한다")
	void deleteWithRetry_retriesAfterException() {
		// given
		given(redisTemplate.delete(redisKey))
				.willThrow(new RuntimeException("Connection error"))
				.willReturn(true);
		// when
		guestCartService.deleteGuestCartWithRetry(guestId);
		// then
		verify(redisTemplate, times(2)).delete(redisKey);
	}
	
}