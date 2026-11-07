package com.dulich.ticketmanagement.repository;

import com.dulich.ticketmanagement.entity.FlightPassenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface FlightPassengerRepository extends JpaRepository<FlightPassenger, Long> {

    List<FlightPassenger> findByFlightBookingId(Long flightBookingId);

    @Query("SELECT p.seatNumber FROM FlightPassenger p " +
           "WHERE p.flightBooking.flight.id = :flightId " +
           "AND p.flightBooking.status IN ('HELD', 'CONFIRMED', 'ISSUED', 'CHANGED') " +
           "AND p.seatNumber IS NOT NULL")
    Set<String> findOccupiedSeatNumbers(@Param("flightId") Long flightId);

    @Query("SELECT p.returnSeatNumber FROM FlightPassenger p " +
           "WHERE p.flightBooking.returnFlight.id = :flightId " +
           "AND p.flightBooking.status IN ('HELD', 'CONFIRMED', 'ISSUED', 'CHANGED') " +
           "AND p.returnSeatNumber IS NOT NULL")
    Set<String> findOccupiedReturnSeatNumbers(@Param("flightId") Long flightId);
}
