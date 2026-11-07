package com.dulich.ticketmanagement.dto;

public class SeatAvailabilityDto {

    private Long seatId;
    private Integer seatNumber;
    private String seatLabel;
    private String positionNote;
    private boolean available;
    private String occupiedReason; // Ví dụ: Đã có khách đi từ Ga Hà Nội -> Ga Vinh

    public SeatAvailabilityDto() {
    }

    public SeatAvailabilityDto(Long seatId, Integer seatNumber, String seatLabel, String positionNote, boolean available, String occupiedReason) {
        this.seatId = seatId;
        this.seatNumber = seatNumber;
        this.seatLabel = seatLabel;
        this.positionNote = positionNote;
        this.available = available;
        this.occupiedReason = occupiedReason;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getSeatLabel() {
        return seatLabel;
    }

    public void setSeatLabel(String seatLabel) {
        this.seatLabel = seatLabel;
    }

    public String getPositionNote() {
        return positionNote;
    }

    public void setPositionNote(String positionNote) {
        this.positionNote = positionNote;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getOccupiedReason() {
        return occupiedReason;
    }

    public void setOccupiedReason(String occupiedReason) {
        this.occupiedReason = occupiedReason;
    }
}
