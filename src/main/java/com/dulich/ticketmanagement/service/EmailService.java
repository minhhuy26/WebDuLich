package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.entity.Reservation;

public interface EmailService {

    void sendReservationHeldEmail(Reservation reservation);

    void sendReservationConfirmedEmail(Reservation reservation);

    void sendReservationCancelledEmail(Reservation reservation);

    void sendConsultationConfirmedEmail(Reservation reservation);
}
