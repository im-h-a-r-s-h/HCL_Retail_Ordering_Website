package pizza_ordering.dto;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        int quantity,
        double price
) {
}
