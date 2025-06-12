package shop.dodream.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shop.dodream.cart.util.BookAvailabilityChecker;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuestCartItemResponse {
	private Long bookId;
	private String title;
	private String author;
	private Long quantity;
	private Long stockQuantity;
	private Long price;
	private boolean isAvailable;
	
	public static GuestCartItemResponse of(GuestCartItem item, BookDto book) {
		boolean isAvailable = BookAvailabilityChecker.isAvailable(book, item.getQuantity());
		
		return new GuestCartItemResponse(
				item.getBookId(),
				book.getTitle(),
				book.getAuthor(),
				item.getQuantity(),
				book.getStockQuantity(),
				book.getDiscountPrice(),
				isAvailable
		);
	}
}
