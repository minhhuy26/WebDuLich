package com.dulich.ticketmanagement.entity;

import com.dulich.ticketmanagement.entity.enums.PassengerType;
import com.dulich.ticketmanagement.entity.enums.TicketStatus;
import com.dulich.ticketmanagement.entity.enums.TransportType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Ve")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "don_dat_cho_id")
    private Reservation reservation;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "loai_phuong_tien", nullable = false, length = 20)
    private TransportType transportType;

    @NotBlank
    @Column(name = "so_hieu_chuyen", nullable = false, length = 50)
    private String vehicleCode;

    @NotBlank
    @Column(name = "ten_phuong_tien", nullable = false, length = 100)
    private String vehicleName;

    @NotBlank
    @Column(name = "tuyen_duong", nullable = false, length = 200)
    private String routeInfo;

    @NotBlank
    @Column(name = "diem_di", nullable = false, length = 100)
    private String departurePlace;

    @NotBlank
    @Column(name = "diem_den", nullable = false, length = 100)
    private String arrivalPlace;

    @NotNull
    @Column(name = "gio_di", nullable = false)
    private LocalDateTime departureTime;

    @NotNull
    @Column(name = "gio_den", nullable = false)
    private LocalDateTime arrivalTime;

    @NotBlank
    @Column(name = "so_ghe", nullable = false, length = 50, columnDefinition = "NVARCHAR(50)")
    private String seatNumber;

    @NotBlank
    @Column(name = "hang_ve", nullable = false, length = 50)
    private String seatClass;

    @Column(name = "ten_hanh_khach", length = 100)
    private String passengerName;

    @Column(name = "so_cccd", length = 30)
    private String idCard;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "loai_khach", nullable = false, length = 20)
    private PassengerType passengerType = PassengerType.ADULT;

    @NotNull
    @Column(name = "gia_goc", nullable = false, precision = 18, scale = 2)
    private BigDecimal basePrice = BigDecimal.ZERO;

    @NotNull
    @Column(name = "phi_dich_vu", nullable = false, precision = 18, scale = 2)
    private BigDecimal serviceFee = BigDecimal.ZERO;

    @NotNull
    @Column(name = "phu_phi", nullable = false, precision = 18, scale = 2)
    private BigDecimal extraFee = BigDecimal.ZERO;

    @NotNull
    @Column(name = "gia_cuoi", nullable = false, precision = 18, scale = 2)
    private BigDecimal finalPrice = BigDecimal.ZERO;

    @Column(name = "ma_ve_dien_tu", length = 50)
    private String ticketCode;

    @Column(name = "du_lieu_qr", columnDefinition = "NVARCHAR(MAX)")
    private String qrCodeData;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 20)
    private TicketStatus status = TicketStatus.AVAILABLE;

    @Column(name = "ngay_tao", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Ticket() {
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

    public TransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }

    public String getVehicleCode() {
        return vehicleCode;
    }

    public void setVehicleCode(String vehicleCode) {
        this.vehicleCode = vehicleCode;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getRouteInfo() {
        return routeInfo;
    }

    public void setRouteInfo(String routeInfo) {
        this.routeInfo = routeInfo;
    }

    public String getDeparturePlace() {
        return departurePlace;
    }

    public void setDeparturePlace(String departurePlace) {
        this.departurePlace = departurePlace;
    }

    public String getArrivalPlace() {
        return arrivalPlace;
    }

    public void setArrivalPlace(String arrivalPlace) {
        this.arrivalPlace = arrivalPlace;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getSeatClass() {
        return seatClass;
    }

    public String getSeatClassDisplayName() {
        if (seatClass == null) return "";
        switch (seatClass.trim().toUpperCase()) {
            case "ECONOMY": return "Phổ Thông";
            case "BUSINESS": return "Thương Gia";
            case "FIRST": return "Hạng Nhất";
            case "SOFT_SEAT": return "Ghế Mềm";
            case "HARD_SEAT": return "Ghế Cứng";
            case "SLEEPER_4": return "Giường Khoang 4";
            case "SLEEPER_6": return "Giường Khoang 6";
            default: return seatClass;
        }
    }

    public void setSeatClass(String seatClass) {
        this.seatClass = seatClass;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public PassengerType getPassengerType() {
        return passengerType;
    }

    public void setPassengerType(PassengerType passengerType) {
        this.passengerType = passengerType;
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

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public String getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
