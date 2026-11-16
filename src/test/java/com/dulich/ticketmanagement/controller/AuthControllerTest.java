package com.dulich.ticketmanagement.controller;

import com.dulich.ticketmanagement.entity.User;
import com.dulich.ticketmanagement.entity.enums.UserRole;
import com.dulich.ticketmanagement.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Nghiệp vụ xác thực & Đổi mật khẩu cá nhân (AuthController)")
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("traveler_an");
        sampleUser.setPassword("$2a$10$oldEncodedPassword");
        sampleUser.setRole(UserRole.ROLE_TRAVELER);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "traveler_an", "oldPassword", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Đổi mật khẩu thành công: Mật khẩu cũ đúng, mật khẩu mới hợp lệ và được mã hóa BCrypt")
    void testProcessChangePasswordSuccess() {
        when(userRepository.findByUsername("traveler_an")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("oldPassword", "$2a$10$oldEncodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("newPassword123", "$2a$10$oldEncodedPassword")).thenReturn(false);
        when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$10$newEncodedPassword");

        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = authController.processChangePassword(
                "oldPassword", "newPassword123", "newPassword123", redirectAttributes);

        assertEquals("redirect:/change-password", view);
        assertNotNull(redirectAttributes.getFlashAttributes().get("successMessage"));
        assertEquals("$2a$10$newEncodedPassword", sampleUser.getPassword());
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    @DisplayName("Đổi mật khẩu thất bại: Mật khẩu hiện tại không chính xác")
    void testProcessChangePasswordFailsWhenWrongCurrentPassword() {
        when(userRepository.findByUsername("traveler_an")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongCurrent", "$2a$10$oldEncodedPassword")).thenReturn(false);

        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = authController.processChangePassword(
                "wrongCurrent", "newPassword123", "newPassword123", redirectAttributes);

        assertEquals("redirect:/change-password", view);
        assertTrue(redirectAttributes.getFlashAttributes().get("errorMessage").toString().contains("không chính xác"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đổi mật khẩu thất bại: Mật khẩu xác nhận không trùng khớp")
    void testProcessChangePasswordFailsWhenConfirmMismatch() {
        when(userRepository.findByUsername("traveler_an")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("oldPassword", "$2a$10$oldEncodedPassword")).thenReturn(true);

        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = authController.processChangePassword(
                "oldPassword", "newPassword123", "differentPassword", redirectAttributes);

        assertEquals("redirect:/change-password", view);
        assertTrue(redirectAttributes.getFlashAttributes().get("errorMessage").toString().contains("không trùng khớp"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đổi mật khẩu thất bại: Mật khẩu mới trùng với mật khẩu hiện tại")
    void testProcessChangePasswordFailsWhenSameAsCurrent() {
        when(userRepository.findByUsername("traveler_an")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("oldPassword", "$2a$10$oldEncodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("oldPassword", "$2a$10$oldEncodedPassword")).thenReturn(true);

        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = authController.processChangePassword(
                "oldPassword", "oldPassword", "oldPassword", redirectAttributes);

        assertEquals("redirect:/change-password", view);
        assertTrue(redirectAttributes.getFlashAttributes().get("errorMessage").toString().contains("không được trùng"));
        verify(userRepository, never()).save(any());
    }
}
