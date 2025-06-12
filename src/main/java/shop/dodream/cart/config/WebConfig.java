package shop.dodream.cart.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import shop.dodream.cart.filter.GuestIdFilter;
import shop.dodream.cart.util.GuestIdUtil;

@Configuration
public class WebConfig {
	
	private final GuestIdUtil guestIdUtil;
	
	public WebConfig(GuestIdUtil guestIdUtil) {
		this.guestIdUtil = guestIdUtil;
	}
	
	@Bean
	public FilterRegistrationBean<GuestIdFilter> guestIdFilter() {
		FilterRegistrationBean<GuestIdFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new GuestIdFilter(guestIdUtil));
		registrationBean.addUrlPatterns("/carts/guest", "/carts/guest/*");
		registrationBean.setOrder(1);
		return registrationBean;
	}
}