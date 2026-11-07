package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.entity.FlightBooking;
import com.dulich.ticketmanagement.entity.TrainBooking;

/**
 * Dịch vụ gửi email thông báo vé tự động về Gmail của khách hàng:
 * - Thông báo giữ chỗ (HELD) kèm thời gian đếm ngược
 * - Thông báo xác nhận thanh toán & vé điện tử chính thức (CONFIRMED / ISSUED)
 * - Thông báo hủy vé & hoàn tiền (CANCELLED)
 */
public interface EmailService {

    void sendFlightBookingHeldEmail(FlightBooking booking);

    void sendFlightBookingConfirmedEmail(FlightBooking booking);

    void sendFlightBookingCancelledEmail(FlightBooking booking);

    void sendTrainBookingHeldEmail(TrainBooking booking);

    void sendTrainBookingConfirmedEmail(TrainBooking booking);

    void sendTrainBookingCancelledEmail(TrainBooking booking);
}
