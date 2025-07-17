package shop.dodream.cart.service;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shop.dodream.cart.client.BookClient;
import shop.dodream.cart.dto.BookListResponseRecord;
import shop.dodream.cart.dto.CartItemRequest;
import shop.dodream.cart.dto.CartItemResponse;
import shop.dodream.cart.dto.GuestCartItem;
import shop.dodream.cart.entity.Cart;
import shop.dodream.cart.entity.CartItem;
import shop.dodream.cart.exception.DataNotFoundException;
import shop.dodream.cart.repository.CartItemRepository;
import shop.dodream.cart.repository.CartRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartItemServiceTest {
	
	// 테스트 대상 클래스. @Mock으로 생성된 객체들이 자동으로 주입됩니다.
	@InjectMocks
	private CartItemService cartItemService;
	
	// Mock 객체 생성
	@Mock
	private CartItemRepository cartItemRepository;
	
	@Mock
	private BookClient bookClient;
	
	@Mock
	private CartRepository cartRepository;
	
	// 테스트에서 공통으로 사용할 변수들
	private Cart cart;
	private CartItem cartItem1;
	private CartItem cartItem2;
	private BookListResponseRecord book1;
	private BookListResponseRecord book2;
	
	// 각 테스트 메소드 실행 전에 호출되어 테스트 환경을 설정합니다.
	@BeforeEach
	void setUp() {
		// 공통 Cart 객체 생성
		cart = new Cart(1L, "user123");
		
		// 공통 CartItem 객체 생성
		cartItem1 = new CartItem(10L, 2L, 101L, cart, 15000L);
		cartItem2 = new CartItem(11L, 1L, 102L, cart, 20000L);
		
		// 공통 Book 정보 객체 생성
		book1 = new BookListResponseRecord(101L, "JPA 프로그래밍", 15000L, "/books/101.jpg");
		book2 = new BookListResponseRecord(102L, "스프링 부트 완벽 정복", 20000L, "/books/102.jpg");
	}
	
	@Test
	@DisplayName("성공: 장바구니에 담긴 아이템 목록을 정상적으로 조회한다")
	void getCartItems_Success() {
		// given (준비)
		Long cartId = cart.getCartId();
		List<CartItem> itemsInDb = List.of(cartItem1, cartItem2);
		List<BookListResponseRecord> booksFromClient = List.of(book1, book2);
		List<Long> bookIds = List.of(101L, 102L);
		
		// Repository와 Client의 동작 Mocking
		when(cartItemRepository.findByCart_CartId(cartId)).thenReturn(itemsInDb);
		when(bookClient.getBooksByIds(bookIds)).thenReturn(booksFromClient);
		
		// when (실행)
		List<CartItemResponse> result = cartItemService.getCartItems(cartId);
		
		// then (검증)
		assertThat(result).isNotNull();
		assertThat(result).hasSize(2);
		
		// 첫 번째 아이템 검증
		assertThat(result.get(0).getCartItemId()).isEqualTo(cartItem1.getCartItemId());
		assertThat(result.get(0).getTitle()).isEqualTo(book1.getTitle());
		assertThat(result.get(0).getQuantity()).isEqualTo(cartItem1.getQuantity());
		
		// 두 번째 아이템 검증
		assertThat(result.get(1).getCartItemId()).isEqualTo(cartItem2.getCartItemId());
		assertThat(result.get(1).getTitle()).isEqualTo(book2.getTitle());
		assertThat(result.get(1).getQuantity()).isEqualTo(cartItem2.getQuantity());
		
		// Mock 객체의 메소드가 정확히 1번씩 호출되었는지 검증
		verify(cartItemRepository, times(1)).findByCart_CartId(cartId);
		verify(bookClient, times(1)).getBooksByIds(bookIds);
	}
	
	@Test
	@DisplayName("성공: 장바구니가 비어있을 경우 빈 리스트를 반환한다")
	void getCartItems_WhenCartIsEmpty_ShouldReturnEmptyList() {
		// given
		Long cartId = cart.getCartId();
		when(cartItemRepository.findByCart_CartId(cartId)).thenReturn(Collections.emptyList());
		
		// when
		List<CartItemResponse> result = cartItemService.getCartItems(cartId);
		
		// then
		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
		
		// BookClient는 호출되지 않아야 함
		verify(bookClient, never()).getBooksByIds(anyList());
	}
	
	@Test
	@DisplayName("성공: 새로운 상품을 장바구니에 추가한다")
	void addCartItem_WhenItemIsNew_ShouldCreateNewItem() {
		// given
		CartItemRequest request = new CartItemRequest(cart.getCartId(), book1.getBookId(), 1L);
		
		when(cartRepository.findById(request.getCartId())).thenReturn(Optional.of(cart));
		when(cartItemRepository.findByCart_CartIdAndBookId(request.getCartId(), request.getBookId())).thenReturn(null); // 기존 아이템 없음
		when(bookClient.getBooksByIds(List.of(request.getBookId()))).thenReturn(List.of(book1));
		// save 메소드가 호출될 때, 입력으로 받은 CartItem 객체를 그대로 반환하도록 설정
		when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
			CartItem itemToSave = invocation.getArgument(0);
			itemToSave.setCartItemId(99L); // 새로운 ID 부여 시뮬레이션
			return itemToSave;
		});
		
		// when
		CartItemResponse result = cartItemService.addCartItem(request);
		
		// then
		assertThat(result).isNotNull();
		assertThat(result.getBookId()).isEqualTo(request.getBookId());
		assertThat(result.getQuantity()).isEqualTo(request.getQuantity());
		assertThat(result.getTitle()).isEqualTo(book1.getTitle());
		
		// ArgumentCaptor를 사용하여 save 메소드에 전달된 실제 CartItem 객체를 캡처
		ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
		verify(cartItemRepository, times(1)).save(captor.capture());
		
		CartItem savedItem = captor.getValue();
		assertThat(savedItem.getBookId()).isEqualTo(request.getBookId());
		assertThat(savedItem.getQuantity()).isEqualTo(request.getQuantity());
		assertThat(savedItem.getCart()).isEqualTo(cart);
		assertThat(savedItem.getSalePrice()).isEqualTo(book1.getSalePrice());
	}
	
	@Test
	@DisplayName("성공: 이미 장바구니에 있는 상품을 추가하면 수량이 증가한다")
	void addCartItem_WhenItemExists_ShouldUpdateQuantity() {
		// given
		CartItemRequest request = new CartItemRequest(cart.getCartId(), book1.getBookId(), 2L); // 2개 추가 요청
		
		// 기존에 2개가 담겨있던 cartItem1을 반환하도록 설정
		when(cartRepository.findById(request.getCartId())).thenReturn(Optional.of(cart));
		when(cartItemRepository.findByCart_CartIdAndBookId(request.getCartId(), request.getBookId())).thenReturn(cartItem1);
		when(bookClient.getBooksByIds(List.of(request.getBookId()))).thenReturn(List.of(book1));
		when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem1); // 변경된 cartItem1 반환
		
		// when
		CartItemResponse result = cartItemService.addCartItem(request);
		
		// then
		assertThat(result).isNotNull();
		// 기존 수량(2) + 요청 수량(2) = 4
		assertThat(result.getQuantity()).isEqualTo(4L);
		assertThat(result.getCartItemId()).isEqualTo(cartItem1.getCartItemId());
		
		// save 메소드가 호출되었는지, 그리고 수량이 정확히 업데이트 되었는지 검증
		ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
		verify(cartItemRepository, times(1)).save(captor.capture());
		
		CartItem savedItem = captor.getValue();
		assertThat(savedItem.getQuantity()).isEqualTo(4L);
	}
	
	@Test
	@DisplayName("실패: 존재하지 않는 장바구니에 아이템을 추가하면 예외가 발생한다")
	void addCartItem_WhenCartNotFound_ShouldThrowException() {
		// given
		CartItemRequest request = new CartItemRequest(999L, book1.getBookId(), 1L);
		when(cartRepository.findById(request.getCartId())).thenReturn(Optional.empty());
		
		// when & then
		DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> {
			cartItemService.addCartItem(request);
		});
		
		assertThat(exception.getMessage()).isEqualTo("Cart not found with id: " + request.getCartId());
		// 아이템 조회나 저장은 일어나지 않아야 함
		verify(cartItemRepository, never()).findByCart_CartIdAndBookId(any(), any());
		verify(cartItemRepository, never()).save(any());
	}
	
	@Test
	@DisplayName("성공: 장바구니 아이템의 수량을 정상적으로 변경한다")
	void updateCartItemQuantity_Success() {
		// given
		Long cartId = cart.getCartId();
		Long cartItemId = cartItem1.getCartItemId();
		Long newQuantity = 5L;
		
		when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem1));
		when(bookClient.getBooksByIds(List.of(cartItem1.getBookId()))).thenReturn(List.of(book1));
		when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
		when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
		
		// when
		CartItemResponse result = cartItemService.updateCartItemQuantity(cartId, cartItemId, newQuantity);
		
		// then
		assertThat(result).isNotNull();
		assertThat(result.getQuantity()).isEqualTo(newQuantity);
		assertThat(result.getSalePrice()).isEqualTo(book1.getSalePrice()); // 가격도 최신화되었는지 확인
		
		ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
		verify(cartItemRepository, times(1)).save(captor.capture());
		
		CartItem updatedItem = captor.getValue();
		assertThat(updatedItem.getQuantity()).isEqualTo(newQuantity);
		assertThat(updatedItem.getSalePrice()).isEqualTo(book1.getSalePrice());
	}
	
	@Test
	@DisplayName("실패: 변경하려는 아이템이 없으면 예외가 발생한다")
	void updateCartItemQuantity_WhenItemNotFound_ShouldThrowException() {
		// given
		Long cartId = 1L;
		Long nonExistentCartItemId = 999L;
		Long newQuantity = 5L;
		when(cartItemRepository.findById(nonExistentCartItemId)).thenReturn(Optional.empty());
		
		// when & then
		assertThrows(DataNotFoundException.class, () -> {
			cartItemService.updateCartItemQuantity(cartId, nonExistentCartItemId, newQuantity);
		});
		
		verify(cartItemRepository, never()).save(any());
	}
	
	@Test
	@DisplayName("성공: 장바구니의 모든 아이템을 삭제한다")
	void removeAllCartItems_Success() {
		// given
		Long cartId = cart.getCartId();
		when(cartItemRepository.findByCart_CartId(cartId)).thenReturn(List.of(cartItem1, cartItem2));
		doNothing().when(cartItemRepository).deleteByCart_CartId(cartId);
		
		// when
		cartItemService.removeAllCartItems(cartId);
		
		// then
		verify(cartItemRepository, times(1)).deleteByCart_CartId(cartId);
	}
	
	@Test
	@DisplayName("성공: 특정 책 ID에 해당하는 아이템을 삭제한다")
	void removeCartItemByBookId_Success() {
		// given
		Long cartId = cart.getCartId();
		Long bookIdToRemove = book1.getBookId();
		when(cartItemRepository.findByCart_CartIdAndBookId(cartId, bookIdToRemove)).thenReturn(cartItem1);
		doNothing().when(cartItemRepository).deleteByCart_CartIdAndBookId(cartId, bookIdToRemove);
		
		// when
		cartItemService.removeCartItemByBookId(cartId, bookIdToRemove);
		
		// then
		verify(cartItemRepository, times(1)).deleteByCart_CartIdAndBookId(cartId, bookIdToRemove);
	}
	
	@Test
	@DisplayName("실패: 삭제하려는 아이템이 없으면 예외가 발생한다")
	void removeCartItemByBookId_WhenItemNotFound_ShouldThrowException() {
		// given
		Long cartId = cart.getCartId();
		Long bookIdToRemove = 999L; // 존재하지 않는 책 ID
		when(cartItemRepository.findByCart_CartIdAndBookId(cartId, bookIdToRemove)).thenReturn(null);
		
		// when & then
		assertThrows(DataNotFoundException.class, () -> {
			cartItemService.removeCartItemByBookId(cartId, bookIdToRemove);
		});
		
		verify(cartItemRepository, never()).deleteByCart_CartIdAndBookId(anyLong(), anyLong());
	}
	
	@Test
	@DisplayName("성공: 비회원 장바구니 아이템을 회원 장바구니에 병합한다")
	void mergeGuestItemsIntoMemberCart_Success() {
		// given
		// 비회원 장바구니: book1(수량 1), book2(수량 3)
		GuestCartItem guestItem1 = new GuestCartItem(book1.getBookId(), 1L);
		GuestCartItem guestItem2 = new GuestCartItem(book2.getBookId(), 3L);
		List<GuestCartItem> guestItems = List.of(guestItem1, guestItem2);
		
		// 회원 장바구니에는 이미 book1이 수량 2개 존재
		// cartItem1 (bookId: 101L, quantity: 2L)
		
		List<Long> bookIdsToFetch = List.of(101L, 102L);
		when(bookClient.getBooksByIds(bookIdsToFetch)).thenReturn(List.of(book1, book2));
		
		// 기존에 존재하는 아이템(book1)에 대한 Mocking
		when(cartItemRepository.findByCart_CartIdAndBookId(cart.getCartId(), book1.getBookId())).thenReturn(cartItem1);
		// 새로 추가될 아이템(book2)에 대한 Mocking
		when(cartItemRepository.findByCart_CartIdAndBookId(cart.getCartId(), book2.getBookId())).thenReturn(null);
		
		// when
		cartItemService.mergeGuestItemsIntoMemberCart(guestItems, cart);
		
		// then
		// save 메소드가 2번 호출되었는지 검증 (1번은 수량 업데이트, 1번은 신규 생성)
		ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
		verify(cartItemRepository, times(2)).save(captor.capture());
		
		List<CartItem> savedItems = captor.getAllValues();
		
		// book1 (기존 아이템) 검증: 기존 수량(2) + 게스트 수량(1) = 3
		CartItem updatedItem = savedItems.stream().filter(item -> item.getBookId().equals(book1.getBookId())).findFirst().orElseThrow();
		assertThat(updatedItem.getQuantity()).isEqualTo(3L);
		
		// book2 (신규 아이템) 검증: 게스트 수량(3)
		CartItem newItem = savedItems.stream().filter(item -> item.getBookId().equals(book2.getBookId())).findFirst().orElseThrow();
		assertThat(newItem.getQuantity()).isEqualTo(3L);
		assertThat(newItem.getCart()).isEqualTo(cart);
		assertThat(newItem.getSalePrice()).isEqualTo(book2.getSalePrice());
	}
	
}

