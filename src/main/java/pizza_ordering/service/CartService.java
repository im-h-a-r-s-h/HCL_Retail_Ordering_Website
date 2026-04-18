package pizza_ordering.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pizza_ordering.dto.CartAddRequest;
import pizza_ordering.dto.CartItemResponse;
import pizza_ordering.dto.CartItemUpdateRequest;
import pizza_ordering.dto.CartResponse;
import pizza_ordering.entity.Cart;
import pizza_ordering.entity.CartItem;
import pizza_ordering.entity.Product;
import pizza_ordering.exception.BadRequestException;
import pizza_ordering.exception.InsufficientStockException;
import pizza_ordering.exception.ResourceNotFoundException;
import pizza_ordering.repository.CartItemRepository;
import pizza_ordering.repository.CartRepository;
import pizza_ordering.repository.ProductRepository;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public CartService(CartRepository cartRepository,
                       ProductRepository productRepository,
                       CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional
    public CartResponse addToCart(CartAddRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Cart cart = cartRepository.findByUserId(request.userId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(request.userId());
                    newCart.setItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProduct(product);
                    newItem.setQuantity(0);
                    return newItem;
                });

        int updatedQuantity = item.getQuantity() + request.quantity();
        validateStock(product, updatedQuantity);

        boolean isNewItem = item.getId() == null;
        item.setQuantity(updatedQuantity);
        cartItemRepository.save(item);

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }
        if (isNewItem) {
            cart.getItems().add(item);
        }

        return toResponse(cartRepository.save(cart));
    }

    public CartResponse getCart(Long cartId) {
        return toResponse(getCartEntity(cartId));
    }

    public CartResponse getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));
        return toResponse(cart);
    }

    @Transactional
    public CartResponse updateCartItemQuantity(Long cartId, Long itemId, CartItemUpdateRequest request) {
        Cart cart = getCartEntity(cartId);
        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        validateStock(item.getProduct(), request.quantity());
        item.setQuantity(request.quantity());
        cartItemRepository.save(item);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeCartItem(Long cartId, Long itemId) {
        Cart cart = getCartEntity(cartId);
        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (cart.getItems() != null) {
            cart.getItems().removeIf(existing -> existing.getId().equals(itemId));
        }
        cartItemRepository.delete(item);
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse clearCart(Long cartId) {
        Cart cart = getCartEntity(cartId);
        if (cart.getItems() != null) {
            cart.getItems().clear();
        }
        return toResponse(cartRepository.save(cart));
    }

    public Cart getCartEntity(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    private void validateStock(Product product, int quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }
        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException("Requested quantity exceeds available stock for " + product.getName());
        }
    }

    public CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems() == null
                ? List.of()
                : cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getProduct().getPrice(),
                        item.getQuantity() * item.getProduct().getPrice()
                ))
                .toList();

        double total = items.stream()
                .mapToDouble(CartItemResponse::lineTotal)
                .sum();

        return new CartResponse(cart.getId(), cart.getUserId(), total, items);
    }
}
