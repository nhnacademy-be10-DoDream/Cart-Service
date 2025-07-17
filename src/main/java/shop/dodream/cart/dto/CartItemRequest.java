package shop.dodream.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItemRequest {
	private Long cartId;
	private Long bookId;
	@NotNull
	@Min(1)
	private Long quantity;
}
