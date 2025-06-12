package shop.dodream.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import shop.dodream.cart.entity.Cart;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class CartResponse {
	private Long cartId;
	private String userId;
	private String guestId;
	private List<CartItemResponse> items;
	
	
	public static CartResponse of(Cart cart) {
		return new CartResponse(
				cart.getCartId(),
				cart.getUserId(),
				cart.getGuestId(),
				new ArrayList<>()
		);
	}
	
	public static CartResponse of(Cart cart, List<CartItemResponse> items) {
		return new CartResponse(
				cart.getCartId(),
				cart.getUserId(),
				cart.getGuestId(),
				items
		);
	}
}

