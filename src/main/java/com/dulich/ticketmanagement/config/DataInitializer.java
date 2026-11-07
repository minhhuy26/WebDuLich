package com.dulich.ticketmanagement.config;

import com.dulich.ticketmanagement.entity.User;
import com.dulich.ticketmanagement.entity.enums.UserRole;
import com.dulich.ticketmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createOrUpdateUser("admin", "admin123", "Quản Trị Viên Hệ Thống", "admin@dulich.com", "0901112222", UserRole.ADMIN);
        createOrUpdateUser("nhanvien", "nhanvien123", "Nguyễn Thị Mai (Nhân Viên)", "nhanvien@dulich.com", "0903334444", UserRole.NHANVIEN);
        createOrUpdateUser("khachhang", "khach123", "Trần Văn An (Khách Hàng)", "khachhang@dulich.com", "0912345678", UserRole.KHACH);

        log.info("Khởi tạo và cập nhật BCrypt mật khẩu cho 3 tài khoản mẫu thành công!");
    }

    private void createOrUpdateUser(String username, String rawPassword, String fullName, String email, String phone, UserRole role) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhone(phone);
            user.setRole(role);
            user.setStatus(true);
            userRepository.save(user);
            log.info("Tạo mới tài khoản: {} với vai trò {}", username, role);
        } else {
            // Đảm bảo mật khẩu được băm BCrypt và role đúng
            if (!user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$")) {
                user.setPassword(passwordEncoder.encode(rawPassword));
            }
            user.setRole(role);
            user.setFullName(fullName);
            user.setPhone(phone);
            userRepository.save(user);
        }
    }
}
