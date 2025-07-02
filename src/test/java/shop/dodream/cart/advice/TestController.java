package shop.dodream.cart.advice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.dodream.cart.exception.DataNotFoundException;
import shop.dodream.cart.exception.DuplicationException;
import shop.dodream.cart.exception.MissingIdentifierException;

@RestController
@RequestMapping("/test")
public class TestController {
	
	@GetMapping("/not-found")
	public void throwNotFound() {
		throw new DataNotFoundException("Data not found");
	}
	
	@GetMapping("/missing-id")
	public void throwMissingId() {
		throw new MissingIdentifierException("Missing ID");
	}
	
	@GetMapping("/duplicate")
	public void throwDuplicate() {
		throw new DuplicationException("Duplicate found");
	}
}

