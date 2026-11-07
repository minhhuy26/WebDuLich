package com.dulich.ticketmanagement.entity;

import com.dulich.ticketmanagement.entity.enums.CoachType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "train_coaches")
public class TrainCoach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Đoàn tàu không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @NotNull(message = "Số thứ tự toa không được để trống")
    @Column(name = "coach_number", nullable = false)
    private Integer coachNumber; // 1, 2, 3...

    @NotBlank(message = "Tên toa không được để trống")
    @Column(name = "coach_name", nullable = false, length = 50)
    private String coachName;

    @NotNull(message = "Loại toa không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(name = "coach_type", nullable = false, length = 30)
    private CoachType coachType;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @NotNull(message = "Đơn giá km không được để trống")
    @Column(name = "price_per_km", nullable = false, precision = 18, scale = 2)
    private BigDecimal pricePerKm;

    @OneToMany(mappedBy = "coach", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("seatNumber ASC")
    private List<TrainSeat> seats = new ArrayList<>();

    public TrainCoach() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public Integer getCoachNumber() {
        return coachNumber;
    }

    public void setCoachNumber(Integer coachNumber) {
        this.coachNumber = coachNumber;
    }

    public String getCoachName() {
        return coachName;
    }

    public void setCoachName(String coachName) {
        this.coachName = coachName;
    }

    public CoachType getCoachType() {
        return coachType;
    }

    public void setCoachType(CoachType coachType) {
        this.coachType = coachType;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }

    public BigDecimal getPricePerKm() {
        return pricePerKm;
    }

    public void setPricePerKm(BigDecimal pricePerKm) {
        this.pricePerKm = pricePerKm;
    }

    public List<TrainSeat> getSeats() {
        return seats;
    }

    public void setSeats(List<TrainSeat> seats) {
        this.seats = seats;
    }
}
