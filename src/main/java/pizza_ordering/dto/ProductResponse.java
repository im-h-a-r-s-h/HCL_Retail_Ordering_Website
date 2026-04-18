package pizza_ordering.dto;

public record ProductResponse(
        Long id,
        String name,
        String description,
        double price,
        int stockQuantity,
        String category
) {
}
