package com.dulich.ticketmanagement.controller;

import com.dulich.ticketmanagement.entity.Appointment;
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
 * Trạm điều phối đặt lịch hẹn tư vấn (AppointmentController)
 * Thực thi Workflow 4 (Appointment Consultation Booking) theo đặc tả của Giảng viên.
 */
@Controller
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // 1. HIỂN THỊ FORM ĐĂNG KÝ HẸN (GET /appointments/schedule HOẶC /appointments/new HOẶC /consultations/new HOẶC /consultations/book)
    @GetMapping({"/appointments/schedule", "/appointments/new", "/consultations/new", "/consultations/book"})
    public String showAppointmentForm(Model model) {
        Appointment appointment = new Appointment();
        // Mặc định chọn 09:00 ngày làm việc tiếp theo
        LocalDateTime defaultDate = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0);
        if (defaultDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            defaultDate = defaultDate.plusDays(1);
        }
        appointment.setPreferredDate(defaultDate);
        appointment.setTransportType(TransportType.AIR);

        model.addAttribute("appointment", appointment);
        model.addAttribute("transportTypes", TransportType.values());
        return "appointment/appointment_form";
    }

    // 2. TIẾP NHẬN ĐĂNG KÝ HẸN (POST /appointments/schedule HOẶC /appointments HOẶC /consultations) & MÔ HÌNH PRG
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
            Appointment saved = appointmentService.createAppointment(appointment);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký lịch hẹn tư vấn thành công! Mã cuộc hẹn của quý khách: " + saved.getAppointmentCode() +
                    ". Chuyên viên tư vấn sẽ liên hệ qua SĐT: " + saved.getPhone());

            // PRG Pattern: Redirect 303 sang màn hình xác nhận
            return "redirect:/appointments/success?code=" + saved.getAppointmentCode();
        } catch (BusinessException ex) {
            model.addAttribute("transportTypes", TransportType.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "appointment/appointment_form";
        }
    }

    // 3. MÀN HÌNH XÁC NHẬN HẸN THÀNH CÔNG (GET /appointments/success)
    @GetMapping({"/appointments/success", "/consultations/success"})
    public String showAppointmentSuccess(@RequestParam("code") String code, Model model) {
        Appointment appointment = appointmentService.findByCode(code);
        model.addAttribute("appointment", appointment);
        return "appointment/appointment_success";
    }

    // 4. DANH SÁCH LỊCH HẸN CHO NHÂN VIÊN ĐIỀU HÀNH & ADMIN (GET /appointments)
    @GetMapping({"/appointments", "/consultations"})
    public String listAppointments(@RequestParam(value = "phone", required = false) String phone, Model model) {
        List<Appointment> list;
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
