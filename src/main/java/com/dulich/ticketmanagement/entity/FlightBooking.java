package com.dulich.ticketmanagement.entity;

import com.dulich.ticketmanagement.entity.enums.FlightBookingStatus;
import com.dulich.ticketmanagement.entity.enums.FlightSeatClass;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flight_bookings")
public class FlightBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Mã đặt chỗ không được để trống")
    @Column(name = "booking_code", nullable = false, unique = true, length = 30)
    private String bookingCode;

    @NotNull(message = "Chuyến bay không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_flight_id")
    private Flight returnFlight;

    @Column(name = "is_round_trip", nullable = false)
    private Boolean isRoundTrip = false;

    @NotBlank(message = "Tên người đặt không được để trống")
    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Column(name = "customer_phone", nullable = false, length = 20)
    private String customerPhone;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Column(name = "customer_email", nullable = false, length = 100)
    private String customerEmail;

    @NotNull(message = "Hạng ghế không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false, length = 20)
    private FlightSeatClass seatClass = FlightSeatClass.ECONOMY;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_seat_class", length = 20)
    private FlightSeatClass returnSeatClass;

    @Column(name = "passenger_count", nullable = false)
    private Integer passengerCount = 1;

    @Column(name = "total_fare", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalFare = BigDecimal.ZERO;

    @Column(name = "total_tax_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalTaxFee = BigDecimal.ZERO;

    @Column(name = "total_baggage_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalBaggageFee = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "refund_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(name = "cancellation_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal cancellationFee = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private FlightBookingStatus status = FlightBookingStatus.HELD;

    @NotNull
    @Column(name = "hold_expires_at", nullable = false)
    private LocalDateTime holdExpiresAt;

    @Column(name = "booking_time", nullable = false)
    private LocalDateTime bookingTime;

    @Column(name = "confirmed_time")
    private LocalDateTime confirmedTime;

    @Column(name = "cancelled_time")
    private LocalDateTime cancelledTime;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @OneToMany(mappedBy = "flightBooking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FlightPassenger> passengers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.bookingTime == null) {
            this.bookingTime = LocalDateTime.now();
        }
        if (this.holdExpiresAt == null) {
            this.holdExpiresAt = this.bookingTime.plusMinutes(15);
        }
    }

    public void addPassenger(FlightPassenger passenger) {
        passengers.add(passenger);
        passenger.setFlightBooking(this);
    }

    public FlightBooking() {
    }

    // Getters and Setters
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

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
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

    public Integer getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(Integer passengerCount) {
        this.passengerCount = passengerCount;
    }

    public BigDecimal getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(BigDecimal totalFare) {
        this.totalFare = totalFare;
    }

    public BigDecimal getTotalTaxFee() {
        return totalTaxFee;
    }

    public void setTotalTaxFee(BigDecimal totalTaxFee) {
        this.totalTaxFee = totalTaxFee;
    }

    public BigDecimal getTotalBaggageFee() {
        return totalBaggageFee;
    }

    public void setTotalBaggageFee(BigDecimal totalBaggageFee) {
        this.totalBaggageFee = totalBaggageFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public BigDecimal getCancellationFee() {
        return cancellationFee;
    }

    public void setCancellationFee(BigDecimal cancellationFee) {
        this.cancellationFee = cancellationFee;
    }

    public FlightBookingStatus getStatus() {
        return status;
    }

    public void setStatus(FlightBookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public LocalDateTime getConfirmedTime() {
        return confirmedTime;
    }

    public void setConfirmedTime(LocalDateTime confirmedTime) {
        this.confirmedTime = confirmedTime;
    }

    public LocalDateTime getCancelledTime() {
        return cancelledTime;
    }

    public void setCancelledTime(LocalDateTime cancelledTime) {
        this.cancelledTime = cancelledTime;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public List<FlightPassenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<FlightPassenger> passengers) {
        this.passengers = passengers;
    }

    public Flight getReturnFlight() {
        return returnFlight;
    }

    public void setReturnFlight(Flight returnFlight) {
        this.returnFlight = returnFlight;
    }

    public Boolean getIsRoundTrip() {
        return isRoundTrip;
    }

    public boolean isRoundTrip() {
        return Boolean.TRUE.equals(isRoundTrip);
    }

    public void setIsRoundTrip(Boolean roundTrip) {
        isRoundTrip = roundTrip;
    }

    public FlightSeatClass getReturnSeatClass() {
        return returnSeatClass;
    }

    public void setReturnSeatClass(FlightSeatClass returnSeatClass) {
        this.returnSeatClass = returnSeatClass;
    }
}
