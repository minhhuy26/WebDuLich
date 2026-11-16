package com.dulich.ticketmanagement.repository;

import com.dulich.ticketmanagement.entity.Ticket;
import com.dulich.ticketmanagement.entity.enums.TicketStatus;
import com.dulich.ticketmanagement.entity.enums.TransportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketCode(String ticketCode);

    List<Ticket> findByReservationId(Long reservationId);

    List<Ticket> findByTransportTypeAndStatus(TransportType transportType, TicketStatus status);

    List<Ticket> findByStatusOrderByDepartureTimeAsc(TicketStatus status);

    @Query("SELECT DISTINCT t.transportType, t.departurePlace FROM Ticket t WHERE t.status = 'AVAILABLE' ORDER BY t.departurePlace ASC")
    List<Object[]> findDistinctAvailableDeparturesWithType();

    @Query("SELECT DISTINCT t.transportType, t.arrivalPlace FROM Ticket t WHERE t.status = 'AVAILABLE' ORDER BY t.arrivalPlace ASC")
    List<Object[]> findDistinctAvailableArrivalsWithType();

    @Query("SELECT t FROM Ticket t WHERE (:type IS NULL OR t.transportType = :type) " +
           "AND (:departure IS NULL OR LOWER(t.departurePlace) LIKE LOWER(CONCAT('%', :departure, '%')) " +
           "     OR (:departureAlt IS NOT NULL AND LOWER(t.departurePlace) LIKE LOWER(CONCAT('%', :departureAlt, '%')))) " +
           "AND (:arrival IS NULL OR LOWER(t.arrivalPlace) LIKE LOWER(CONCAT('%', :arrival, '%')) " +
           "     OR (:arrivalAlt IS NOT NULL AND LOWER(t.arrivalPlace) LIKE LOWER(CONCAT('%', :arrivalAlt, '%')))) " +
           "AND t.departureTime >= :startTime AND t.departureTime <= :endTime " +
           "AND t.status = 'AVAILABLE' ORDER BY t.departureTime ASC")
    List<Ticket> searchAvailableTickets(@Param("type") TransportType type,
                                        @Param("departure") String departure,
                                        @Param("departureAlt") String departureAlt,
                                        @Param("arrival") String arrival,
                                        @Param("arrivalAlt") String arrivalAlt,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    @Query("SELECT DISTINCT t.vehicleCode FROM Ticket t WHERE t.transportType = :type")
    List<String> findDistinctVehiclesByType(@Param("type") TransportType type);

    @Query("SELECT t FROM Ticket t WHERE t.vehicleCode = :vehicleCode AND t.departureTime = :depTime")
    List<Ticket> findByVehicleAndDepartureTime(@Param("vehicleCode") String vehicleCode,
                                              @Param("depTime") LocalDateTime depTime);

    @Query("SELECT t.seatNumber FROM Ticket t WHERE t.vehicleCode = :vehicleCode AND t.status IN ('HELD', 'ISSUED')")
    List<String> findOccupiedSeatNumbers(@Param("vehicleCode") String vehicleCode);
}
