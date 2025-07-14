package shop.dodream.cart.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import shop.dodream.cart.entity.Cart;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CartRepositoryTest {

	@Autowired
	CartRepository cartRepository;


	@Test
	void testFindByUserId() {
		Cart cart = new Cart(1L,"member1");
		cartRepository.save(cart);
		Optional<Cart> result = cartRepository.findByUserId(cart.getUserId());
		assertThat(result).isPresent();
		assertThat(result.get().getUserId()).isEqualTo("member1");
	}
	
}
