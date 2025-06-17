package shop.dodream.cart.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Cart {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long cartId;
	//회원일 경우
	private String userId;
	//비회원일 경우
	private String guestId;
	
	public Cart(String userId, Object o) {
		this.userId = userId;
		this.guestId = (String) o;
	}
}
