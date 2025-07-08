package shop.dodream.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.dodream.cart.entity.CartItem;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	
	List<CartItem> findByCart_CartId(Long cartId);
	
	CartItem findByCart_CartIdAndBookId(Long cartId, Long bookId);
	
	void deleteByCart_CartIdAndBookId(Long cartId, Long bookId);
	
	void deleteByCart_CartId(Long cartId);
	
}
