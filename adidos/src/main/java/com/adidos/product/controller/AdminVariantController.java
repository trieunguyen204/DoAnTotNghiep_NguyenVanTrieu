package com.adidos.product.controller;

import com.adidos.product.dto.ProductResponse;
import com.adidos.product.dto.VariantDetailResponse;
import com.adidos.product.dto.VariantRequest;
import com.adidos.product.repository.ColorRepository;
import com.adidos.product.repository.SizeRepository;
import com.adidos.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products/{productId}/variants")
@RequiredArgsConstructor
public class AdminVariantController {

    private final ProductService productService;
    private final SizeRepository sizeRepository;
    private final ColorRepository colorRepository;

    // 1. Hiển thị danh sách biến thể của 1 sản phẩm
    @GetMapping
    public String listVariants(@PathVariable Long productId, Model model) {
        ProductResponse product = productService.getProductDetail(productId);
        model.addAttribute("product", product);
        model.addAttribute("sizes", sizeRepository.findAll());
        model.addAttribute("colors", colorRepository.findAll());
        return "admin/product/variant_management";
    }

    // 2. Thêm / Sửa biến thể
    @PostMapping("/save")
    public String saveVariant(@PathVariable Long productId, @ModelAttribute VariantRequest request, RedirectAttributes ra) {
        try {
            request.setProductId(productId);
            productService.saveVariant(request);
            ra.addFlashAttribute("message", "Lưu biến thể thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products/" + productId + "/variants";
    }

    // 3. Xóa biến thể
    @PostMapping("/{variantId}/delete")
    public String deleteVariant(@PathVariable Long productId, @PathVariable Long variantId, RedirectAttributes ra) {
        try {
            productService.deleteVariant(variantId);
            ra.addFlashAttribute("message", "Xóa biến thể thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products/" + productId + "/variants";
    }

    // 4. API Lấy chi tiết ảnh của biến thể (Dùng cho Popup)
    @GetMapping("/{variantId}/images")
    @ResponseBody
    public VariantDetailResponse getVariantImages(@PathVariable Long variantId) {
        return productService.getVariantDetail(variantId);
    }

    // 5. Upload ảnh mới
    @PostMapping("/{variantId}/images/upload")
    public String uploadImages(@PathVariable Long productId, @PathVariable Long variantId,
                               @RequestParam("files") MultipartFile[] files,

                               @RequestParam(required = false, defaultValue = "false") boolean applyToSameColor,
                               RedirectAttributes ra) {
        try {
            // Nhớ truyền thêm biến applyToSameColor vào Service
            productService.uploadImages(variantId, files, applyToSameColor);
            ra.addFlashAttribute("message", "Tải ảnh lên thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi upload ảnh: " + e.getMessage());
        }
        return "redirect:/admin/products/" + productId + "/variants";
    }

    // 6. Đặt ảnh làm đại diện (Primary)
    @PostMapping("/{variantId}/images/{imageId}/primary")
    public String setPrimaryImage(@PathVariable Long productId, @PathVariable Long variantId, @PathVariable Long imageId, RedirectAttributes ra) {
        try {
            productService.setPrimaryImage(imageId, variantId);
            ra.addFlashAttribute("message", "Đã cập nhật ảnh đại diện thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products/" + productId + "/variants";
    }

    // 7. Xóa ảnh
    @PostMapping("/{variantId}/images/{imageId}/delete")
    public String deleteImage(@PathVariable Long productId, @PathVariable Long variantId, @PathVariable Long imageId) {
        productService.deleteImage(imageId);
        return "redirect:/admin/products/" + productId + "/variants";
    }
}