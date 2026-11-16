package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.entity.Appointment;
import com.dulich.ticketmanagement.entity.enums.AppointmentStatus;

import java.util.List;

public interface AppointmentService {

    Appointment createAppointment(Appointment appointment);

    Appointment findByCode(String code);

    List<Appointment> findAll();

    List<Appointment> findByPhone(String phone);

    Appointment updateStatus(Long id, AppointmentStatus status, Long operatorId);

    long count();
}
