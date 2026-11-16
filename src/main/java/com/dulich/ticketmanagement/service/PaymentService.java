package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.dto.PaymentRequestDto;
import com.dulich.ticketmanagement.entity.TransDetail;

import java.util.List;

public interface PaymentService {

    TransDetail processPayment(PaymentRequestDto dto);

    TransDetail getByPaymentCode(String paymentCode);

    List<TransDetail> getPaymentsByReservation(Long reservationId);

    List<TransDetail> getAllPayments();
}
