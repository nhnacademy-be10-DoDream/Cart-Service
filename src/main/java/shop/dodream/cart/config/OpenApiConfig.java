package shop.dodream.cart.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				       .info(new Info().title("장바구니 서비스 API")
						             .version("v1.0")
						             .description("장바구니 생성, 수정, 상세 조회 등을 제공하는 API 문서입니다."));
	}
}
