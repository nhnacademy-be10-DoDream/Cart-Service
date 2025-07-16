package shop.dodream.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import shop.dodream.cart.entity.CartItem;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class CartItemResponse implements Serializable {
	@NotNull
	private Long cartItemId;
	@NotNull
	private Long bookId;
	@NotNull
	private String title;
	@NotNull
	private Long salePrice;
	@NotNull
	@Min(1)
	private Long quantity;
	@NotNull
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


