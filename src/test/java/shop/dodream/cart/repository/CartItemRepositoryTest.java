package shop.dodream.cart.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import shop.dodream.cart.entity.CartItem;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CartItemRepositoryTest {

	@Autowired
	CartItemRepository cartItemRepository;

	@Test
	void testFindByCartId() {
		CartItem cartItem = new CartItem(1L, 3L,3L,1L,3000L);
		cartItemRepository.save(cartItem);

		List<CartItem> result = cartItemRepository.findByCartId(cartItem.getCartId());
		assertThat(result).hasSize(1);
	}

	@Test
	void testFindByCartIdAndBookId() {
		CartItem cartItem = new CartItem(1L, 3L,3L,1L,3000L);
		cartItemRepository.save(cartItem);

		CartItem result = cartItemRepository.findByCartIdAndBookId(cartItem.getCartId(), cartItem.getBookId());
		assertThat(result.getCartId()).isEqualTo(cartItem.getCartId());
		assertThat(result.getBookId()).isEqualTo(cartItem.getBookId());
		assertThat(result.getQuantity()).isEqualTo(cartItem.getQuantity());
		assertThat(result.getSalePrice()).isEqualTo(cartItem.getSalePrice());
	}

	@Test
	void testDeleteByCartIdAndBookId() {
		CartItem cartItem = new CartItem(1L, 3L,3L,1L,3000L);
		cartItemRepository.save(cartItem);

		CartItem beforeDelete = cartItemRepository.findByCartIdAndBookId(1L, 3L);
		assertThat(beforeDelete).isNotNull();

		cartItemRepository.deleteByCartIdAndBookId(1L, 3L);

		CartItem afterDelete = cartItemRepository.findByCartIdAndBookId(1L, 3L);
		assertThat(afterDelete).isNull();
	}

	@Test
	void testDeleteByCartId() {
		CartItem cartItem = new CartItem(null, 1L, 3L, 1L, 3000L);
		cartItemRepository.save(cartItem);

		List<CartItem> beforeDelete = cartItemRepository.findByCartId(1L);
		assertThat(beforeDelete).isNotEmpty();

		cartItemRepository.deleteByCartId(1L);

		List<CartItem> afterDelete = cartItemRepository.findByCartId(1L);
		assertThat(afterDelete).isEmpty();
	}

}
