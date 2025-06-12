package shop.dodream.cart.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
public class CartItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long cartItemId;
	//수량
	private Long quantity;
	//도서고유ID
	private Long bookId;
	//카드고유ID
	private Long cartId;
	//할인가
	private Long price;
	//책구매가능여부
	private boolean isAvailable;
	
	public CartItem(Long cartId, Long bookId) {
		this.cartId = cartId;
		this.bookId = bookId;
	}
	
}
