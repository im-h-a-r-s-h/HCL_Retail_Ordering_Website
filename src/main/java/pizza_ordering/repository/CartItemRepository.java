package pizza_ordering.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pizza_ordering.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByIdAndCartId(Long id, Long cartId);

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}
