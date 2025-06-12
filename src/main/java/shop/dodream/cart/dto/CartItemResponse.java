package shop.dodream.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import shop.dodream.cart.entity.CartItem;
import shop.dodream.cart.util.BookAvailabilityChecker;

@Data
@AllArgsConstructor
public class CartItemResponse {
	private Long cartItemId;
	private Long bookId;
	private String bookTitle;
	private String author;
	private Long price;
	private Long quantity;
	private Long stockQuantity;
	private boolean isAvailable;
	
	public static CartItemResponse of(CartItem item, BookDto book) {
		boolean isAvailable = BookAvailabilityChecker.isAvailable(book, item.getQuantity());
		
		return new CartItemResponse(
				item.getCartItemId(),
				item.getBookId(),
				book.getTitle(),
				book.getAuthor(),
				book.getDiscountPrice(),
				item.getQuantity(),
				book.getStockQuantity(),
				isAvailable
		);
	}
}


