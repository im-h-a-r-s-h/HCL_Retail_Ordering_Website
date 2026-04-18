package pizza_ordering.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record OrderPlaceRequest(
        @NotNull(message = "Cart id is required")
        Long cartId,

        @Email(message = "Email should be valid")
        String customerEmail,

        String couponCode
) {
}
