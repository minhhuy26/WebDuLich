package com.dulich.ticketmanagement.controller;

import com.dulich.ticketmanagement.entity.Appointment;
import com.dulich.ticketmanagement.entity.Reservation;
import com.dulich.ticketmanagement.entity.User;
import com.dulich.ticketmanagement.entity.enums.ReservationStatus;
import com.dulich.ticketmanagement.repository.AppointmentRepository;
import com.dulich.ticketmanagement.repository.ReservationRepository;
import com.dulich.ticketmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/my-bookings")
public class CustomerController {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final AppointmentRepository appointmentRepository;

    @Autowired
    public CustomerController(UserRepository userRepository,
                              ReservationRepository reservationRepository,
                              AppointmentRepository appointmentRepository) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping
    public String showMyBookings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);

        List<Reservation> reservations = new ArrayList<>();
        List<Appointment> appointments = new ArrayList<>();
        BigDecimal totalSpent = BigDecimal.ZERO;
        long confirmedCount = 0;
        long holdingCount = 0;

        if (currentUser != null) {
            reservations = reservationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
            if (reservations.isEmpty() && currentUser.getPhone() != null) {
                reservations = reservationRepository.findByCustomerPhoneOrderByCreatedAtDesc(currentUser.getPhone());
            }
            if (currentUser.getPhone() != null) {
                appointments = appointmentRepository.findByPhoneOrderByCreatedAtDesc(currentUser.getPhone());
            }

            for (Reservation r : reservations) {
                if (r.getStatus() == ReservationStatus.CONFIRMED) {
                    confirmedCount++;
                    totalSpent = totalSpent.add(r.getTotalAmount());
                } else if (r.getStatus() == ReservationStatus.CREATED) {
                    holdingCount++;
                }
            }
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("reservations", reservations);
        model.addAttribute("appointments", appointments);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("holdingCount", holdingCount);
        model.addAttribute("totalSpent", totalSpent);

        return "customer/my_bookings";
    }

    @GetMapping("/lookup")
    public String showLookupForm(@RequestParam(value = "code", required = false) String code,
                                 @RequestParam(value = "phone", required = false) String phone,
                                 Model model) {
        if (code != null && !code.trim().isEmpty()) {
            if (phone != null && !phone.trim().isEmpty()) {
                return "redirect:/reservations/" + code.trim().toUpperCase() + "?phone=" + phone.trim();
            }
            model.addAttribute("code", code.trim().toUpperCase());
        }
        if (phone != null && !phone.trim().isEmpty()) {
            List<Reservation> list = reservationRepository.findByCustomerPhoneOrderByCreatedAtDesc(phone.trim());
            model.addAttribute("phone", phone.trim());
            model.addAttribute("foundReservations", list);
        }
        return "customer/guest_lookup";
    }

    @PostMapping("/lookup")
    public String handleLookupPost(@RequestParam(value = "code", required = false) String code,
                                   @RequestParam(value = "phone", required = false) String phone) {
        if (code != null && !code.trim().isEmpty()) {
            if (phone != null && !phone.trim().isEmpty()) {
                return "redirect:/reservations/" + code.trim().toUpperCase() + "?phone=" + phone.trim();
            }
            return "redirect:/reservations/" + code.trim().toUpperCase();
        }
        if (phone != null && !phone.trim().isEmpty()) {
            return "redirect:/my-bookings/lookup?phone=" + phone.trim();
        }
        return "redirect:/my-bookings/lookup";
    }
}
