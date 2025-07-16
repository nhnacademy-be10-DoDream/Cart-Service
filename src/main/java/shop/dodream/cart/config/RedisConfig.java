package shop.dodream.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import shop.dodream.cart.dto.GuestCart;

import java.time.Duration;
import java.util.Map;

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
	
	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
		// 1. 기본 캐시 설정: 10분 TTL, Null 값 캐싱 안함
		RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
				                                        .entryTtl(Duration.ofMinutes(10)) // 기본 TTL 10분
				                                        .disableCachingNullValues() // Null 값은 캐시하지 않음
				                                        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
				                                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
		
		// 2. 특정 캐시 그룹을 위한 설정: 'cart' 캐시는 30분 TTL
		Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
				"cart", defaultConfig.entryTtl(Duration.ofMinutes(30)) // 'cart' 캐시는 30분
		);
		
		return RedisCacheManager.builder(connectionFactory)
				       .cacheDefaults(defaultConfig) // 3. 기본 설정을 적용
				       .withInitialCacheConfigurations(cacheConfigurations) // 4. 특정 캐시 설정을 적용
				       .build();
	}
}
