//package shop.dodream.cart.service;
//
//
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import shop.dodream.cart.client.BookClient;
//import shop.dodream.cart.dto.BookListResponseRecord;
//import shop.dodream.cart.dto.CartItemRequest;
//import shop.dodream.cart.dto.CartItemResponse;
//import shop.dodream.cart.dto.GuestCartItem;
//import shop.dodream.cart.entity.Cart;
//import shop.dodream.cart.entity.CartItem;
//import shop.dodream.cart.exception.DataNotFoundException;
//import shop.dodream.cart.repository.CartItemRepository;
//import shop.dodream.cart.repository.CartRepository;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("CartItemService 단위 테스트 (최신 서비스 코드 반영)")
//class CartItemServiceTest {
//
//	@Mock
//	private CartItemRepository cartItemRepository;
//
//	@Mock
//	private CartRepository cartRepository;
//
//	@Mock
//	private BookClient bookClient;
//
//	@InjectMocks
//	private CartItemService cartItemService;
//
//	private final Long cartId = 1L;
//	private final Long cartItemId = 10L;
//	private final Long bookId = 100L;
//	private Cart testCart;
//	private CartItem testCartItem;
//	private BookListResponseRecord mockBook;
//
//	@BeforeEach
//	void setUp() {
//		testCart = new Cart(cartId, "user123");
//		testCartItem = new CartItem(cartItemId, 2L, bookId, testCart, 15000L);
//		mockBook = new BookListResponseRecord(bookId, "Mock Book", 15000L, "url");
//	}
//
//	@Test
//	@DisplayName("성공적으로 장바구니 아이템 목록을 반환한다")
//	void getCartItems_success() {
//		// given
//		given(cartItemRepository.findByCart_CartId(cartId)).willReturn(List.of(testCartItem));
//		given(bookClient.getBooksByIds(List.of(bookId))).willReturn(List.of(mockBook));
//
//		// when
//		List<CartItemResponse> responses = cartItemService.getCartItems(cartId);
//
//		// then
//		assertThat(responses).hasSize(1);
//		assertThat(responses.get(0).getBookId()).isEqualTo(bookId);
//		verify(cartItemRepository).findByCart_CartId(cartId);
//		verify(bookClient).getBooksByIds(anyList());
//	}
//
//	@Test
//	@DisplayName("장바구니는 있지만 도서 정보를 찾을 수 없으면 예외를 던진다")
//	void getCartItems_bookNotFound_throwsException() {
//		// given
//		given(cartItemRepository.findByCart_CartId(cartId)).willReturn(List.of(testCartItem));
//		given(bookClient.getBooksByIds(anyList())).willReturn(Collections.emptyList());
//
//		// when & then
//		assertThatThrownBy(() -> cartItemService.getCartItems(cartId))
//				.isInstanceOf(DataNotFoundException.class)
//				.hasMessageContaining("도서 정보를 찾을 수 없습니다: id=" + bookId);
//	}
//
//	@Test
//	@DisplayName("장바구니에 아이템이 없으면 빈 리스트를 반환한다 (BookClient 호출 없음)")
//	void getCartItems_whenEmpty_returnsEmptyList() {
//		// given
//		given(cartItemRepository.findByCart_CartId(cartId)).willReturn(Collections.emptyList());
//
//		// when
//		List<CartItemResponse> responses = cartItemService.getCartItems(cartId);
//
//		// then
//		assertThat(responses).isEmpty();
//		verify(bookClient, never()).getBooksByIds(anyList());
//	}
//
//	@Test
//	@DisplayName("새로운 아이템을 성공적으로 추가한다")
//	void addNewItem_success() {
//		// given
//		CartItemRequest request = new CartItemRequest(cartId, bookId, 2L);
//		given(cartRepository.findById(cartId)).willReturn(Optional.of(testCart));
//		given(cartItemRepository.findByCart_CartIdAndBookId(cartId, bookId)).willReturn(null);
//		given(bookClient.getBooksByIds(anyList())).willReturn(List.of(mockBook));
//		given(cartItemRepository.save(any(CartItem.class))).willReturn(testCartItem);
//
//		// when
//		CartItemResponse response = cartItemService.addCartItem(request);
//
//		// then
//		assertThat(response).isNotNull();
//		assertThat(response.getQuantity()).isEqualTo(2L);
//		verify(cartItemRepository).save(any(CartItem.class));
//	}
//
//	@Test
//	@DisplayName("이미 존재하는 아이템을 추가하면 수량을 더한다")
//	void addExistingItem_updatesQuantity() {
//		// given
//		CartItemRequest request = new CartItemRequest(cartId, bookId, 3L);
//		given(cartRepository.findById(cartId)).willReturn(Optional.of(testCart));
//		given(cartItemRepository.findByCart_CartIdAndBookId(cartId, bookId)).willReturn(testCartItem);
//		given(bookClient.getBooksByIds(anyList())).willReturn(List.of(mockBook));
//		given(cartItemRepository.save(any(CartItem.class))).willAnswer(invocation -> invocation.getArgument(0));
//
//		// when
//		CartItemResponse response = cartItemService.addCartItem(request);
//
//		// then
//		assertThat(response.getQuantity()).isEqualTo(5L); // 2 + 3 = 5
//		verify(cartItemRepository).save(testCartItem);
//	}
//
//	@Test
//	@DisplayName("장바구니를 찾을 수 없으면 예외를 던진다")
//	void addCartItem_cartNotFound_throwsException() {
//		// given
//		CartItemRequest request = new CartItemRequest(cartId, bookId, 1L);
//		given(cartRepository.findById(cartId)).willReturn(Optional.empty());
//
//		// when & then
//		assertThatThrownBy(() -> cartItemService.addCartItem(request))
//				.isInstanceOf(DataNotFoundException.class);
//	}
//
//	@Test
//	@DisplayName("도서 정보를 찾을 수 없으면 예외를 던진다")
//	void addCartItem_bookNotFound_throwsException() {
//		// given
//		CartItemRequest request = new CartItemRequest(cartId, bookId, 1L);
//		given(cartRepository.findById(cartId)).willReturn(Optional.of(testCart));
//		given(cartItemRepository.findByCart_CartIdAndBookId(cartId, bookId)).willReturn(null);
//		given(bookClient.getBooksByIds(anyList())).willReturn(Collections.emptyList());
//
//		// when & then
//		assertThatThrownBy(() -> cartItemService.addCartItem(request))
//				.isInstanceOf(DataNotFoundException.class);
//	}
//
//	@Test
//	@DisplayName("성공적으로 아이템 수량을 변경한다")
//	void updateQuantity_success() {
//		// given
//		given(cartItemRepository.findById(cartItemId)).willReturn(Optional.of(testCartItem));
//		given(bookClient.getBooksByIds(anyList())).willReturn(List.of(mockBook));
//		given(cartItemRepository.save(any(CartItem.class))).willAnswer(i -> i.getArgument(0));
//
//		// when
//		CartItemResponse response = cartItemService.updateCartItemQuantity(cartItemId, 5L);
//
//		// then
//		assertThat(response.getQuantity()).isEqualTo(5L);
//		verify(cartItemRepository).save(testCartItem);
//	}
//
//	@Test
//	@DisplayName("수정할 아이템을 찾을 수 없으면 예외를 던진다")
//	void updateQuantity_itemNotFound_throwsException() {
//		// given
//		given(cartItemRepository.findById(anyLong())).willReturn(Optional.empty());
//
//		// when & then
//		assertThatThrownBy(() -> cartItemService.updateCartItemQuantity(999L, 5L))
//				.isInstanceOf(DataNotFoundException.class)
//				.hasMessageContaining("Cart item to update not found");
//	}
//
//	@Test
//	@DisplayName("수정 시 도서 정보를 찾을 수 없으면 예외를 던진다")
//	void updateQuantity_bookNotFound_throwsException() {
//		// given
//		given(cartItemRepository.findById(cartItemId)).willReturn(Optional.of(testCartItem));
//		given(bookClient.getBooksByIds(anyList())).willReturn(Collections.emptyList());
//
//		// when & then
//		assertThatThrownBy(() -> cartItemService.updateCartItemQuantity(cartItemId, 5L))
//				.isInstanceOf(DataNotFoundException.class)
//				.hasMessageContaining("도서 정보를 찾을 수 없습니다");
//	}
//
//	@Test
//	@DisplayName("removeAllCartItems: 장바구니의 모든 아이템을 성공적으로 삭제한다")
//	void removeAllCartItems_success() {
//		// given
//		given(cartItemRepository.findByCart_CartId(cartId)).willReturn(List.of(testCartItem));
//		doNothing().when(cartItemRepository).deleteByCart_CartId(cartId);
//		// when
//		cartItemService.removeAllCartItems(cartId);
//		// then
//		verify(cartItemRepository).deleteByCart_CartId(cartId);
//	}
//
//	@Test
//	@DisplayName("removeAllCartItems: 삭제할 아이템이 없을 때 호출 시 아무 작업도 하지 않는다")
//	void removeAllCartItems_whenEmpty_doesNothing() {
//		// given
//		given(cartItemRepository.findByCart_CartId(cartId)).willReturn(Collections.emptyList());
//		// when
//		cartItemService.removeAllCartItems(cartId);
//		// then
//		verify(cartItemRepository, never()).deleteByCart_CartId(anyLong());
//	}
//
//	@Test
//	@DisplayName("removeCartItemByBookId: Book ID로 특정 아이템을 성공적으로 삭제한다")
//	void removeCartItemByBookId_success() {
//		// given
//		given(cartItemRepository.findByCart_CartIdAndBookId(cartId, bookId)).willReturn(testCartItem);
//		doNothing().when(cartItemRepository).deleteByCart_CartIdAndBookId(cartId, bookId);
//		// when
//		cartItemService.removeCartItemByBookId(cartId, bookId);
//		// then
//		verify(cartItemRepository).deleteByCart_CartIdAndBookId(cartId, bookId);
//	}
//
//	@Test
//	@DisplayName("removeCartItemByBookId: Book ID로 삭제 실패 시 예외를 던진다")
//	void removeCartItemByBookId_notFound_throwsException() {
//		// given
//		given(cartItemRepository.findByCart_CartIdAndBookId(anyLong(), anyLong())).willReturn(null);
//		// when & then
//		assertThatThrownBy(() -> cartItemService.removeCartItemByBookId(cartId, bookId))
//				.isInstanceOf(DataNotFoundException.class);
//		verify(cartItemRepository, never()).deleteByCart_CartIdAndBookId(anyLong(), anyLong());
//	}
//
//	@Test
//	@DisplayName("새로운 아이템을 성공적으로 병합한다")
//	void mergeNewItem_success() {
//		// given
//		GuestCartItem guestItem = new GuestCartItem(bookId, 3L);
//		List<GuestCartItem> guestItems = List.of(guestItem);
//		given(bookClient.getBooksByIds(List.of(bookId))).willReturn(List.of(mockBook));
//		given(cartItemRepository.findByCart_CartIdAndBookId(cartId, bookId)).willReturn(null);
//
//		// when
//		cartItemService.mergeGuestItemsIntoMemberCart(guestItems, testCart);
//
//		// then
//		verify(cartItemRepository).save(any(CartItem.class));
//	}
//
//	@Test
//	@DisplayName("기존에 있는 아이템과 병합하여 수량을 더한다")
//	void mergeWithExistingItem_updatesQuantity() {
//		// given
//		GuestCartItem guestItem = new GuestCartItem(bookId, 3L);
//		given(bookClient.getBooksByIds(List.of(bookId))).willReturn(List.of(mockBook));
//		given(cartItemRepository.findByCart_CartIdAndBookId(cartId, bookId)).willReturn(testCartItem);
//
//		// when
//		cartItemService.mergeGuestItemsIntoMemberCart(List.of(guestItem), testCart);
//
//		// then
//		verify(cartItemRepository).save(testCartItem);
//		assertThat(testCartItem.getQuantity()).isEqualTo(5L); // 2 + 3
//	}
//
//	@Test
//	@DisplayName("병합할 도서 정보를 찾을 수 없으면 예외를 던진다")
//	void mergeItem_bookNotFound_throwsException() {
//		// given
//		GuestCartItem guestItem = new GuestCartItem(bookId, 1L);
//		given(bookClient.getBooksByIds(anyList())).willReturn(Collections.emptyList());
//
//		// when & then
//		assertThatThrownBy(() -> cartItemService.mergeGuestItemsIntoMemberCart(List.of(guestItem), testCart))
//				.isInstanceOf(DataNotFoundException.class);
//	}
//
//	@Test
//	@DisplayName("병합할 비회원 아이템 리스트가 비어있으면 BookClient는 호출되지만 아이템은 저장되지 않는다")
//	void mergeWithEmptyGuestItems_doesNothing() {
//		// given
//		List<GuestCartItem> emptyGuestItems = Collections.emptyList();
//		given(bookClient.getBooksByIds(Collections.emptyList())).willReturn(Collections.emptyList());
//
//		// when
//		cartItemService.mergeGuestItemsIntoMemberCart(emptyGuestItems, testCart);
//
//		// then
//		verify(bookClient).getBooksByIds(Collections.emptyList());
//		verify(cartItemRepository, never()).save(any());
//	}
//
//}
//
