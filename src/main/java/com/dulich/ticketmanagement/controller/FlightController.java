package com.dulich.ticketmanagement.controller;

import com.dulich.ticketmanagement.dto.ChangeFlightRequestDto;
import com.dulich.ticketmanagement.dto.FlightBookingRequestDto;
import com.dulich.ticketmanagement.dto.FlightSearchDto;
import com.dulich.ticketmanagement.dto.PassengerInputDto;
import com.dulich.ticketmanagement.entity.Flight;
import com.dulich.ticketmanagement.entity.FlightBooking;
import com.dulich.ticketmanagement.entity.User;
import com.dulich.ticketmanagement.entity.enums.FlightSeatClass;
import com.dulich.ticketmanagement.entity.enums.PassengerType;
import com.dulich.ticketmanagement.exception.BusinessException;
import com.dulich.ticketmanagement.repository.FlightBookingRepository;
import com.dulich.ticketmanagement.repository.FlightRepository;
import com.dulich.ticketmanagement.repository.UserRepository;
import com.dulich.ticketmanagement.service.EmailService;
import com.dulich.ticketmanagement.service.impl.FlightTicketService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/flights")
public class FlightController {

    private final FlightTicketService flightTicketService;
    private final FlightRepository flightRepository;
    private final FlightBookingRepository flightBookingRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Autowired
    public FlightController(FlightTicketService flightTicketService,
                            FlightRepository flightRepository,
                            FlightBookingRepository flightBookingRepository,
                            UserRepository userRepository,
                            EmailService emailService) {
        this.flightTicketService = flightTicketService;
        this.flightRepository = flightRepository;
        this.flightBookingRepository = flightBookingRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    private void validateBookingAccess(FlightBooking booking, String inputPhone, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() 
                && !(auth instanceof AnonymousAuthenticationToken);

        if (isAuthenticated) {
            boolean isStaffOrAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_NHANVIEN"));
            if (isStaffOrAdmin) {
                return;
            }

            User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
            if (currentUser != null && currentUser.getPhone() != null 
                    && currentUser.getPhone().trim().equals(booking.getCustomerPhone().trim())) {
                return;
            }

            throw new BusinessException("Bạn không có quyền thực hiện thao tác trên đơn vé này!");
        }

        // Khách vãng lai: Được phép nếu session đã tạo đơn này, hoặc xác thực đúng SĐT đặt vé
        String sessionBookingKey = "BOOKING_AUTH_" + booking.getBookingCode();
        if (Boolean.TRUE.equals(session.getAttribute(sessionBookingKey))) {
            return;
        }

        if (inputPhone != null && inputPhone.trim().equals(booking.getCustomerPhone().trim())) {
            session.setAttribute(sessionBookingKey, true);
            return;
        }

        throw new BusinessException("Vui lòng cung cấp đúng số điện thoại đặt vé để xác thực quyền thao tác!");
    }

    // 1. TÌM KIẾM CHUYẾN BAY (HỖ TRỢ MỘT CHIỀU & KHỨ HỒI)
    @GetMapping
    public String listFlights(@ModelAttribute("searchDto") FlightSearchDto searchDto, Model model) {
        List<Flight> flights = flightTicketService.searchFlights(searchDto);
        model.addAttribute("flights", flights);
        if (searchDto.isRoundTrip()) {
            List<Flight> returnFlights = flightTicketService.searchReturnFlights(searchDto);
            model.addAttribute("returnFlights", returnFlights);
        } else {
            model.addAttribute("returnFlights", Collections.emptyList());
        }
        model.addAttribute("seatClasses", FlightSeatClass.values());
        model.addAttribute("searchDto", searchDto);
        return "flight/flight_list";
    }

    // 2. FORM NHẬP THÔNG TIN HÀNH KHÁCH & ĐẶT GIỮ CHỖ
    @GetMapping("/{id}/book")
    public String bookFlightForm(@PathVariable("id") Long flightId,
                                 @RequestParam(value = "seatClass", defaultValue = "ECONOMY") FlightSeatClass seatClass,
                                 @RequestParam(value = "returnFlightId", required = false) Long returnFlightId,
                                 @RequestParam(value = "returnSeatClass", required = false) FlightSeatClass returnSeatClass,
                                 @RequestParam(value = "passengerCount", defaultValue = "1") int passengerCount,
                                 Model model) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy chuyến bay ID: " + flightId));

        Flight returnFlight = null;
        if (returnFlightId != null) {
            returnFlight = flightRepository.findById(returnFlightId)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy chuyến bay về ID: " + returnFlightId));
        }

        if (passengerCount < 1) passengerCount = 1;
        if (passengerCount > 9) passengerCount = 9;

        FlightBookingRequestDto requestDto = new FlightBookingRequestDto();
        requestDto.setFlightId(flightId);
        requestDto.setSeatClass(seatClass);
        if (returnFlight != null) {
            requestDto.setReturnFlightId(returnFlightId);
            requestDto.setRoundTrip(true);
            requestDto.setReturnSeatClass(returnSeatClass != null ? returnSeatClass : seatClass);
        }

        // Ưu đãi thành viên: Tự động lưu sẵn thông tin cho khách đã đăng nhập (đỡ cần điền lại)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
            if (currentUser != null) {
                requestDto.setCustomerName(currentUser.getFullName());
                requestDto.setCustomerPhone(currentUser.getPhone());
                requestDto.setCustomerEmail(currentUser.getEmail());
                model.addAttribute("isMember", true);
                model.addAttribute("memberUser", currentUser);
            }
        }

        List<PassengerInputDto> passengers = new ArrayList<>();
        for (int i = 0; i < passengerCount; i++) {
            PassengerInputDto p = new PassengerInputDto();
            p.setPassengerType(PassengerType.ADULT);
            p.setBaggageKg(0);
            if (i == 0 && requestDto.getCustomerName() != null) {
                p.setFullName(requestDto.getCustomerName());
            }
            passengers.add(p);
        }
        requestDto.setPassengers(passengers);

        Set<String> occupiedSeats = flightTicketService.getOccupiedSeats(flightId);
        Set<String> occupiedReturnSeats = returnFlight != null ? flightTicketService.getOccupiedSeats(returnFlightId) : Collections.emptySet();

        model.addAttribute("flight", flight);
        model.addAttribute("returnFlight", returnFlight);
        model.addAttribute("isRoundTrip", returnFlight != null);
        model.addAttribute("bookingRequest", requestDto);
        model.addAttribute("seatClasses", FlightSeatClass.values());
        model.addAttribute("passengerTypes", PassengerType.values());
        model.addAttribute("occupiedSeats", occupiedSeats);
        model.addAttribute("occupiedReturnSeats", occupiedReturnSeats);

        return "flight/flight_booking_form";
    }

    // 3. XỬ LÝ ĐẶT GIỮ CHỖ (HOLD RESERVATION)
    @PostMapping("/hold")
    public String holdBooking(@Valid @ModelAttribute("bookingRequest") FlightBookingRequestDto bookingRequest,
                              BindingResult bindingResult,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            Flight flight = flightRepository.findById(bookingRequest.getFlightId()).orElse(null);
            Flight returnFlight = bookingRequest.getReturnFlightId() != null ? flightRepository.findById(bookingRequest.getReturnFlightId()).orElse(null) : null;
            model.addAttribute("flight", flight);
            model.addAttribute("returnFlight", returnFlight);
            model.addAttribute("isRoundTrip", returnFlight != null);
            model.addAttribute("seatClasses", FlightSeatClass.values());
            model.addAttribute("passengerTypes", PassengerType.values());
            model.addAttribute("occupiedSeats", flightTicketService.getOccupiedSeats(bookingRequest.getFlightId()));
            model.addAttribute("occupiedReturnSeats", bookingRequest.getReturnFlightId() != null ? flightTicketService.getOccupiedSeats(bookingRequest.getReturnFlightId()) : Collections.emptySet());
            return "flight/flight_booking_form";
        }

        try {
            FlightBooking booking = flightTicketService.createHoldBooking(bookingRequest);
            session.setAttribute("BOOKING_AUTH_" + booking.getBookingCode(), true);

            // Lưu vào danh sách đơn của phiên (session) để khách vãng lai xem lại đơn đã đặt
            @SuppressWarnings("unchecked")
            List<String> guestFlightBookings = (List<String>) session.getAttribute("GUEST_FLIGHT_BOOKINGS");
            if (guestFlightBookings == null) {
                guestFlightBookings = new ArrayList<>();
            }
            if (!guestFlightBookings.contains(booking.getBookingCode())) {
                guestFlightBookings.add(booking.getBookingCode());
            }
            session.setAttribute("GUEST_FLIGHT_BOOKINGS", guestFlightBookings);

            emailService.sendFlightBookingHeldEmail(booking);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Giữ chỗ thành công! Thông tin vé đã được chuyển về Gmail: " + booking.getCustomerEmail() + ". Chỗ của bạn sẽ được giữ trong 15 phút.");
            return "redirect:/flights/booking/" + booking.getBookingCode();
        } catch (BusinessException ex) {
            Flight flight = flightRepository.findById(bookingRequest.getFlightId()).orElse(null);
            Flight returnFlight = bookingRequest.getReturnFlightId() != null ? flightRepository.findById(bookingRequest.getReturnFlightId()).orElse(null) : null;
            model.addAttribute("flight", flight);
            model.addAttribute("returnFlight", returnFlight);
            model.addAttribute("isRoundTrip", returnFlight != null);
            model.addAttribute("seatClasses", FlightSeatClass.values());
            model.addAttribute("passengerTypes", PassengerType.values());
            model.addAttribute("occupiedSeats", flightTicketService.getOccupiedSeats(bookingRequest.getFlightId()));
            model.addAttribute("occupiedReturnSeats", bookingRequest.getReturnFlightId() != null ? flightTicketService.getOccupiedSeats(bookingRequest.getReturnFlightId()) : Collections.emptySet());
            model.addAttribute("errorMessage", ex.getMessage());
            return "flight/flight_booking_form";
        }
    }

    // 4. XEM CHI TIẾT ĐƠN VÉ & ĐỒNG HỒ ĐẾM NGƯỢC GIỮ CHỖ
    @GetMapping("/booking/{code}")
    public String viewBooking(@PathVariable("code") String bookingCode,
                              @RequestParam(value = "phone", required = false) String phone,
                              HttpSession session,
                              Model model) {
        FlightBooking booking = flightBookingRepository.findByBookingCodeWithDetails(bookingCode)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn đặt vé: " + bookingCode));
        try {
            validateBookingAccess(booking, phone, session);
        } catch (BusinessException ex) {
            model.addAttribute("bookingCode", bookingCode);
            model.addAttribute("lookupAction", "/flights/booking/" + bookingCode);
            model.addAttribute("ticketLabel", "vé máy bay");
            return "booking_access";
        }

        model.addAttribute("booking", booking);
        model.addAttribute("now", LocalDateTime.now());
        return "flight/flight_booking_detail";
    }

    // 5. XÁC NHẬN THANH TOÁN & PHÁT HÀNH VÉ ĐIỆN TỬ
    @PostMapping("/booking/{code}/confirm")
    public String confirmBooking(@PathVariable("code") String bookingCode,
                                 @RequestParam(value = "phone", required = false) String phone,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        try {
            FlightBooking booking = flightBookingRepository.findByBookingCodeWithDetails(bookingCode)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy đơn đặt vé: " + bookingCode));
            validateBookingAccess(booking, phone, session);
            FlightBooking confirmed = flightTicketService.confirmAndIssueTicket(bookingCode);
            emailService.sendFlightBookingConfirmedEmail(confirmed);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Xác nhận thanh toán & phát hành vé điện tử thành công cho mã đơn " + booking.getBookingCode() + "! Vé điện tử chính thức đã được gửi về Gmail: " + booking.getCustomerEmail());
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/flights/booking/" + bookingCode;
    }

    // 6. HỦY VÉ VÀ TÍNH TIỀN HOÀN THEO CHÍNH SÁCH
    @PostMapping("/booking/{code}/cancel")
    public String cancelBooking(@PathVariable("code") String bookingCode,
                                @RequestParam(value = "reason", required = false) String reason,
                                @RequestParam(value = "phone", required = false) String phone,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        try {
            FlightBooking booking = flightBookingRepository.findByBookingCodeWithDetails(bookingCode)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy đơn đặt vé: " + bookingCode));
            validateBookingAccess(booking, phone, session);
            FlightBooking cancelledBooking = flightTicketService.cancelBooking(bookingCode, reason);
            emailService.sendFlightBookingCancelledEmail(cancelledBooking);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Hủy vé thành công! Thông báo hủy vé đã được gửi về Gmail: " + booking.getCustomerEmail() +
                    " - Tiền hoàn lại: " + String.format("%,.0f VNĐ", cancelledBooking.getRefundAmount()));
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/flights/booking/" + bookingCode;
    }

    // 7. FORM ĐỔI CHUYẾN BAY
    @GetMapping("/booking/{code}/change")
    public String changeFlightForm(@PathVariable("code") String bookingCode,
                                   @RequestParam(value = "phone", required = false) String phone,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        FlightBooking booking = flightBookingRepository.findByBookingCodeWithDetails(bookingCode)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn đặt vé: " + bookingCode));

        try {
            validateBookingAccess(booking, phone, session);
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/flights/booking/" + bookingCode;
        }

        List<Flight> availableFlights = flightRepository.findUpcomingFlights(LocalDateTime.now().plusHours(2));

        ChangeFlightRequestDto changeRequest = new ChangeFlightRequestDto();
        changeRequest.setNewSeatClass(booking.getSeatClass());

        model.addAttribute("booking", booking);
        model.addAttribute("availableFlights", availableFlights);
        model.addAttribute("seatClasses", FlightSeatClass.values());
        model.addAttribute("changeRequest", changeRequest);

        return "flight/flight_change_form";
    }

    // 8. XỬ LÝ ĐỔI CHUYẾN BAY
    @PostMapping("/booking/{code}/change")
    public String processChangeFlight(@PathVariable("code") String bookingCode,
                                      @RequestParam(value = "phone", required = false) String phone,
                                      @ModelAttribute("changeRequest") ChangeFlightRequestDto request,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        try {
            FlightBooking booking = flightBookingRepository.findByBookingCodeWithDetails(bookingCode)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy đơn đặt vé: " + bookingCode));
            validateBookingAccess(booking, phone, session);
            flightTicketService.changeFlight(bookingCode, request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đổi chuyến bay thành công! Thông tin vé đã được cập nhật sang chuyến bay mới.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/flights/booking/" + bookingCode + "/change";
        }
        return "redirect:/flights/booking/" + bookingCode;
    }

    // 9. TRA CỨU & QUẢN LÝ ĐƠN ĐẶT VÉ MÁY BAY
    @GetMapping("/bookings")
    public String listAllBookings(@RequestParam(value = "searchKeyword", required = false) String searchKeyword,
                                  Model model,
                                  Authentication auth) {
        boolean isStaffOrAdmin = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)
                && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_NHANVIEN"));

        boolean hasSearched = (searchKeyword != null && !searchKeyword.isBlank());
        List<FlightBooking> bookings;

        if (isStaffOrAdmin) {
            // Quản lý / Nhân viên: Xem toàn bộ đơn hệ thống hoặc lọc theo từ khóa
            if (hasSearched) {
                String kw = searchKeyword.trim().toLowerCase();
                bookings = flightBookingRepository.findAllWithFlight().stream()
                        .filter(b -> (b.getBookingCode() != null && b.getBookingCode().toLowerCase().contains(kw)) ||
                                     (b.getCustomerPhone() != null && b.getCustomerPhone().contains(kw)) ||
                                     (b.getCustomerName() != null && b.getCustomerName().toLowerCase().contains(kw)) ||
                                     (b.getFlight() != null && b.getFlight().getFlightNumber() != null && b.getFlight().getFlightNumber().toLowerCase().contains(kw)))
                        .toList();
            } else {
                bookings = flightBookingRepository.findAllWithFlight();
            }
        } else {
            // Khách chưa đăng nhập: Bảo mật thông tin hành khách, BẮT BUỘC phải tra cứu (SĐT hoặc Mã đơn) mới ra đơn của mình
            if (hasSearched) {
                String kw = searchKeyword.trim().toLowerCase();
                bookings = flightBookingRepository.findAllWithFlight().stream()
                        .filter(b -> (b.getBookingCode() != null && b.getBookingCode().toLowerCase().contains(kw)) ||
                                     (b.getCustomerPhone() != null && b.getCustomerPhone().contains(kw)))
                        .toList();
            } else {
                bookings = java.util.Collections.emptyList();
            }
        }

        model.addAttribute("isManager", isStaffOrAdmin);
        model.addAttribute("hasSearched", hasSearched);
        model.addAttribute("bookings", bookings);
        model.addAttribute("searchKeyword", searchKeyword);
        return "flight/flight_booking_list";
    }
}
