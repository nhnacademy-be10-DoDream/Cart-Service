package shop.dodream.cart.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import shop.dodream.cart.dto.BookDto;

@FeignClient(name = "book-service")
public interface BookClient {
	
	@GetMapping("/books/{id}")
	BookDto getBookById(@PathVariable("id") Long id);
}
