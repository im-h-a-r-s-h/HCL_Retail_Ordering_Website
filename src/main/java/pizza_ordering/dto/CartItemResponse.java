package pizza_ordering.dto;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        int quantity,
        double unitPrice,
        double lineTotal
) {
}
