package pizza_ordering.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pizza_ordering.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}