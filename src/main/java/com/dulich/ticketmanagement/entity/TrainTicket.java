package com.dulich.ticketmanagement.entity;

import com.dulich.ticketmanagement.entity.enums.PassengerPriority;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "train_tickets")
public class TrainTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_booking_id", nullable = false)
    private TrainBooking trainBooking;

    @NotNull(message = "Chỗ ngồi không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private TrainSeat seat;

    @NotBlank(message = "Tên hành khách không được để trống")
    @Column(name = "passenger_name", nullable = false, length = 100)
    private String passengerName;

    @Column(name = "passenger_id_card", length = 20)
    private String passengerIdCard;

    @NotNull(message = "Đối tượng ưu tiên không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(name = "priority_category", nullable = false, length = 30)
    private PassengerPriority priorityCategory = PassengerPriority.STANDARD;

    @Column(name = "priority_doc_id", length = 50)
    private String priorityDocId; // Mã sinh viên, CCCD người già...

    @Column(name = "departure_station_index", nullable = false)
    private Integer departureStationIndex;

    @Column(name = "arrival_station_index", nullable = false)
    private Integer arrivalStationIndex;

    @Column(name = "distance_km", nullable = false)
    private Integer distanceKm;

    @Column(name = "original_fare", nullable = false, precision = 18, scale = 2)
    private BigDecimal originalFare = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "final_fare", nullable = false, precision = 18, scale = 2)
    private BigDecimal finalFare = BigDecimal.ZERO;

    @Column(name = "ticket_code", length = 30)
    private String ticketCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_seat_id")
    private TrainSeat returnSeat;

    @Column(name = "return_ticket_code", length = 30)
    private String returnTicketCode;

    @Column(name = "return_original_fare", nullable = false, precision = 18, scale = 2)
    private BigDecimal returnOriginalFare = BigDecimal.ZERO;

    @Column(name = "return_discount_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal returnDiscountAmount = BigDecimal.ZERO;

    @Column(name = "return_final_fare", nullable = false, precision = 18, scale = 2)
    private BigDecimal returnFinalFare = BigDecimal.ZERO;

    public TrainTicket() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TrainBooking getTrainBooking() {
        return trainBooking;
    }

    public void setTrainBooking(TrainBooking trainBooking) {
        this.trainBooking = trainBooking;
    }

    public TrainSeat getSeat() {
        return seat;
    }

    public void setSeat(TrainSeat seat) {
        this.seat = seat;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPassengerIdCard() {
        return passengerIdCard;
    }

    public void setPassengerIdCard(String passengerIdCard) {
        this.passengerIdCard = passengerIdCard;
    }

    public PassengerPriority getPriorityCategory() {
        return priorityCategory;
    }

    public void setPriorityCategory(PassengerPriority priorityCategory) {
        this.priorityCategory = priorityCategory;
    }

    public String getPriorityDocId() {
        return priorityDocId;
    }

    public void setPriorityDocId(String priorityDocId) {
        this.priorityDocId = priorityDocId;
    }

    public Integer getDepartureStationIndex() {
        return departureStationIndex;
    }

    public void setDepartureStationIndex(Integer departureStationIndex) {
        this.departureStationIndex = departureStationIndex;
    }

    public Integer getArrivalStationIndex() {
        return arrivalStationIndex;
    }

    public void setArrivalStationIndex(Integer arrivalStationIndex) {
        this.arrivalStationIndex = arrivalStationIndex;
    }

    public Integer getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Integer distanceKm) {
        this.distanceKm = distanceKm;
    }

    public BigDecimal getOriginalFare() {
        return originalFare;
    }

    public void setOriginalFare(BigDecimal originalFare) {
        this.originalFare = originalFare;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getFinalFare() {
        return finalFare;
    }

    public void setFinalFare(BigDecimal finalFare) {
        this.finalFare = finalFare;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public TrainSeat getReturnSeat() {
        return returnSeat;
    }

    public void setReturnSeat(TrainSeat returnSeat) {
        this.returnSeat = returnSeat;
    }

    public String getReturnTicketCode() {
        return returnTicketCode;
    }

    public void setReturnTicketCode(String returnTicketCode) {
        this.returnTicketCode = returnTicketCode;
    }

    public BigDecimal getReturnOriginalFare() {
        return returnOriginalFare;
    }

    public void setReturnOriginalFare(BigDecimal returnOriginalFare) {
        this.returnOriginalFare = returnOriginalFare;
    }

    public BigDecimal getReturnDiscountAmount() {
        return returnDiscountAmount;
    }

    public void setReturnDiscountAmount(BigDecimal returnDiscountAmount) {
        this.returnDiscountAmount = returnDiscountAmount;
    }

    public BigDecimal getReturnFinalFare() {
        return returnFinalFare;
    }

    public void setReturnFinalFare(BigDecimal returnFinalFare) {
        this.returnFinalFare = returnFinalFare;
    }
}
