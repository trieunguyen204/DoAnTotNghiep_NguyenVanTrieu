package com.adidos.product.controller;

import com.adidos.product.dto.ProductRequest;
import com.adidos.product.dto.ProductResponse;
import com.adidos.product.mapper.ProductMapper;
import com.adidos.product.repository.ProductRepository;
import com.adidos.product.service.CategoryService;
import com.adidos.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ProductRepository productRepository;

    @GetMapping
    public String productManagement(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Model model
    ) {
        Page<ProductResponse> productPage = productService.getProductsPage(page, size);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("productPage", productPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("size", size);


        model.addAttribute("categories", categoryService.getSubCategoriesForForm());

        return "admin/product/product_management";
    }


    @PostMapping("/save")
    public String saveProduct(@ModelAttribute ProductRequest request, RedirectAttributes ra) {
        try {
            String cleanName = request.getName().trim();

            // KIỂM TRA TRÙNG TÊN SẢN PHẨM
            var existing = productRepository.findByName(cleanName);
            if (existing.isPresent() && (request.getId() == null || !existing.get().getId().equals(request.getId()))) {
                ra.addFlashAttribute("error", "Tên sản phẩm '" + cleanName + "' đã tồn tại!");
                return "redirect:/admin/products";
            }

            request.setName(cleanName);
            productService.saveProduct(request);
            ra.addFlashAttribute("message", "Lưu sản phẩm thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productService.deleteProduct(id);
            ra.addFlashAttribute("message", "Xóa sản phẩm thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }



}