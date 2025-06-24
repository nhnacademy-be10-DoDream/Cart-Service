package shop.dodream.cart.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import shop.dodream.cart.client.BookClient;
import shop.dodream.cart.dto.BookDto;
import shop.dodream.cart.dto.CartItemRequest;
import shop.dodream.cart.dto.CartItemResponse;
import shop.dodream.cart.dto.GuestCartItem;
import shop.dodream.cart.entity.Cart;
import shop.dodream.cart.entity.CartItem;
import shop.dodream.cart.exception.DataNotFoundException;
import shop.dodream.cart.repository.CartItemRepository;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;



@SpringBootTest
@Transactional
class CartItemServiceTest {
	
	@Autowired
	private CartItemService cartItemService;
	
	@Autowired
	private CartItemRepository cartItemRepository;
	
	@MockBean
	private BookClient bookClient;
	
	private final Long cartId = 1L;
	private final Long bookId = 100L;
	
	@BeforeEach
	void setup() {
		BookDto mockBook = new BookDto(bookId, "testbook", 3000L, 3000L, 10L, "test");
		given(bookClient.getBookById(anyLong())).willReturn(mockBook);
		given(bookClient.getBooksByIds(List.of(bookId))).willReturn(List.of(mockBook));
	}
	
	@Test
	void testAddCartItem() {
		CartItemRequest request = new CartItemRequest(cartId, bookId, 2L);
		CartItemResponse response = cartItemService.addCartItem(request);
		
		assertThat(response.getBookId()).isEqualTo(bookId);
		assertThat(response.getQuantity()).isEqualTo(2L);
	}
	
	@Test
	void testAddCartItem_whenAlreadyExists() {
		cartItemRepository.save(new CartItem(null, 3L, bookId, cartId, 3000L, 3000L));
		
		CartItemRequest request = new CartItemRequest(cartId, bookId, 2L);
		CartItemResponse response = cartItemService.addCartItem(request);
		
		assertThat(response.getQuantity()).isEqualTo(5L);
	}
	
	@Test
	void testGetCartItems() {
		cartItemRepository.save(new CartItem(null, 3L, bookId, cartId, 3000L, 3000L));
		
		List<CartItemResponse> items = cartItemService.getCartItems(cartId);
		
		assertThat(items).hasSize(1);
		assertThat(items.get(0).getBookId()).isEqualTo(bookId);
	}
	
	@Test
	void testGetCartItems_whenBookNotFound() {
		cartItemRepository.save(new CartItem(null, 3L, bookId, cartId, 3000L, 3000L));
		given(bookClient.getBooksByIds(List.of(bookId))).willReturn(List.of()); // 빈 리스트
		
		assertThatThrownBy(() -> cartItemService.getCartItems(cartId))
				.isInstanceOf(DataNotFoundException.class)
				.hasMessageContaining("도서 정보를 찾을 수 없습니다");
	}
	
	@Test
	void testUpdateCartItemQuantity() {
		CartItem saved = cartItemRepository.save(new CartItem(null, 3L, bookId, cartId, 3000L, 3000L));
		CartItemResponse response = cartItemService.updateCartItemQuantity(saved.getCartItemId(), 5L);
		
		assertThat(response.getQuantity()).isEqualTo(5L);
	}
	
	@Test
	void testUpdateCartItemQuantityFail() {
		assertThatThrownBy(() -> cartItemService.updateCartItemQuantity(999L, 5L))
				.isInstanceOf(DataNotFoundException.class)
				.hasMessageContaining("Cart item to update not found");
	}
	
	@Test
	void testRemoveCartItem() {
		CartItem saved = cartItemRepository.save(new CartItem(null, 3L, bookId, cartId, 3000L, 3000L));
		cartItemService.removeCartItem(saved.getCartItemId());
		
		assertThat(cartItemRepository.existsById(saved.getCartItemId())).isFalse();
	}
	
	@Test
	void testRemoveCartItemFail() {
		assertThatThrownBy(() -> cartItemService.removeCartItem(999L))
				.isInstanceOf(DataNotFoundException.class);
	}
	
	@Test
	void testRemoveAllCartItems() {
		cartItemRepository.save(new CartItem(null, 3L, bookId, cartId, 3000L, 3000L));
		cartItemService.removeAllCartItems(cartId);
		
		assertThat(cartItemRepository.findByCartId(cartId)).isEmpty();
	}
	
	@Test
	void testRemoveAllCartItemsFail() {
		assertThatThrownBy(() -> cartItemService.removeAllCartItems(cartId))
				.isInstanceOf(DataNotFoundException.class)
				.hasMessageContaining("No cart items to remove");
	}
	
	@Test
	void testRemoveCartItemsByBookId() {
		cartItemRepository.save(new CartItem(null, 3L, bookId, cartId, 3000L, 3000L));
		cartItemService.removeCartItemsByBookId(cartId, bookId);
		
		assertThat(cartItemRepository.findByCartIdAndBookId(cartId, bookId)).isNull();
	}
	
	@Test
	void testRemoveCartItemsByBookIdFail() {
		assertThatThrownBy(() -> cartItemService.removeCartItemsByBookId(cartId, bookId))
				.isInstanceOf(DataNotFoundException.class)
				.hasMessageContaining("No cart item found");
	}
	
	@Test
	void testGetCartItemByBookIdSuccess() {
		cartItemRepository.save(new CartItem(null, 3L, bookId, cartId, 3000L, 3000L));
		CartItem item = cartItemService.getCartItemByBookId(cartId, bookId);
		
		assertThat(item).isNotNull();
		assertThat(item.getBookId()).isEqualTo(bookId);
	}
	
	@Test
	void testGetCartItemByBookIdFail() {
		assertThatThrownBy(() -> cartItemService.getCartItemByBookId(cartId, bookId))
				.isInstanceOf(DataNotFoundException.class);
	}
	
	@Test
	void testGetBookByIdForItem() {
		CartItem item = new CartItem(null, 3L, bookId, cartId, 3000L, 3000L);
		BookDto dto = cartItemService.getBookByIdForItem(item);
		
		assertThat(dto).isNotNull();
		assertThat(dto.getId()).isEqualTo(bookId);
	}
	
	@Test
	void testGetBookByIdForItem_bookClientReturnsNull() {
		CartItem item = new CartItem(null, 3L, bookId, cartId, 3000L, 3000L);
		given(bookClient.getBookById(bookId)).willReturn(null);
		
		assertThatThrownBy(() -> cartItemService.getBookByIdForItem(item))
				.isInstanceOf(DataNotFoundException.class)
				.hasMessageContaining("도서 정보를 찾을 수 없습니다");
	}
	
	@Test
	void testGetCartItemById() {
		CartItem saved = cartItemRepository.save(new CartItem(null, 3L, bookId, cartId, 3000L, 3000L));
		CartItem item = cartItemService.getCartItemById(saved.getCartItemId());
		
		assertThat(item).isNotNull();
		assertThat(item.getCartItemId()).isEqualTo(saved.getCartItemId());
	}
	
	@Test
	void testGetCartItemByIdFail() {
		assertThatThrownBy(() -> cartItemService.getCartItemById(999L))
				.isInstanceOf(DataNotFoundException.class);
	}
	
	@Test
	void testMergeGuestItemsIntoMemberCart() {
		GuestCartItem guestItem = new GuestCartItem(bookId, 2L);
		List<GuestCartItem> guestItems = List.of(guestItem);
		
		Cart cart = new Cart();
		cart.setCartId(cartId);
		
		cartItemService.mergeGuestItemsIntoMemberCart(guestItems, cart);
		
		CartItem item = cartItemRepository.findByCartIdAndBookId(cartId, bookId);
		assertThat(item).isNotNull();
		assertThat(item.getQuantity()).isEqualTo(2L);
	}
	
	@Test
	void testMergeGuestItemsIntoMemberCart_whenItemAlreadyExists() {
		cartItemRepository.save(new CartItem(null, 3L, bookId, cartId, 3000L, 3000L));
		
		GuestCartItem guestItem = new GuestCartItem(bookId, 2L);
		List<GuestCartItem> guestItems = List.of(guestItem);
		
		Cart cart = new Cart();
		cart.setCartId(cartId);
		
		cartItemService.mergeGuestItemsIntoMemberCart(guestItems, cart);
		
		CartItem item = cartItemRepository.findByCartIdAndBookId(cartId, bookId);
		assertThat(item).isNotNull();
		assertThat(item.getQuantity()).isEqualTo(5L); // 기존 3 + 2
	}
	
	@Test
	void testMergeGuestItemsIntoMemberCart_whenBookNotFound_shouldThrowException() {
		// given: guestItems에 포함된 bookId가 bookClient.getBooksByIds() 응답에 없음
		GuestCartItem guestItem = new GuestCartItem(bookId, 2L);
		List<GuestCartItem> guestItems = List.of(guestItem);
		
		Cart cart = new Cart();
		cart.setCartId(cartId);
		
		// BookClient 응답에 해당 bookId 빠뜨리기
		given(bookClient.getBooksByIds(List.of(bookId))).willReturn(List.of()); // 빈 리스트
		
		// when & then: 예외가 발생해야 함
		assertThatThrownBy(() -> cartItemService.mergeGuestItemsIntoMemberCart(guestItems, cart))
				.isInstanceOf(DataNotFoundException.class)
				.hasMessageContaining("Book not found for ID: " + bookId);
	}
	
}


