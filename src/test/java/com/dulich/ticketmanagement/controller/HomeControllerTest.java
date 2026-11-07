package com.dulich.ticketmanagement.controller;

import com.dulich.ticketmanagement.dto.SystemStatsDto;
import com.dulich.ticketmanagement.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("Kiểm thử thống kê trang chủ HomeController (Bug 22)")
class HomeControllerTest {

    private HomeController homeController;
    private UserRepository userRepository;
    private CustomerRepository customerRepository;
    private BookingRepository bookingRepository;
    private FlightBookingRepository flightBookingRepository;
    private TrainBookingRepository trainBookingRepository;
    private TrainStationRepository trainStationRepository;
    private TrainRepository trainRepository;
    private PaymentRepository paymentRepository;
    private ConsultationRepository consultationRepository;
    private DataSource dataSource;
    private Connection connection;
    private DatabaseMetaData metaData;

    @BeforeEach
    void setUp() throws SQLException {
        userRepository = Mockito.mock(UserRepository.class);
        customerRepository = Mockito.mock(CustomerRepository.class);
        bookingRepository = Mockito.mock(BookingRepository.class);
        flightBookingRepository = Mockito.mock(FlightBookingRepository.class);
        trainBookingRepository = Mockito.mock(TrainBookingRepository.class);
        trainStationRepository = Mockito.mock(TrainStationRepository.class);
        trainRepository = Mockito.mock(TrainRepository.class);
        paymentRepository = Mockito.mock(PaymentRepository.class);
        consultationRepository = Mockito.mock(ConsultationRepository.class);
        dataSource = Mockito.mock(DataSource.class);
        connection = Mockito.mock(Connection.class);
        metaData = Mockito.mock(DatabaseMetaData.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getURL()).thenReturn("jdbc:sqlserver://localhost:1433");
        when(connection.getCatalog()).thenReturn("DatVeDuLich");

        homeController = new HomeController(
                userRepository,
                customerRepository,
                bookingRepository,
                flightBookingRepository,
                trainBookingRepository,
                trainStationRepository,
                trainRepository,
                paymentRepository,
                consultationRepository,
                dataSource
        );
    }

    @Test
    @DisplayName("Bug 22 Test: Thống kê số lượng đơn đặt vé trên trang chủ phải tính tổng đơn vé máy bay và vé tàu hỏa")
    void testBug22_Index_BookingCountIncludesFlightAndTrainBookings() {
        when(userRepository.count()).thenReturn(5L);
        when(customerRepository.count()).thenReturn(10L);
        when(bookingRepository.count()).thenReturn(0L); // Bảng cũ rỗng
        when(flightBookingRepository.count()).thenReturn(15L); // 15 đơn vé máy bay
        when(trainBookingRepository.count()).thenReturn(10L); // 10 đơn vé tàu hỏa
        when(paymentRepository.count()).thenReturn(20L);
        when(consultationRepository.count()).thenReturn(8L);

        Model model = new ExtendedModelMap();
        String view = homeController.index(model);

        assertEquals("home", view);
        SystemStatsDto stats = (SystemStatsDto) model.getAttribute("stats");
        assertNotNull(stats);
        assertEquals(25L, stats.getBookingCount(), "Số đơn trên trang chủ phải tính tổng vé máy bay (15) + vé tàu (10)");
    }
}
