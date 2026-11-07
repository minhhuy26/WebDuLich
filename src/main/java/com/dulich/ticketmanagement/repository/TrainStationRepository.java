package com.dulich.ticketmanagement.repository;

import com.dulich.ticketmanagement.entity.TrainStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainStationRepository extends JpaRepository<TrainStation, Long> {

    List<TrainStation> findAllByOrderByStationIndexAsc();

    List<TrainStation> findByRouteNameOrderByStationIndexAsc(String routeName);

    Optional<TrainStation> findByStationCode(String stationCode);

    Optional<TrainStation> findByStationName(String stationName);
}
