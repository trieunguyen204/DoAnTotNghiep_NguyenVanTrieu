package com.adidos.user.controller;

import com.adidos.user.dto.UserResponse;
import com.adidos.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    // 1. Hiển thị danh sách & Tìm kiếm
    @GetMapping
    public String adminUsers(@RequestParam(required = false) String keyword, Model model) {
        List<UserResponse> users;
        if (keyword != null && !keyword.isEmpty()) {
            users = userService.searchUsers(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            users = userService.getAll();
        }
        model.addAttribute("users", users);
        return "admin/user/user_management";
    }

    // 2. Khóa / Mở khóa tài khoản
    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam String status) {
        userService.changeUserStatus(id, status);
        return "redirect:/admin/users";
    }

    // Sửa thông tin tài khoản
    @PostMapping("/{id}/edit")
    public String editUser(@PathVariable Long id,
                           @RequestParam String fullName,
                           @RequestParam String phone,
                           @RequestParam String role,
                           RedirectAttributes redirectAttributes) {
        try {
            userService.adminUpdateUser(id, fullName, phone, role);
            redirectAttributes.addFlashAttribute("message", "Cập nhật thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // Xóa tài khoản
    @DeleteMapping("/{id}")
    @ResponseBody
    public String deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return "success";
    }

    // 5. Xóa tài khoản
    @PostMapping("/{id}/delete")
    public String deleteUserFallback(@PathVariable Long id) {
        userService.delete(id);
        return "redirect:/admin/users";
    }
}