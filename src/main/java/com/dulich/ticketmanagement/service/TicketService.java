package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.entity.Booking;
import com.dulich.ticketmanagement.entity.enums.TicketType;

import java.math.BigDecimal;

/**
 * Interface chung định nghĩa các nghiệp vụ cốt lõi cho từng loại vé:
 * - Quy trình đặt vé
 * - Thuật toán tính phí và giá vé
 * - Quy tắc kiểm tra tính hợp lệ (Validation)
 * - Chính sách hủy/đổi vé
 */
public interface TicketService {

    /**
     * Xác định loại vé phụ trách (VÉ MÁY BAY hoặc VÉ TÀU HỎA)
     */
    TicketType getTicketType();

    /**
     * Thuật toán tính toán tổng chi phí và các phụ phí theo chính sách từng loại vé
     *
     * @param basePrice Giá vé cơ bản
     * @param seatClass Hạng ghế/khoang (Economy/Business với Máy bay, Ghế mềm/Giường nằm với Tàu hỏa)
     * @param passengerCount Số lượng hành khách
     * @param extraServices Phí dịch vụ bổ sung (hành lý, suất ăn...)
     * @return Tổng số tiền phải trả
     */
    BigDecimal calculateTotalFare(BigDecimal basePrice, String seatClass, int passengerCount, BigDecimal extraServices);

    /**
     * Kiểm tra tính hợp lệ nghiệp vụ riêng biệt cho loại vé
     * (Máy bay: CCCD/Hộ chiếu, quy chuẩn hành lý, thời gian tối thiểu trước giờ bay...)
     * (Tàu hỏa: Ga xuất phát/đến, vị trí toa, giường nằm tầng 1/2...)
     */
    void validateBooking(Booking booking);

    /**
     * Xử lý quy trình đặt chỗ và xuất vé
     */
    void processBooking(Booking booking);

    /**
     * Chính sách xử lý hủy/đổi vé theo quy định riêng của loại vé
     */
    void cancelBooking(Booking booking, String reason);
}
