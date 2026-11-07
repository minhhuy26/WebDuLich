package com.dulich.ticketmanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TrainBookingRequestDto {

    @NotNull(message = "Chưa chọn đoàn tàu")
    private Long trainId;

    @NotNull(message = "Chưa chọn ga đi")
    private Long departureStationId;

    @NotNull(message = "Chưa chọn ga đến")
    private Long arrivalStationId;

    @NotNull(message = "Chưa chọn ngày khởi hành")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate departureDate;

    private boolean roundTrip = false;

    private Long returnTrainId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate returnDate;

    @NotBlank(message = "Tên người liên hệ không được để trống")
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[0-9]{9,10})$", message = "Số điện thoại không hợp lệ")
    private String customerPhone;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String customerEmail;

    @NotEmpty(message = "Danh sách hành khách không được để trống")
    @Valid
    private List<TrainPassengerInputDto> passengers = new ArrayList<>();

    public TrainBookingRequestDto() {
    }

    public Long getTrainId() {
        return trainId;
    }

    public void setTrainId(Long trainId) {
        this.trainId = trainId;
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public List<TrainPassengerInputDto> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<TrainPassengerInputDto> passengers) {
        this.passengers = passengers;
    }

    public boolean isRoundTrip() {
        return roundTrip;
    }

    public void setRoundTrip(boolean roundTrip) {
        this.roundTrip = roundTrip;
    }

    public Long getReturnTrainId() {
        return returnTrainId;
    }

    public void setReturnTrainId(Long returnTrainId) {
        this.returnTrainId = returnTrainId;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
}
