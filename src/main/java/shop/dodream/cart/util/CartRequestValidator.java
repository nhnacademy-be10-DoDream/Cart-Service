package shop.dodream.cart.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import shop.dodream.cart.dto.CartRequest;
import shop.dodream.cart.dto.ValidCartRequest;

@Component
public class CartRequestValidator implements ConstraintValidator<ValidCartRequest, CartRequest> {
	
	@Override
	public boolean isValid(CartRequest value, ConstraintValidatorContext context) {
		if (value == null) return false;
		
		boolean hasUserId = value.getUserId() != null;
		boolean hasGuestId = StringUtils.hasText(value.getGuestId());
		
		return hasUserId || hasGuestId;
	}
}
