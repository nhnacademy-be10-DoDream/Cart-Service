package shop.dodream.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemRequest {
	private Long cartId;
	private Long bookId;
	private Long quantity;
}
