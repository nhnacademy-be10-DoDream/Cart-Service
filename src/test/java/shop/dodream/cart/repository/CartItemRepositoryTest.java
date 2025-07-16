package shop.dodream.cart.repository;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import shop.dodream.cart.entity.Cart;
import shop.dodream.cart.entity.CartItem;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@DisplayName("CartItemRepository 테스트")
class CartItemRepositoryTest {
	
	@Autowired
	private TestEntityManager entityManager;
	
	@Autowired
	private CartItemRepository cartItemRepository;
	
	private Cart cart;
	
	@BeforeEach
	void setUp() {
		// 모든 테스트 전에 공통으로 사용할 Cart 엔티티를 생성하고 영속화합니다.
		Cart newCart = new Cart();
		newCart.setUserId("test-user");
		cart = entityManager.persistAndFlush(newCart);
	}
	
	private CartItem createAndPersistItem(Long bookId, Long quantity, Long price) {
		CartItem item = new CartItem();
		item.setCart(cart);
		item.setBookId(bookId);
		item.setQuantity(quantity);
		item.setSalePrice(price);
		return entityManager.persistAndFlush(item);
	}
	
	@Test
	@DisplayName("findByCart_CartId: 특정 장바구니의 모든 아이템을 반환한다")
	void findByCart_CartId_returnsAllItemsInCart() {
		// given
		createAndPersistItem(101L, 2L, 1000L);
		createAndPersistItem(102L, 1L, 2000L);
		
		// when
		List<CartItem> foundItems = cartItemRepository.findByCart_CartId(cart.getCartId());
		
		// then
		assertThat(foundItems).hasSize(2);
		assertThat(foundItems).extracting(CartItem::getBookId).containsExactlyInAnyOrder(101L, 102L);
	}
	
	@Test
	@DisplayName("findByCart_CartId: 아이템이 없는 장바구니는 빈 리스트를 반환한다")
	void findByCart_CartId_whenEmpty_returnsEmptyList() {
		// when
		List<CartItem> foundItems = cartItemRepository.findByCart_CartId(cart.getCartId());
		
		// then
		assertThat(foundItems).isEmpty();
	}
	
	@Test
	@DisplayName("findByCart_CartIdAndBookId: 특정 장바구니의 특정 도서 아이템을 반환한다")
	void findByCart_CartIdAndBookId_returnsSpecificItem() {
		// given
		createAndPersistItem(101L, 2L, 1000L);
		CartItem targetItem = createAndPersistItem(102L, 5L, 2500L);
		
		// when
		CartItem foundItem = cartItemRepository.findByCart_CartIdAndBookId(cart.getCartId(), 102L);
		
		// then
		assertThat(foundItem).isNotNull();
		assertThat(foundItem.getCartItemId()).isEqualTo(targetItem.getCartItemId());
		assertThat(foundItem.getQuantity()).isEqualTo(5L);
	}
	
	@Test
	@DisplayName("findByCart_CartIdAndBookId: 해당 아이템이 없으면 null을 반환한다")
	void findByCart_CartIdAndBookId_whenNotExists_returnsNull() {
		// when
		CartItem foundItem = cartItemRepository.findByCart_CartIdAndBookId(cart.getCartId(), 999L);
		
		// then
		assertThat(foundItem).isNull();
	}
	
	@Test
	@DisplayName("deleteByCart_CartIdAndBookId: 특정 아이템만 정확히 삭제한다")
	void deleteByCart_CartIdAndBookId_deletesOnlySpecifiedItem() {
		// given
		createAndPersistItem(201L, 1L, 1000L);
		createAndPersistItem(202L, 1L, 2000L); // 이 아이템은 남아있어야 함
		
		// when
		cartItemRepository.deleteByCart_CartIdAndBookId(cart.getCartId(), 201L);
		entityManager.flush(); // delete 쿼리 실행
		entityManager.clear(); // 영속성 컨텍스트 초기화하여 DB에서 다시 조회
		
		// then
		List<CartItem> remainingItems = cartItemRepository.findAll();
		assertThat(remainingItems).hasSize(1);
		assertThat(remainingItems.get(0).getBookId()).isEqualTo(202L);
	}
	
	@Test
	@DisplayName("deleteByCart_CartId: 특정 장바구니의 모든 아이템을 삭제한다")
	void deleteByCart_CartId_deletesAllItemsInCart() {
		// given
		createAndPersistItem(301L, 1L, 1000L);
		createAndPersistItem(302L, 1L, 2000L);
		
		// when
		cartItemRepository.deleteByCart_CartId(cart.getCartId());
		entityManager.flush();
		entityManager.clear();
		
		// then
		List<CartItem> remainingItems = cartItemRepository.findByCart_CartId(cart.getCartId());
		assertThat(remainingItems).isEmpty();
	}
	
	@Test
	@DisplayName("수량이 1보다 작은 아이템 저장 시 ConstraintViolationException 발생")
	void whenSavingItemWithInvalidQuantity_throwsException() {
		// given
		CartItem invalidItem = new CartItem();
		invalidItem.setCart(cart);
		invalidItem.setBookId(401L);
		invalidItem.setQuantity(0L); // @Min(1) 위반
		
		// when & then
		assertThatThrownBy(() -> cartItemRepository.saveAndFlush(invalidItem))
				.isInstanceOf(ConstraintViolationException.class);
	}
	
	@Test
	@DisplayName("Cart가 null인 아이템 저장 시 Exception 발생")
	void whenSavingItemWithNullCart_throwsException() {
		// given
		CartItem invalidItem = new CartItem();
		invalidItem.setCart(null); // @JoinColumn(nullable = false) 위반
		invalidItem.setBookId(501L);
		invalidItem.setQuantity(1L);
		
		// when & then
		assertThatThrownBy(() -> cartItemRepository.saveAndFlush(invalidItem))
				.isInstanceOf(Exception.class); // 보통 ConstraintViolationException 이나 다른 Persistence 예외
	}
	
}
