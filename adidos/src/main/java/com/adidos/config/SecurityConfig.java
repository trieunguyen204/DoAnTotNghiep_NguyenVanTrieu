package com.adidos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
// ĐÃ XÓA @RequiredArgsConstructor ở đây
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Tiêm CustomOAuth2UserService trực tiếp qua tham số của hàm để phá vòng lặp khởi tạo
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                       // public
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/products/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/product/{id}",
                                "/category/{id}",
                                "/uploads/**",
                                "/cart",
                                "/api/cart/**",
                                "/403",
                                "/404",
                                "/500",
                                "/payment/payos/return",
                                "/payment/payos/cancel",
                                "/api/payment/payos/webhook"
                        )
                        .permitAll()

                        // CHỈ ADMIN MỚI ĐƯỢC VÀO CÁC ĐƯỜNG DẪN NÀY
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Các yêu cầu khác phải đăng nhập (USER hoặc ADMIN)
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customSuccessHandler())
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(customSuccessHandler())
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/403")
                )
        ;
        return http.build();
    }

    @Bean
    public CustomSuccessHandler customSuccessHandler() {
        return new CustomSuccessHandler();
    }
}