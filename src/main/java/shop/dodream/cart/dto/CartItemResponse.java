package shop.dodream.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import shop.dodream.cart.entity.CartItem;

@Data
@AllArgsConstructor
public class CartItemResponse {
	private Long cartItemId;
	private Long bookId;
	private String bookTitle;
	private Long salePrice;
	@NotNull
	@Min(1)
	private Long quantity;
	private String bookUrl;
	
	public static CartItemResponse of(CartItem item, BookDto book) {
		
		return new CartItemResponse(
				item.getCartItemId(),
				item.getBookId(),
				book.getTitle(),
				book.getSalePrice(),
				item.getQuantity(),
				book.getBookUrl()
		);
	}
}


