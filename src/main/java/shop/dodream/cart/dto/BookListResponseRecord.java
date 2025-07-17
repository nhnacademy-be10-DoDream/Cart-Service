package shop.dodream.cart.dto;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookListResponseRecord {
	private Long bookId;
	private String title;
	private Long salePrice;
	private String bookUrl;
}
