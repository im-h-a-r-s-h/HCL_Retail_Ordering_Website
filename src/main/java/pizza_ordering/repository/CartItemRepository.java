package pizza_ordering.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pizza_ordering.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}