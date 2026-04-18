package pizza_ordering.service;

import org.springframework.stereotype.Service;

@Service
public class PromotionService {

    public double calculateDiscount(double subtotal, String couponCode) {
        if (couponCode == null || couponCode.isBlank()) {
            return 0;
        }

        return switch (couponCode.trim().toUpperCase()) {
            case "SAVE10" -> subtotal * 0.10;
            case "PIZZA20" -> subtotal * 0.20;
            case "WELCOME5" -> subtotal * 0.05;
            default -> 0;
        };
    }
}
