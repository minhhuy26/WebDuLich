package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.entity.Appointment;
import com.dulich.ticketmanagement.entity.Reservation;

public interface EmailService {

    void sendReservationHeldEmail(Reservation reservation);

    void sendReservationConfirmedEmail(Reservation reservation);

    void sendReservationCancelledEmail(Reservation reservation);

    void sendAppointmentConfirmedEmail(Appointment appointment);
}
