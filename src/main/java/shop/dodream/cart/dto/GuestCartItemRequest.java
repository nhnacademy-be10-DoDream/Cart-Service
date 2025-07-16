package shop.dodream.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuestCartItemRequest {
	@NotNull
	private Long bookId;
	@NotNull
	@Min(1)
	private Long quantity;
}

