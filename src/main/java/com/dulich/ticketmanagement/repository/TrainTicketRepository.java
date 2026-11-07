package com.dulich.ticketmanagement.repository;

import com.dulich.ticketmanagement.entity.TrainTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainTicketRepository extends JpaRepository<TrainTicket, Long> {

    List<TrainTicket> findByTrainBookingId(Long trainBookingId);

    /**
     * Lấy tất cả các vé đang có hiệu lực (HELD, CONFIRMED, ISSUED) của đoàn tàu trong một ngày khởi hành
     * để phục vụ thuật toán kiểm tra chồng lấn chặng [depIndex, arrIndex)
     */
    @Query("SELECT t FROM TrainTicket t " +
           "JOIN t.trainBooking b " +
           "WHERE ((b.train.id = :trainId AND b.departureDate = :date) " +
           "    OR (b.isRoundTrip = true AND b.returnTrain.id = :trainId AND b.returnDepartureDate = :date)) " +
           "AND b.status IN ('HELD', 'CONFIRMED', 'ISSUED')")
    List<TrainTicket> findActiveTicketsOnDate(@Param("trainId") Long trainId, @Param("date") LocalDate date);

    /**
     * Lấy các vé đang chiếm của một ghế cụ thể trên tàu vào ngày đi
     */
    @Query("SELECT t FROM TrainTicket t " +
           "JOIN t.trainBooking b " +
           "WHERE ((t.seat.id = :seatId AND b.departureDate = :date) " +
           "    OR (b.isRoundTrip = true AND t.returnSeat.id = :seatId AND b.returnDepartureDate = :date)) " +
           "AND b.status IN ('HELD', 'CONFIRMED', 'ISSUED')")
    List<TrainTicket> findActiveTicketsForSeat(@Param("seatId") Long seatId, @Param("date") LocalDate date);
}
