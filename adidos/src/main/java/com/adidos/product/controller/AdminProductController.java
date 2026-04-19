package com.adidos.product.controller;

import com.adidos.product.dto.ProductRequest;
import com.adidos.product.dto.ProductResponse;
import com.adidos.product.mapper.ProductMapper;
import com.adidos.product.repository.ProductRepository;
import com.adidos.product.service.CategoryService;
import com.adidos.product.service.ProductService;
import lombok.RequiredArgsConstructor;
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
    public String listProducts(@RequestParam(required = false) String keyword, Model model) {
        List<ProductResponse> products;
        if (keyword != null && !keyword.isEmpty()) {

            products = productRepository.searchProducts(keyword).stream()
                    .map(ProductMapper::toProductResponse).collect(Collectors.toList());
            model.addAttribute("keyword", keyword);
        } else {
            products = productService.getAllActiveProducts();
        }
        model.addAttribute("products", products);

        model.addAttribute("categories", categoryService.getSubCategoriesForForm());
        return "admin/product/product_management";
    }





    @PostMapping("/save")
    public String saveProduct(@ModelAttribute ProductRequest request, RedirectAttributes ra) {
        try {
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