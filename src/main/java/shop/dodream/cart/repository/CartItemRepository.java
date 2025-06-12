package shop.dodream.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.dodream.cart.entity.CartItem;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	
	List<CartItem> findByCartId(Long cartId);
	
	CartItem findByCartIdAndBookId(Long cartId, Long bookId);
	
	void deleteByCartIdAndBookId(Long cartId, Long bookId);
	
	void deleteByCartId(Long cartId);
	
}
