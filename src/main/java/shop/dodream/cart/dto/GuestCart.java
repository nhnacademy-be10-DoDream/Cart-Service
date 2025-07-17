package shop.dodream.cart.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GuestCart {
	private String guestId;
	private List<GuestCartItem> items = new ArrayList<>();
}
