package shop.dodream.cart.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import shop.dodream.cart.dto.BookDto;

import java.util.List;

@FeignClient(name = "bookClient", url = "http://localhost:10320")
public interface BookClient {
	
	@GetMapping("/books/{bookId}")
	BookDto getBookById(@PathVariable("bookId") Long id);
	@GetMapping("/books")
	List<BookDto> getBooksByIds(@RequestParam("ids") List<Long> bookIds);
}
