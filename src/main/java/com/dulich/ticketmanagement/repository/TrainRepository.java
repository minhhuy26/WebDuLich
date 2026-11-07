package com.dulich.ticketmanagement.repository;

import com.dulich.ticketmanagement.entity.Train;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Train t WHERE t.id = :id")
    Optional<Train> findByIdForUpdate(@Param("id") Long id);

    Optional<Train> findByTrainCode(String trainCode);

    List<Train> findByStatus(String status);

    @Query("SELECT t FROM Train t WHERE t.status = 'ACTIVE' AND t.departureStationName LIKE %:keyword% ORDER BY t.trainCode ASC")
    List<Train> findActiveByDepartureKeyword(@Param("keyword") String keyword);

    @Query("SELECT t FROM Train t LEFT JOIN FETCH t.coaches c WHERE t.id = :id")
    Optional<Train> findByIdWithCoaches(@Param("id") Long id);
}
