package pizza_ordering.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

import pizza_ordering.entity.*;
import pizza_ordering.repository.*;

@Service
public class OrderService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public OrderService(CartRepository cartRepository,
                        ProductRepository productRepository,
                        OrderRepository orderRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public Order placeOrder(Long cartId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0;

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            // 🔥 Inventory check
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Out of stock");
            }

            // 🔥 Reduce stock
            product.setStockQuantity(
                    product.getStockQuantity() - cartItem.getQuantity()
            );
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());

            total += product.getPrice() * cartItem.getQuantity();
            orderItems.add(orderItem);
        }

        Order order = new Order();
        order.setUserId(cart.getUserId());
        order.setTotalAmount(total);
        order.setStatus("CONFIRMED");
        order.setItems(orderItems);

        // 🔁 Link both sides
        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }

        return orderRepository.save(order);
    }
}