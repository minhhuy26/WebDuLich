package com.dulich.ticketmanagement.repository;

import com.dulich.ticketmanagement.entity.Reservation;
import com.dulich.ticketmanagement.entity.enums.ReservationStatus;
import com.dulich.ticketmanagement.entity.enums.TransportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByBookingCode(String bookingCode);

    @Query("SELECT r FROM Reservation r LEFT JOIN FETCH r.tickets WHERE r.bookingCode = :code")
    Optional<Reservation> findByBookingCodeWithTickets(@Param("code") String code);

    List<Reservation> findByCustomerPhoneOrderByCreatedAtDesc(String customerPhone);

    List<Reservation> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);

    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Reservation> findByStatusOrderByCreatedAtDesc(ReservationStatus status);

    List<Reservation> findByTransportTypeOrderByCreatedAtDesc(TransportType transportType);

    @Query("SELECT r FROM Reservation r WHERE r.status = :status AND r.holdExpiresAt < :now")
    List<Reservation> findExpiredReservations(@Param("status") ReservationStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT r FROM Reservation r LEFT JOIN FETCH r.tickets LEFT JOIN FETCH r.user ORDER BY r.createdAt DESC")
    List<Reservation> findAllWithDetails();

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.status = :status")
    long countByStatus(@Param("status") ReservationStatus status);

    @Query("SELECT COALESCE(SUM(r.totalAmount), 0) FROM Reservation r WHERE r.status = 'CONFIRMED'")
    java.math.BigDecimal calculateTotalRevenue();
}
