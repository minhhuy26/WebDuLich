package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.entity.Appointment;
import com.dulich.ticketmanagement.entity.enums.AppointmentStatus;
import com.dulich.ticketmanagement.entity.enums.TransportType;
import com.dulich.ticketmanagement.repository.AppointmentRepository;
import com.dulich.ticketmanagement.repository.UserRepository;
import com.dulich.ticketmanagement.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Nghiệp vụ đặt lịch hẹn tư vấn (AppointmentService)")
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    @Test
    @DisplayName("Tạo lịch hẹn mới: Tự động phát sinh mã hẹn APT và trạng thái mặc định PENDING")
    void testCreateAppointment() {
        Appointment apt = new Appointment();
        apt.setCustomerName("Lê Hoàng Long");
        apt.setPhone("0988776655");
        apt.setEmail("long.le@gmail.com");
        apt.setTransportType(TransportType.AIR);
        LocalDateTime validDate = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);
        if (validDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            validDate = validDate.plusDays(1);
        }
        apt.setPreferredDate(validDate);
        apt.setNotes("Tư vấn đặt vé máy bay đoàn");

        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> {
            Appointment saved = i.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Appointment result = appointmentService.createAppointment(apt);

        assertNotNull(result);
        assertNotNull(result.getAppointmentCode());
        assertTrue(result.getAppointmentCode().startsWith("APT-"));
        assertEquals(AppointmentStatus.PENDING, result.getStatus());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }
}
