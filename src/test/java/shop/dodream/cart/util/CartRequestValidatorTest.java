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
		req.setUserId(null);
		req.setGuestId(null);
		
		boolean result = validator.isValid(req, null);
		assertThat(result).isFalse();
	}
	
	@ParameterizedTest
	@CsvSource({
			"user, null",       // userId만 존재
			"null, guest-abc",  // guestId만 존재
			"user, guest-abc"   // 둘 다 존재
	})
	void shouldReturnTrue_whenAtLeastOneIdIsPresent(String userId, String guestId) {
		CartRequest req = new CartRequest();
		req.setUserId("null".equals(userId) ? null : userId);
		req.setGuestId("null".equals(guestId) ? null : guestId);
		
		boolean result = validator.isValid(req, null);
		
		assertThat(result).isTrue();
	}
}
