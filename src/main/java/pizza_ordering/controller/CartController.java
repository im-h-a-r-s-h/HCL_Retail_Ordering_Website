package pizza_ordering.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pizza_ordering.dto.CartAddRequest;
import pizza_ordering.dto.CartItemUpdateRequest;
import pizza_ordering.dto.CartResponse;
import pizza_ordering.service.CartService;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addToCart(@Valid @RequestBody CartAddRequest request) {
        return cartService.addToCart(request);
    }

    @GetMapping("/{cartId}")
    public CartResponse getCart(@PathVariable Long cartId) {
        return cartService.getCart(cartId);
    }

    @GetMapping("/user/{userId}")
    public CartResponse getCartByUser(@PathVariable Long userId) {
        return cartService.getCartByUserId(userId);
    }

    @PatchMapping("/{cartId}/items/{itemId}")
    public CartResponse updateCartItemQuantity(@PathVariable Long cartId, @PathVariable Long itemId,
                                               @Valid @RequestBody CartItemUpdateRequest request) {
        return cartService.updateCartItemQuantity(cartId, itemId, request);
    }

    @DeleteMapping("/{cartId}/items/{itemId}")
    public CartResponse removeCartItem(@PathVariable Long cartId, @PathVariable Long itemId) {
        return cartService.removeCartItem(cartId, itemId);
    }

    @DeleteMapping("/{cartId}/items")
    public CartResponse clearCart(@PathVariable Long cartId) {
        return cartService.clearCart(cartId);
    }
}
