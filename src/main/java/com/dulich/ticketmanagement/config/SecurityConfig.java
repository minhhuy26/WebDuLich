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
            .csrf(csrf -> {})
            .authorizeHttpRequests(auth -> auth
                // 1. Tài nguyên tĩnh công khai
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/error").permitAll()

                // 2. Tra cứu danh mục vé, tìm kiếm, chính sách và trang chủ (công khai)
                .requestMatchers("/", "/catalog/**", "/tickets/**", "/flights/**", "/trains/**", "/policies", "/my-bookings/lookup").permitAll()

                // 3. Quy trình Đặt vé & Giữ chỗ & Thanh toán (Công khai cho cả khách vãng lai và thành viên)
                .requestMatchers("/reservations/book", "/reservations/hold", "/reservations/**").permitAll()
                .requestMatchers("/payments/**").permitAll()

                // 4. Đặt lịch hẹn tư vấn trực tiếp (Workflow 4)
                .requestMatchers("/appointments/schedule", "/appointments/new", "/appointments/success").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/appointments").permitAll()
                .requestMatchers("/consultations/**").permitAll()

                // 5. Đăng ký & Đăng nhập
                .requestMatchers("/login", "/register").permitAll()

                // 6. Quản lý lịch hẹn tư vấn cho Nhân viên điều hành & Admin
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/appointments").hasAnyAuthority("ROLE_OPERATOR", "ROLE_AGENT", "ROLE_ADMIN")

                // 7. Khu vực tra cứu đơn cá nhân
                .requestMatchers("/my-bookings/**").hasAnyAuthority("ROLE_TRAVELER", "ROLE_AGENT", "ROLE_OPERATOR", "ROLE_ADMIN")

                // 8. Quản trị hệ thống (Chỉ ROLE_ADMIN)
                .requestMatchers("/admin/users/**").hasAuthority("ROLE_ADMIN")

                // 9. Quản trị vận hành & Báo cáo doanh thu (ROLE_OPERATOR & ROLE_ADMIN)
                .requestMatchers("/admin/**").hasAnyAuthority("ROLE_OPERATOR", "ROLE_ADMIN")

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
            boolean isStaffOrAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_OPERATOR"));
            if (isStaffOrAdmin) {
                response.sendRedirect("/admin");
            } else {
                response.sendRedirect("/my-bookings");
            }
        };
    }
}
