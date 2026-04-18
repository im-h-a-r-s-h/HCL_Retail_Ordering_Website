package pizza_ordering.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pizza_ordering.dto.OrderItemResponse;
import pizza_ordering.dto.OrderPlaceRequest;
import pizza_ordering.dto.OrderResponse;
import pizza_ordering.entity.Cart;
import pizza_ordering.entity.CartItem;
import pizza_ordering.entity.Order;
import pizza_ordering.entity.OrderItem;
import pizza_ordering.entity.Product;
import pizza_ordering.exception.BadRequestException;
import pizza_ordering.exception.InsufficientStockException;
import pizza_ordering.exception.ResourceNotFoundException;
import pizza_ordering.repository.CartItemRepository;
import pizza_ordering.repository.CartRepository;
import pizza_ordering.repository.OrderRepository;
import pizza_ordering.repository.ProductRepository;

@Service
public class OrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PromotionService promotionService;
    private final EmailService emailService;

    public OrderService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                        ProductRepository productRepository, OrderRepository orderRepository,
                        PromotionService promotionService, EmailService emailService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.promotionService = promotionService;
        this.emailService = emailService;
    }

    @Transactional
    public OrderResponse placeOrder(OrderPlaceRequest request) {
        Cart cart = cartRepository.findById(request.cartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot place an order with an empty cart");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        double subtotal = 0;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException("Out of stock for product: " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItems.add(orderItem);
            subtotal += product.getPrice() * cartItem.getQuantity();
        }

        double discountAmount = promotionService.calculateDiscount(subtotal, request.couponCode());
        double finalTotal = Math.max(0, subtotal - discountAmount);
        int loyaltyPoints = (int) finalTotal / 10;

        Order order = new Order();
        order.setUserId(cart.getUserId());
        order.setCustomerEmail(request.customerEmail());
        order.setCouponCode(request.couponCode());
        order.setDiscountAmount(discountAmount);
        order.setLoyaltyPointsEarned(loyaltyPoints);
        order.setTotalAmount(finalTotal);
        order.setStatus("CONFIRMED");
        order.setItems(orderItems);

        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }

        Order savedOrder = orderRepository.save(order);
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);
        emailService.sendOrderConfirmation(savedOrder);
        return toResponse(savedOrder);
    }

    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return toResponse(order);
    }

    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByIdDesc(userId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public OrderResponse reorder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Cart cart = cartRepository.findByUserId(order.getUserId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(order.getUserId());
                    newCart.setItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        for (OrderItem orderItem : order.getItems()) {
            Product product = productRepository.findById(orderItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            if (product.getStockQuantity() < orderItem.getQuantity()) {
                throw new InsufficientStockException("Cannot reorder product due to stock shortage: " + product.getName());
            }

            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(orderItem.getQuantity());
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
        return placeOrder(new OrderPlaceRequest(cart.getId(), order.getCustomerEmail(), null));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getCustomerEmail(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getDiscountAmount(),
                order.getCouponCode(),
                order.getLoyaltyPointsEarned(),
                items
        );
    }
}
