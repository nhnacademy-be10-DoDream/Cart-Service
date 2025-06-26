package shop.dodream.cart.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GuestIdUtilTest {
	
	GuestIdUtil guestIdUtil = new GuestIdUtil();
	
	@Test
	void shouldReturnExistingGuestId_whenCookieExists() {
		// given
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		Cookie guestCookie = new Cookie("guestId", "existing-id");
		
		// WebUtils.getCookie(...)은 static이라 실제 쿠키 배열 세팅이 필요함
		when(request.getCookies()).thenReturn(new Cookie[]{guestCookie});
		
		// when
		String result = guestIdUtil.getOrCreateGuestId(request, response);
		
		// then
		assertThat(result).isEqualTo("existing-id");
		verify(response, never()).addCookie(any()); // 쿠키 추가 X
	}
	
	@Test
	void shouldCreateNewGuestId_whenCookieDoesNotExist() {
		// given
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		
		// 쿠키 없음
		when(request.getCookies()).thenReturn(null);
		
		// when
		String result = guestIdUtil.getOrCreateGuestId(request, response);
		
		// then
		assertThat(result).isNotNull();
		assertThat(result).matches("^[a-f0-9\\-]{36}$"); // UUID 형식
		
		// 쿠키 추가 확인
		verify(response, times(1)).addCookie(argThat(cookie ->
				                                             cookie.getName().equals("guestId") &&
						                                             cookie.getValue().equals(result) &&
						                                             cookie.getMaxAge() == 60 * 60 * 24 * 7 &&
						                                             cookie.getPath().equals("/")
		));
	}
}
