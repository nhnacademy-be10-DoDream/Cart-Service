package shop.dodream.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import shop.dodream.cart.entity.CartItem;



@Getter
@Setter
@AllArgsConstructor
public class CartItemResponse {
	private Long cartItemId;
	private Long bookId;
	private String title;
	private Long salePrice;
	private Long quantity;
	private String bookUrl;
	
	public static CartItemResponse of(CartItem item, BookListResponseRecord book) {
		
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


