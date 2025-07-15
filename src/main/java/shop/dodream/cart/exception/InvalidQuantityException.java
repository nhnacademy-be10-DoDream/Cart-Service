package shop.dodream.cart.exception;

public class InvalidQuantityException extends RuntimeException {
	public InvalidQuantityException(String message) {
		super(message);
	}
}
