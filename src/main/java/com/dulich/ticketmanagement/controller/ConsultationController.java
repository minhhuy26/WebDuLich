package com.dulich.ticketmanagement.controller;

import com.dulich.ticketmanagement.entity.Consultation;
import com.dulich.ticketmanagement.entity.enums.TicketType;
import com.dulich.ticketmanagement.exception.BusinessException;
import com.dulich.ticketmanagement.service.ConsultationService;
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

@Controller
@RequestMapping("/consultations")
public class ConsultationController {

    private final ConsultationService consultationService;

    @Autowired
    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @GetMapping("/new")
    public String showConsultationForm(Model model) {
        Consultation consultation = new Consultation();
        // Mặc định chọn 09:00 ngày làm việc tiếp theo
        LocalDateTime defaultDate = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0);
        if (defaultDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            defaultDate = defaultDate.plusDays(1);
        }
        consultation.setPreferredDate(defaultDate);
        consultation.setTicketType(TicketType.FLIGHT);

        model.addAttribute("consultation", consultation);
        model.addAttribute("ticketTypes", TicketType.values());
        return "consultation/consultation_form";
    }

    @PostMapping
    public String registerConsultation(@Valid @ModelAttribute("consultation") Consultation consultation,
                                       BindingResult bindingResult,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("ticketTypes", TicketType.values());
            return "consultation/consultation_form";
        }

        try {
            Consultation saved = consultationService.createConsultation(consultation);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký lịch hẹn tư vấn thành công! Mã cuộc hẹn của bạn là: " + saved.getConsultationCode()
                            + ". Chuyên viên tư vấn sẽ liên hệ qua SĐT " + saved.getPhone());
            return "redirect:/consultations/success?code=" + saved.getConsultationCode();
        } catch (BusinessException ex) {
            model.addAttribute("ticketTypes", TicketType.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "consultation/consultation_form";
        }
    }

    @GetMapping("/success")
    public String showSuccess(@RequestParam("code") String code, Model model) {
        Consultation consultation = consultationService.findByCode(code);
        model.addAttribute("consultation", consultation);
        return "consultation/consultation_success";
    }

    @GetMapping
    public String listConsultations(@RequestParam(value = "phone", required = false) String phone, Model model) {
        List<Consultation> consultations;
        if (phone != null && !phone.trim().isEmpty()) {
            consultations = consultationService.findByPhone(phone.trim());
        } else {
            consultations = consultationService.findAll();
        }
        model.addAttribute("consultations", consultations);
        model.addAttribute("searchPhone", phone);
        return "consultation/consultation_list";
    }
}
