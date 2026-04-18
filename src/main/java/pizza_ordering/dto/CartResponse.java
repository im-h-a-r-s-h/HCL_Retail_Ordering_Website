package pizza_ordering.dto;

import java.util.List;

public record CartResponse(
        Long id,
        Long userId,
        double totalAmount,
        List<CartItemResponse> items
) {
}
