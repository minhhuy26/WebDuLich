package com.dulich.ticketmanagement.service.impl;

import com.dulich.ticketmanagement.dto.PaymentRequestDto;
import com.dulich.ticketmanagement.entity.Reservation;
import com.dulich.ticketmanagement.entity.TransDetail;
import com.dulich.ticketmanagement.exception.ResourceNotFoundException;
import com.dulich.ticketmanagement.repository.TransDetailRepository;
import com.dulich.ticketmanagement.service.PaymentService;
import com.dulich.ticketmanagement.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class PaymentServiceImpl implements PaymentService {

    private final TransDetailRepository transDetailRepository;
    private final ReservationService reservationService;

    @Autowired
    public PaymentServiceImpl(TransDetailRepository transDetailRepository,
                              ReservationService reservationService) {
        this.transDetailRepository = transDetailRepository;
        this.reservationService = reservationService;
    }

    @Override
    public TransDetail processPayment(PaymentRequestDto dto) {
        Reservation reservation = reservationService.confirmAndIssue(
                dto.getBookingCode(),
                dto.getPaymentMethod(),
                "VNPAY-" + System.currentTimeMillis() % 10000000,
                dto.getNote() != null ? dto.getNote() : "Giao dịch thanh toán trực tuyến"
        );

        return transDetailRepository.findByReservationIdOrderByTransactionTimeDesc(reservation.getId())
                .stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin giao dịch thanh toán vừa tạo!"));
    }

    @Override
    @Transactional(readOnly = true)
    public TransDetail getByPaymentCode(String paymentCode) {
        return transDetailRepository.findByPaymentCode(paymentCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch thanh toán: " + paymentCode));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransDetail> getPaymentsByReservation(Long reservationId) {
        return transDetailRepository.findByReservationIdOrderByTransactionTimeDesc(reservationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransDetail> getAllPayments() {
        return transDetailRepository.findAllWithReservation();
    }
}
