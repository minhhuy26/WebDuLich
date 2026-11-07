package com.dulich.ticketmanagement.repository;

import com.dulich.ticketmanagement.entity.TicketDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketDetailRepository extends JpaRepository<TicketDetail, Long> {
    List<TicketDetail> findByBookingId(Long bookingId);
}
