package com.dulich.ticketmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Keep CSRF protection enabled for every state-changing browser request.
            // Thymeleaf adds the token automatically to forms that use th:action.
            .csrf(csrf -> {})
            .authorizeHttpRequests(auth -> auth
                // Tài nguyên tĩnh công khai
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/error").permitAll()
                // Danh sách lịch hẹn bảo mật cho Nhân viên & Admin
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/consultations").hasAnyRole("NHANVIEN", "ADMIN")
                // Trang chủ, tìm kiếm vé, đặt vé và tra cứu đơn vé công khai (khách vãng lai và thành viên)
                .requestMatchers("/", "/flights", "/flights/*/book", "/flights/hold", "/flights/booking/**", "/flights/bookings").permitAll()
                .requestMatchers("/trains", "/trains/*/select-coach", "/trains/booking-form", "/trains/hold", "/trains/booking/**", "/trains/bookings").permitAll()
                // Khách hàng đặt lịch tư vấn công khai
                .requestMatchers("/consultations/new", "/consultations/success").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/consultations").permitAll()
                // Đăng ký, đăng nhập
                .requestMatchers("/login", "/register").permitAll()
                // Khách xem đơn của chính mình
                .requestMatchers("/my-bookings/**").hasAnyRole("KHACH", "NHANVIEN", "ADMIN")
                // Admin quản lý chuyến bay, tàu, ga, người dùng (chỉ ADMIN)
                .requestMatchers("/admin/flights/**", "/admin/trains/**", "/admin/stations/**", "/admin/users/**").hasRole("ADMIN")
                // Nhân viên & Admin xử lý đơn vé, lịch hẹn và thống kê
                .requestMatchers("/admin/**").hasAnyRole("NHANVIEN", "ADMIN")
                // Tất cả request khác cần đăng nhập
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(customAuthenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            boolean isAdminOrStaff = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_NHANVIEN"));
            if (isAdminOrStaff) {
                response.sendRedirect("/admin");
            } else {
                response.sendRedirect("/my-bookings");
            }
        };
    }
}
