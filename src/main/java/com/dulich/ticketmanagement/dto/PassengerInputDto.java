package com.dulich.ticketmanagement.dto;

import com.dulich.ticketmanagement.entity.enums.PassengerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PassengerInputDto {

    @NotBlank(message = "Họ tên hành khách không được để trống")
    private String fullName;

    private String idCard; // CCCD 12 số hoặc Hộ chiếu (bắt buộc với người lớn)

    @NotNull(message = "Loại hành khách không được để trống")
    private PassengerType passengerType = PassengerType.ADULT;

    private String seatNumber; // Chỗ ngồi (Em bé không chiếm chỗ)

    private String returnSeatNumber; // Chỗ ngồi chiều về cho vé khứ hồi

    private Integer baggageKg = 0; // Số kg hành lý ký gửi

    public PassengerInputDto() {
    }

    public PassengerInputDto(String fullName, String idCard, PassengerType passengerType, String seatNumber, Integer baggageKg) {
        this.fullName = fullName;
        this.idCard = idCard;
        this.passengerType = passengerType;
        this.seatNumber = seatNumber;
        this.baggageKg = baggageKg;
    }

    public PassengerInputDto(String fullName, String idCard, PassengerType passengerType, String seatNumber, String returnSeatNumber, Integer baggageKg) {
        this.fullName = fullName;
        this.idCard = idCard;
        this.passengerType = passengerType;
        this.seatNumber = seatNumber;
        this.returnSeatNumber = returnSeatNumber;
        this.baggageKg = baggageKg;
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

    public String getReturnSeatNumber() {
        return returnSeatNumber;
    }

    public void setReturnSeatNumber(String returnSeatNumber) {
        this.returnSeatNumber = returnSeatNumber;
    }

    public Integer getBaggageKg() {
        return baggageKg;
    }

    public void setBaggageKg(Integer baggageKg) {
        this.baggageKg = baggageKg;
    }
}
