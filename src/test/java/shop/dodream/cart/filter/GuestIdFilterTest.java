package shop.dodream.cart.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shop.dodream.cart.util.GuestIdUtil;

import java.io.IOException;

import static org.mockito.Mockito.*;

class GuestIdFilterTest {
	
	private GuestIdUtil guestIdUtil;
	private GuestIdFilter guestIdFilter;
	
	@BeforeEach
	void setUp() {
		guestIdUtil = mock(GuestIdUtil.class);
		guestIdFilter = new GuestIdFilter(guestIdUtil);
	}
	
	@Test
	void doFilter_should_call_getOrCreateGuestId_and_chainDoFilter() throws ServletException, IOException {
		// given
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);
		
		// when
		guestIdFilter.doFilter(request, response, chain);
		
		// then
		verify(guestIdUtil, times(1)).getOrCreateGuestId(request, response);
		verify(chain, times(1)).doFilter(request, response);
	}
}
