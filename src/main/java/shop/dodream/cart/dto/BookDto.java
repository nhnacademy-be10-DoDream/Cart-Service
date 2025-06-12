package shop.dodream.cart.dto;

import lombok.Data;

@Data
public class BookDto {
	private Long id;
	private String title;
	private String author;
	private Long discountPrice;
	private Long stockQuantity;
	public enum BookStatus {
		SELL, SOLD_OUT, REMOVED, LOW_STOCK
	}
	private BookStatus status;
}
