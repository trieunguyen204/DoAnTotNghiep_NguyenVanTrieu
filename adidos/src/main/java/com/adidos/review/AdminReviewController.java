package com.adidos.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("reviews", reviewService.getAllRootReviews());
        return "admin/review/review_management";
    }

    @PostMapping("/{id}/reply")
    public String reply(@PathVariable Long id,
                        @RequestParam String content,
                        Principal principal,
                        RedirectAttributes redirectAttributes) {
        try {
            reviewService.adminReplyReview(id, principal.getName(), content);
            redirectAttributes.addFlashAttribute("success", "Đã phản hồi đánh giá.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/reviews";
    }
}