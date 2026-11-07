package com.dulich.ticketmanagement.dto;

import com.dulich.ticketmanagement.entity.enums.TicketType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class ConsultationRequestDto {

    @NotBlank(message = "Họ tên không được để trống")
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[0-9]{9,10})$", message = "Số điện thoại phải từ 10-11 số bắt đầu bằng số 0")
    private String phone;

    @Email(message = "Email không hợp lệ")
    private String email;

    @NotNull(message = "Vui lòng chọn loại vé quan tâm")
    private TicketType ticketType;

    @NotNull(message = "Vui lòng chọn thời gian mong muốn tư vấn")
    @Future(message = "Thời gian tư vấn phải ở tương lai")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime preferredDate;

    private String notes;

    public ConsultationRequestDto() {
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public void setTicketType(TicketType ticketType) {
        this.ticketType = ticketType;
    }

    public LocalDateTime getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(LocalDateTime preferredDate) {
        this.preferredDate = preferredDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
