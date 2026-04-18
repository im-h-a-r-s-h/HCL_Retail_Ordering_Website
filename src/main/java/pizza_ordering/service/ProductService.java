package pizza_ordering.service;

import org.springframework.stereotype.Service;
import java.util.List;
import pizza_ordering.entity.Product;
import pizza_ordering.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}