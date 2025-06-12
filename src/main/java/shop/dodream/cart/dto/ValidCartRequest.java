package shop.dodream.cart.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import shop.dodream.cart.util.CartRequestValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CartRequestValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCartRequest {
	String message() default "회원 ID 또는 세션 ID 중 하나는 반드시 있어야 합니다.";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
