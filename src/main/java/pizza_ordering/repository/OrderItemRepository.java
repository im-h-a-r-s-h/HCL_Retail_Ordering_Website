package pizza_ordering.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pizza_ordering.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}