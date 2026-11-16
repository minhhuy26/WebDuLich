package com.dulich.ticketmanagement.repository;

import com.dulich.ticketmanagement.entity.Appointment;
import com.dulich.ticketmanagement.entity.enums.AppointmentStatus;
import com.dulich.ticketmanagement.entity.enums.TransportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByAppointmentCode(String appointmentCode);

    List<Appointment> findByPhoneOrderByCreatedAtDesc(String phone);

    List<Appointment> findByStatusOrderByPreferredDateAsc(AppointmentStatus status);

    List<Appointment> findByTransportTypeOrderByCreatedAtDesc(TransportType transportType);

    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.user LEFT JOIN FETCH a.assignedOperator ORDER BY a.createdAt DESC")
    List<Appointment> findAllWithDetails();

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.preferredDate BETWEEN :start AND :end AND a.status <> 'CANCELLED'")
    long countAppointmentsOnDate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
