package shop.dodream.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import shop.dodream.cart.client.BookClient;
import shop.dodream.cart.dto.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuestCartService {
	
	private final RedisTemplate<String, GuestCart> redisTemplate;
	private final BookClient bookClient;
	
	private static final String REDIS_KEY_PREFIX = "guest_cart:";
	
	public GuestCartResponse getCart(String guestId) {
		GuestCart cart = redisTemplate.opsForValue().get(buildKey(guestId));
		if (cart == null) {
			return new GuestCartResponse(guestId, new ArrayList<>());
		}
		
		List<GuestCartItemResponse> itemResponses = cart.getItems().stream()
				                                            .map(item -> {
					                                            BookDto book = bookClient.getBookById(item.getBookId());
					                                            return GuestCartItemResponse.of(item, book);
				                                            })
				                                            .collect(Collectors.toList());
		
		return new GuestCartResponse(guestId, itemResponses);
	}
	
	public GuestCartResponse addCartItem(String guestId, GuestCartItemRequest request) {
		String key = buildKey(guestId);
		GuestCart cart = redisTemplate.opsForValue().get(key);
		if (cart == null) {
			cart = new GuestCart(guestId, new ArrayList<>());
		}
		
		Optional<GuestCartItem> existing = cart.getItems().stream()
				                                   .filter(i -> i.getBookId().equals(request.getBookId()))
				                                   .findFirst();
		
		if (existing.isPresent()) {
			existing.get().setQuantity(existing.get().getQuantity() + request.getQuantity());
		} else {
			cart.getItems().add(new GuestCartItem(request.getBookId(), request.getQuantity()));
		}
		
		redisTemplate.opsForValue().set(key, cart, Duration.ofDays(7));
		
		return getCart(guestId);
	}
	
	public void deleteCart(String guestId) {
		redisTemplate.delete(buildKey(guestId));
	}
	
	public GuestCart getRawCart(String guestId) {
		return redisTemplate.opsForValue().get(buildKey(guestId));
	}
	
	private String buildKey(String guestId) {
		return REDIS_KEY_PREFIX + guestId;
	}
}
