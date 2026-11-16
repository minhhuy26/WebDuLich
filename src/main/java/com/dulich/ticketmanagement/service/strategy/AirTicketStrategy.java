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
import java.util.regex.Pattern;

/**
 * Hiện thực hóa quy tắc nghiệp vụ VÉ MÁY BAY (AirTicketStrategy):
 * - Thuế phí an ninh soi chiếu và quản trị sân bay: 120.000đ/khách (miễn phí em bé).
 * - Chính sách giá theo độ tuổi: Người lớn 100%, Trẻ em (2-12 tuổi) 75%, Em bé (<2 tuổi) 10%.
 * - Hạng thương gia (Business): phụ thu +80% giá cơ bản.
 * - Validation: Bắt buộc CCCD/Hộ chiếu chuẩn với người lớn; số lượng em bé không được vượt quá số người lớn.
 * - Hủy đổi vé: trước >= 24h phí 10%, trong vòng 24h phí 30%, đã cất cánh không hoàn tiền.
 */
@Component
public class AirTicketStrategy implements TicketStrategy {

    public static final BigDecimal AIRPORT_TAX_FEE = new BigDecimal("120000");
    private static final Pattern CCCD_PATTERN = Pattern.compile("^[0-9]{12}$");
    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[A-Z][0-9]{7,8}$");

    @Override
    public TransportType getTransportType() {
        return TransportType.AIR;
    }

    @Override
    public BigDecimal calculatePrice(Ticket ticket, PassengerType passengerType, BigDecimal extraFee) {
        BigDecimal base = ticket.getBasePrice() != null ? ticket.getBasePrice() : BigDecimal.ZERO;
        BigDecimal multiplier = BigDecimal.ONE;

        if (passengerType == PassengerType.CHILD) {
            multiplier = new BigDecimal("0.75");
        } else if (passengerType == PassengerType.INFANT) {
            multiplier = new BigDecimal("0.10");
        }

        BigDecimal calculatedBase = base.multiply(multiplier).setScale(0, RoundingMode.HALF_UP);
        BigDecimal serviceFee = (passengerType == PassengerType.INFANT) ? BigDecimal.ZERO : AIRPORT_TAX_FEE;
        BigDecimal extra = extraFee != null ? extraFee : BigDecimal.ZERO;

        return calculatedBase.add(serviceFee).add(extra);
    }

    @Override
    public void validateReservation(Reservation reservation, List<Ticket> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            throw new BusinessException("Đơn đặt vé máy bay phải chọn ít nhất 1 chỗ ngồi!");
        }

        int adultCount = 0;
        int infantCount = 0;

        for (Ticket t : tickets) {
            if (t.getPassengerType() == PassengerType.ADULT) {
                adultCount++;
                if (t.getIdCard() == null || t.getIdCard().trim().isEmpty()) {
                    throw new BusinessException("Hành khách người lớn (" + t.getPassengerName() + ") bắt buộc phải nhập số CCCD hoặc Hộ chiếu!");
                }
                String idCard = t.getIdCard().trim().toUpperCase();
                if (!CCCD_PATTERN.matcher(idCard).matches() && !PASSPORT_PATTERN.matcher(idCard).matches()) {
                    throw new BusinessException("Số giấy tờ '" + idCard + "' của khách " + t.getPassengerName() + " không hợp lệ (CCCD phải gồm 12 chữ số, Hộ chiếu 1 chữ cái và 7-8 số)!");
                }
            } else if (t.getPassengerType() == PassengerType.INFANT) {
                infantCount++;
            }
        }

        if (adultCount == 0 && !tickets.isEmpty()) {
            throw new BusinessException("Chuyến bay yêu cầu phải có ít nhất 1 hành khách người lớn đi cùng!");
        }

        if (infantCount > adultCount) {
            throw new BusinessException("Theo quy chuẩn an toàn hàng không, mỗi người lớn chỉ được kèm tối đa 1 em bé dưới 2 tuổi!");
        }
    }

    @Override
    public BigDecimal calculateCancellationFee(Reservation reservation) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departure = reservation.getDepartureDate();

        if (departure.isBefore(now)) {
            throw new BusinessException("Chuyến bay đã cất cánh, không được phép hoàn hủy vé!");
        }

        long hoursBefore = Duration.between(now, departure).toHours();
        BigDecimal total = reservation.getTotalAmount();

        if (hoursBefore >= 24) {
            return total.multiply(new BigDecimal("0.10")).setScale(0, RoundingMode.HALF_UP);
        } else {
            return total.multiply(new BigDecimal("0.30")).setScale(0, RoundingMode.HALF_UP);
        }
    }

    @Override
    public String getPolicyDescription() {
        return "Quy định Hàng không: Thuế sân bay 120k/khách. Em bé 10%, Trẻ em 75%. Phí hủy >=24h là 10%, <24h là 30%.";
    }
}
