package com.dulich.ticketmanagement.repository;

import com.dulich.ticketmanagement.entity.TrainBooking;
import com.dulich.ticketmanagement.entity.enums.TrainBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainBookingRepository extends JpaRepository<TrainBooking, Long> {

    @Query("SELECT b FROM TrainBooking b " +
           "LEFT JOIN FETCH b.train " +
           "LEFT JOIN FETCH b.returnTrain " +
           "LEFT JOIN FETCH b.departureStation " +
           "LEFT JOIN FETCH b.arrivalStation " +
           "LEFT JOIN FETCH b.tickets t " +
           "LEFT JOIN FETCH t.seat s " +
           "LEFT JOIN FETCH s.coach " +
           "LEFT JOIN FETCH t.returnSeat rs " +
           "LEFT JOIN FETCH rs.coach " +
           "WHERE b.bookingCode = :code")
    Optional<TrainBooking> findByBookingCodeWithDetails(@Param("code") String bookingCode);

    Optional<TrainBooking> findByBookingCode(String bookingCode);

    @Query("SELECT b FROM TrainBooking b WHERE b.status = :status AND b.holdExpiresAt < :now")
    List<TrainBooking> findExpiredBookings(@Param("status") TrainBookingStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM TrainBooking b " +
           "LEFT JOIN FETCH b.train " +
           "LEFT JOIN FETCH b.returnTrain " +
           "LEFT JOIN FETCH b.departureStation " +
           "LEFT JOIN FETCH b.arrivalStation " +
           "ORDER BY b.bookingTime DESC")
    List<TrainBooking> findAllWithDetails();

    List<TrainBooking> findByCustomerPhone(String customerPhone);
    List<TrainBooking> findByCustomerEmail(String customerEmail);
}
