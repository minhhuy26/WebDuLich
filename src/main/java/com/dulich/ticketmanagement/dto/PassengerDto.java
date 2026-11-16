package com.dulich.ticketmanagement.dto;

import com.dulich.ticketmanagement.entity.enums.PassengerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class PassengerDto {

    @NotBlank(message = "Họ và tên hành khách không được để trống")
    private String fullName;

    private String idCard;

    @NotNull(message = "Vui lòng chọn loại hành khách")
    private PassengerType passengerType = PassengerType.ADULT;

    private String seatNumber;

    private BigDecimal extraFee = BigDecimal.ZERO;

    public PassengerDto() {
    }

    public PassengerDto(String fullName, String idCard, PassengerType passengerType, String seatNumber) {
        this.fullName = fullName;
        this.idCard = idCard;
        this.passengerType = passengerType;
        this.seatNumber = seatNumber;
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

    public BigDecimal getExtraFee() {
        return extraFee;
    }

    public void setExtraFee(BigDecimal extraFee) {
        this.extraFee = extraFee;
    }
}
