package shop.dodream.cart.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shop.dodream.cart.dto.CartRequest;

import static org.assertj.core.api.Assertions.assertThat;

class CartRequestValidatorTest {
	
	private CartRequestValidator validator;
	
	@BeforeEach
	void setUp() {
		validator = new CartRequestValidator();
	}
	
	@Test
	void shouldReturnFalse_whenRequestIsNull() {
		boolean result = validator.isValid(null, null);
		assertThat(result).isFalse();
	}
	
	@Test
	void shouldReturnFalse_whenBothUserIdAndGuestIdAreMissing() {
		CartRequest req = new CartRequest();
		req.setUserId(null);
		req.setGuestId(null);
		
		boolean result = validator.isValid(req, null);
		assertThat(result).isFalse();
	}
	
	@Test
	void shouldReturnTrue_whenUserIdIsPresent() {
		CartRequest req = new CartRequest();
		req.setUserId("user");  // userId만 있음
		req.setGuestId(null);
		
		boolean result = validator.isValid(req, null);
		assertThat(result).isTrue();
	}
	
	@Test
	void shouldReturnTrue_whenGuestIdIsPresent() {
		CartRequest req = new CartRequest();
		req.setUserId(null);
		req.setGuestId("guest-abc");
		
		boolean result = validator.isValid(req, null);
		assertThat(result).isTrue();
	}
	
	@Test
	void shouldReturnTrue_whenBothArePresent() {
		CartRequest req = new CartRequest();
		req.setUserId("user");
		req.setGuestId("guest-abc");
		
		boolean result = validator.isValid(req, null);
		assertThat(result).isTrue();
	}
}
