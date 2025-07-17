package shop.dodream.cart.dto;


import lombok.*;
import shop.dodream.cart.entity.Cart;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse  {
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

