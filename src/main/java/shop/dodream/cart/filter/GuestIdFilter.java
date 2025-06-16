package shop.dodream.cart.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import shop.dodream.cart.util.GuestIdUtil;

import java.io.IOException;


@RequiredArgsConstructor
public class GuestIdFilter implements Filter {
	
	private final GuestIdUtil guestIdUtil;
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		
		// guestId 쿠키 없으면 생성해서 응답에 추가
		guestIdUtil.getOrCreateGuestId(req, res);
		
		// 다음 필터 / 서블릿 호출
		chain.doFilter(request, response);
	}
}
