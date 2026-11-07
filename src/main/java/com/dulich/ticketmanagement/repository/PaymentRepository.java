package com.dulich.ticketmanagement.repository;

import com.dulich.ticketmanagement.entity.Payment;
import com.dulich.ticketmanagement.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentCode(String paymentCode);
    List<Payment> findByBookingId(Long bookingId);
    List<Payment> findByPaymentStatus(PaymentStatus status);
}
