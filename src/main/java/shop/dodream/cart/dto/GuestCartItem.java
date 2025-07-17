package shop.dodream.cart.dto;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GuestCartItem {
	private Long bookId;
	private Long quantity;
}
