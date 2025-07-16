package shop.dodream.cart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@EqualsAndHashCode
public class CartItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long cartItemId;
	//수량
	@NotNull
	@Min(1)
	private Long quantity;
	//도서고유ID
	private Long bookId;
	//카트고유ID
	@ManyToOne
	@JoinColumn(name = "cart_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Cart cart;
	//정가
	private Long salePrice;

	public CartItem(Cart cart, Long bookId) {
		this.cart = cart;
		this.bookId = bookId;
	}
}

