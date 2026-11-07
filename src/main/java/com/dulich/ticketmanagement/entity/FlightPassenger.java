package com.dulich.ticketmanagement.entity;

import com.dulich.ticketmanagement.entity.enums.PassengerType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "flight_passengers")
public class FlightPassenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_booking_id", nullable = false)
    private FlightBooking flightBooking;

    @NotBlank(message = "Họ tên hành khách không được để trống")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "id_card", length = 20)
    private String idCard;

    @NotNull(message = "Loại hành khách không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(name = "passenger_type", nullable = false, length = 20)
    private PassengerType passengerType = PassengerType.ADULT;

    @Column(name = "seat_number", length = 10)
    private String seatNumber;

    @Column(name = "baggage_kg", nullable = false)
    private Integer baggageKg = 0;

    @Column(name = "base_fare", nullable = false, precision = 18, scale = 2)
    private BigDecimal baseFare = BigDecimal.ZERO;

    @Column(name = "airport_tax", nullable = false, precision = 18, scale = 2)
    private BigDecimal airportTax = BigDecimal.ZERO;

    @Column(name = "excess_baggage_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal excessBaggageFee = BigDecimal.ZERO;

    @Column(name = "final_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal finalPrice = BigDecimal.ZERO;

    @Column(name = "ticket_code", length = 30)
    private String ticketCode;

    @Column(name = "return_seat_number", length = 10)
    private String returnSeatNumber;

    @Column(name = "return_ticket_code", length = 30)
    private String returnTicketCode;

    public FlightPassenger() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FlightBooking getFlightBooking() {
        return flightBooking;
    }

    public void setFlightBooking(FlightBooking flightBooking) {
        this.flightBooking = flightBooking;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public PassengerType getPassengerType() {
        return passengerType;
    }

    public void setPassengerType(PassengerType passengerType) {
        this.passengerType = passengerType;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Integer getBaggageKg() {
        return baggageKg;
    }

    public void setBaggageKg(Integer baggageKg) {
        this.baggageKg = baggageKg;
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }

    public BigDecimal getAirportTax() {
        return airportTax;
    }

    public void setAirportTax(BigDecimal airportTax) {
        this.airportTax = airportTax;
    }

    public BigDecimal getExcessBaggageFee() {
        return excessBaggageFee;
    }

    public void setExcessBaggageFee(BigDecimal excessBaggageFee) {
        this.excessBaggageFee = excessBaggageFee;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public String getReturnSeatNumber() {
        return returnSeatNumber;
    }

    public void setReturnSeatNumber(String returnSeatNumber) {
        this.returnSeatNumber = returnSeatNumber;
    }

    public String getReturnTicketCode() {
        return returnTicketCode;
    }

    public void setReturnTicketCode(String returnTicketCode) {
        this.returnTicketCode = returnTicketCode;
    }
}
