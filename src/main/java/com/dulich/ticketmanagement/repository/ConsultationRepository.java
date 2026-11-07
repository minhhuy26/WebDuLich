package com.dulich.ticketmanagement.repository;

import com.dulich.ticketmanagement.entity.Consultation;
import com.dulich.ticketmanagement.entity.enums.ConsultationStatus;
import com.dulich.ticketmanagement.entity.enums.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    Optional<Consultation> findByConsultationCode(String consultationCode);
    List<Consultation> findByPhone(String phone);
    List<Consultation> findByEmail(String email);
    List<Consultation> findByTicketType(TicketType ticketType);
    List<Consultation> findByStatus(ConsultationStatus status);
    List<Consultation> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(c) FROM Consultation c WHERE c.preferredDate >= :startTime AND c.preferredDate < :endTime AND c.status != :excludeStatus AND (:excludeId IS NULL OR c.id != :excludeId)")
    long countAppointmentsInSlot(@Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime,
                                 @Param("excludeStatus") ConsultationStatus excludeStatus,
                                 @Param("excludeId") Long excludeId);
}
