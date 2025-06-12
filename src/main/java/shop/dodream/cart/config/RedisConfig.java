package shop.dodream.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import shop.dodream.cart.dto.GuestCart;

@Configuration
public class RedisConfig {
	@Bean
	public RedisTemplate<String, GuestCart> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, GuestCart> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new Jackson2JsonRedisSerializer<>(GuestCart.class));
		return template;
	}
}
