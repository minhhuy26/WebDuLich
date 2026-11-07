package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.service.impl.FlightTicketService;
import com.dulich.ticketmanagement.service.impl.TrainTicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BookingHoldExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(BookingHoldExpirationScheduler.class);

    private final FlightTicketService flightTicketService;
    private final TrainTicketService trainTicketService;

    @Autowired
    public BookingHoldExpirationScheduler(FlightTicketService flightTicketService, TrainTicketService trainTicketService) {
        this.flightTicketService = flightTicketService;
        this.trainTicketService = trainTicketService;
    }

    /**
     * Tự động quét và nhả chỗ định kỳ mỗi 60 giây:
     * - Vé máy bay: Hết hạn sau 15 phút
     * - Vé tàu hỏa: Hết hạn sau 10 phút
     */
    @Scheduled(fixedRate = 60000)
    public void runHoldExpirationCleanup() {
        try {
            int releasedFlights = flightTicketService.releaseExpiredHoldBookings();
            if (releasedFlights > 0) {
                log.info("[SCHEDULED TASK] Đã nhả ghế tự động cho {} đơn vé máy bay hết hạn 15 phút.", releasedFlights);
            }

            int releasedTrains = trainTicketService.releaseExpiredHoldBookings();
            if (releasedTrains > 0) {
                log.info("[SCHEDULED TASK] Đã nhả chỗ tự động cho {} đơn vé tàu hỏa hết hạn 10 phút.", releasedTrains);
            }
        } catch (Exception ex) {
            log.error("[SCHEDULED TASK ERROR] Lỗi khi thực hiện quét dọn đơn giữ chỗ hết hạn: {}", ex.getMessage());
        }
    }
}
