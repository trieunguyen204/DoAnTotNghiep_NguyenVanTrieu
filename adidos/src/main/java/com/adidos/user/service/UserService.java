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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProviderRepository providerRepository;

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