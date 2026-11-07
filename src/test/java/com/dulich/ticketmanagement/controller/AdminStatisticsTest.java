package com.dulich.ticketmanagement.controller;

import com.dulich.ticketmanagement.dto.AdminStatisticsDto;
import com.dulich.ticketmanagement.entity.FlightBooking;
import com.dulich.ticketmanagement.entity.TrainBooking;
import com.dulich.ticketmanagement.entity.User;
import com.dulich.ticketmanagement.entity.enums.FlightBookingStatus;
import com.dulich.ticketmanagement.entity.enums.TrainBookingStatus;
import com.dulich.ticketmanagement.entity.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử thống kê doanh thu Admin Dashboard (Bug 13)")
class AdminStatisticsTest {

    private AdminController adminController;
    private com.dulich.ticketmanagement.repository.UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(com.dulich.ticketmanagement.repository.UserRepository.class);
        adminController = new AdminController(
                Mockito.mock(com.dulich.ticketmanagement.repository.FlightBookingRepository.class),
                Mockito.mock(com.dulich.ticketmanagement.repository.TrainBookingRepository.class),
                Mockito.mock(com.dulich.ticketmanagement.service.impl.FlightTicketService.class),
                Mockito.mock(com.dulich.ticketmanagement.service.impl.TrainTicketService.class),
                Mockito.mock(com.dulich.ticketmanagement.service.ConsultationService.class),
                Mockito.mock(com.dulich.ticketmanagement.repository.FlightRepository.class),
                Mockito.mock(com.dulich.ticketmanagement.repository.TrainRepository.class),
                Mockito.mock(com.dulich.ticketmanagement.repository.TrainStationRepository.class),
                userRepository,
                Mockito.mock(org.springframework.security.crypto.password.PasswordEncoder.class)
        );
    }

    @Test
    @DisplayName("Bug 13 Test: Thống kê doanh thu phải tính cả đơn vé ISSUED và CHANGED, không chỉ CONFIRMED")
    void testBug13_ComputeStatistics_IncludesIssuedAndChangedBookings() {
        LocalDateTime now = LocalDateTime.now();
        String currentMonth = now.format(DateTimeFormatter.ofPattern("MM/yyyy"));

        // Vé máy bay phát hành thành công (ISSUED)
        FlightBooking fbIssued = new FlightBooking();
        fbIssued.setStatus(FlightBookingStatus.ISSUED);
        fbIssued.setPassengerCount(2);
        fbIssued.setTotalAmount(new BigDecimal("3000000"));
        fbIssued.setConfirmedTime(now);

        // Vé máy bay đã đổi chuyến (CHANGED)
        FlightBooking fbChanged = new FlightBooking();
        fbChanged.setStatus(FlightBookingStatus.CHANGED);
        fbChanged.setPassengerCount(1);
        fbChanged.setTotalAmount(new BigDecimal("1500000"));
        fbChanged.setConfirmedTime(now);

        // Vé máy bay đã xác nhận (CONFIRMED)
        FlightBooking fbConfirmed = new FlightBooking();
        fbConfirmed.setStatus(FlightBookingStatus.CONFIRMED);
        fbConfirmed.setPassengerCount(1);
        fbConfirmed.setTotalAmount(new BigDecimal("1000000"));
        fbConfirmed.setConfirmedTime(now);

        // Vé tàu hỏa đã phát hành (ISSUED)
        TrainBooking tbIssued = new TrainBooking();
        tbIssued.setStatus(TrainBookingStatus.ISSUED);
        tbIssued.setTicketCount(3);
        tbIssued.setTotalAmount(new BigDecimal("2000000"));
        tbIssued.setConfirmedTime(now);

        // Vé tàu hỏa đã xác nhận (CONFIRMED)
        TrainBooking tbConfirmed = new TrainBooking();
        tbConfirmed.setStatus(TrainBookingStatus.CONFIRMED);
        tbConfirmed.setTicketCount(2);
        tbConfirmed.setTotalAmount(new BigDecimal("1200000"));
        tbConfirmed.setConfirmedTime(now);

        AdminStatisticsDto stats = adminController.computeStatistics(
                List.of(fbIssued, fbChanged, fbConfirmed),
                List.of(tbIssued, tbConfirmed),
                Collections.emptyList()
        );

        // Tổng vé bay bán được: 2 (ISSUED) + 1 (CHANGED) + 1 (CONFIRMED) = 4
        assertEquals(4, stats.getTotalFlightTicketsSold(), "Tổng số vé bay phải tính cả ISSUED và CHANGED");
        // Doanh thu vé bay: 3tr + 1.5tr + 1tr = 5.5tr
        assertEquals(0, stats.getFlightRevenue().compareTo(new BigDecimal("5500000")), "Doanh thu bay phải gồm cả ISSUED và CHANGED");

        // Tổng vé tàu bán được: 3 (ISSUED) + 2 (CONFIRMED) = 5
        assertEquals(5, stats.getTotalTrainTicketsSold(), "Tổng số vé tàu phải tính cả ISSUED");
        // Doanh thu vé tàu: 2tr + 1.2tr = 3.2tr
        assertEquals(0, stats.getTrainRevenue().compareTo(new BigDecimal("3200000")), "Doanh thu tàu phải gồm cả ISSUED");

        // Tổng vé và tổng doanh thu
        assertEquals(9, stats.getTotalTicketsSold());
        assertEquals(0, stats.getTotalRevenue().compareTo(new BigDecimal("8700000")));

        // Doanh thu theo tháng
        assertTrue(stats.getRevenueByMonth().containsKey(currentMonth));
        assertEquals(0, stats.getRevenueByMonth().get(currentMonth).compareTo(new BigDecimal("8700000")), "Doanh thu tháng phải bao gồm các đơn ISSUED và CHANGED");
    }

    @Test
    @DisplayName("Bug 19 Test: Tạo tài khoản trùng Email phải báo lỗi rõ ràng thay vì để Hibernate ném lỗi 500")
    void testBug19_AddUser_DuplicateEmail_ShowsErrorMessage() {
        User user = new User();
        user.setUsername("newuser");
        user.setEmail("admin@dulich.com");
        user.setPassword("123456");
        user.setRole(UserRole.NHANVIEN);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("admin@dulich.com")).thenReturn(true);

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String view = adminController.addUser(user, redirectAttributes);

        assertEquals("redirect:/admin/users", view);
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("errorMessage"),
                "Phải có flash errorMessage khi email đã tồn tại");
        assertTrue(redirectAttributes.getFlashAttributes().get("errorMessage").toString().contains("Email"));
        verify(userRepository, never()).save(any(User.class));
    }
}
