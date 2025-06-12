package shop.dodream.cart.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import shop.dodream.cart.exception.DataNotFoundException;
import shop.dodream.cart.exception.DuplicationException;
import shop.dodream.cart.exception.MissingIdentifierException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(DataNotFoundException.class)
	public ResponseEntity<String> handleNotFound(DataNotFoundException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(MissingIdentifierException.class)
	public ResponseEntity<String> handleMissingIdentifier(MissingIdentifierException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(DuplicationException.class)
	public ResponseEntity<String> handleDuplication(DuplicationException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
	}
}
