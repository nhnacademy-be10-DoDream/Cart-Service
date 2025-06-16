package shop.dodream.cart.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import shop.dodream.cart.client.BookClient;
import shop.dodream.cart.dto.BookDto;
import shop.dodream.cart.dto.CartResponse;
import shop.dodream.cart.entity.Cart;
import shop.dodream.cart.exception.DataNotFoundException;
import shop.dodream.cart.exception.MissingIdentifierException;
import shop.dodream.cart.repository.CartRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Transactional
public class CartServiceTest {
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	private CartRepository cartRepository;
	
	@MockBean
	private BookClient bookClient;
	
	@Autowired
	private GuestCartService guestCartService;
	
	private final String userId = "testUserId";
	private final String guestId = "testGuestId";

	@BeforeEach
	void setup() {
		BookDto mockBook = new BookDto(1L,"testbook",3000L,10L);
		given(bookClient.getBookById(anyLong())).willReturn(mockBook);
	}
	
	@Test
	void testSaveCartByUserId() {
		CartResponse cartResponse = cartService.saveCart(userId,null);
		
		assertThat(cartResponse).isNotNull();
		assertThat(cartResponse.getUserId()).isEqualTo(userId);
		assertThat(cartResponse.getGuestId()).isNull();
	}
	
	@Test
	void testSaveCartByGuestId() {
		CartResponse cartResponse = cartService.saveCart(null,guestId);
		
		assertThat(cartResponse).isNotNull();
		assertThat(cartResponse.getUserId()).isEqualTo(null);
		assertThat(cartResponse.getGuestId()).isEqualTo(guestId);
	}
	
	@Test
	void testSaveCartFail(){
		assertThatThrownBy(()-> cartService.saveCart(null,null))
				.isInstanceOf(MissingIdentifierException.class);
	}
	
	@Test
	void testGetCartByUserId(){
		cartService.saveCart(userId,null);
		Optional<CartResponse> cartResponse = cartService.getCartByUserId(userId);
		
		assertThat(cartResponse).isPresent();
		assertThat(cartResponse.get().getUserId()).isEqualTo(userId);
		assertThat(cartResponse.get().getItems()).isEmpty();
	}
	
	@Test
	void testGetCartByGuestId(){
		cartService.saveCart(null,guestId);
		Optional<CartResponse> cartResponse = cartService.getCartByGuestId(guestId);
		
		assertThat(cartResponse).isPresent();
		assertThat(cartResponse.get().getGuestId()).isEqualTo(guestId);
		assertThat(cartResponse.get().getItems()).isEmpty();
	}
	
	@Test
	void testDeleteCartByCartId(){
		Cart cart = new Cart(1L,userId,guestId);
		cartRepository.save(cart);
		cartService.deleteCart(cart.getCartId());
		assertThat(cartRepository.findById(cart.getCartId())).isEmpty();
	}
	
	@Test
	void testDeleteCartByCartIdFail(){
		assertThrows(DataNotFoundException.class, () -> {
			cartService.deleteCart(999L);
		});
	}
	
	
}
