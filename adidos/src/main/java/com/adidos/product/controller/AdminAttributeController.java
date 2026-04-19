package com.adidos.product.controller;

import com.adidos.product.entity.Color;
import com.adidos.product.entity.Size;
import com.adidos.product.repository.ColorRepository;
import com.adidos.product.repository.SizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/attributes")
@RequiredArgsConstructor
public class AdminAttributeController {

    private final SizeRepository sizeRepository;
    private final ColorRepository colorRepository;


    // --- XỬ LÝ SIZE ---
    @PostMapping("/sizes/save")
    public String saveSize(@RequestParam(required = false) Long id, @RequestParam String sizeName, RedirectAttributes ra) {
        String cleanName = sizeName.trim();

        // Kiểm tra trùng lặp
        Optional<Size> existing = sizeRepository.findBySizeName(cleanName);
        if (existing.isPresent() && (id == null || !existing.get().getId().equals(id))) {
            ra.addFlashAttribute("error", "Kích cỡ '" + cleanName + "' đã tồn tại!");
            return "redirect:/admin/attributes";
        }

        Size size = (id != null) ? sizeRepository.findById(id).orElse(new Size()) : new Size();
        size.setSizeName(cleanName);
        sizeRepository.save(size);
        ra.addFlashAttribute("message", "Lưu kích cỡ thành công!");
        return "redirect:/admin/attributes";
    }

    @PostMapping("/sizes/{id}/delete")
    public String deleteSize(@PathVariable Long id, RedirectAttributes ra) {
        try {
            sizeRepository.deleteById(id);
            ra.addFlashAttribute("message", "Xóa kích cỡ thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa kích cỡ đang được sử dụng!");
        }
        return "redirect:/admin/attributes";
    }

    // --- XỬ LÝ COLOR ---
    @PostMapping("/colors/save")
    public String saveColor(@RequestParam(required = false) Long id, @RequestParam String colorName, RedirectAttributes ra) {
        String cleanName = colorName.trim();

        // Kiểm tra trùng lặp
        Optional<Color> existing = colorRepository.findByColorName(cleanName);
        if (existing.isPresent() && (id == null || !existing.get().getId().equals(id))) {
            ra.addFlashAttribute("error", "Màu sắc '" + cleanName + "' đã tồn tại!");
            return "redirect:/admin/attributes";
        }

        Color color = (id != null) ? colorRepository.findById(id).orElse(new Color()) : new Color();
        color.setColorName(cleanName);
        colorRepository.save(color);
        ra.addFlashAttribute("message", "Lưu màu sắc thành công!");
        return "redirect:/admin/attributes";
    }

    @PostMapping("/colors/{id}/delete")
    public String deleteColor(@PathVariable Long id, RedirectAttributes ra) {
        try {
            colorRepository.deleteById(id);
            ra.addFlashAttribute("message", "Xóa màu sắc thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa màu sắc đang được sử dụng!");
        }
        return "redirect:/admin/attributes";
    }

    @GetMapping
    public String listAttributes(@RequestParam(required = false) String sizeKw,
                                 @RequestParam(required = false) String colorKw, Model model) {
        model.addAttribute("sizes", (sizeKw != null) ? sizeRepository.searchSizes(sizeKw) : sizeRepository.findAll());
        model.addAttribute("colors", (colorKw != null) ? colorRepository.searchColors(colorKw) : colorRepository.findAll());
        model.addAttribute("sizeKw", sizeKw);
        model.addAttribute("colorKw", colorKw);
        return "admin/product/attribute_management";
    }
}