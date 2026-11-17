package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.entity.Reservation;
import com.dulich.ticketmanagement.entity.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentService {

    Reservation scheduleConsultation(String bookingCode, LocalDateTime preferredDate, String notes);

    Reservation updateStatus(Long reservationId, AppointmentStatus status, Long operatorId);

    List<Reservation> findAll();

    List<Reservation> findByPhone(String phone);

    Reservation findByBookingCode(String bookingCode);

    long count();
}
