package com.dulich.ticketmanagement.entity;

import com.dulich.ticketmanagement.entity.enums.PassengerType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_details")
public class TicketDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Đơn đặt vé không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @NotBlank(message = "Tên hành khách không được để trống")
    @Column(name = "passenger_name", nullable = false, length = 100)
    private String passengerName;

    @Column(name = "passenger_id_card", length = 20)
    private String passengerIdCard;

    @NotNull(message = "Loại hành khách không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(name = "passenger_type", nullable = false, length = 20)
    private PassengerType passengerType = PassengerType.ADULT;

    @NotBlank(message = "Mã chuyến đi (chuyến bay / tàu) không được để trống")
    @Column(name = "vehicle_code", nullable = false, length = 50)
    private String vehicleCode;

    @NotBlank(message = "Hạng vé/chỗ không được để trống")
    @Column(name = "seat_class", nullable = false, length = 50)
    private String seatClass;

    @Column(name = "seat_number", length = 20)
    private String seatNumber;

    @NotNull(message = "Giá gốc vé không được để trống")
    @Column(name = "base_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal basePrice = BigDecimal.ZERO;

    @NotNull(message = "Phí dịch vụ không được để trống")
    @Column(name = "service_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal serviceFee = BigDecimal.ZERO;

    @NotNull(message = "Phí phụ thu/hành lý không được để trống")
    @Column(name = "extra_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal extraFee = BigDecimal.ZERO;

    @NotNull(message = "Tổng giá vé hành khách không được để trống")
    @Column(name = "final_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal finalPrice = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.finalPrice == null || this.finalPrice.compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal base = this.basePrice != null ? this.basePrice : BigDecimal.ZERO;
            BigDecimal service = this.serviceFee != null ? this.serviceFee : BigDecimal.ZERO;
            BigDecimal extra = this.extraFee != null ? this.extraFee : BigDecimal.ZERO;
            this.finalPrice = base.add(service).add(extra);
        }
    }

    public TicketDetail() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
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

    public PassengerType getPassengerType() {
        return passengerType;
    }

    public void setPassengerType(PassengerType passengerType) {
        this.passengerType = passengerType;
    }

    public String getVehicleCode() {
        return vehicleCode;
    }

    public void setVehicleCode(String vehicleCode) {
        this.vehicleCode = vehicleCode;
    }

    public String getSeatClass() {
        return seatClass;
    }

    public void setSeatClass(String seatClass) {
        this.seatClass = seatClass;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(BigDecimal serviceFee) {
        this.serviceFee = serviceFee;
    }

    public BigDecimal getExtraFee() {
        return extraFee;
    }

    public void setExtraFee(BigDecimal extraFee) {
        this.extraFee = extraFee;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
