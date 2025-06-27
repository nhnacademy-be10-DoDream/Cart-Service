package shop.dodream.cart.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import shop.dodream.cart.filter.GuestIdFilter;
import shop.dodream.cart.util.GuestIdUtil;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class WebConfigTest {
	
	@Mock
	GuestIdUtil guestIdUtil;
	
	WebConfig webConfig;
	
	@BeforeEach
	void setUp() {
		webConfig = new WebConfig(guestIdUtil);
	}
	
	@Test
	void guestIdFilter_should_register_with_correct_properties() {
		FilterRegistrationBean<GuestIdFilter> registrationBean = webConfig.guestIdFilter();
		
		// Bean이 null이 아니어야 함
		assertThat(registrationBean).isNotNull();
		
		// 필터 객체 타입 확인
		assertThat(registrationBean.getFilter()).isInstanceOf(GuestIdFilter.class);
		
		// URL 패턴 확인
		assertThat(registrationBean.getUrlPatterns())
				.containsExactly("/carts/guest", "/carts/guest/*");
		
		// 순서 확인
		assertThat(registrationBean.getOrder()).isEqualTo(1);
	}
}
