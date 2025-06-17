package shop.dodream.cart.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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
	@NotNull
	private Long bookId;
	//카드고유ID
	private Long cartId;
	//할인가
	private Long discountPrice;
	//정가
	private Long originalPrice;

}

