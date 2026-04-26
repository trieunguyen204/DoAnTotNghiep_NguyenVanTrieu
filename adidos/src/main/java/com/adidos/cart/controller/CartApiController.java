package com.adidos.cart.controller;

import com.adidos.cart.dto.CartItemRequest;
import com.adidos.cart.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;


    // Xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<?> removeCartItem(@PathVariable Long id) {
        try {
            cartService.removeCartItem(id);
            return ResponseEntity.ok("Đã xóa sản phẩm khỏi giỏ hàng");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/count")
    public ResponseEntity<Integer> getCartCount(Principal principal, HttpSession session) {

        String identifier = (principal != null) ? principal.getName() : session.getId();
        boolean isLogged = (principal != null);
        return ResponseEntity.ok(cartService.getCartItemCount(identifier, isLogged));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartItemRequest request, Principal principal, HttpSession session) {
        try {
            session.setAttribute("GUEST_CART", true);
            String identifier = (principal != null) ? principal.getName() : session.getId();
            boolean isLogged = (principal != null);
            cartService.addToCart(identifier, isLogged, request);
            return ResponseEntity.ok("Đã thêm vào giỏ hàng thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateQuantity(@PathVariable Long id, @RequestParam Integer quantity) {
        try {
            cartService.updateQuantity(id, quantity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}