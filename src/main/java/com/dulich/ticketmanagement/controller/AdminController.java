package com.dulich.ticketmanagement.controller;

import com.dulich.ticketmanagement.entity.*;
import com.dulich.ticketmanagement.entity.enums.*;
import com.dulich.ticketmanagement.exception.BusinessException;
import com.dulich.ticketmanagement.repository.*;
import com.dulich.ticketmanagement.service.AppointmentService;
import com.dulich.ticketmanagement.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final TicketRepository ticketRepository;
    private final AppointmentService appointmentService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminController(ReservationRepository reservationRepository,
                           ReservationService reservationService,
                           TicketRepository ticketRepository,
                           AppointmentService appointmentService,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
        this.ticketRepository = ticketRepository;
        this.appointmentService = appointmentService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. DASHBOARD QUẢN TRỊ TỔNG QUAN
    @GetMapping
    public String dashboard(Model model) {
        long totalReservations = reservationRepository.count();
        long confirmedCount = reservationRepository.countByStatus(ReservationStatus.CONFIRMED);
        long holdingCount = reservationRepository.countByStatus(ReservationStatus.CREATED);
        BigDecimal totalRevenue = reservationRepository.calculateTotalRevenue();
        long totalAppointments = appointmentService.count();
        long totalTickets = ticketRepository.count();
        long totalUsers = userRepository.count();

        List<Reservation> recentReservations = reservationRepository.findAllWithDetails();
        if (recentReservations.size() > 10) {
            recentReservations = recentReservations.subList(0, 10);
        }

        List<Reservation> recentAppointments = appointmentService.findAll();
        if (recentAppointments.size() > 5) {
            recentAppointments = recentAppointments.subList(0, 5);
        }

        model.addAttribute("totalReservations", totalReservations);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("holdingCount", holdingCount);
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        model.addAttribute("totalAppointments", totalAppointments);
        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("recentReservations", recentReservations);
        model.addAttribute("recentAppointments", recentAppointments);

        return "admin/dashboard";
    }

    // 2. QUẢN LÝ ĐƠN ĐẶT CHỖ
    @GetMapping("/reservations")
    public String listReservations(@RequestParam(value = "type", required = false) TransportType type,
                                   @RequestParam(value = "status", required = false) ReservationStatus status,
                                   Model model) {
        List<Reservation> list;
        if (type != null) {
            list = reservationRepository.findByTransportTypeOrderByCreatedAtDesc(type);
        } else if (status != null) {
            list = reservationRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            list = reservationRepository.findAllWithDetails();
        }

        model.addAttribute("reservations", list);
        model.addAttribute("transportTypes", TransportType.values());
        model.addAttribute("reservationStatuses", ReservationStatus.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedStatus", status);

        return "admin/manage_reservations";
    }

    @PostMapping("/reservations/{code}/cancel")
    public String cancelReservationAdmin(@PathVariable("code") String bookingCode,
                                         @RequestParam(value = "reason", required = false) String reason,
                                         RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancelReservation(bookingCode, "Hủy bởi Điều hành hệ thống: " + reason);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn vé " + bookingCode);
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/reservations";
    }

    // 3. QUẢN LÝ KHO VÉ & PHƯƠNG TIỆN (TICKETS INVENTORY)
    @GetMapping("/tickets")
    public String listTickets(@RequestParam(value = "type", required = false) TransportType type, Model model) {
        List<Ticket> list;
        if (type != null) {
            list = ticketRepository.findByTransportTypeAndStatus(type, TicketStatus.AVAILABLE);
        } else {
            list = ticketRepository.findAll();
        }

        model.addAttribute("tickets", list);
        model.addAttribute("transportTypes", TransportType.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("newTicket", new Ticket());

        return "admin/manage_tickets";
    }

    @PostMapping("/tickets/new")
    public String createTicket(@ModelAttribute("newTicket") Ticket ticket,
                               @RequestParam("depDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate depDate,
                               @RequestParam("depHour") int depHour,
                               @RequestParam("depMinute") int depMinute,
                               RedirectAttributes redirectAttributes) {
        try {
            ticket.setDepartureTime(depDate.atTime(depHour, depMinute));
            ticket.setArrivalTime(ticket.getDepartureTime().plusHours(3));
            ticket.setRouteInfo(ticket.getDeparturePlace() + " ➔ " + ticket.getArrivalPlace());
            ticket.setStatus(TicketStatus.AVAILABLE);
            BigDecimal base = ticket.getBasePrice() != null ? ticket.getBasePrice() : BigDecimal.ZERO;
            BigDecimal service = ticket.getServiceFee() != null ? ticket.getServiceFee() : BigDecimal.ZERO;
            BigDecimal extra = ticket.getExtraFee() != null ? ticket.getExtraFee() : BigDecimal.ZERO;
            ticket.setBasePrice(base);
            ticket.setServiceFee(service);
            ticket.setExtraFee(extra);
            ticket.setFinalPrice(base.add(service).add(extra));
            ticketRepository.save(ticket);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm vé/chuyến mới vào kho thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + ex.getMessage());
        }
        return "redirect:/admin/tickets";
    }

    // 4. QUẢN LÝ LỊCH HẸN TƯ VẤN (APPOINTMENTS GẮN LIỀN VỚI ĐƠN ĐẶT VÉ)
    @GetMapping("/appointments")
    public String manageAppointments(Model model) {
        model.addAttribute("appointments", appointmentService.findAll());
        model.addAttribute("operators", userRepository.findByRole(UserRole.ROLE_OPERATOR));
        model.addAttribute("statuses", AppointmentStatus.values());
        return "admin/manage_appointments";
    }

    @PostMapping("/appointments/{id}/status")
    public String updateAppointmentStatus(@PathVariable("id") Long id,
                                          @RequestParam("status") AppointmentStatus status,
                                          @RequestParam(value = "operatorId", required = false) Long operatorId,
                                          RedirectAttributes redirectAttributes) {
        try {
            appointmentService.updateStatus(id, status, operatorId);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái cuộc hẹn thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/appointments";
    }

    // 5. QUẢN LÝ TÀI KHOẢN NGƯỜI DÙNG & PHÂN QUYỀN RBAC (CHỈ ROLE_ADMIN)
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("roles", UserRole.values());
        return "admin/manage_users";
    }

    @PostMapping("/users/new")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String createUser(@RequestParam("username") String username,
                             @RequestParam("password") String rawPassword,
                             @RequestParam("fullName") String fullName,
                             @RequestParam("email") String email,
                             @RequestParam("phone") String phone,
                             @RequestParam("role") UserRole role,
                             RedirectAttributes redirectAttributes) {
        try {
            if (userRepository.existsByUsername(username)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tên đăng nhập đã tồn tại!");
                return "redirect:/admin/users";
            }
            User user = new User(username, passwordEncoder.encode(rawPassword), fullName, email, phone, role);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo tài khoản mới thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }
}
