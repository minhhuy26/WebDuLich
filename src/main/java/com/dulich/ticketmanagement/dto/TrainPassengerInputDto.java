package com.dulich.ticketmanagement.dto;

import com.dulich.ticketmanagement.entity.enums.PassengerPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TrainPassengerInputDto {

    @NotBlank(message = "Tên hành khách không được để trống")
    private String passengerName;

    private String passengerIdCard;

    @NotNull(message = "Đối tượng ưu tiên không được để trống")
    private PassengerPriority priorityCategory = PassengerPriority.STANDARD;

    private String priorityDocId; // Mã sinh viên, CCCD/Năm sinh người già, Giấy khai sinh trẻ em

    @NotNull(message = "Chưa chọn chỗ ngồi/giường nằm")
    private Long seatId;

    private Long returnSeatId;

    public TrainPassengerInputDto() {
    }

    public TrainPassengerInputDto(String passengerName, String passengerIdCard, PassengerPriority priorityCategory, String priorityDocId, Long seatId) {
        this.passengerName = passengerName;
        this.passengerIdCard = passengerIdCard;
        this.priorityCategory = priorityCategory;
        this.priorityDocId = priorityDocId;
        this.seatId = seatId;
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

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public Long getReturnSeatId() {
        return returnSeatId;
    }

    public void setReturnSeatId(Long returnSeatId) {
        this.returnSeatId = returnSeatId;
    }
}
