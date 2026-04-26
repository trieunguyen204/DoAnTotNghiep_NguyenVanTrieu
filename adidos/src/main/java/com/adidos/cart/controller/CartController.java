package com.adidos.cart.controller;

import com.adidos.cart.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public String viewCart(Model model, Principal principal, HttpSession session) {
        String identifier = (principal != null) ? principal.getName() : session.getId();
        boolean isLogged = (principal != null);


        model.addAttribute("cartItems", cartService.getCartByUser(identifier, isLogged));
        return "cart/cart";
    }
}