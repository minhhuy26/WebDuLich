package com.dulich.ticketmanagement.repository;

import com.dulich.ticketmanagement.entity.TrainCoach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainCoachRepository extends JpaRepository<TrainCoach, Long> {

    List<TrainCoach> findByTrainIdOrderByCoachNumberAsc(Long trainId);

    @Query("SELECT c FROM TrainCoach c LEFT JOIN FETCH c.seats WHERE c.id = :id")
    Optional<TrainCoach> findByIdWithSeats(@Param("id") Long coachId);
}
