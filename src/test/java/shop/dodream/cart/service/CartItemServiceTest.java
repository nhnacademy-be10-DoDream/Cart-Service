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
import shop.dodream.cart.entity.CartItem;
import shop.dodream.cart.exception.DataNotFoundException;
import shop.dodream.cart.repository.CartItemRepository;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;


//@SpringBootTest
//@Transactional
//class CartItemServiceTest {
//
//	@Autowired
//	private CartItemService cartItemService;
//
//	@Autowired
//	private CartItemRepository cartItemRepository;
//
//	@MockBean
//	private BookClient bookClient;
//
//	private final Long cartId = 1L;
//	private final Long bookId = 100L;
//
//	@BeforeEach
//	void setup() {
//		BookDto mockBook = new BookDto(bookId,"testbook",3000L,3000L,10L,"test");
//		given(bookClient.getBookById(anyLong())).willReturn(mockBook);
//	}
//
//	@Test
//	void testAddCartItem() {
//		CartItemRequest request = new CartItemRequest(cartId, bookId, 2L);
//		CartItemResponse response = cartItemService.addCartItem(request);
//
//		assertThat(response.getBookId()).isEqualTo(bookId);
//		assertThat(response.getQuantity()).isEqualTo(2L);
//	}
//
//	@Test
//	void testAddCartItem2() {
//		cartItemRepository.save(new CartItem(1L,3L,bookId,cartId,3000L, 3000L));
//
//		CartItemRequest request = new CartItemRequest(cartId, bookId, 2L);
//		CartItemResponse response = cartItemService.addCartItem(request);
//
//		assertThat(response.getQuantity()).isEqualTo(5L);
//	}
//
//	@Test
//	void testGetCartItems() {
//		BookDto bookDto = new BookDto(bookId,"testbook",3000L,3000L,10L,"test");
//		cartItemRepository.save(new CartItem(1L,3L,bookId,cartId,3000L, 3000L));
//
//		when(bookClient.getBooksByIds(List.of(bookId))).thenReturn(List.of(bookDto));
//		List<CartItemResponse> items = cartItemService.getCartItems(cartId);
//
//		assertThat(items).hasSize(1);
//		assertThat(items.get(0).getBookId()).isEqualTo(bookId);
//	}
//
//	@Test
//	void testUpdateCartItemQuantity() {
//		CartItem saved = cartItemRepository.save(new CartItem(1L,3L,bookId,cartId,3000L, 3000L));
//
//		CartItemResponse response = cartItemService.updateCartItemQuantity(saved.getCartItemId(), 5L);
//
//		assertThat(response.getQuantity()).isEqualTo(5L);
//	}
//
//	@Test
//	void testRemoveCartItem() {
//		CartItem saved = cartItemRepository.save(new CartItem(1L,3L,bookId,cartId,3000L, 3000L));
//
//		cartItemService.removeCartItem(saved.getCartItemId());
//
//		assertThat(cartItemRepository.existsById(saved.getCartItemId())).isFalse();
//	}
//
//	@Test
//	void testRemoveCartItemFail() {
//		assertThatThrownBy(() -> cartItemService.removeCartItem(999L))
//				.isInstanceOf(DataNotFoundException.class);
//	}
//
//	@Test
//	void testRemoveAllCartItems() {
//		cartItemRepository.save(new CartItem(1L,3L,bookId,cartId,3000L, 3000L));
//		cartItemService.removeAllCartItems(cartId);
//
//		assertThat(cartItemRepository.findByCartId(cartId)).isEmpty();
//	}
//
//	@Test
//	void testRemoveAllCartItemsFail() {
//		assertThatThrownBy(() -> cartItemService.removeAllCartItems(cartId))
//				.isInstanceOf(DataNotFoundException.class);
//	}
//
//	@Test
//	void testRemoveCartItemsByBookId() {
//		cartItemRepository.save(new CartItem(1L,3L,bookId,cartId,3000L, 3000L));
//		cartItemService.removeCartItemsByBookId(cartId, bookId);
//
//		assertThat(cartItemRepository.findByCartIdAndBookId(cartId, bookId)).isNull();
//	}
//
//	@Test
//	void testRemoveCartItemsByBookIdFail() {
//		assertThatThrownBy(() -> cartItemService.removeCartItemsByBookId(cartId, bookId))
//				.isInstanceOf(DataNotFoundException.class);
//	}
//
//	@Test
//	void testGetCartItemByBookId() {
//		cartItemRepository.save(new CartItem(1L,3L,bookId,cartId,3000L, 3000L));
//
//		CartItem item = cartItemService.getCartItemByBookId(cartId, bookId);
//
//		assertThat(item).isNotNull();
//		assertThat(item.getBookId()).isEqualTo(bookId);
//	}
//
//	@Test
//	void testGetCartItemByBookIdFail() {
//		assertThatThrownBy(() -> cartItemService.getCartItemByBookId(cartId, bookId))
//				.isInstanceOf(DataNotFoundException.class);
//	}
//
//	@Test
//	void testGetBookByIdForItem() {
//		CartItem item = new CartItem(1L,3L,bookId,cartId,3000L, 3000L);
//		BookDto dto = cartItemService.getBookByIdForItem(item);
//
//		assertThat(dto).isNotNull();
//		assertThat(dto.getId()).isEqualTo(bookId);
//	}
//
//	@Test
//	void testGetCartItemsById() {
//		CartItem saved = cartItemRepository.save(new CartItem(null, 3L, bookId, cartId, 3000L, 3000L));
//
//		CartItem item = cartItemService.getCartItemById(saved.getCartItemId());
//
//		assertThat(item).isNotNull();
//		assertThat(item.getCartItemId()).isEqualTo(saved.getCartItemId());
//	}
//
//	@Test
//	void testGetCartItemByIdFail() {
//		assertThatThrownBy(() -> cartItemService.getCartItemById(999L)).isInstanceOf(DataNotFoundException.class);
//	}
//}

