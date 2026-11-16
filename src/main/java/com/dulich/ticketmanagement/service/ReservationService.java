package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.dto.ReservationRequestDto;
import com.dulich.ticketmanagement.entity.Reservation;
import com.dulich.ticketmanagement.entity.User;
import com.dulich.ticketmanagement.entity.enums.PaymentMethod;

import java.util.List;

public interface ReservationService {

    Reservation createHoldReservation(ReservationRequestDto dto, User currentUser);

    Reservation getByBookingCode(String bookingCode);

    Reservation confirmAndIssue(String bookingCode, PaymentMethod paymentMethod, String transactionRef, String note);

    Reservation cancelReservation(String bookingCode, String reason);

    List<Reservation> getAllReservations();

    List<Reservation> getReservationsByPhone(String phone);

    List<Reservation> getReservationsByUserId(Long userId);

    void releaseExpiredHoldReservations();
}
