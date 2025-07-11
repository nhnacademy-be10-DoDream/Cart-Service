package shop.dodream.cart.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import shop.dodream.cart.dto.BookListResponseRecord;

import java.util.List;

@FeignClient(name = "book")
public interface BookClient {
	
	@GetMapping("/public/books")
	List<BookListResponseRecord> getBooksByIds(@RequestParam("ids") List<Long> bookIds);
}
