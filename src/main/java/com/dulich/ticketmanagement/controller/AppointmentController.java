package com.dulich.ticketmanagement.controller;

import com.dulich.ticketmanagement.entity.Appointment;
import com.dulich.ticketmanagement.entity.Reservation;
import com.dulich.ticketmanagement.entity.enums.AppointmentStatus;
import com.dulich.ticketmanagement.entity.enums.TransportType;
import com.dulich.ticketmanagement.exception.BusinessException;
import com.dulich.ticketmanagement.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Trạm điều phối đặt lịch hẹn tư vấn chuyến đi (AppointmentController).
 * Nghiệp vụ chuẩn hóa: Lịch hẹn tư vấn chỉ xuất hiện và áp dụng sau khi khách đặt vé xong.
 * Dữ liệu tư vấn được lưu trữ trực tiếp trong đơn đặt vé (Reservation/DonDatCho).
 */
@Controller
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // 1. HIỂN THỊ FORM ĐĂNG KÝ HẸN TƯ VẤN CHO ĐƠN VÉ (GET)
    @GetMapping({"/appointments/schedule", "/appointments/new", "/consultations/new", "/consultations/book"})
    public String showAppointmentForm(@RequestParam(value = "bookingCode", required = false) String bookingCode,
                                      Model model) {
        Appointment appointment = new Appointment();
        LocalDateTime defaultDate = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0);
        if (defaultDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            defaultDate = defaultDate.plusDays(1);
        }
        appointment.setPreferredDate(defaultDate);

        if (bookingCode != null && !bookingCode.trim().isEmpty()) {
            try {
                Reservation reservation = appointmentService.findByBookingCode(bookingCode.trim());
                appointment.setBookingCode(reservation.getBookingCode());
                appointment.setCustomerName(reservation.getCustomerName());
                appointment.setPhone(reservation.getCustomerPhone());
                appointment.setEmail(reservation.getCustomerEmail());
                appointment.setTransportType(reservation.getTransportType());
                if (reservation.getConsultationNotes() != null) {
                    appointment.setNotes(reservation.getConsultationNotes());
                }
                model.addAttribute("reservation", reservation);
            } catch (Exception ex) {
                model.addAttribute("errorMessage", "Không tìm thấy đơn vé: " + bookingCode);
            }
        }

        model.addAttribute("appointment", appointment);
        model.addAttribute("transportTypes", TransportType.values());
        return "appointment/appointment_form";
    }

    // 2. TIẾP NHẬN ĐĂNG KÝ HẸN TƯ VẤN (POST) & MÔ HÌNH PRG
    @PostMapping({"/appointments/schedule", "/appointments", "/consultations"})
    public String registerAppointment(@Valid @ModelAttribute("appointment") Appointment appointment,
                                      BindingResult bindingResult,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("transportTypes", TransportType.values());
            return "appointment/appointment_form";
        }

        try {
            Reservation updated = appointmentService.scheduleConsultation(
                    appointment.getBookingCode(),
                    appointment.getPreferredDate(),
                    appointment.getNotes()
            );

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký lịch hẹn tư vấn cho đơn vé " + updated.getBookingCode() + " thành công! Chuyên viên VietTravel Hub sẽ liên hệ qua SĐT: " + updated.getCustomerPhone());

            // PRG Pattern: Redirect 303 sang màn hình xác nhận hoặc chi tiết đơn
            return "redirect:/appointments/success?code=" + updated.getBookingCode();
        } catch (BusinessException ex) {
            model.addAttribute("transportTypes", TransportType.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "appointment/appointment_form";
        }
    }

    // 3. MÀN HÌNH XÁC NHẬN HẸN THÀNH CÔNG (GET /appointments/success)
    @GetMapping({"/appointments/success", "/consultations/success"})
    public String showAppointmentSuccess(@RequestParam("code") String code, Model model) {
        Reservation reservation = appointmentService.findByBookingCode(code);
        Appointment appointment = Appointment.fromReservation(reservation);
        model.addAttribute("appointment", appointment);
        model.addAttribute("reservation", reservation);
        return "appointment/appointment_success";
    }

    // 4. DANH SÁCH LỊCH HẸN CHO NHÂN VIÊN ĐIỀU HÀNH & ADMIN (GET /appointments)
    @GetMapping({"/appointments", "/consultations"})
    public String listAppointments(@RequestParam(value = "phone", required = false) String phone, Model model) {
        List<Reservation> list;
        if (phone != null && !phone.trim().isEmpty()) {
            list = appointmentService.findByPhone(phone.trim());
        } else {
            list = appointmentService.findAll();
        }
        model.addAttribute("appointments", list);
        model.addAttribute("searchPhone", phone);
        model.addAttribute("appointmentStatuses", AppointmentStatus.values());
        return "appointment/appointment_list";
    }
}
