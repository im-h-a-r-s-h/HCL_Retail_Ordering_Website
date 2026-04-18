package pizza_ordering.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pizza_ordering.entity.ProductCategory;

public record ProductRequest(
        @NotBlank(message = "Product name is required")
        String name,

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        Double price,

        @Min(value = 0, message = "Stock quantity cannot be negative")
        int stockQuantity,

        @NotNull(message = "Category is required")
        ProductCategory category
) {
}
