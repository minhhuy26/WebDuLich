package com.dulich.ticketmanagement.service.strategy;

import com.dulich.ticketmanagement.entity.Reservation;
import com.dulich.ticketmanagement.entity.Ticket;
import com.dulich.ticketmanagement.entity.enums.PassengerType;
import com.dulich.ticketmanagement.entity.enums.TransportType;
import com.dulich.ticketmanagement.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Hiện thực hóa quy tắc nghiệp vụ VÉ TÀU HỎA (TrainTicketStrategy):
 * - Phí bảo hiểm hành khách đường sắt: 10.000đ/vé.
 * - Chính sách ưu tiên đối tượng:
 *   + Trẻ em: giảm 25% giá vé.
 *   + Học sinh / Sinh viên: giảm 10% giá vé.
 *   + Người cao tuổi (>= 60 tuổi): giảm 15% giá vé.
 * - Validation: Điểm ga đi và ga đến không được trùng nhau; phải đặt trước tối thiểu 30 phút.
 * - Hủy vé: trước >= 24h phí 10%, từ 4h - 24h phí 20%, < 4h phí 30%, sau giờ chạy không hoàn trả.
 */
@Component
public class TrainTicketStrategy implements TicketStrategy {

    public static final BigDecimal RAIL_INSURANCE_FEE = new BigDecimal("10000");

    @Override
    public TransportType getTransportType() {
        return TransportType.TRAIN;
    }

    @Override
    public BigDecimal calculatePrice(Ticket ticket, PassengerType passengerType, BigDecimal extraFee) {
        BigDecimal base = ticket.getBasePrice() != null ? ticket.getBasePrice() : BigDecimal.ZERO;
        BigDecimal multiplier = BigDecimal.ONE;

        if (passengerType == PassengerType.CHILD) {
            multiplier = new BigDecimal("0.75");
        } else if (passengerType == PassengerType.STUDENT) {
            multiplier = new BigDecimal("0.90");
        } else if (passengerType == PassengerType.SENIOR) {
            multiplier = new BigDecimal("0.85");
        }

        BigDecimal calculatedBase = base.multiply(multiplier).setScale(0, RoundingMode.HALF_UP);
        BigDecimal extra = extraFee != null ? extraFee : BigDecimal.ZERO;

        return calculatedBase.add(RAIL_INSURANCE_FEE).add(extra);
    }

    @Override
    public void validateReservation(Reservation reservation, List<Ticket> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            throw new BusinessException("Đơn đặt vé tàu hỏa phải có ít nhất 1 chỗ ngồi/giường nằm!");
        }

        if (reservation.getDeparturePlace() != null && reservation.getDeparturePlace().equalsIgnoreCase(reservation.getArrivalPlace())) {
            throw new BusinessException("Ga khởi hành và ga đến không được trùng nhau!");
        }

        LocalDateTime now = LocalDateTime.now();
        if (reservation.getDepartureDate().isBefore(now.plusMinutes(30))) {
            throw new BusinessException("Vé tàu hỏa chỉ được đặt trước giờ khởi hành tối thiểu 30 phút!");
        }
    }

    @Override
    public BigDecimal calculateCancellationFee(Reservation reservation) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departure = reservation.getDepartureDate();

        if (departure.isBefore(now)) {
            throw new BusinessException("Tàu đã khởi hành, không thể hoàn trả vé!");
        }

        long hoursBefore = Duration.between(now, departure).toHours();
        BigDecimal total = reservation.getTotalAmount();

        if (hoursBefore >= 24) {
            return total.multiply(new BigDecimal("0.10")).setScale(0, RoundingMode.HALF_UP);
        } else if (hoursBefore >= 4) {
            return total.multiply(new BigDecimal("0.20")).setScale(0, RoundingMode.HALF_UP);
        } else {
            return total.multiply(new BigDecimal("0.30")).setScale(0, RoundingMode.HALF_UP);
        }
    }

    @Override
    public String getPolicyDescription() {
        return "Quy định Đường sắt: Bảo hiểm 10k/vé. Sinh viên -10%, Người cao tuổi -15%, Trẻ em -25%. Hủy >=24h phí 10%, 4-24h phí 20%.";
    }
}
