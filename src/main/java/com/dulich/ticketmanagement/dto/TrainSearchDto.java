package com.dulich.ticketmanagement.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class TrainSearchDto {

    private Long departureStationId;
    private Long arrivalStationId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate departureDate;

    private boolean roundTrip = false;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate returnDate;

    public TrainSearchDto() {
    }

    public boolean isRoundTrip() {
        return roundTrip;
    }

    public void setRoundTrip(boolean roundTrip) {
        this.roundTrip = roundTrip;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public Long getDepartureStationId() {
        return departureStationId;
    }

    public void setDepartureStationId(Long departureStationId) {
        this.departureStationId = departureStationId;
    }

    public Long getArrivalStationId() {
        return arrivalStationId;
    }

    public void setArrivalStationId(Long arrivalStationId) {
        this.arrivalStationId = arrivalStationId;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }
}
