package shop.dodream.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDetailResponse {
	private Long bookId;
	private String title;
	private Long salePrice;
	private List<String> bookUrls;
}
