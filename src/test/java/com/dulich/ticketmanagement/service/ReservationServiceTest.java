package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.dto.PassengerDto;
import com.dulich.ticketmanagement.dto.ReservationRequestDto;
import com.dulich.ticketmanagement.entity.Reservation;
import com.dulich.ticketmanagement.entity.Ticket;
import com.dulich.ticketmanagement.entity.enums.PassengerType;
import com.dulich.ticketmanagement.entity.enums.ReservationStatus;
import com.dulich.ticketmanagement.entity.enums.TicketStatus;
import com.dulich.ticketmanagement.entity.enums.TransportType;
import com.dulich.ticketmanagement.repository.ReservationRepository;
import com.dulich.ticketmanagement.repository.TicketRepository;
import com.dulich.ticketmanagement.repository.TransDetailRepository;
import com.dulich.ticketmanagement.service.impl.ReservationServiceImpl;
import com.dulich.ticketmanagement.service.strategy.AirTicketStrategy;
import com.dulich.ticketmanagement.service.strategy.TicketStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Nghiệp vụ đặt giữ chỗ và phát hành vé (ReservationService)")
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TransDetailRepository transDetailRepository;

    @Mock
    private TicketStrategyFactory strategyFactory;

    @Mock
    private QrCodeService qrCodeService;

    @Mock
    private EmailService emailService;

    @Mock
    private TicketCatalogService ticketCatalogService;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private AirTicketStrategy airStrategy;

    @BeforeEach
    void setUp() {
        airStrategy = new AirTicketStrategy();
    }

    @Test
    @DisplayName("Đặt giữ chỗ thành công: Chuyển trạng thái vé sang HELD và tính đúng tổng tiền")
    void testCreateHoldReservationSuccess() {
        // Arrange
        Long ticketId = 10L;
        Ticket sampleTicket = new Ticket();
        sampleTicket.setId(ticketId);
        sampleTicket.setTransportType(TransportType.AIR);
        sampleTicket.setVehicleCode("VN210");
        sampleTicket.setVehicleName("Vietnam Airlines");
        sampleTicket.setDeparturePlace("Hà Nội");
        sampleTicket.setArrivalPlace("Sài Gòn");
        sampleTicket.setDepartureTime(LocalDateTime.now().plusDays(2));
        sampleTicket.setBasePrice(new BigDecimal("1500000"));
        sampleTicket.setStatus(TicketStatus.AVAILABLE);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(sampleTicket));
        when(strategyFactory.getStrategy(TransportType.AIR)).thenReturn(airStrategy);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        ReservationRequestDto request = new ReservationRequestDto();
        request.setTicketId(ticketId);
        request.setTransportType(TransportType.AIR);
        request.setVehicleCode("VN210");
        request.setVehicleName("Vietnam Airlines");
        request.setDeparturePlace("Hà Nội");
        request.setArrivalPlace("Sài Gòn");
        request.setDepartureDate(LocalDate.now().plusDays(2));
        request.setSeatClass("ECONOMY");
        request.setCustomerName("Trần Văn An");
        request.setCustomerPhone("0912345678");
        request.setCustomerEmail("an.tran@gmail.com");

        PassengerDto passenger = new PassengerDto();
        passenger.setFullName("Trần Văn An");
        passenger.setIdCard("001200001234");
        passenger.setPassengerType(PassengerType.ADULT);
        passenger.setSeatNumber("12A");
        request.setPassengers(List.of(passenger));

        // Act
        Reservation result = reservationService.createHoldReservation(request, null);

        // Assert
        assertNotNull(result);
        assertEquals(ReservationStatus.CREATED, result.getStatus());
        assertEquals(new BigDecimal("1620000"), result.getTotalAmount());
        assertNotNull(result.getHoldExpiresAt());
        assertEquals(TicketStatus.HELD, sampleTicket.getStatus());
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(emailService, times(1)).sendReservationHeldEmail(any(Reservation.class));
    }

    @Test
    @DisplayName("Đặt giữ chỗ thất bại: Báo lỗi khi vé gốc trong kho đã bị giữ hoặc đã bán")
    void testCreateHoldReservationFailsWhenTicketNotAvailable() {
        Long ticketId = 20L;
        Ticket sampleTicket = new Ticket();
        sampleTicket.setId(ticketId);
        sampleTicket.setStatus(TicketStatus.HELD); // Already held by someone else

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(sampleTicket));

        ReservationRequestDto request = new ReservationRequestDto();
        request.setTicketId(ticketId);
        request.setTransportType(TransportType.AIR);
        request.setCustomerName("Lê Văn C");
        request.setCustomerPhone("0900000000");
        request.setCustomerEmail("c@gmail.com");
        request.setDeparturePlace("Hà Nội");
        request.setArrivalPlace("Sài Gòn");
        request.setDepartureDate(LocalDate.now().plusDays(2));
        request.setPassengers(List.of(new PassengerDto()));

        assertThrows(com.dulich.ticketmanagement.exception.BusinessException.class,
                () -> reservationService.createHoldReservation(request, null));
    }

    @Test
    @DisplayName("Đặt giữ chỗ thất bại: Báo lỗi khi 2 hành khách chọn cùng một vị trí ghế")
    void testCreateHoldReservationFailsWhenDuplicateSeats() {
        Long ticketId = 30L;
        Ticket sampleTicket = new Ticket();
        sampleTicket.setId(ticketId);
        sampleTicket.setStatus(TicketStatus.AVAILABLE);
        sampleTicket.setSeatNumber("12A");
        sampleTicket.setBasePrice(new BigDecimal("1500000"));

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(sampleTicket));
        when(strategyFactory.getStrategy(TransportType.AIR)).thenReturn(airStrategy);

        ReservationRequestDto request = new ReservationRequestDto();
        request.setTicketId(ticketId);
        request.setTransportType(TransportType.AIR);
        request.setVehicleCode("VN210");
        request.setDeparturePlace("Hà Nội");
        request.setArrivalPlace("Sài Gòn");
        request.setDepartureDate(LocalDate.now().plusDays(2));
        request.setCustomerName("Trần Văn An");
        request.setCustomerPhone("0912345678");
        request.setCustomerEmail("an.tran@gmail.com");

        PassengerDto p1 = new PassengerDto();
        p1.setFullName("Khách Một");
        p1.setIdCard("001200001234");
        p1.setPassengerType(PassengerType.ADULT);
        p1.setSeatNumber("12A");

        PassengerDto p2 = new PassengerDto();
        p2.setFullName("Khách Hai");
        p2.setIdCard("001200005678");
        p2.setPassengerType(PassengerType.ADULT);
        p2.setSeatNumber("12A"); // Cùng chọn ghế 12A

        request.setPassengers(List.of(p1, p2));

        com.dulich.ticketmanagement.exception.BusinessException ex = assertThrows(
                com.dulich.ticketmanagement.exception.BusinessException.class,
                () -> reservationService.createHoldReservation(request, null));

        assertTrue(ex.getMessage().contains("Trùng lặp"));
    }

    @Test
    @DisplayName("Hủy đơn vé đã thanh toán: Tự động ghi nhận giao dịch REFUNDED vào trans_details")
    void testCancelConfirmedReservationGeneratesRefund() {
        String bookingCode = "RES-CONFIRMED-01";
        Reservation res = new Reservation();
        res.setId(50L);
        res.setBookingCode(bookingCode);
        res.setTransportType(TransportType.AIR);
        res.setStatus(ReservationStatus.CONFIRMED);
        res.setDepartureDate(LocalDateTime.now().plusDays(5));
        res.setTotalAmount(new BigDecimal("2000000"));
        res.setCustomerEmail("khach@gmail.com");

        Ticket t = new Ticket();
        t.setId(1L);
        t.setStatus(TicketStatus.ISSUED);
        t.setSeatNumber("14A");
        res.setTickets(List.of(t));

        when(reservationRepository.findByBookingCodeWithTickets(bookingCode)).thenReturn(Optional.of(res));
        when(strategyFactory.getStrategy(TransportType.AIR)).thenReturn(airStrategy);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        Reservation cancelled = reservationService.cancelReservation(bookingCode, "Khách bận việc đột xuất");

        assertNotNull(cancelled);
        assertEquals(ReservationStatus.CANCELLED, cancelled.getStatus());
        assertEquals(TicketStatus.AVAILABLE, t.getStatus());
        // Phải ghi nhận giao dịch REFUNDED trong transDetailRepository
        verify(transDetailRepository, times(1)).save(any(com.dulich.ticketmanagement.entity.TransDetail.class));
        verify(emailService, times(1)).sendReservationCancelledEmail(any(Reservation.class));
    }
}
