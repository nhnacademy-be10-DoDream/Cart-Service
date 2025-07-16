package shop.dodream.cart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shop.dodream.cart.entity.Cart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse implements Serializable {
	@NotNull
	private Long cartId;
	private String userId;
	private List<CartItemResponse> items;
	
	
	public static CartResponse of(Cart cart) {
		return new CartResponse(
				cart.getCartId(),
				cart.getUserId(),
				new ArrayList<>()
		);
	}
	
	public static CartResponse of(Cart cart, List<CartItemResponse> items) {
		return new CartResponse(
				cart.getCartId(),
				cart.getUserId(),
				items
		);
	}
}

