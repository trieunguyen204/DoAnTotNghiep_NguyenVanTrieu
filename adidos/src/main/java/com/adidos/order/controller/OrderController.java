package com.adidos.order.controller;

import com.adidos.cart.dto.CartItemResponse;
import com.adidos.cart.service.CartService;
import com.adidos.order.dto.CheckoutRequest;
import com.adidos.order.dto.OrderResponse;
import com.adidos.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {


}