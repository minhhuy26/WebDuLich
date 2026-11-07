package com.dulich.ticketmanagement.dto;

import com.dulich.ticketmanagement.entity.enums.FlightSeatClass;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ChangeFlightRequestDto {

    @NotNull(message = "Chưa chọn chuyến bay mới để đổi")
    private Long newFlightId;

    @NotNull(message = "Vui lòng chọn hạng ghế mới")
    private FlightSeatClass newSeatClass;

    private List<String> newSeatNumbers;

    public ChangeFlightRequestDto() {
    }

    public Long getNewFlightId() {
        return newFlightId;
    }

    public void setNewFlightId(Long newFlightId) {
        this.newFlightId = newFlightId;
    }

    public FlightSeatClass getNewSeatClass() {
        return newSeatClass;
    }

    public void setNewSeatClass(FlightSeatClass newSeatClass) {
        this.newSeatClass = newSeatClass;
    }

    public List<String> getNewSeatNumbers() {
        return newSeatNumbers;
    }

    public void setNewSeatNumbers(List<String> newSeatNumbers) {
        this.newSeatNumbers = newSeatNumbers;
    }
}
