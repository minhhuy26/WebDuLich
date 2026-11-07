package com.dulich.ticketmanagement.controller;

import com.dulich.ticketmanagement.entity.Consultation;
import com.dulich.ticketmanagement.entity.FlightBooking;
import com.dulich.ticketmanagement.entity.TrainBooking;
import com.dulich.ticketmanagement.entity.User;
import com.dulich.ticketmanagement.repository.ConsultationRepository;
import com.dulich.ticketmanagement.repository.FlightBookingRepository;
import com.dulich.ticketmanagement.repository.TrainBookingRepository;
import com.dulich.ticketmanagement.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử tra cứu đơn vé cá nhân /my-bookings (Bug 16)")
class CustomerControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FlightBookingRepository flightBookingRepository;

    @Mock
    private TrainBookingRepository trainBookingRepository;

    @Mock
    private ConsultationRepository consultationRepository;

    @InjectMocks
    private CustomerController customerController;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user1", "pass")
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Bug 16 Test: Tra cứu đơn cá nhân theo cả Số điện thoại HOẶC Email, không bị sót đơn và loại bỏ trùng lặp")
    void testBug16_ShowMyBookings_SearchesByPhoneOrEmail_AndDeduplicates() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");
        user.setPhone("0912345678");
        user.setEmail("user1@example.com");

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));

        // fb1 khớp cả SĐT và Email; fb2 chỉ khớp Email (đặt qua email trước đó)
        FlightBooking fb1 = new FlightBooking();
        fb1.setId(10L);
        fb1.setBookingCode("VN-AIR-10");

        FlightBooking fb2 = new FlightBooking();
        fb2.setId(20L);
        fb2.setBookingCode("VN-AIR-20");

        when(flightBookingRepository.findByCustomerPhone("0912345678")).thenReturn(List.of(fb1));
        when(flightBookingRepository.findByCustomerEmail("user1@example.com")).thenReturn(List.of(fb1, fb2));

        // tb1 chỉ khớp Email (đơn đặt vé tàu với email)
        TrainBooking tb1 = new TrainBooking();
        tb1.setId(30L);
        tb1.setBookingCode("SE-TRN-30");

        when(trainBookingRepository.findByCustomerPhone("0912345678")).thenReturn(List.of());
        when(trainBookingRepository.findByCustomerEmail("user1@example.com")).thenReturn(List.of(tb1));

        // c1 khớp SĐT
        Consultation c1 = new Consultation();
        c1.setId(40L);
        c1.setConsultationCode("CS-40");

        when(consultationRepository.findByPhone("0912345678")).thenReturn(List.of(c1));
        when(consultationRepository.findByEmail("user1@example.com")).thenReturn(List.of(c1));

        Model model = new ExtendedModelMap();
        String view = customerController.showMyBookings(model);

        assertEquals("customer/my_bookings", view);

        @SuppressWarnings("unchecked")
        List<FlightBooking> myFlights = (List<FlightBooking>) model.getAttribute("myFlights");
        @SuppressWarnings("unchecked")
        List<TrainBooking> myTrains = (List<TrainBooking>) model.getAttribute("myTrains");
        @SuppressWarnings("unchecked")
        List<Consultation> myConsultations = (List<Consultation>) model.getAttribute("myConsultations");

        assertNotNull(myFlights);
        assertEquals(2, myFlights.size(), "myFlights phải chứa cả fb1 và fb2 (không bị sót đơn khớp email, không bị trùng fb1)");

        assertNotNull(myTrains);
        assertEquals(1, myTrains.size(), "myTrains phải tìm thấy tb1 qua email");

        assertNotNull(myConsultations);
        assertEquals(1, myConsultations.size(), "myConsultations phải deduplicate c1");
    }
}
