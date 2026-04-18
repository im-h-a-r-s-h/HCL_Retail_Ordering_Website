package pizza_ordering.controller;

import org.springframework.web.bind.annotation.*;
import pizza_ordering.entity.Order;
import pizza_ordering.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/place")
    public Order placeOrder(@RequestParam Long cartId) {
        return orderService.placeOrder(cartId);
    }
}