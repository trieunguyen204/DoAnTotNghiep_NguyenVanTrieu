package com.adidos.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/add")
    public String addReview(@ModelAttribute ReviewRequest request,
                            @RequestParam Long orderId,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        try {
            reviewService.addReview(principal.getName(), request);
            redirectAttributes.addFlashAttribute("success", "Đánh giá sản phẩm thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile/orders/" + orderId;
    }
}