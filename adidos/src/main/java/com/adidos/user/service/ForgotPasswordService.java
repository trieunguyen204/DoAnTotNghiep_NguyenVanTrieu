package com.adidos.user.service;

import com.adidos.user.entity.PasswordResetToken;
import com.adidos.user.entity.User;
import com.adidos.user.repository.PasswordResetTokenRepository;
import com.adidos.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;

    public void sendResetMail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (user.getPassword() == null) {
            throw new RuntimeException(
                    "Tài khoản này đăng nhập bằng Google/Facebook"
            );
        }

        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();

        tokenRepository.save(resetToken);

        String resetLink =
                "http://localhost:8080/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Reset mật khẩu Adidos");
        message.setText(
                "Nhấn vào link sau để đặt lại mật khẩu: "
                        + resetLink
        );

        mailSender.send(message);
    }

    public void resetPassword(String token, String newPassword,
                              PasswordEncoder encoder) {

        PasswordResetToken resetToken =
                tokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new RuntimeException("Token không hợp lệ"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token đã hết hạn");
        }

        User user = resetToken.getUser();

        user.setPassword(encoder.encode(newPassword));

        userRepository.save(user);

        tokenRepository.delete(resetToken);
    }
}