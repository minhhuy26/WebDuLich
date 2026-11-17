package com.dulich.ticketmanagement.entity;

import com.dulich.ticketmanagement.entity.enums.AppointmentStatus;
import com.dulich.ticketmanagement.entity.enums.TransportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Model biểu mẫu tiếp nhận thông tin lịch hẹn tư vấn sau khi khách đặt vé xong.
 * Không còn là bảng riêng biệt trong CSDL (CSDL tinh gọn chuẩn 4 bảng).
 * Toàn bộ thông tin được đồng bộ trực tiếp vào bảng DonDatCho (Reservation).
 */
public class Appointment {

    private Long id;

    @NotBlank(message = "Vui lòng nhập hoặc chọn mã đơn đặt vé của quý khách")
    private String bookingCode;

    private String customerName;
    private String phone;
    private String email;

    @NotNull(message = "Vui lòng chọn thời gian hẹn mong muốn")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime preferredDate;

    private TransportType transportType = TransportType.AIR;
    private String notes;
    private AppointmentStatus status = AppointmentStatus.PENDING;
    private User assignedOperator;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Appointment() {
    }

    public Appointment(String bookingCode, LocalDateTime preferredDate, String notes) {
        this.bookingCode = bookingCode;
        this.preferredDate = preferredDate;
        this.notes = notes;
    }

    public static Appointment fromReservation(Reservation r) {
        Appointment a = new Appointment();
        a.setId(r.getId());
        a.setBookingCode(r.getBookingCode());
        a.setCustomerName(r.getCustomerName());
        a.setPhone(r.getCustomerPhone());
        a.setEmail(r.getCustomerEmail());
        a.setPreferredDate(r.getConsultationDate());
        a.setTransportType(r.getTransportType());
        a.setNotes(r.getConsultationNotes());
        a.setStatus(r.getConsultationStatus());
        a.setAssignedOperator(r.getAssignedConsultant());
        a.setCreatedAt(r.getCreatedAt());
        return a;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
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

    public LocalDateTime getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(LocalDateTime preferredDate) {
        this.preferredDate = preferredDate;
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public User getAssignedOperator() {
        return assignedOperator;
    }

    public void setAssignedOperator(User assignedOperator) {
        this.assignedOperator = assignedOperator;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
