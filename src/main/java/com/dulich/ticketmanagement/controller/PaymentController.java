package com.dulich.ticketmanagement.controller;

import com.dulich.ticketmanagement.dto.PaymentRequestDto;
import com.dulich.ticketmanagement.entity.Reservation;
import com.dulich.ticketmanagement.entity.TransDetail;
import com.dulich.ticketmanagement.entity.User;
import com.dulich.ticketmanagement.exception.BusinessException;
import com.dulich.ticketmanagement.exception.ResourceNotFoundException;
import com.dulich.ticketmanagement.repository.UserRepository;
import com.dulich.ticketmanagement.service.PaymentService;
import com.dulich.ticketmanagement.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Trạm điều phối thanh toán giao dịch (PaymentController)
 * Thực thi Workflow 3 (Payment & State Machine Lifecycle) theo đặc tả của Giảng viên.
 */
@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final ReservationService reservationService;
    private final UserRepository userRepository;

    @Autowired
    public PaymentController(PaymentService paymentService,
                             ReservationService reservationService,
                             UserRepository userRepository) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
        this.userRepository = userRepository;
    }

    private boolean isAuthorizedToPay(Reservation reservation, String phone, HttpSession session) {
        // 1. Quản trị viên (ROLE_ADMIN) và Điều hành viên (ROLE_OPERATOR)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            boolean isStaffOrAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_OPERATOR"));
            if (isStaffOrAdmin) {
                return true;
            }
            User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
            if (currentUser != null) {
                if (reservation.getUser() != null && reservation.getUser().getId().equals(currentUser.getId())) {
                    return true;
                }
                if (currentUser.getPhone() != null && !currentUser.getPhone().isBlank()
                        && currentUser.getPhone().trim().equals(reservation.getCustomerPhone().trim())) {
                    return true;
                }
            }
        }

        // 2. Quyền trong phiên làm việc hiện tại
        if (Boolean.TRUE.equals(session.getAttribute("AUTH_BOOKING_" + reservation.getBookingCode()))) {
            return true;
        }

        // 3. Khách vãng lai xác thực đúng số điện thoại đặt vé
        if (phone != null && !phone.isBlank()) {
            String cleanParamPhone = phone.replaceAll("[^0-9]", "");
            String cleanResPhone = reservation.getCustomerPhone() != null ? reservation.getCustomerPhone().replaceAll("[^0-9]", "") : "";
            if (!cleanParamPhone.isEmpty() && cleanParamPhone.equals(cleanResPhone)) {
                session.setAttribute("AUTH_BOOKING_" + reservation.getBookingCode(), true);
                return true;
            }
        }

        return false;
    }

    // TIẾP NHẬN XỬ LÝ THANH TOÁN (POST /payments/process) & MÔ HÌNH PRG
    @PostMapping("/process")
    public String processPayment(@Valid @ModelAttribute("paymentRequest") PaymentRequestDto paymentRequest,
                                 BindingResult bindingResult,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Thông tin thanh toán không hợp lệ!");
            return "redirect:/reservations/" + paymentRequest.getBookingCode();
        }

        Reservation reservation;
        try {
            reservation = reservationService.getByBookingCode(paymentRequest.getBookingCode());
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/my-bookings/lookup";
        }

        // Kiểm tra phân quyền truy cập và thanh toán (chặn IDOR exploit)
        if (!isAuthorizedToPay(reservation, paymentRequest.getCustomerPhone(), session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Quý khách không có quyền thực hiện thanh toán cho đơn đặt vé này!");
            return "redirect:/my-bookings/lookup?code=" + paymentRequest.getBookingCode();
        }

        try {
            TransDetail detail = paymentService.processPayment(paymentRequest);
            session.setAttribute("AUTH_BOOKING_" + paymentRequest.getBookingCode(), true);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thanh toán thành công qua " + detail.getPaymentMethod().getDisplayName() +
                    "! Mã giao dịch: " + detail.getPaymentCode() + ". Vé điện tử và mã QR Code đã được kích hoạt thành công!");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        // PRG Pattern: Redirect 303 sang GET kết quả
        return "redirect:/reservations/" + paymentRequest.getBookingCode();
    }
}
