package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.dto.PaymentRequestDto;
import com.dulich.ticketmanagement.entity.Reservation;
import com.dulich.ticketmanagement.entity.TransDetail;
import com.dulich.ticketmanagement.entity.enums.PaymentMethod;
import com.dulich.ticketmanagement.entity.enums.PaymentStatus;
import com.dulich.ticketmanagement.entity.enums.ReservationStatus;
import com.dulich.ticketmanagement.exception.BusinessException;
import com.dulich.ticketmanagement.exception.ResourceNotFoundException;
import com.dulich.ticketmanagement.repository.TransDetailRepository;
import com.dulich.ticketmanagement.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Nghiệp vụ thanh toán giao dịch (PaymentService)")
class PaymentServiceTest {

    @Mock
    private TransDetailRepository transDetailRepository;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("Thanh toán thành công: Xác nhận đơn giữ chỗ và trả về giao dịch TransDetail")
    void testProcessPaymentSuccess() {
        String bookingCode = "RES-202611-0001";
        Reservation mockReservation = new Reservation();
        mockReservation.setId(100L);
        mockReservation.setBookingCode(bookingCode);
        mockReservation.setStatus(ReservationStatus.CONFIRMED);
        mockReservation.setTotalAmount(new BigDecimal("1620000"));

        TransDetail mockTrans = new TransDetail(
                mockReservation,
                "PAY-202611-001",
                new BigDecimal("1620000"),
                PaymentMethod.VNPAY,
                PaymentStatus.SUCCESS,
                "REF-123456",
                "Thanh toán test"
        );

        when(reservationService.confirmAndIssue(eq(bookingCode), eq(PaymentMethod.VNPAY), anyString(), anyString()))
                .thenReturn(mockReservation);
        when(transDetailRepository.findByReservationIdOrderByTransactionTimeDesc(100L))
                .thenReturn(List.of(mockTrans));

        PaymentRequestDto dto = new PaymentRequestDto();
        dto.setBookingCode(bookingCode);
        dto.setPaymentMethod(PaymentMethod.VNPAY);
        dto.setAmount(new BigDecimal("1620000"));
        dto.setNote("Thanh toán test");

        TransDetail result = paymentService.processPayment(dto);

        assertNotNull(result);
        assertEquals("PAY-202611-001", result.getPaymentCode());
        assertEquals(PaymentStatus.SUCCESS, result.getPaymentStatus());
        assertEquals(new BigDecimal("1620000"), result.getAmount());
        verify(reservationService, times(1)).confirmAndIssue(eq(bookingCode), eq(PaymentMethod.VNPAY), anyString(), anyString());
    }

    @Test
    @DisplayName("Thanh toán thất bại: Báo lỗi khi đơn hàng đã quá hạn 15 phút hoặc đã hủy")
    void testProcessPaymentFailsWhenExpired() {
        String bookingCode = "RES-202611-EXPIRED";
        when(reservationService.confirmAndIssue(eq(bookingCode), any(), any(), any()))
                .thenThrow(new BusinessException("Đơn đặt vé đã quá hạn giữ chỗ (15 phút)! Vui lòng tiến hành đặt vé mới."));

        PaymentRequestDto dto = new PaymentRequestDto();
        dto.setBookingCode(bookingCode);
        dto.setPaymentMethod(PaymentMethod.VNPAY);

        BusinessException ex = assertThrows(BusinessException.class, () -> paymentService.processPayment(dto));
        assertTrue(ex.getMessage().contains("quá hạn"));
    }

    @Test
    @DisplayName("Tra cứu giao dịch theo mã thanh toán (PAY-...) thành công")
    void testGetByPaymentCode() {
        String payCode = "PAY-202611-9999";
        TransDetail mockTrans = new TransDetail();
        mockTrans.setPaymentCode(payCode);
        mockTrans.setPaymentStatus(PaymentStatus.SUCCESS);

        when(transDetailRepository.findByPaymentCode(payCode)).thenReturn(Optional.of(mockTrans));

        TransDetail found = paymentService.getByPaymentCode(payCode);
        assertNotNull(found);
        assertEquals(payCode, found.getPaymentCode());
    }

    @Test
    @DisplayName("Tra cứu giao dịch ném ngoại lệ khi không tìm thấy mã thanh toán")
    void testGetByPaymentCodeNotFound() {
        when(transDetailRepository.findByPaymentCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.getByPaymentCode("UNKNOWN"));
    }
}
