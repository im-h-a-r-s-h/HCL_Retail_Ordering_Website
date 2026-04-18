package pizza_ordering.dto;

import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        String customerEmail,
        String status,
        double totalAmount,
        double discountAmount,
        String couponCode,
        int loyaltyPointsEarned,
        List<OrderItemResponse> items
) {
}
