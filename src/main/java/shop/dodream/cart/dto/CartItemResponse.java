package shop.dodream.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import shop.dodream.cart.entity.CartItem;

@Data
@AllArgsConstructor
public class CartItemResponse {
	private Long cartItemId;
	private Long bookId;
	private String bookTitle;
	private Long price;
	private Long quantity;
	private Long stockQuantity;
	
	public static CartItemResponse of(CartItem item, BookDto book) {
		
		return new CartItemResponse(
				item.getCartItemId(),
				item.getBookId(),
				book.getTitle(),
				book.getDiscountPrice(),
				item.getQuantity(),
				book.getStockQuantity()
				
		);
	}
}


