package com.dulich.ticketmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "train_stations")
public class TrainStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên tuyến không được để trống")
    @Column(name = "route_name", nullable = false, length = 100)
    private String routeName;

    @NotBlank(message = "Mã ga không được để trống")
    @Column(name = "station_code", nullable = false, length = 20)
    private String stationCode;

    @NotBlank(message = "Tên ga không được để trống")
    @Column(name = "station_name", nullable = false, length = 100)
    private String stationName;

    @NotNull(message = "Thứ tự ga không được để trống")
    @Column(name = "station_index", nullable = false)
    private Integer stationIndex; // 0, 1, 2...

    @NotNull(message = "Cự ly km không được để trống")
    @Column(name = "distance_km", nullable = false)
    private Integer distanceKm;

    public TrainStation() {
    }

    public TrainStation(String routeName, String stationCode, String stationName, Integer stationIndex, Integer distanceKm) {
        this.routeName = routeName;
        this.stationCode = stationCode;
        this.stationName = stationName;
        this.stationIndex = stationIndex;
        this.distanceKm = distanceKm;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getStationCode() {
        return stationCode;
    }

    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public Integer getStationIndex() {
        return stationIndex;
    }

    public void setStationIndex(Integer stationIndex) {
        this.stationIndex = stationIndex;
    }

    public Integer getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Integer distanceKm) {
        this.distanceKm = distanceKm;
    }
}
