package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.entity.*;
import com.dulich.ticketmanagement.entity.enums.FlightSeatClass;
import com.dulich.ticketmanagement.entity.enums.PassengerPriority;
import com.dulich.ticketmanagement.entity.enums.PassengerType;
import com.dulich.ticketmanagement.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Kiểm thử EmailService gửi thông báo vé máy bay & vé tàu hỏa")
class EmailServiceTest {

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl();
    }

    @Test
    @DisplayName("Gửi email vé máy bay khi giữ chỗ thành công")
    void testSendFlightBookingHeldEmail() {
        Flight flight = new Flight();
        flight.setFlightNumber("VN215");
        flight.setAirline("Vietnam Airlines");
        flight.setDepartureCity("Hà Nội");
        flight.setDepartureAirport("HAN");
        flight.setArrivalCity("TP. Hồ Chí Minh");
        flight.setArrivalAirport("SGN");
        flight.setDepartureTime(LocalDateTime.now().plusDays(2));

        FlightBooking booking = new FlightBooking();
        booking.setBookingCode("VN-AIR-TEST123");
        booking.setFlight(flight);
        booking.setCustomerName("Nguyễn Văn A");
        booking.setCustomerEmail("customer@gmail.com");
        booking.setCustomerPhone("0912345678");
        booking.setSeatClass(FlightSeatClass.ECONOMY);
        booking.setPassengerCount(1);
        booking.setTotalAmount(new BigDecimal("1500000"));
        booking.setHoldExpiresAt(LocalDateTime.now().plusMinutes(15));

        FlightPassenger passenger = new FlightPassenger();
        passenger.setFullName("Nguyễn Văn A");
        passenger.setPassengerType(PassengerType.ADULT);
        passenger.setSeatNumber("12A");
        passenger.setBaggageKg(20);
        passenger.setTicketCode("TK-FLIGHT-001");
        booking.addPassenger(passenger);

        assertDoesNotThrow(() -> emailService.sendFlightBookingHeldEmail(booking));
        assertDoesNotThrow(() -> emailService.sendFlightBookingConfirmedEmail(booking));
        assertDoesNotThrow(() -> emailService.sendFlightBookingCancelledEmail(booking));
    }

    @Test
    @DisplayName("Gửi email vé tàu hỏa khi đặt chỗ thành công")
    void testSendTrainBookingHeldEmail() {
        Train train = new Train();
        train.setTrainCode("SE1");
        train.setRouteName("Hà Nội - Sài Gòn");

        TrainStation dep = new TrainStation("Hà Nội - Sài Gòn", "HAN", "Ga Hà Nội", 0, 0);
        TrainStation arr = new TrainStation("Hà Nội - Sài Gòn", "SGO", "Ga Sài Gòn", 10, 1726);

        TrainBooking booking = new TrainBooking();
        booking.setBookingCode("SE-TRN-TEST123");
        booking.setTrain(train);
        booking.setDepartureStation(dep);
        booking.setArrivalStation(arr);
        booking.setDepartureDate(LocalDate.now().plusDays(3));
        booking.setDepartureDatetime(LocalDateTime.now().plusDays(3));
        booking.setCustomerName("Trần Thị B");
        booking.setCustomerEmail("tranthib@gmail.com");
        booking.setCustomerPhone("0987654321");
        booking.setTicketCount(1);
        booking.setTotalAmount(new BigDecimal("1200000"));
        booking.setHoldExpiresAt(LocalDateTime.now().plusMinutes(10));

        TrainCoach coach = new TrainCoach();
        coach.setCoachNumber(1);
        coach.setCoachName("Toa 1 - Ghế Mềm Điều Hòa");

        TrainSeat seat = new TrainSeat();
        seat.setCoach(coach);
        seat.setSeatLabel("Ghế 10");

        TrainTicket ticket = new TrainTicket();
        ticket.setPassengerName("Trần Thị B");
        ticket.setSeat(seat);
        ticket.setPriorityCategory(PassengerPriority.STANDARD);
        ticket.setTicketCode("TK-TRAIN-001");
        ticket.setFinalFare(new BigDecimal("1200000"));
        booking.addTicket(ticket);

        assertDoesNotThrow(() -> emailService.sendTrainBookingHeldEmail(booking));
        assertDoesNotThrow(() -> emailService.sendTrainBookingConfirmedEmail(booking));
        assertDoesNotThrow(() -> emailService.sendTrainBookingCancelledEmail(booking));
    }

    @Test
    @DisplayName("Gửi email với địa chỉ email trống hoặc không hợp lệ không phát sinh ngoại lệ")
    void testSendEmail_WithInvalidEmail_DoesNotThrow() {
        FlightBooking booking = new FlightBooking();
        booking.setCustomerEmail("invalid-email");
        assertDoesNotThrow(() -> emailService.sendFlightBookingHeldEmail(booking));

        booking.setCustomerEmail(null);
        assertDoesNotThrow(() -> emailService.sendFlightBookingHeldEmail(booking));
    }
}
