package shop.dodream.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import shop.dodream.cart.entity.CartItem;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
public class CartItemResponse {
	private Long cartItemId;
	private Long bookId;
	private String title;
	private Long salePrice;
	@NotNull
	@Min(1)
	private Long quantity;
	private List<String> bookUrls;
	
	public static CartItemResponse of(CartItem item, BookDetailResponse book) {
		
		return new CartItemResponse(
				item.getCartItemId(),
				item.getBookId(),
				book.getTitle(),
				book.getSalePrice(),
				item.getQuantity(),
				book.getBookUrls()
		);
	}
	
	public static CartItemResponse of(CartItem item, BookListResponseRecord book) {
		
		return new CartItemResponse(
				item.getCartItemId(),
				item.getBookId(),
				book.getTitle(),
				book.getSalePrice(),
				item.getQuantity(),
				Collections.singletonList(book.getBookUrl())
		);
	}
}


