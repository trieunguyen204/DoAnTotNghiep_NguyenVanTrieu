package com.adidos.promotion;

import com.adidos.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/promotions")
@RequiredArgsConstructor
public class AdminPromotionController {

    private final PromotionService promotionService;
    private final CategoryService categoryService;

    // View Giao diện
    @GetMapping
    public String index(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/promotion/promotion_management";
    }

    // API Lấy danh sách
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<?> getAllPromotions() {
        return ResponseEntity.ok(promotionService.findAll());
    }

    // API Thêm/Sửa
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> savePromotion(@RequestBody Promotion promotion) {
        return ResponseEntity.ok(promotionService.save(promotion));
    }

    // API Xóa
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deletePromotion(@PathVariable Long id) {
        promotionService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}