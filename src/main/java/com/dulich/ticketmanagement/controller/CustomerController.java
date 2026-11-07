package com.dulich.ticketmanagement.controller;

import com.dulich.ticketmanagement.entity.FlightBooking;
import com.dulich.ticketmanagement.entity.TrainBooking;
import com.dulich.ticketmanagement.entity.Consultation;
import com.dulich.ticketmanagement.entity.User;
import com.dulich.ticketmanagement.repository.ConsultationRepository;
import com.dulich.ticketmanagement.repository.FlightBookingRepository;
import com.dulich.ticketmanagement.repository.TrainBookingRepository;
import com.dulich.ticketmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/my-bookings")
public class CustomerController {

    private final UserRepository userRepository;
    private final FlightBookingRepository flightBookingRepository;
    private final TrainBookingRepository trainBookingRepository;
    private final ConsultationRepository consultationRepository;

    @Autowired
    public CustomerController(UserRepository userRepository,
                              FlightBookingRepository flightBookingRepository,
                              TrainBookingRepository trainBookingRepository,
                              ConsultationRepository consultationRepository) {
        this.userRepository = userRepository;
        this.flightBookingRepository = flightBookingRepository;
        this.trainBookingRepository = trainBookingRepository;
        this.consultationRepository = consultationRepository;
    }

    @GetMapping
    public String showMyBookings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);

        List<FlightBooking> myFlights = new ArrayList<>();
        List<TrainBooking> myTrains = new ArrayList<>();
        List<Consultation> myConsultations = new ArrayList<>();

        if (currentUser != null) {
            String phone = currentUser.getPhone();
            String email = currentUser.getEmail();

            Map<Long, FlightBooking> flightMap = new LinkedHashMap<>();
            Map<Long, TrainBooking> trainMap = new LinkedHashMap<>();
            Map<Long, Consultation> consultMap = new LinkedHashMap<>();

            if (phone != null && !phone.isBlank()) {
                flightBookingRepository.findByCustomerPhone(phone).forEach(b -> flightMap.put(b.getId(), b));
                trainBookingRepository.findByCustomerPhone(phone).forEach(b -> trainMap.put(b.getId(), b));
                consultationRepository.findByPhone(phone).forEach(c -> consultMap.put(c.getId(), c));
            }

            if (email != null && !email.isBlank()) {
                flightBookingRepository.findByCustomerEmail(email).forEach(b -> flightMap.put(b.getId(), b));
                trainBookingRepository.findByCustomerEmail(email).forEach(b -> trainMap.put(b.getId(), b));
                consultationRepository.findByEmail(email).forEach(c -> consultMap.put(c.getId(), c));
            }

            myFlights.addAll(flightMap.values());
            myTrains.addAll(trainMap.values());
            myConsultations.addAll(consultMap.values());

            // Tích điểm & Hạng thành viên (100.000 VNĐ = 1 điểm tích lũy)
            BigDecimal totalSpent = BigDecimal.ZERO;
            for (FlightBooking fb : myFlights) {
                if (fb.getTotalAmount() != null && ("CONFIRMED".equals(fb.getStatus().name()) || "ISSUED".equals(fb.getStatus().name()))) {
                    totalSpent = totalSpent.add(fb.getTotalAmount());
                }
            }
            for (TrainBooking tb : myTrains) {
                if (tb.getTotalAmount() != null && ("CONFIRMED".equals(tb.getStatus().name()) || "ISSUED".equals(tb.getStatus().name()))) {
                    totalSpent = totalSpent.add(tb.getTotalAmount());
                }
            }

            long rewardPoints = totalSpent.divide(new BigDecimal("100000"), 0, RoundingMode.FLOOR).longValue();
            String memberTier = "Thành Viên Bạc";
            String tierBadgeClass = "badge-neutral";
            int discountPercent = 0;
            if (totalSpent.compareTo(new BigDecimal("20000000")) >= 0) {
                memberTier = "Thành Viên Kim Cương";
                tierBadgeClass = "badge-danger";
                discountPercent = 5;
            } else if (totalSpent.compareTo(new BigDecimal("5000000")) >= 0) {
                memberTier = "Thành Viên Vàng";
                tierBadgeClass = "badge-warning";
                discountPercent = 3;
            }

            model.addAttribute("totalSpent", totalSpent);
            model.addAttribute("rewardPoints", rewardPoints);
            model.addAttribute("memberTier", memberTier);
            model.addAttribute("tierBadgeClass", tierBadgeClass);
            model.addAttribute("discountPercent", discountPercent);
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("myFlights", myFlights);
        model.addAttribute("myTrains", myTrains);
        model.addAttribute("myConsultations", myConsultations);

        return "customer/my_bookings";
    }
}
