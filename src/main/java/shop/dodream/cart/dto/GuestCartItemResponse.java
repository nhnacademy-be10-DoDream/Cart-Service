package shop.dodream.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuestCartItemResponse {
	private Long bookId;
	private String title;
	private Long quantity;
	private Long stockQuantity;
	private Long price;
	
	
	public static GuestCartItemResponse of(GuestCartItem item, BookDto book) {
		
		return new GuestCartItemResponse(
				item.getBookId(),
				book.getTitle(),
				item.getQuantity(),
				book.getStockQuantity(),
				book.getDiscountPrice()
		);
	}
}
