package com.dulich.ticketmanagement.repository;

import com.dulich.ticketmanagement.entity.TrainSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainSeatRepository extends JpaRepository<TrainSeat, Long> {

    List<TrainSeat> findByCoachIdOrderBySeatNumberAsc(Long coachId);
}
