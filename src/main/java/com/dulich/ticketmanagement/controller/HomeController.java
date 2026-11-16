package com.dulich.ticketmanagement.controller;

import com.dulich.ticketmanagement.dto.SystemStatsDto;
import com.dulich.ticketmanagement.dto.TicketSearchDto;
import com.dulich.ticketmanagement.entity.enums.TransportType;
import com.dulich.ticketmanagement.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.sql.DataSource;
import java.sql.Connection;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final TransDetailRepository transDetailRepository;
    private final AppointmentRepository appointmentRepository;
    private final DataSource dataSource;

    @Autowired
    public HomeController(UserRepository userRepository,
                          ReservationRepository reservationRepository,
                          TransDetailRepository transDetailRepository,
                          AppointmentRepository appointmentRepository,
                          DataSource dataSource) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.transDetailRepository = transDetailRepository;
        this.appointmentRepository = appointmentRepository;
        this.dataSource = dataSource;
    }

    @GetMapping("/")
    public String index(Model model) {
        SystemStatsDto stats = new SystemStatsDto();

        try (Connection conn = dataSource.getConnection()) {
            stats.setDatabaseConnected(true);
            stats.setDatabaseUrl(conn.getMetaData().getURL());
            stats.setDatabaseName(conn.getCatalog());
            stats.setUserCount(userRepository.count());
            stats.setCustomerCount(reservationRepository.count());
            stats.setBookingCount(reservationRepository.count());
            stats.setPaymentCount(transDetailRepository.count());
            stats.setConsultationCount(appointmentRepository.count());
        } catch (Exception ex) {
            log.error("Không thể kết nối CSDL khi tải trang chủ: {}", ex.getMessage());
            stats.setDatabaseConnected(false);
            stats.setDatabaseUrl("Chưa thể kết nối cơ sở dữ liệu.");
        }

        model.addAttribute("stats", stats);
        model.addAttribute("searchDto", new TicketSearchDto());
        model.addAttribute("transportTypes", TransportType.values());
        model.addAttribute("appName", "Hệ Thống Quản Lý Đặt Vé Du Lịch");
        model.addAttribute("appVersion", "2.0.0-ENTERPRISE");
        model.addAttribute("javaVersion", System.getProperty("java.version"));

        return "home";
    }

    @GetMapping("/policies")
    public String policies() {
        return "policy/policies";
    }
}
