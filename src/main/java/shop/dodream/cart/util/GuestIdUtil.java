package shop.dodream.cart.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.util.UUID;

@Component
public class GuestIdUtil {
	
	public String getOrCreateGuestId(HttpServletRequest request, HttpServletResponse response) {
		Cookie cookie = WebUtils.getCookie(request, "guestId");
		if (cookie != null) return cookie.getValue();
		
		String guestId = UUID.randomUUID().toString();
		Cookie newCookie = new Cookie("guestId", guestId);
		newCookie.setPath("/");
		newCookie.setMaxAge(60 * 60 * 24 * 7); // 7일
		newCookie.setHttpOnly(true);
		newCookie.setSecure(true);
		response.addCookie(newCookie);
		return guestId;
	}
}
