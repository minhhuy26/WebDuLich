package com.dulich.ticketmanagement.config;

import com.dulich.ticketmanagement.entity.User;
import com.dulich.ticketmanagement.entity.enums.UserRole;
import com.dulich.ticketmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
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
        createOrUpdateUser("admin", "admin123", "Quản Trị Viên Hệ Thống", "admin@dulich.vn", "0901000001", UserRole.ROLE_ADMIN);
        createOrUpdateUser("operator_mai", "operator123", "Nguyễn Thị Mai (Điều Hành Vé)", "mai.operator@dulich.vn", "0902000002", UserRole.ROLE_OPERATOR);
        createOrUpdateUser("agent_saigontour", "agent123", "Đại Lý Du Lịch Sài Gòn", "contact@saigontour.vn", "0903000003", UserRole.ROLE_AGENT);
        createOrUpdateUser("traveler_an", "traveler123", "Trần Văn An (Du Khách)", "an.tran@gmail.com", "0912345678", UserRole.ROLE_TRAVELER);

        log.info("Khởi tạo và đồng bộ 4 tài khoản chuẩn RBAC (Admin, Operator, Agent, Traveler) thành công!");
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
            log.info("Đã tạo mới tài khoản mẫu: {} vai trò {}", username, role);
        } else {
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
