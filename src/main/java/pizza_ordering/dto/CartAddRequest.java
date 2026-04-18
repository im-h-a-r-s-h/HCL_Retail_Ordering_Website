package pizza_ordering.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartAddRequest(
        @NotNull(message = "User id is required")
        Long userId,

        @NotNull(message = "Product id is required")
        Long productId,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {
}
