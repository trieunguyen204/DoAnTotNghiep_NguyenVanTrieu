package com.adidos.product.controller;

import com.adidos.product.entity.Category;
import com.adidos.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;

    // 1. Hiển thị danh sách
    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        // Chỉ lấy danh mục gốc để làm option "Danh mục cha" trong Form
        model.addAttribute("rootCategories", categoryRepository.findByParentIsNull());
        return "admin/product/category_management";
    }

    // 2. Thêm / Sửa danh mục
    @PostMapping("/save")
    public String saveCategory(@RequestParam(required = false) Long id,
                               @RequestParam String name,
                               @RequestParam(required = false) Long parentId,
                               RedirectAttributes ra) {
        try {
            String cleanName = name.trim();

            // KIỂM TRA TRÙNG TÊN
            var existing = categoryRepository.findByName(cleanName);
            if (existing.isPresent() && (id == null || !existing.get().getId().equals(id))) {
                ra.addFlashAttribute("error", "Tên danh mục '" + cleanName + "' đã tồn tại!");
                return "redirect:/admin/categories";
            }

            Category category;
            if (id != null) {
                category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
            } else {
                category = new Category();
            }

            category.setName(cleanName);

            // Xử lý danh mục cha
            if (parentId != null && parentId > 0) {
                // Không cho phép tự chọn chính mình làm cha
                if (id != null && id.equals(parentId)) {
                    throw new RuntimeException("Danh mục không thể làm cha của chính nó!");
                }
                category.setParent(categoryRepository.findById(parentId).orElse(null));
            } else {
                category.setParent(null);
            }

            categoryRepository.save(category);
            ra.addFlashAttribute("message", "Lưu danh mục thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // 3. Xóa danh mục
    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes ra) {
        try {
            categoryRepository.deleteById(id);
            ra.addFlashAttribute("message", "Xóa danh mục thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi xóa (Có thể danh mục này đang chứa sản phẩm hoặc danh mục con): " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }
}