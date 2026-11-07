package com.dulich.ticketmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "train_seats")
public class TrainSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Toa tàu không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private TrainCoach coach;

    @NotNull(message = "Số ghế không được để trống")
    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @NotBlank(message = "Nhãn ghế không được để trống")
    @Column(name = "seat_label", nullable = false, length = 100)
    private String seatLabel;

    @Column(name = "position_note", length = 100)
    private String positionNote; // Cửa sổ, Lối đi, Giường tầng 1...

    public TrainSeat() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TrainCoach getCoach() {
        return coach;
    }

    public void setCoach(TrainCoach coach) {
        this.coach = coach;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getSeatLabel() {
        return seatLabel;
    }

    public void setSeatLabel(String seatLabel) {
        this.seatLabel = seatLabel;
    }

    public String getPositionNote() {
        return positionNote;
    }

    public void setPositionNote(String positionNote) {
        this.positionNote = positionNote;
    }
}
