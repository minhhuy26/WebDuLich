package com.dulich.ticketmanagement.controller;

import com.dulich.ticketmanagement.dto.SystemStatsDto;
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
    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;
    private final FlightBookingRepository flightBookingRepository;
    private final TrainBookingRepository trainBookingRepository;
    private final TrainStationRepository trainStationRepository;
    private final TrainRepository trainRepository;
    private final PaymentRepository paymentRepository;
    private final ConsultationRepository consultationRepository;
    private final DataSource dataSource;

    @Autowired
    public HomeController(UserRepository userRepository,
                          CustomerRepository customerRepository,
                          BookingRepository bookingRepository,
                          FlightBookingRepository flightBookingRepository,
                          TrainBookingRepository trainBookingRepository,
                          TrainStationRepository trainStationRepository,
                          TrainRepository trainRepository,
                          PaymentRepository paymentRepository,
                          ConsultationRepository consultationRepository,
                          DataSource dataSource) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.bookingRepository = bookingRepository;
        this.flightBookingRepository = flightBookingRepository;
        this.trainBookingRepository = trainBookingRepository;
        this.trainStationRepository = trainStationRepository;
        this.trainRepository = trainRepository;
        this.paymentRepository = paymentRepository;
        this.consultationRepository = consultationRepository;
        this.dataSource = dataSource;
    }

    @GetMapping("/")
    public String index(Model model) {
        SystemStatsDto stats = new SystemStatsDto();

        // Kiểm tra kết nối SQL Server
        try (Connection conn = dataSource.getConnection()) {
            stats.setDatabaseConnected(true);
            stats.setDatabaseUrl(conn.getMetaData().getURL());
            stats.setDatabaseName(conn.getCatalog());
            stats.setUserCount(userRepository.count());
            stats.setCustomerCount(customerRepository.count());
            long totalBookings = flightBookingRepository.count() + trainBookingRepository.count() + bookingRepository.count();
            stats.setBookingCount(totalBookings);
            stats.setPaymentCount(paymentRepository.count());
            stats.setConsultationCount(consultationRepository.count());
        } catch (Exception ex) {
            log.error("Không thể kết nối CSDL khi tải trang chủ: {}", ex.getMessage());
            stats.setDatabaseConnected(false);
            // Never place connection details or driver error messages in a public model.
            stats.setDatabaseUrl("Chưa thể kết nối cơ sở dữ liệu.");
        }

        model.addAttribute("stats", stats);
        model.addAttribute("appName", "Hệ Thống Quản Lý Đặt Vé Du Lịch");
        model.addAttribute("appVersion", "1.0.0-SNAPSHOT");
        model.addAttribute("javaVersion", System.getProperty("java.version"));

        // Lấy danh sách mẫu mới nhất để hiển thị trực quan
        try {
            model.addAttribute("stations", trainStationRepository.findAllByOrderByStationIndexAsc());
            model.addAttribute("trains", trainRepository.findByStatus("ACTIVE"));
            model.addAttribute("recentBookings", bookingRepository.findAllWithDetails());
            model.addAttribute("recentConsultations", consultationRepository.findAll());
        } catch (Exception e) {
            // Trường hợp DB chưa tạo bảng hoặc rỗng
            model.addAttribute("stations", java.util.Collections.emptyList());
            model.addAttribute("trains", java.util.Collections.emptyList());
            model.addAttribute("recentBookings", java.util.Collections.emptyList());
            model.addAttribute("recentConsultations", java.util.Collections.emptyList());
        }

        return "home";
    }
}
