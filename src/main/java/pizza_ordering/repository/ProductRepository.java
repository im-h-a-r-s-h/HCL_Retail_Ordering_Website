package pizza_ordering.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pizza_ordering.entity.Product;
import pizza_ordering.entity.ProductCategory;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(ProductCategory category);
}
