package shop.dodream.cart.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import shop.dodream.cart.dto.BookDetailResponse;
import shop.dodream.cart.dto.BookListResponseRecord;

import java.util.List;

@FeignClient(name = "bookClient")
public interface BookClient {
	
	@GetMapping("/public/books/{bookId}")
	BookDetailResponse getBookById(@PathVariable("bookId") Long id);
	@GetMapping("/public/books")
	List<BookListResponseRecord> getBooksByIds(@RequestParam("ids") List<Long> bookIds);
}
