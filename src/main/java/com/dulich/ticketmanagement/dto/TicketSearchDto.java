package com.dulich.ticketmanagement.dto;

import com.dulich.ticketmanagement.entity.enums.TransportType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class TicketSearchDto {

    private TransportType transportType = null;
    private String departurePlace;
    private String arrivalPlace;
    private String departureAirport;
    private String arrivalAirport;
    private String departureStation;
    private String arrivalStation;
    private String seatClass;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate departureDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate returnDate;

    private boolean roundTrip = false;
    private int passengers = 1;

    public TicketSearchDto() {
    }

    public String getDepartureAirport() {
        return departureAirport;
    }

    public void setDepartureAirport(String departureAirport) {
        this.departureAirport = departureAirport;
        if (departureAirport != null && !departureAirport.trim().isEmpty() && this.departurePlace == null) {
            this.departurePlace = departureAirport.trim();
        }
    }

    public String getArrivalAirport() {
        return arrivalAirport;
    }

    public void setArrivalAirport(String arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
        if (arrivalAirport != null && !arrivalAirport.trim().isEmpty() && this.arrivalPlace == null) {
            this.arrivalPlace = arrivalAirport.trim();
        }
    }

    public String getDepartureStation() {
        return departureStation;
    }

    public void setDepartureStation(String departureStation) {
        this.departureStation = departureStation;
        if (departureStation != null && !departureStation.trim().isEmpty() && this.departurePlace == null) {
            this.departurePlace = departureStation.trim();
        }
    }

    public String getArrivalStation() {
        return arrivalStation;
    }

    public void setArrivalStation(String arrivalStation) {
        this.arrivalStation = arrivalStation;
        if (arrivalStation != null && !arrivalStation.trim().isEmpty() && this.arrivalPlace == null) {
            this.arrivalPlace = arrivalStation.trim();
        }
    }

    public String getSeatClass() {
        return seatClass;
    }

    public void setSeatClass(String seatClass) {
        this.seatClass = seatClass;
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }

    public String getDeparturePlace() {
        return departurePlace;
    }

    public void setDeparturePlace(String departurePlace) {
        this.departurePlace = departurePlace;
    }

    public String getArrivalPlace() {
        return arrivalPlace;
    }

    public void setArrivalPlace(String arrivalPlace) {
        this.arrivalPlace = arrivalPlace;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public boolean isRoundTrip() {
        return roundTrip;
    }

    public void setRoundTrip(boolean roundTrip) {
        this.roundTrip = roundTrip;
    }

    public int getPassengers() {
        return passengers;
    }

    public void setPassengers(int passengers) {
        this.passengers = passengers;
    }
}
