package pizza_ordering.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pizza_ordering.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {
}