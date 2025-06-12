package shop.dodream.cart.util;

import shop.dodream.cart.dto.BookDto;

public class BookAvailabilityChecker {
	public static boolean isAvailable(BookDto bookDto, long requestedQuantity) {
		return bookDto.getStatus() == BookDto.BookStatus.SELL
				       && bookDto.getStockQuantity() != null
				       && bookDto.getStockQuantity() >= requestedQuantity;
	}
}
