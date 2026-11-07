package com.dulich.ticketmanagement.controller;

import com.dulich.ticketmanagement.entity.User;
import com.dulich.ticketmanagement.entity.enums.UserRole;
import com.dulich.ticketmanagement.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                @RequestParam(value = "registered", required = false) String registered,
                                Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không chính xác!");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Bạn đã đăng xuất thành công khỏi hệ thống.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("user") User user,
                                  BindingResult bindingResult,
                                  @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                                  @RequestParam(value = "agreeTerms", defaultValue = "false") boolean agreeTerms,
                                  Model model) {
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            model.addAttribute("confirmPasswordError", "Vui lòng nhập lại mật khẩu để xác nhận!");
        } else if (!confirmPassword.equals(user.getPassword())) {
            model.addAttribute("confirmPasswordError", "Mật khẩu xác nhận không trùng khớp!");
        }

        if (!agreeTerms) {
            model.addAttribute("agreeTermsError", "Bạn cần đồng ý với Điều khoản sử dụng và Chính sách bảo mật!");
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            bindingResult.rejectValue("username", "error.username", "Tên đăng nhập này đã có người sử dụng!");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            bindingResult.rejectValue("email", "error.email", "Email này đã được đăng ký tài khoản!");
        }

        if (bindingResult.hasErrors() || model.containsAttribute("confirmPasswordError") || !agreeTerms) {
            model.addAttribute("confirmPassword", confirmPassword);
            model.addAttribute("agreeTerms", agreeTerms);
            return "auth/register";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(UserRole.KHACH);
        user.setStatus(true);
        userRepository.save(user);

        return "redirect:/login?registered=true";
    }
}
