package pizza_ordering.service;

import java.util.List;
import org.springframework.stereotype.Service;
import pizza_ordering.dto.ProductRequest;
import pizza_ordering.dto.ProductResponse;
import pizza_ordering.entity.Product;
import pizza_ordering.entity.ProductCategory;
import pizza_ordering.exception.ResourceNotFoundException;
import pizza_ordering.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse saveProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        product.setCategory(request.category());
        return toResponse(productRepository.save(product));
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<ProductResponse> getProductsByCategory(ProductCategory category) {
        return productRepository.findByCategory(category).stream().map(this::toResponse).toList();
    }

    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return toResponse(product);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCategory().name()
        );
    }
}
