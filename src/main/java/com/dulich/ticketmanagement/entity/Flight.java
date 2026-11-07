package com.dulich.ticketmanagement.entity;

import com.dulich.ticketmanagement.entity.enums.FlightSeatClass;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "flights")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Số hiệu chuyến bay không được để trống")
    @Column(name = "flight_number", nullable = false, unique = true, length = 20)
    private String flightNumber;

    @NotBlank(message = "Hãng hàng không không được để trống")
    @Column(name = "airline", nullable = false, length = 100)
    private String airline;

    @NotBlank(message = "Mã sân bay đi không được để trống")
    @Column(name = "departure_airport", nullable = false, length = 10)
    private String departureAirport;

    @NotBlank(message = "Thành phố đi không được để trống")
    @Column(name = "departure_city", nullable = false, length = 100)
    private String departureCity;

    @NotBlank(message = "Mã sân bay đến không được để trống")
    @Column(name = "arrival_airport", nullable = false, length = 10)
    private String arrivalAirport;

    @NotBlank(message = "Thành phố đến không được để trống")
    @Column(name = "arrival_city", nullable = false, length = 100)
    private String arrivalCity;

    @NotNull(message = "Thời gian khởi hành không được để trống")
    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @NotNull(message = "Thời gian hạ cánh không được để trống")
    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @NotNull(message = "Giá vé cơ bản không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá cơ bản phải lớn hơn 0")
    @Column(name = "base_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "economy_seats_total", nullable = false)
    private Integer economySeatsTotal = 150;

    @Column(name = "economy_seats_available", nullable = false)
    private Integer economySeatsAvailable = 150;

    @Column(name = "premium_seats_total", nullable = false)
    private Integer premiumSeatsTotal = 30;

    @Column(name = "premium_seats_available", nullable = false)
    private Integer premiumSeatsAvailable = 30;

    @Column(name = "business_seats_total", nullable = false)
    private Integer businessSeatsTotal = 16;

    @Column(name = "business_seats_available", nullable = false)
    private Integer businessSeatsAvailable = 16;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Flight() {
    }

    public int getAvailableSeats(FlightSeatClass seatClass) {
        return switch (seatClass) {
            case ECONOMY -> economySeatsAvailable != null ? economySeatsAvailable : 0;
            case PREMIUM -> premiumSeatsAvailable != null ? premiumSeatsAvailable : 0;
            case BUSINESS -> businessSeatsAvailable != null ? businessSeatsAvailable : 0;
        };
    }

    public void reduceAvailableSeats(FlightSeatClass seatClass, int count) {
        switch (seatClass) {
            case ECONOMY -> this.economySeatsAvailable = Math.max(0, this.economySeatsAvailable - count);
            case PREMIUM -> this.premiumSeatsAvailable = Math.max(0, this.premiumSeatsAvailable - count);
            case BUSINESS -> this.businessSeatsAvailable = Math.max(0, this.businessSeatsAvailable - count);
        }
    }

    public void restoreAvailableSeats(FlightSeatClass seatClass, int count) {
        switch (seatClass) {
            case ECONOMY -> this.economySeatsAvailable = Math.min(this.economySeatsTotal, this.economySeatsAvailable + count);
            case PREMIUM -> this.premiumSeatsAvailable = Math.min(this.premiumSeatsTotal, this.premiumSeatsAvailable + count);
            case BUSINESS -> this.businessSeatsAvailable = Math.min(this.businessSeatsTotal, this.businessSeatsAvailable + count);
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public String getDepartureAirport() {
        return departureAirport;
    }

    public void setDepartureAirport(String departureAirport) {
        this.departureAirport = departureAirport;
    }

    public String getDepartureCity() {
        return departureCity;
    }

    public void setDepartureCity(String departureCity) {
        this.departureCity = departureCity;
    }

    public String getArrivalAirport() {
        return arrivalAirport;
    }

    public void setArrivalAirport(String arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
    }

    public String getArrivalCity() {
        return arrivalCity;
    }

    public void setArrivalCity(String arrivalCity) {
        this.arrivalCity = arrivalCity;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public Integer getEconomySeatsTotal() {
        return economySeatsTotal;
    }

    public void setEconomySeatsTotal(Integer economySeatsTotal) {
        this.economySeatsTotal = economySeatsTotal;
    }

    public Integer getEconomySeatsAvailable() {
        return economySeatsAvailable;
    }

    public void setEconomySeatsAvailable(Integer economySeatsAvailable) {
        this.economySeatsAvailable = economySeatsAvailable;
    }

    public Integer getPremiumSeatsTotal() {
        return premiumSeatsTotal;
    }

    public void setPremiumSeatsTotal(Integer premiumSeatsTotal) {
        this.premiumSeatsTotal = premiumSeatsTotal;
    }

    public Integer getPremiumSeatsAvailable() {
        return premiumSeatsAvailable;
    }

    public void setPremiumSeatsAvailable(Integer premiumSeatsAvailable) {
        this.premiumSeatsAvailable = premiumSeatsAvailable;
    }

    public Integer getBusinessSeatsTotal() {
        return businessSeatsTotal;
    }

    public void setBusinessSeatsTotal(Integer businessSeatsTotal) {
        this.businessSeatsTotal = businessSeatsTotal;
    }

    public Integer getBusinessSeatsAvailable() {
        return businessSeatsAvailable;
    }

    public void setBusinessSeatsAvailable(Integer businessSeatsAvailable) {
        this.businessSeatsAvailable = businessSeatsAvailable;
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
}
