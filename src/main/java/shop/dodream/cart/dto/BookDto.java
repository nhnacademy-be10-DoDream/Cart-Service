package shop.dodream.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDto {
	private Long id;
	private String title;
	private Long discountPrice;
	private Long stockQuantity;
}
