package com.dulich.ticketmanagement.entity;

import com.dulich.ticketmanagement.entity.enums.PaymentMethod;
import com.dulich.ticketmanagement.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "GiaoDich")
public class TransDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "don_dat_cho_id", nullable = false)
    private Reservation reservation;

    @NotBlank(message = "Mã thanh toán không được để trống")
    @Column(name = "ma_giao_dich", nullable = false, unique = true, length = 50)
    private String paymentCode;

    @NotNull
    @Column(name = "so_tien", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "phuong_thuc", nullable = false, length = 30)
    private PaymentMethod paymentMethod = PaymentMethod.VNPAY;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.SUCCESS;

    @Column(name = "ma_tham_chieu", length = 100)
    private String transactionReference;

    @Column(name = "ghi_chu", length = 255)
    private String note;

    @Column(name = "thoi_gian_giao_dich", nullable = false)
    private LocalDateTime transactionTime;

    @PrePersist
    protected void onCreate() {
        if (this.transactionTime == null) {
            this.transactionTime = LocalDateTime.now();
        }
        if (this.paymentCode == null || this.paymentCode.trim().isEmpty()) {
            this.paymentCode = "PAY-" + System.currentTimeMillis() % 1000000;
        }
    }

    public TransDetail() {
    }

    public TransDetail(Reservation reservation, String paymentCode, BigDecimal amount, PaymentMethod paymentMethod, PaymentStatus paymentStatus, String transactionReference, String note) {
        this.reservation = reservation;
        this.paymentCode = paymentCode;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.transactionReference = transactionReference;
        this.note = note;
        this.transactionTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public String getPaymentCode() {
        return paymentCode;
    }

    public void setPaymentCode(String paymentCode) {
        this.paymentCode = paymentCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }
}
