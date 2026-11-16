package com.dulich.ticketmanagement.repository;

import com.dulich.ticketmanagement.entity.TransDetail;
import com.dulich.ticketmanagement.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransDetailRepository extends JpaRepository<TransDetail, Long> {

    Optional<TransDetail> findByPaymentCode(String paymentCode);

    List<TransDetail> findByReservationIdOrderByTransactionTimeDesc(Long reservationId);

    List<TransDetail> findByPaymentStatusOrderByTransactionTimeDesc(PaymentStatus status);

    @Query("SELECT td FROM TransDetail td JOIN FETCH td.reservation ORDER BY td.transactionTime DESC")
    List<TransDetail> findAllWithReservation();
}
