package pizza_ordering.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pizza_ordering.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByIdDesc(Long userId);
}
