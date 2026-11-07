package com.dulich.ticketmanagement.repository;

import com.dulich.ticketmanagement.entity.FlightBooking;
import com.dulich.ticketmanagement.entity.enums.FlightBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightBookingRepository extends JpaRepository<FlightBooking, Long> {

    @Query("SELECT b FROM FlightBooking b LEFT JOIN FETCH b.flight LEFT JOIN FETCH b.returnFlight LEFT JOIN FETCH b.passengers WHERE b.bookingCode = :code")
    Optional<FlightBooking> findByBookingCodeWithDetails(@Param("code") String bookingCode);

    Optional<FlightBooking> findByBookingCode(String bookingCode);

    @Query("SELECT b FROM FlightBooking b WHERE b.status = :status AND b.holdExpiresAt < :now")
    List<FlightBooking> findExpiredBookings(@Param("status") FlightBookingStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM FlightBooking b LEFT JOIN FETCH b.flight LEFT JOIN FETCH b.returnFlight ORDER BY b.bookingTime DESC")
    List<FlightBooking> findAllWithFlight();

    List<FlightBooking> findByCustomerPhone(String customerPhone);
    List<FlightBooking> findByCustomerEmail(String customerEmail);
}
