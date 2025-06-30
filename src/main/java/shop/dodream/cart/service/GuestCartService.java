package shop.dodream.cart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class GuestCartService {
	
	private static final String REDIS_KEY_PREFIX = "guest_cart:";
	private static final Duration CART_EXPIRATION = Duration.ofDays(7);
	private static final int MAX_ITEM_COUNT = 20;
	private static final int MAX_RETRY = 3;
	private static final long RETRY_DELAY_MS = 500;
	
	private final RedisTemplate<String, GuestCart> redisTemplate;
	private final BookClient bookClient;
	
	public GuestCartResponse getCart(String guestId) {
		GuestCart cart = fetchCart(guestId);
		return buildGuestCartResponse(cart);
	}
	
	public GuestCartResponse addCartItem(String guestId, GuestCartItemRequest request) {
		validateRequest(request);
		GuestCart cart = fetchCart(guestId);
		
		Optional<GuestCartItem> existing = cart.getItems().stream()
				                                   .filter(i -> i.getBookId().equals(request.getBookId()))
				                                   .findFirst();
		
		if (existing.isPresent()) {
			long newQuantity = existing.get().getQuantity() + request.getQuantity();
			if (newQuantity > MAX_ITEM_COUNT) {
				newQuantity = MAX_ITEM_COUNT; // 20개 제한 예시
			}
			existing.get().setQuantity(newQuantity);
		} else {
			if (cart.getItems().size() >= MAX_ITEM_COUNT) {
				throw new IllegalStateException("장바구니는 최대 " + MAX_ITEM_COUNT + "개까지만 담을 수 있습니다.");
			}
			long quantityToAdd = request.getQuantity();
			if (quantityToAdd > MAX_ITEM_COUNT) {
				quantityToAdd = MAX_ITEM_COUNT; // 신규 아이템도 제한
			}
			cart.getItems().add(new GuestCartItem(request.getBookId(), quantityToAdd));
		}
		
		saveCart(guestId, cart);
		return buildGuestCartResponse(cart);
	}
	
	public void removeItem(String guestId, Long bookId) {
		GuestCart cart = fetchCart(guestId);
		cart.setItems(cart.getItems().stream()
				              .filter(item -> !item.getBookId().equals(bookId))
				              .collect(Collectors.toList()));
		saveCart(guestId, cart);
	}
	
	public void deleteCart(String guestId) {
		redisTemplate.delete(buildKey(guestId));
	}
	
	public GuestCart getRawCart(String guestId) {
		return redisTemplate.opsForValue().get(buildKey(guestId));
	}
	
	private GuestCart fetchCart(String guestId) {
		GuestCart cart = redisTemplate.opsForValue().get(buildKey(guestId));
		return cart != null ? cart : new GuestCart(guestId, new ArrayList<>());
	}
	
	private void saveCart(String guestId, GuestCart cart) {
		redisTemplate.opsForValue().set(buildKey(guestId), cart, CART_EXPIRATION);
	}
	
	private String buildKey(String guestId) {
		return REDIS_KEY_PREFIX + guestId;
	}
	
	private GuestCartResponse buildGuestCartResponse(GuestCart cart) {
		List<GuestCartItemResponse> itemResponses = cart.getItems().stream()
				                                            .map(item -> {
					                                            BookDto book = safeGetBook(item.getBookId());
					                                            return GuestCartItemResponse.of(item, book);
				                                            })
				                                            .toList();
		return new GuestCartResponse(cart.getGuestId(), itemResponses);
	}
	
	private BookDto safeGetBook(Long bookId) {
		try {
			return bookClient.getBookById(bookId);
		} catch (Exception e) {
			// todo 로그 구현 필요
			return null;
		}
	}
	
	private void validateRequest(GuestCartItemRequest request) {
		if (request.getQuantity() <= 0) {
			throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
		}
		if (request.getBookId() == null) {
			throw new IllegalArgumentException("bookId는 null이 될 수 없습니다.");
		}
	}
	
	public void deleteGuestCartWithRetry(String guestId) {
		String key = REDIS_KEY_PREFIX + guestId;
		int attempt = 0;
		boolean deleted = false;
		
		while (attempt < MAX_RETRY && !deleted) {
			try {
				deleted = Boolean.TRUE.equals(redisTemplate.delete(key));
				if (!deleted) {
					log.warn("Redis guest cart [{}] delete failed (attempt {}/{})", key, attempt + 1, MAX_RETRY);
					Thread.sleep(RETRY_DELAY_MS);
				}
			} catch (Exception e) {
				log.error("Redis guest cart [{}] delete exception (attempt {}/{}): {}", key, attempt + 1, MAX_RETRY, e.getMessage());
				try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
			}
			attempt++;
		}
		
		if (!deleted) {
			log.error("Redis guest cart [{}] delete failed after {} attempts", key, MAX_RETRY);
		}
	}
}
