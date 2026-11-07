package com.dulich.ticketmanagement.entity;

import com.dulich.ticketmanagement.entity.enums.ConsultationStatus;
import com.dulich.ticketmanagement.entity.enums.TicketType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultations")
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consultation_code", nullable = false, unique = true, length = 30)
    private String consultationCode;

    @NotBlank(message = "Họ tên khách hàng không được để trống")
    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[0-9]{9,10})$", message = "Số điện thoại không hợp lệ")
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Email(message = "Email không đúng định dạng")
    @Column(name = "email", length = 100)
    private String email;

    @NotNull(message = "Loại vé quan tâm không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type", nullable = false, length = 20)
    private TicketType ticketType;

    @NotNull(message = "Thời gian hẹn tư vấn không được để trống")
    @Column(name = "preferred_date", nullable = false)
    private LocalDateTime preferredDate;

    @NotNull(message = "Trạng thái lịch hẹn không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ConsultationStatus status = ConsultationStatus.PENDING;

    @Column(name = "notes", length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Consultation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConsultationCode() {
        return consultationCode;
    }

    public void setConsultationCode(String consultationCode) {
        this.consultationCode = consultationCode;
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

    public ConsultationStatus getStatus() {
        return status;
    }

    public void setStatus(ConsultationStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public User getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(User assignedUser) {
        this.assignedUser = assignedUser;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
