package shop.dodream.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuestCartItemResponse {
	private Long bookId;
	private String title;
	@NotNull
	@Min(1)
	private Long quantity;
	private Long stockQuantity;
	private Long originalPrice;
	private Long discountPrice;
	private String imageUrl;
	
	
	public static GuestCartItemResponse of(GuestCartItem item, BookDto book) {
		
		return new GuestCartItemResponse(
				item.getBookId(),
				book.getTitle(),
				item.getQuantity(),
				book.getStockQuantity(),
				book.getOriginalPrice(),
				book.getDiscountPrice(),
				book.getImageUrl()
		);
	}
}
