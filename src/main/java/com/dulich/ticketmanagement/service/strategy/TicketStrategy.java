package com.dulich.ticketmanagement.service.strategy;

import com.dulich.ticketmanagement.entity.Reservation;
import com.dulich.ticketmanagement.entity.Ticket;
import com.dulich.ticketmanagement.entity.enums.PassengerType;
import com.dulich.ticketmanagement.entity.enums.TransportType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interface Strategy Pattern định nghĩa bộ quy tắc nghiệp vụ đặc thù
 * cho từng phương tiện vận tải (Hàng không, Tàu hỏa, Xe khách, Tàu thủy)
 * Tuân thủ nguyên lý Open/Closed Principle (OCP) theo chuẩn đề cương của Giảng viên.
 */
public interface TicketStrategy {

    TransportType getTransportType();

    /**
     * Thuật toán tính toán giá vé cuối cùng sau khi cộng trừ thuế phí, phụ thu và giảm giá đối tượng
     */
    BigDecimal calculatePrice(Ticket ticket, PassengerType passengerType, BigDecimal extraFee);

    /**
     * Thẩm định tính hợp lệ của đơn đặt chỗ (CCCD/Hộ chiếu, tỉ lệ trẻ em/người lớn, thời gian đặt tối thiểu...)
     */
    void validateReservation(Reservation reservation, List<Ticket> tickets);

    /**
     * Tính toán phí hủy vé dựa trên khung thời gian đến giờ khởi hành
     */
    BigDecimal calculateCancellationFee(Reservation reservation);

    /**
     * Mô tả chính sách và quy định đặc thù của phương tiện
     */
    String getPolicyDescription();
}
