package shop.dodream.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.dodream.cart.entity.Cart;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
	
	Optional<Cart> findByMemberId(String MemberId);
	Optional<Cart> findBySessionId(String sessionId);
}
