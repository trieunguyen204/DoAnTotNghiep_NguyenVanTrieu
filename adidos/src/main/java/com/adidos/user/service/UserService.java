package com.adidos.user.service;

import com.adidos.user.dto.ProfileUpdateRequest;
import com.adidos.user.dto.UserRequest;
import com.adidos.user.dto.UserResponse;
import com.adidos.user.entity.User;
import com.adidos.user.entity.UserProvider;
import com.adidos.user.repository.UserProviderRepository;
import com.adidos.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProviderRepository providerRepository;

    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsersPage(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );

        return userRepository.searchUsersPage(
                keyword == null ? "" : keyword.trim(),
                pageable
        ).map(this::toResponse);
    }

    public List<UserResponse> getAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getById(Long id) {
        return toResponse(userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found")));
    }

    public void create(UserRequest request) {
        // 1. Lưu User chính
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role("USER")
                .status("ACTIVE")
                .build();
        user = userRepository.save(user);

        // 2. Lưu định danh LOCAL vào user_provider
        UserProvider localProvider = UserProvider.builder()
                .user(user)
                .provider("LOCAL")
                .providerId(user.getEmail()) // Dùng email làm ID cho local
                .email(user.getEmail())
                .build();
        providerRepository.save(localProvider);
    }

    public void update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .gender(user.getGender())
                .dob(user.getDob())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }

    public UserResponse getProfileByEmail(String email) {
        return toResponse(userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found")));
    }

    public void updateProfile(String email, ProfileUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());
        user.setDob(request.getDob());

        if (request.getAvatarFile() != null && !request.getAvatarFile().isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + request.getAvatarFile().getOriginalFilename();

                Path uploadPath = Paths.get("uploads/avatars");

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(request.getAvatarFile().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                user.setAvatarUrl("/uploads/avatars/" + fileName);

            } catch (IOException e) {
                throw new RuntimeException("Lỗi upload avatar: " + e.getMessage());
            }
        }

        if (request.getOldPassword() != null && !request.getOldPassword().isBlank()
                && request.getNewPassword() != null && !request.getNewPassword().isBlank()) {

            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new RuntimeException("Mật khẩu hiện tại không đúng");
            }

            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw new RuntimeException("Mật khẩu xác nhận không khớp");
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        userRepository.save(user);
    }

    // --- CÁC HÀM DÀNH CHO ADMIN ---

    @Transactional
    public void changeUserStatus(Long userId, String newStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Ngăn không cho Admin tự khóa chính mình (tránh lỗi ngớ ngẩn)
        if ("ADMIN".equals(user.getRole()) && "LOCKED".equals(newStatus)) {
            throw new RuntimeException("Không thể khóa tài khoản Quản trị viên!");
        }

        user.setStatus(newStatus);
        userRepository.save(user);
    }

    @Transactional
    public void changeUserRole(Long userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        user.setRole(newRole);
        userRepository.save(user);
    }

    public List<UserResponse> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAll();
        }
        return userRepository.searchUsers(keyword).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void adminUpdateUser(Long id, String fullName, String phone, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setRole(role);
        userRepository.save(user);
    }


}