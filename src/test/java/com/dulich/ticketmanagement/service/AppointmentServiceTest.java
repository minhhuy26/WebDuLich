package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.entity.Reservation;
import com.dulich.ticketmanagement.entity.enums.AppointmentStatus;
import com.dulich.ticketmanagement.entity.enums.ReservationStatus;
import com.dulich.ticketmanagement.entity.enums.TransportType;
import com.dulich.ticketmanagement.repository.ReservationRepository;
import com.dulich.ticketmanagement.repository.UserRepository;
import com.dulich.ticketmanagement.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Nghiệp vụ đặt lịch hẹn tư vấn theo đơn vé (AppointmentService)")
class AppointmentServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    @Test
    @DisplayName("Đăng ký hẹn tư vấn sau khi đặt vé: Lưu thông tin tư vấn vào DonDatCho và trạng thái PENDING")
    void testScheduleConsultation() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setBookingCode("RES-202611-0001");
        reservation.setCustomerName("Lê Hoàng Long");
        reservation.setCustomerPhone("0988776655");
        reservation.setCustomerEmail("long.le@gmail.com");
        reservation.setTransportType(TransportType.AIR);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        LocalDateTime validDate = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);
        if (validDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            validDate = validDate.plusDays(1);
        }

        when(reservationRepository.findByBookingCode("RES-202611-0001")).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        Reservation result = appointmentService.scheduleConsultation("RES-202611-0001", validDate, "Tư vấn nâng hạng ghế");

        assertNotNull(result);
        assertTrue(result.getHasConsultation());
        assertEquals(AppointmentStatus.PENDING, result.getConsultationStatus());
        assertEquals("Tư vấn nâng hạng ghế", result.getConsultationNotes());
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }
}
