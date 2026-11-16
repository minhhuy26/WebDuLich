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
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type", nullable = false, length = 20)
    private TransportType transportType;

    @NotBlank
    @Column(name = "vehicle_code", nullable = false, length = 50)
    private String vehicleCode;

    @NotBlank
    @Column(name = "vehicle_name", nullable = false, length = 100)
    private String vehicleName;

    @NotBlank
    @Column(name = "route_info", nullable = false, length = 200)
    private String routeInfo;

    @NotBlank
    @Column(name = "departure_place", nullable = false, length = 100)
    private String departurePlace;

    @NotBlank
    @Column(name = "arrival_place", nullable = false, length = 100)
    private String arrivalPlace;

    @NotNull
    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @NotNull
    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @NotBlank
    @Column(name = "seat_number", nullable = false, length = 50, columnDefinition = "NVARCHAR(50)")
    private String seatNumber;

    @NotBlank
    @Column(name = "seat_class", nullable = false, length = 50)
    private String seatClass;

    @Column(name = "passenger_name", length = 100)
    private String passengerName;

    @Column(name = "id_card", length = 30)
    private String idCard;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "passenger_type", nullable = false, length = 20)
    private PassengerType passengerType = PassengerType.ADULT;

    @NotNull
    @Column(name = "base_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal basePrice = BigDecimal.ZERO;

    @NotNull
    @Column(name = "service_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal serviceFee = BigDecimal.ZERO;

    @NotNull
    @Column(name = "extra_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal extraFee = BigDecimal.ZERO;

    @NotNull
    @Column(name = "final_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal finalPrice = BigDecimal.ZERO;

    @Column(name = "ticket_code", length = 50)
    private String ticketCode;

    @Column(name = "qr_code_data", columnDefinition = "NVARCHAR(MAX)")
    private String qrCodeData;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TicketStatus status = TicketStatus.AVAILABLE;

    @Column(name = "created_at", nullable = false, updatable = false)
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
