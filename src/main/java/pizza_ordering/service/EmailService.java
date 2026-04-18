package pizza_ordering.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pizza_ordering.entity.Order;

@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    public void sendOrderConfirmation(Order order) {
        if (order.getCustomerEmail() == null || order.getCustomerEmail().isBlank()) {
            return;
        }

        LOGGER.info("Order confirmation sent to {} for order {} with total {}", order.getCustomerEmail(),
                order.getId(), order.getTotalAmount());
    }
}
