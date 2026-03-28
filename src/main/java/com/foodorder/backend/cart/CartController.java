package com.foodorder.backend.cart;

import com.foodorder.backend.cart.dto.request.CartRequest;
import com.foodorder.backend.cart.dto.response.CartResponse;
import com.foodorder.backend.security.CustomUserDetails;
import com.foodorder.backend.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý giỏ hàng của người dùng
 */
@RestController
@RequestMapping("/api/v1/client/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "APIs for cart management - Requires authentication")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Add item to cart", description = "Add a food item to the authenticated user's cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Added successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Food item not found")
    })
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        cartService.addToCart(user.getId(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update quantity", description = "Update the quantity of an item in the cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/update")
    public ResponseEntity<?> updateQuantity(@RequestBody CartRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        cartService.updateQuantity(user.getId(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Remove item from cart", description = "Remove a food item from the cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Removed successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFromCart(
            @Parameter(description = "Food item ID") @RequestParam Long foodId,
            @Parameter(description = "Variant ID (optional)") @RequestParam(required = false) Long variantId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        cartService.removeFromCart(user.getId(), foodId, variantId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get cart", description = "Retrieve all items in the user's cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping
    public ResponseEntity<List<CartResponse>> getUserCart(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(cartService.getUserCart(user.getId()));
    }

    @Operation(summary = "Clear cart", description = "Remove all items from the cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cleared successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        cartService.clearCart(user.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Sync cart", description = "Sync cart items from client to server (used on login).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Synced successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/sync")
    public ResponseEntity<?> syncCart(@RequestBody List<CartRequest> cartItems,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        cartService.syncCart(user.getId(), cartItems);
        return ResponseEntity.ok().build();
    }

}
