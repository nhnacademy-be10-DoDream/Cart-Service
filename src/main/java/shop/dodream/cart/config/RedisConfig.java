package shop.dodream.cart.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
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
		// ObjectMapper를 커스터마이징하여 타입 정보를 포함하도록 설정합니다.
		// 이것이 오류 해결의 핵심입니다.
		PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
				                               .allowIfBaseType(Object.class)
				                               .build();
		
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
		
		// 커스터마이징된 ObjectMapper를 사용하는 새로운 Serializer 생성
		GenericJackson2JsonRedisSerializer redisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
		
		// 1. 기본 캐시 설정
		RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
				                                        .entryTtl(Duration.ofMinutes(10))
				                                        .disableCachingNullValues()
				                                        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
				                                        // 수정한 Serializer를 값(value) 직렬화에 사용합니다.
				                                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer));
		
		// 2. 특정 캐시 그룹을 위한 설정
		Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
				"cart", defaultConfig.entryTtl(Duration.ofMinutes(30))
		);
		
		return RedisCacheManager.builder(connectionFactory)
				       .cacheDefaults(defaultConfig)
				       .withInitialCacheConfigurations(cacheConfigurations)
				       .build();
	}
}
