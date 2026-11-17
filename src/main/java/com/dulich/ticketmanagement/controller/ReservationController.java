package com.dulich.ticketmanagement.controller;

import com.dulich.ticketmanagement.dto.PassengerDto;
import com.dulich.ticketmanagement.dto.ReservationRequestDto;
import com.dulich.ticketmanagement.entity.Reservation;
import com.dulich.ticketmanagement.entity.Ticket;
import com.dulich.ticketmanagement.entity.User;
import com.dulich.ticketmanagement.entity.enums.PassengerType;
import com.dulich.ticketmanagement.entity.enums.PaymentMethod;
import com.dulich.ticketmanagement.exception.BusinessException;
import com.dulich.ticketmanagement.repository.UserRepository;
import com.dulich.ticketmanagement.service.AppointmentService;
import com.dulich.ticketmanagement.service.ReservationService;
import com.dulich.ticketmanagement.service.TicketCatalogService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Trạm điều phối đặt chỗ vé du lịch (ReservationController)
 * Tuân thủ mô hình POST/Redirect/GET (PRG Pattern) theo đúng Workflow 1 của Giảng viên.
 */
@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final TicketCatalogService ticketCatalogService;
    private final UserRepository userRepository;
    private final com.dulich.ticketmanagement.service.QrCodeService qrCodeService;
    private final AppointmentService appointmentService;

    @Autowired
    public ReservationController(ReservationService reservationService,
                                 TicketCatalogService ticketCatalogService,
                                 UserRepository userRepository,
                                 com.dulich.ticketmanagement.service.QrCodeService qrCodeService,
                                 AppointmentService appointmentService) {
        this.reservationService = reservationService;
        this.ticketCatalogService = ticketCatalogService;
        this.userRepository = userRepository;
        this.qrCodeService = qrCodeService;
        this.appointmentService = appointmentService;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }

    private boolean isAuthorizedToViewOrModify(Reservation reservation, String phone, HttpSession session) {
        // 1. Quản trị viên (ROLE_ADMIN) và Điều hành viên (ROLE_OPERATOR) có quyền xem và xử lý mọi đơn
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            boolean isStaffOrAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_OPERATOR"));
            if (isStaffOrAdmin) {
                return true;
            }
            User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
            if (currentUser != null) {
                // Khách hàng đăng nhập là người đặt đơn
                if (reservation.getUser() != null && reservation.getUser().getId().equals(currentUser.getId())) {
                    return true;
                }
                // Hoặc số điện thoại tài khoản khớp với SĐT trong đơn
                if (currentUser.getPhone() != null && !currentUser.getPhone().isBlank()
                        && currentUser.getPhone().trim().equals(reservation.getCustomerPhone().trim())) {
                    return true;
                }
            }
        }

        // 2. Quyền truy cập trong phiên làm việc trình duyệt hiện tại (vừa đặt đơn hoặc vừa tra cứu hợp lệ)
        if (Boolean.TRUE.equals(session.getAttribute("AUTH_BOOKING_" + reservation.getBookingCode()))) {
            return true;
        }

        // 3. Khách vãng lai xác thực đúng số điện thoại đã dùng để đặt vé
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

    // 1. HIỂN THỊ FORM ĐẶT VÉ (GET)
    @GetMapping("/book")
    public String showBookingForm(@RequestParam("ticketId") Long ticketId,
                                  @RequestParam(value = "passengers", defaultValue = "1") int passengerCount,
                                  Model model) {
        Ticket ticket = ticketCatalogService.getTicketById(ticketId);

        ReservationRequestDto requestDto = new ReservationRequestDto();
        requestDto.setTicketId(ticket.getId());
        requestDto.setTransportType(ticket.getTransportType());
        requestDto.setVehicleCode(ticket.getVehicleCode());
        requestDto.setVehicleName(ticket.getVehicleName());
        requestDto.setDeparturePlace(ticket.getDeparturePlace());
        requestDto.setArrivalPlace(ticket.getArrivalPlace());
        requestDto.setDepartureDate(ticket.getDepartureTime().toLocalDate());
        requestDto.setSeatClass(ticket.getSeatClass());

        User currentUser = getCurrentUser();
        if (currentUser != null) {
            requestDto.setCustomerName(currentUser.getFullName());
            requestDto.setCustomerEmail(currentUser.getEmail());
            requestDto.setCustomerPhone(currentUser.getPhone());
        }

        List<PassengerDto> pList = new ArrayList<>();
        for (int i = 0; i < Math.max(1, passengerCount); i++) {
            PassengerDto p = new PassengerDto();
            if (i == 0 && currentUser != null) {
                p.setFullName(currentUser.getFullName());
            }
            p.setPassengerType(PassengerType.ADULT);
            String allocatedSeat = ticketCatalogService.generateConsecutiveSeat(ticket.getSeatNumber(), i, ticket.getTransportType());
            p.setSeatNumber(allocatedSeat);
            pList.add(p);
        }
        requestDto.setPassengers(pList);

        model.addAttribute("ticket", ticket);
        model.addAttribute("bookingRequest", requestDto);
        model.addAttribute("passengerTypes", PassengerType.values());

        return "reservation/booking_form";
    }

    // 2. TIẾP NHẬN FORM ĐẶT GIỮ CHỖ & MÔ HÌNH PRG (POST /reservations/book)
    @PostMapping("/book")
    public String createBooking(@Valid @ModelAttribute("bookingRequest") ReservationRequestDto bookingRequest,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            Ticket ticket = ticketCatalogService.getTicketById(bookingRequest.getTicketId());
            model.addAttribute("ticket", ticket);
            model.addAttribute("passengerTypes", PassengerType.values());
            return "reservation/booking_form";
        }

        try {
            User currentUser = getCurrentUser();
            Reservation reservation = reservationService.createHoldReservation(bookingRequest, currentUser);

            // Cho phép session xem đơn vé
            session.setAttribute("AUTH_BOOKING_" + reservation.getBookingCode(), true);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Giữ chỗ thành công mã đơn: " + reservation.getBookingCode() + "! Quý khách có 15 phút để hoàn tất thanh toán.");

            // PRG Pattern: Trả về HTTP 303 Redirect sang endpoint GET chi tiết
            return "redirect:/reservations/" + reservation.getBookingCode();
        } catch (BusinessException ex) {
            Ticket ticket = ticketCatalogService.getTicketById(bookingRequest.getTicketId());
            model.addAttribute("ticket", ticket);
            model.addAttribute("passengerTypes", PassengerType.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "reservation/booking_form";
        }
    }

    // 3. XEM CHI TIẾT ĐƠN VÉ & ĐỒNG HỒ ĐẾM NGƯỢC (GET /reservations/{code})
    @GetMapping("/{code}")
    public String viewReservationDetail(@PathVariable("code") String bookingCode,
                                        @RequestParam(value = "phone", required = false) String phone,
                                        HttpSession session,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        Reservation reservation;
        try {
            reservation = reservationService.getByBookingCode(bookingCode);
        } catch (com.dulich.ticketmanagement.exception.ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/my-bookings/lookup";
        }

        // Kiểm tra quyền sở hữu đơn đặt vé (Chặn lỗ hổng IDOR / Truy cập trái phép)
        if (!isAuthorizedToViewOrModify(reservation, phone, session)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Để bảo mật thông tin hành khách, vui lòng nhập số điện thoại đã dùng khi đặt vé để xem chi tiết!");
            return "redirect:/my-bookings/lookup?code=" + bookingCode;
        }

        // Đảm bảo tất cả các vé đã xuất (ISSUED) đều có Base64 QR Code hợp lệ
        if (reservation.getTickets() != null) {
            for (Ticket t : reservation.getTickets()) {
                if (t.getStatus() == com.dulich.ticketmanagement.entity.enums.TicketStatus.ISSUED &&
                        (t.getQrCodeData() == null || !t.getQrCodeData().startsWith("data:image"))) {
                    String payload = (t.getQrCodeData() != null && !t.getQrCodeData().isEmpty())
                            ? t.getQrCodeData()
                            : ("ETICKET:" + t.getTicketCode() + "|RES:" + reservation.getBookingCode() + "|PAX:" + t.getPassengerName() + "|SEAT:" + t.getSeatNumber());
                    t.setQrCodeData(qrCodeService.generateBase64QrCode(payload, 200, 200));
                }
            }
        }

        model.addAttribute("reservation", reservation);
        model.addAttribute("now", LocalDateTime.now());
        model.addAttribute("paymentMethods", PaymentMethod.values());

        return "reservation/booking_detail";
    }

    // 4. HỦY ĐƠN ĐẶT CHỖ (POST /reservations/{code}/cancel)
    @PostMapping("/{code}/cancel")
    public String cancelReservation(@PathVariable("code") String bookingCode,
                                    @RequestParam(value = "phone", required = false) String phone,
                                    @RequestParam(value = "reason", required = false) String reason,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        Reservation reservation;
        try {
            reservation = reservationService.getByBookingCode(bookingCode);
        } catch (com.dulich.ticketmanagement.exception.ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/my-bookings/lookup";
        }

        // Kiểm tra quyền sở hữu trước khi thực hiện hủy đơn
        if (!isAuthorizedToViewOrModify(reservation, phone, session)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Quý khách không có quyền hủy đơn đặt vé này!");
            return "redirect:/my-bookings/lookup?code=" + bookingCode;
        }

        try {
            Reservation cancelled = reservationService.cancelReservation(bookingCode, reason);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã hủy thành công đơn vé: " + cancelled.getBookingCode());
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/reservations/" + bookingCode;
    }

    // 5. ĐĂNG KÝ HẸN TƯ VẤN CHO CHUYẾN ĐI (CHỈ XUẤT HIỆN SAU KHI ĐẶT VÉ XONG)
    @PostMapping("/{code}/consultation")
    public String bookConsultationForReservation(@PathVariable("code") String bookingCode,
                                                 @RequestParam("consultationDate") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime consultationDate,
                                                 @RequestParam(value = "notes", required = false) String notes,
                                                 RedirectAttributes redirectAttributes) {
        try {
            appointmentService.scheduleConsultation(bookingCode, consultationDate, notes);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký lịch hẹn tư vấn chuyến đi thành công! Chuyên viên VietTravel Hub sẽ liên hệ hỗ trợ bạn.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/reservations/" + bookingCode;
    }
}
