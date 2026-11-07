package com.dulich.ticketmanagement.repository;

import com.dulich.ticketmanagement.entity.Flight;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Flight f WHERE f.id = :id")
    Optional<Flight> findByIdForUpdate(@Param("id") Long id);

    Optional<Flight> findByFlightNumber(String flightNumber);

    @Query("SELECT f FROM Flight f WHERE f.status = 'ACTIVE' " +
           "AND (:departureAirport IS NULL OR f.departureAirport = :departureAirport OR f.departureCity LIKE %:departureAirport%) " +
           "AND (:arrivalAirport IS NULL OR f.arrivalAirport = :arrivalAirport OR f.arrivalCity LIKE %:arrivalAirport%) " +
           "AND f.departureTime >= :startTime " +
           "AND (:endTime IS NULL OR f.departureTime <= :endTime) " +
           "ORDER BY f.departureTime ASC")
    List<Flight> searchFlights(@Param("departureAirport") String departureAirport,
                               @Param("arrivalAirport") String arrivalAirport,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);

    @Query("SELECT f FROM Flight f WHERE f.status = 'ACTIVE' AND f.departureTime > :now ORDER BY f.departureTime ASC")
    List<Flight> findUpcomingFlights(@Param("now") LocalDateTime now);
}
