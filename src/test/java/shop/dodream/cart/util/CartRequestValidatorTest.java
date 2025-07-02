package shop.dodream.cart.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
		req.setGuestId(null);
		
		boolean result = validator.isValid(req, null);
		assertThat(result).isFalse();
	}
	
	@ParameterizedTest
	@CsvSource({
			"guest-abc",  // guestId만 존재
	})
	void shouldReturnTrue_whenGuestIdIsPresent(String guestId) {
		CartRequest req = new CartRequest();
		req.setGuestId(guestId);
		
		boolean result = validator.isValid(req, null);
		
		assertThat(result).isTrue();
	}
	
	@ParameterizedTest
	@CsvSource({
			"''",     // 빈 문자열
			"'  '",   // 공백 문자열
			"null"    // 문자열 "null"
	})
	void shouldReturnFalse_whenGuestIdIsInvalid(String guestId) {
		CartRequest req = new CartRequest();
		req.setGuestId("null".equals(guestId) ? null : guestId);
		
		boolean result = validator.isValid(req, null);
		
		assertThat(result).isFalse();
	}
}
