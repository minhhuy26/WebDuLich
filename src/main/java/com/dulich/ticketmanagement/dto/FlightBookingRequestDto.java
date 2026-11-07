package com.dulich.ticketmanagement.dto;

import com.dulich.ticketmanagement.entity.enums.FlightSeatClass;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.ArrayList;
import java.util.List;

public class FlightBookingRequestDto {

    @NotNull(message = "Chưa chọn chuyến bay")
    private Long flightId;

    private Long returnFlightId;

    private boolean roundTrip = false;

    @NotBlank(message = "Tên người liên hệ không được để trống")
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[0-9]{9,10})$", message = "Số điện thoại không hợp lệ (10-11 số bắt đầu bằng 0)")
    private String customerPhone;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String customerEmail;

    @NotNull(message = "Vui lòng chọn hạng vé")
    private FlightSeatClass seatClass = FlightSeatClass.ECONOMY;

    private FlightSeatClass returnSeatClass;

    @NotEmpty(message = "Danh sách hành khách không được rỗng")
    @Valid
    private List<PassengerInputDto> passengers = new ArrayList<>();

    public FlightBookingRequestDto() {
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public Long getReturnFlightId() {
        return returnFlightId;
    }

    public void setReturnFlightId(Long returnFlightId) {
        this.returnFlightId = returnFlightId;
    }

    public boolean isRoundTrip() {
        return roundTrip;
    }

    public void setRoundTrip(boolean roundTrip) {
        this.roundTrip = roundTrip;
    }

    public FlightSeatClass getReturnSeatClass() {
        return returnSeatClass;
    }

    public void setReturnSeatClass(FlightSeatClass returnSeatClass) {
        this.returnSeatClass = returnSeatClass;
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

    public FlightSeatClass getSeatClass() {
        return seatClass;
    }

    public void setSeatClass(FlightSeatClass seatClass) {
        this.seatClass = seatClass;
    }

    public List<PassengerInputDto> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<PassengerInputDto> passengers) {
        this.passengers = passengers;
    }
}
