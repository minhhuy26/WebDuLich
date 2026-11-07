package com.dulich.ticketmanagement.controller;

import com.dulich.ticketmanagement.dto.AdminStatisticsDto;
import com.dulich.ticketmanagement.dto.UnifiedBookingDto;
import com.dulich.ticketmanagement.entity.*;
import com.dulich.ticketmanagement.entity.enums.ConsultationStatus;
import com.dulich.ticketmanagement.entity.enums.FlightBookingStatus;
import com.dulich.ticketmanagement.entity.enums.TicketType;
import com.dulich.ticketmanagement.entity.enums.TrainBookingStatus;
import com.dulich.ticketmanagement.entity.enums.UserRole;
import com.dulich.ticketmanagement.exception.BusinessException;
import com.dulich.ticketmanagement.repository.*;
import com.dulich.ticketmanagement.service.ConsultationService;
import com.dulich.ticketmanagement.service.impl.FlightTicketService;
import com.dulich.ticketmanagement.service.impl.TrainTicketService;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final FlightBookingRepository flightBookingRepository;
    private final TrainBookingRepository trainBookingRepository;
    private final FlightTicketService flightTicketService;
    private final TrainTicketService trainTicketService;
    private final ConsultationService consultationService;
    private final FlightRepository flightRepository;
    private final TrainRepository trainRepository;
    private final TrainStationRepository trainStationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminController(FlightBookingRepository flightBookingRepository,
                           TrainBookingRepository trainBookingRepository,
                           FlightTicketService flightTicketService,
                           TrainTicketService trainTicketService,
                           ConsultationService consultationService,
                           FlightRepository flightRepository,
                           TrainRepository trainRepository,
                           TrainStationRepository trainStationRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.flightBookingRepository = flightBookingRepository;
        this.trainBookingRepository = trainBookingRepository;
        this.flightTicketService = flightTicketService;
        this.trainTicketService = trainTicketService;
        this.consultationService = consultationService;
        this.flightRepository = flightRepository;
        this.trainRepository = trainRepository;
        this.trainStationRepository = trainStationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================================
    // 1. DASHBOARD TỔNG HỢP: LỌC ĐƠN ĐẶT VÉ, LỊCH HẸN, THỐNG KÊ DOANH THU
    // =========================================================================
    @GetMapping
    public String adminDashboard(@RequestParam(value = "ticketType", defaultValue = "ALL") String ticketType,
                                 @RequestParam(value = "status", defaultValue = "ALL") String status,
                                 @RequestParam(value = "depDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate depDate,
                                 Model model) {

        List<FlightBooking> rawFlights = flightBookingRepository.findAllWithFlight();
        List<TrainBooking> rawTrains = trainBookingRepository.findAllWithDetails();
        List<Consultation> consultations = consultationService.findAll();

        // 1. Thống kê
        AdminStatisticsDto stats = computeStatistics(rawFlights, rawTrains, consultations);

        // 2. Chuyển đổi và lọc danh sách đơn đặt vé tổng hợp
        List<UnifiedBookingDto> unifiedList = new ArrayList<>();

        if ("ALL".equalsIgnoreCase(ticketType) || "FLIGHT".equalsIgnoreCase(ticketType)) {
            for (FlightBooking fb : rawFlights) {
                if (matchesFlightFilters(fb, status, depDate)) {
                    unifiedList.add(toUnifiedFlightDto(fb));
                }
            }
        }

        if ("ALL".equalsIgnoreCase(ticketType) || "TRAIN".equalsIgnoreCase(ticketType)) {
            for (TrainBooking tb : rawTrains) {
                if (matchesTrainFilters(tb, status, depDate)) {
                    unifiedList.add(toUnifiedTrainDto(tb));
                }
            }
        }

        // Sắp xếp đơn mới nhất lên đầu
        unifiedList.sort(Comparator.comparing(UnifiedBookingDto::getBookingTime, Comparator.nullsLast(Comparator.reverseOrder())));

        model.addAttribute("stats", stats);
        model.addAttribute("bookings", unifiedList);
        model.addAttribute("consultations", consultations);
        model.addAttribute("filterTicketType", ticketType);
        model.addAttribute("filterStatus", status);
        model.addAttribute("filterDepDate", depDate);

        return "admin/dashboard";
    }

    private boolean matchesFlightFilters(FlightBooking fb, String status, LocalDate depDate) {
        if (!"ALL".equalsIgnoreCase(status)) {
            if ("HELD".equalsIgnoreCase(status) || "HOLD".equalsIgnoreCase(status)) {
                if (fb.getStatus() != FlightBookingStatus.HELD) return false;
            } else if ("CONFIRMED".equalsIgnoreCase(status) || "ISSUED".equalsIgnoreCase(status)) {
                if (fb.getStatus() != FlightBookingStatus.CONFIRMED && fb.getStatus() != FlightBookingStatus.ISSUED) return false;
            } else if (!fb.getStatus().name().equalsIgnoreCase(status)) {
                return false;
            }
        }
        if (depDate != null && fb.getFlight() != null) {
            if (!fb.getFlight().getDepartureTime().toLocalDate().isEqual(depDate)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesTrainFilters(TrainBooking tb, String status, LocalDate depDate) {
        if (!"ALL".equalsIgnoreCase(status)) {
            if ("HELD".equalsIgnoreCase(status) || "HOLD".equalsIgnoreCase(status)) {
                if (tb.getStatus() != TrainBookingStatus.HELD) return false;
            } else if ("CONFIRMED".equalsIgnoreCase(status) || "ISSUED".equalsIgnoreCase(status)) {
                if (tb.getStatus() != TrainBookingStatus.CONFIRMED && tb.getStatus() != TrainBookingStatus.ISSUED) return false;
            } else if (!tb.getStatus().name().equalsIgnoreCase(status)) {
                return false;
            }
        }
        if (depDate != null && tb.getDepartureDate() != null) {
            if (!tb.getDepartureDate().isEqual(depDate)) {
                return false;
            }
        }
        return true;
    }

    private UnifiedBookingDto toUnifiedFlightDto(FlightBooking fb) {
        UnifiedBookingDto dto = new UnifiedBookingDto();
        dto.setBookingCode(fb.getBookingCode());
        dto.setTicketType(TicketType.FLIGHT);
        dto.setCustomerName(fb.getCustomerName());
        dto.setCustomerPhone(fb.getCustomerPhone());
        if (fb.getFlight() != null) {
            dto.setRoute(fb.getFlight().getFlightNumber() + " (" + fb.getFlight().getDepartureAirport() + " -> " + fb.getFlight().getArrivalAirport() + ")");
            dto.setDepartureDate(fb.getFlight().getDepartureTime().toLocalDate());
        }
        dto.setBookingTime(fb.getBookingTime());
        dto.setTicketCount(fb.getPassengerCount());
        dto.setTotalAmount(fb.getTotalAmount());
        dto.setRefundAmount(fb.getRefundAmount() != null ? fb.getRefundAmount() : BigDecimal.ZERO);
        dto.setStatusCode(fb.getStatus().name());
        dto.setStatusDisplayName(fb.getStatus().getDisplayName());
        return dto;
    }

    private UnifiedBookingDto toUnifiedTrainDto(TrainBooking tb) {
        UnifiedBookingDto dto = new UnifiedBookingDto();
        dto.setBookingCode(tb.getBookingCode());
        dto.setTicketType(TicketType.TRAIN);
        dto.setCustomerName(tb.getCustomerName());
        dto.setCustomerPhone(tb.getCustomerPhone());
        String depSt = tb.getDepartureStation() != null ? tb.getDepartureStation().getStationName() : "Ga đi";
        String arrSt = tb.getArrivalStation() != null ? tb.getArrivalStation().getStationName() : "Ga đến";
        String trainCode = tb.getTrain() != null ? tb.getTrain().getTrainCode() : "Tàu";
        if (tb.isRoundTrip()) {
            String retTrainCode = tb.getReturnTrain() != null ? tb.getReturnTrain().getTrainCode() : "";
            dto.setRoute(trainCode + (retTrainCode.isEmpty() ? "" : " & " + retTrainCode) + " (" + depSt + " <-> " + arrSt + ") [Khứ hồi]");
        } else {
            dto.setRoute(trainCode + " (" + depSt + " -> " + arrSt + ")");
        }
        dto.setDepartureDate(tb.getDepartureDate());
        dto.setBookingTime(tb.getBookingTime());
        dto.setTicketCount(tb.getTicketCount());
        dto.setTotalAmount(tb.getTotalAmount());
        dto.setRefundAmount(tb.getRefundAmount() != null ? tb.getRefundAmount() : BigDecimal.ZERO);
        dto.setStatusCode(tb.getStatus().name());
        dto.setStatusDisplayName(tb.getStatus().getDisplayName());
        return dto;
    }

    AdminStatisticsDto computeStatistics(List<FlightBooking> flights, List<TrainBooking> trains, List<Consultation> consultations) {
        AdminStatisticsDto stats = new AdminStatisticsDto();

        int flightCount = 0;
        BigDecimal flightRev = BigDecimal.ZERO;
        for (FlightBooking fb : flights) {
            if (fb.getStatus() == FlightBookingStatus.CONFIRMED 
                    || fb.getStatus() == FlightBookingStatus.ISSUED 
                    || fb.getStatus() == FlightBookingStatus.CHANGED) {
                flightCount += fb.getPassengerCount();
                flightRev = flightRev.add(fb.getTotalAmount());
            }
        }

        int trainCount = 0;
        BigDecimal trainRev = BigDecimal.ZERO;
        for (TrainBooking tb : trains) {
            if (tb.getStatus() == TrainBookingStatus.CONFIRMED 
                    || tb.getStatus() == TrainBookingStatus.ISSUED) {
                trainCount += tb.getTicketCount();
                trainRev = trainRev.add(tb.getTotalAmount());
            }
        }

        stats.setTotalFlightTicketsSold(flightCount);
        stats.setTotalTrainTicketsSold(trainCount);
        stats.setTotalTicketsSold(flightCount + trainCount);

        stats.setFlightRevenue(flightRev);
        stats.setTrainRevenue(trainRev);
        stats.setTotalRevenue(flightRev.add(trainRev));

        stats.setTotalConsultations(consultations.size());
        int pendingConsult = (int) consultations.stream()
                .filter(c -> c.getStatus() == ConsultationStatus.PENDING)
                .count();
        stats.setPendingConsultations(pendingConsult);

        // Doanh thu theo tháng (Month key: MM/yyyy)
        Map<String, BigDecimal> monthly = new LinkedHashMap<>();
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MM/yyyy");

        for (FlightBooking fb : flights) {
            if (fb.getStatus() == FlightBookingStatus.CONFIRMED 
                    || fb.getStatus() == FlightBookingStatus.ISSUED 
                    || fb.getStatus() == FlightBookingStatus.CHANGED) {
                LocalDateTime time = fb.getConfirmedTime() != null ? fb.getConfirmedTime() : fb.getBookingTime();
                if (time != null) {
                    String m = time.format(monthFmt);
                    monthly.put(m, monthly.getOrDefault(m, BigDecimal.ZERO).add(fb.getTotalAmount()));
                }
            }
        }
        for (TrainBooking tb : trains) {
            if (tb.getStatus() == TrainBookingStatus.CONFIRMED 
                    || tb.getStatus() == TrainBookingStatus.ISSUED) {
                LocalDateTime time = tb.getConfirmedTime() != null ? tb.getConfirmedTime() : tb.getBookingTime();
                if (time != null) {
                    String m = time.format(monthFmt);
                    monthly.put(m, monthly.getOrDefault(m, BigDecimal.ZERO).add(tb.getTotalAmount()));
                }
            }
        }
        stats.setRevenueByMonth(monthly);

        return stats;
    }

    // =========================================================================
    // 2. THAO TÁC XỬ LÝ ĐƠN VÉ (XÁC NHẬN / HỦY VÉ / HOÀN TIỀN)
    // =========================================================================
    @PostMapping("/bookings/flight/{code}/confirm")
    public String confirmFlightByAdmin(@PathVariable("code") String code, RedirectAttributes redirectAttributes) {
        try {
            flightTicketService.confirmAndIssueTicket(code);
            redirectAttributes.addFlashAttribute("successMessage", "Xác nhận và phát hành vé máy bay thành công cho mã đơn: " + code);
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/bookings/flight/{code}/cancel")
    public String cancelFlightByAdmin(@PathVariable("code") String code,
                                      @RequestParam(value = "reason", defaultValue = "Nhân viên/Admin xử lý hủy") String reason,
                                      RedirectAttributes redirectAttributes) {
        try {
            FlightBooking cancelled = flightTicketService.cancelBooking(code, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn vé máy bay " + code + ". Tiền hoàn lại cho khách: "
                    + String.format("%,.0f VNĐ", cancelled.getRefundAmount()) + " (Phí hủy: " + String.format("%,.0f VNĐ", cancelled.getCancellationFee()) + ")");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/bookings/train/{code}/confirm")
    public String confirmTrainByAdmin(@PathVariable("code") String code, RedirectAttributes redirectAttributes) {
        try {
            trainTicketService.confirmAndIssueTicket(code);
            redirectAttributes.addFlashAttribute("successMessage", "Xác nhận và phát hành vé tàu hỏa thành công cho mã đơn: " + code);
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/bookings/train/{code}/cancel")
    public String cancelTrainByAdmin(@PathVariable("code") String code,
                                     @RequestParam(value = "reason", defaultValue = "Nhân viên/Admin xử lý hủy") String reason,
                                     RedirectAttributes redirectAttributes) {
        try {
            TrainBooking cancelled = trainTicketService.cancelBooking(code, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn vé tàu " + code + ". Tiền hoàn lại cho khách: "
                    + String.format("%,.0f VNĐ", cancelled.getRefundAmount()) + " (Phí hủy: " + String.format("%,.0f VNĐ", cancelled.getCancellationFee()) + ")");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin";
    }

    // =========================================================================
    // 3. QUẢN LÝ LỊCH HẸN TƯ VẤN (XÁC NHẬN, ĐỔI GIỜ, ĐÁNH DẤU HOÀN THÀNH, HỦY)
    // =========================================================================
    @PostMapping("/consultations/{id}/confirm")
    public String confirmConsultation(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        consultationService.updateStatus(id, ConsultationStatus.CONFIRMED);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận lịch hẹn tư vấn #" + id);
        return "redirect:/admin";
    }

    @PostMapping("/consultations/{id}/complete")
    public String completeConsultation(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        consultationService.updateStatus(id, ConsultationStatus.COMPLETED);
        redirectAttributes.addFlashAttribute("successMessage", "Đã đánh dấu hoàn thành tư vấn cho lịch hẹn #" + id);
        return "redirect:/admin";
    }

    @PostMapping("/consultations/{id}/cancel")
    public String cancelConsultation(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        consultationService.updateStatus(id, ConsultationStatus.CANCELLED);
        redirectAttributes.addFlashAttribute("successMessage", "Đã hủy lịch hẹn tư vấn #" + id);
        return "redirect:/admin";
    }

    @PostMapping("/consultations/{id}/reschedule")
    public String rescheduleConsultation(@PathVariable("id") Long id,
                                         @RequestParam("newDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newDateTime,
                                         RedirectAttributes redirectAttributes) {
        try {
            consultationService.reschedule(id, newDateTime);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đổi giờ lịch hẹn #" + id + " thành công sang: " + newDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể đổi lịch hẹn: " + ex.getMessage());
        }
        return "redirect:/admin";
    }

    // =========================================================================
    // 4. ADMIN ONLY: QUẢN LÝ CHUYẾN BAY, TÀU, GA, NGƯỜI DÙNG
    // =========================================================================
    @GetMapping("/flights")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageFlights(Model model) {
        model.addAttribute("flights", flightRepository.findAll());
        model.addAttribute("newFlight", new Flight());
        return "admin/manage_flights";
    }

    @PostMapping("/flights")
    @PreAuthorize("hasRole('ADMIN')")
    public String addFlight(@ModelAttribute("newFlight") Flight flight, RedirectAttributes redirectAttributes) {
        flight.setStatus("ACTIVE");
        flightRepository.save(flight);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm chuyến bay mới " + flight.getFlightNumber() + " thành công!");
        return "redirect:/admin/flights";
    }

    @GetMapping("/trains")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageTrains(Model model) {
        model.addAttribute("trains", trainRepository.findAll());
        model.addAttribute("newTrain", new Train());
        return "admin/manage_trains";
    }

    @PostMapping("/trains")
    @PreAuthorize("hasRole('ADMIN')")
    public String addTrain(@ModelAttribute("newTrain") Train train, RedirectAttributes redirectAttributes) {
        train.setStatus("ACTIVE");
        trainRepository.save(train);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm đoàn tàu mới " + train.getTrainCode() + " thành công!");
        return "redirect:/admin/trains";
    }

    @GetMapping("/stations")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageStations(Model model) {
        model.addAttribute("stations", trainStationRepository.findAllByOrderByStationIndexAsc());
        model.addAttribute("newStation", new TrainStation());
        return "admin/manage_stations";
    }

    @PostMapping("/stations")
    @PreAuthorize("hasRole('ADMIN')")
    public String addStation(@ModelAttribute("newStation") TrainStation station, RedirectAttributes redirectAttributes) {
        trainStationRepository.save(station);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm ga mới " + station.getStationName() + " thành công!");
        return "redirect:/admin/stations";
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("newUser", new User());
        model.addAttribute("roles", UserRole.values());
        return "admin/manage_users";
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String addUser(@ModelAttribute("newUser") User user, RedirectAttributes redirectAttributes) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên đăng nhập không được để trống!");
            return "redirect:/admin/users";
        }
        if (userRepository.existsByUsername(user.getUsername().trim())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên đăng nhập " + user.getUsername() + " đã tồn tại!");
            return "redirect:/admin/users";
        }
        if (user.getEmail() != null && !user.getEmail().isBlank() && userRepository.existsByEmail(user.getEmail().trim())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email " + user.getEmail() + " đã được sử dụng bởi tài khoản khác!");
            return "redirect:/admin/users";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(true);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Tạo tài khoản " + user.getUsername() + " (" + user.getRole().getDisplayName() + ") thành công!");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleUserStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        userRepository.findById(id).ifPresent(u -> {
            u.setStatus(u.getStatus() == null || !u.getStatus());
            userRepository.save(u);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái người dùng " + u.getUsername() + " thành: " + (u.getStatus() ? "Hoạt động" : "Bị khóa"));
        });
        return "redirect:/admin/users";
    }
}
