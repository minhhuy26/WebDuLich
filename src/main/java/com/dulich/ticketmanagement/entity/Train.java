package com.dulich.ticketmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trains")
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Mã đoàn tàu không được để trống")
    @Column(name = "train_code", nullable = false, unique = true, length = 20)
    private String trainCode;

    @NotBlank(message = "Tên tuyến không được để trống")
    @Column(name = "route_name", nullable = false, length = 100)
    private String routeName;

    @NotBlank(message = "Ga xuất phát không được để trống")
    @Column(name = "departure_station_name", nullable = false, length = 100)
    private String departureStationName;

    @NotBlank(message = "Ga cuối không được để trống")
    @Column(name = "arrival_station_name", nullable = false, length = 100)
    private String arrivalStationName;

    @Column(name = "departure_time_of_day", nullable = false, length = 10)
    private String departureTimeOfDay; // 06:00, 19:25...

    @Column(name = "travel_hours", nullable = false)
    private Integer travelHours = 32;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TrainCoach> coaches = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Train() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrainCode() {
        return trainCode;
    }

    public void setTrainCode(String trainCode) {
        this.trainCode = trainCode;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getDepartureStationName() {
        return departureStationName;
    }

    public void setDepartureStationName(String departureStationName) {
        this.departureStationName = departureStationName;
    }

    public String getArrivalStationName() {
        return arrivalStationName;
    }

    public void setArrivalStationName(String arrivalStationName) {
        this.arrivalStationName = arrivalStationName;
    }

    public String getDepartureTimeOfDay() {
        return departureTimeOfDay;
    }

    public void setDepartureTimeOfDay(String departureTimeOfDay) {
        this.departureTimeOfDay = departureTimeOfDay;
    }

    public Integer getTravelHours() {
        return travelHours;
    }

    public void setTravelHours(Integer travelHours) {
        this.travelHours = travelHours;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<TrainCoach> getCoaches() {
        return coaches;
    }

    public void setCoaches(List<TrainCoach> coaches) {
        this.coaches = coaches;
    }
}
