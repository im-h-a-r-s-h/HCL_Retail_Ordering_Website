package pizza_ordering.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pizza_ordering.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);
}
