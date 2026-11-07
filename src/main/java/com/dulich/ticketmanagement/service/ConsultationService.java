package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.entity.Consultation;
import com.dulich.ticketmanagement.entity.enums.ConsultationStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsultationService {
    Consultation createConsultation(Consultation consultation);
    Consultation reschedule(Long id, LocalDateTime newDateTime);
    Consultation updateStatus(Long id, ConsultationStatus status);
    Consultation findById(Long id);
    Consultation findByCode(String code);
    List<Consultation> findAll();
    List<Consultation> findByPhone(String phone);
    void validateAppointmentTime(LocalDateTime dateTime, Long excludeConsultationId);
}
