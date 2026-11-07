package com.dulich.ticketmanagement.repository;

import com.dulich.ticketmanagement.entity.Booking;
import com.dulich.ticketmanagement.entity.enums.BookingStatus;
import com.dulich.ticketmanagement.entity.enums.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingCode(String bookingCode);
    List<Booking> findByCustomerId(Long customerId);
    List<Booking> findByTicketType(TicketType ticketType);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByTicketTypeAndStatus(TicketType ticketType, BookingStatus status);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.customer LEFT JOIN FETCH b.ticketDetails ORDER BY b.createdAt DESC")
    List<Booking> findAllWithDetails();
}
