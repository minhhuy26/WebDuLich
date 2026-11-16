package com.dulich.ticketmanagement.controller;

import com.dulich.ticketmanagement.entity.User;
import com.dulich.ticketmanagement.entity.enums.UserRole;
import com.dulich.ticketmanagement.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        user.setRole(UserRole.ROLE_TRAVELER);
        user.setStatus(true);
        userRepository.save(user);

        return "redirect:/login?registered=true";
    }

    // HIỂN THỊ TRANG ĐỔI MẬT KHẨU
    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
        model.addAttribute("currentUser", currentUser);
        return "auth/change_password";
    }

    // TIẾP NHẬN XỬ LÝ ĐỔI MẬT KHẨU
    @PostMapping("/change-password")
    public String processChangePassword(@RequestParam("currentPassword") String currentPassword,
                                        @RequestParam("newPassword") String newPassword,
                                        @RequestParam("confirmPassword") String confirmPassword,
                                        RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        if (currentPassword == null || currentPassword.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập mật khẩu hiện tại!");
            return "redirect:/change-password";
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu hiện tại không chính xác!");
            return "redirect:/change-password";
        }

        if (newPassword == null || newPassword.trim().length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải có độ dài từ 6 ký tự trở lên!");
            return "redirect:/change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không trùng khớp với mật khẩu mới!");
            return "redirect:/change-password";
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới không được trùng với mật khẩu hiện tại!");
            return "redirect:/change-password";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công! Mật khẩu mới của bạn đã có hiệu lực.");
        return "redirect:/change-password";
    }
}
