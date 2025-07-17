package shop.dodream.cart.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GuestCartResponse {
	private String guestId;
	private List<GuestCartItemResponse> items;
}
