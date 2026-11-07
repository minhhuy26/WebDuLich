package com.dulich.ticketmanagement.dto;

import com.dulich.ticketmanagement.entity.enums.FlightSeatClass;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class FlightSearchDto {
    private String departureAirport;
    private String arrivalAirport;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate departureDate;

    private boolean roundTrip = false;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate returnDate;

    private FlightSeatClass seatClass = FlightSeatClass.ECONOMY;
    private Integer passengerCount = 1;

    public FlightSearchDto() {
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

    public String getDepartureAirport() {
        return departureAirport;
    }

    public void setDepartureAirport(String departureAirport) {
        this.departureAirport = departureAirport;
    }

    public String getArrivalAirport() {
        return arrivalAirport;
    }

    public void setArrivalAirport(String arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public FlightSeatClass getSeatClass() {
        return seatClass;
    }

    public void setSeatClass(FlightSeatClass seatClass) {
        this.seatClass = seatClass;
    }

    public Integer getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(Integer passengerCount) {
        this.passengerCount = passengerCount;
    }
}
