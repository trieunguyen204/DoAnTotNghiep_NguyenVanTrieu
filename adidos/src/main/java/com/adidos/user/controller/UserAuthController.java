package com.adidos.user.controller;

import com.adidos.user.dto.UserRequest;
import com.adidos.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class UserAuthController {

    private final UserService userService;

    // --- XỬ LÝ AUTH ---
    @GetMapping("/login")
    public String showLoginForm(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object lastEmail = session.getAttribute("SPRING_SECURITY_LAST_USERNAME");
            model.addAttribute("lastEmail", lastEmail);
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userRequest", new UserRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("userRequest") UserRequest request,
                                  BindingResult bindingResult,
                                  Model model) {
        // Check trùng email
        if (userService.existsByEmail(request.getEmail())) {
            bindingResult.rejectValue("email", "error.email", "Email này đã được sử dụng!");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.create(request);
            return "redirect:/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", "Đăng ký thất bại: " + e.getMessage());
            return "auth/register";
        }
    }

}