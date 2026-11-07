package com.dulich.ticketmanagement.entity;

import com.dulich.ticketmanagement.entity.enums.TrainBookingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "train_bookings")
public class TrainBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Mã đặt vé tàu không được để trống")
    @Column(name = "booking_code", nullable = false, unique = true, length = 30)
    private String bookingCode;

    @NotNull(message = "Đoàn tàu không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @NotNull(message = "Ga đi không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departure_station_id", nullable = false)
    private TrainStation departureStation;

    @NotNull(message = "Ga đến không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arrival_station_id", nullable = false)
    private TrainStation arrivalStation;

    @NotNull(message = "Ngày khởi hành không được để trống")
    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @NotNull(message = "Thời gian khởi hành không được để trống")
    @Column(name = "departure_datetime", nullable = false)
    private LocalDateTime departureDatetime;

    @NotBlank(message = "Tên khách hàng không được để trống")
    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Column(name = "customer_phone", nullable = false, length = 20)
    private String customerPhone;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Column(name = "customer_email", nullable = false, length = 100)
    private String customerEmail;

    @Column(name = "ticket_count", nullable = false)
    private Integer ticketCount = 1;

    @Column(name = "total_original_fare", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalOriginalFare = BigDecimal.ZERO;

    @Column(name = "total_discount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalDiscount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "refund_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(name = "cancellation_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal cancellationFee = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TrainBookingStatus status = TrainBookingStatus.HELD;

    @Column(name = "is_round_trip", nullable = false)
    private Boolean isRoundTrip = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_train_id")
    private Train returnTrain;

    @Column(name = "return_departure_date")
    private LocalDate returnDepartureDate;

    @Column(name = "return_departure_datetime")
    private LocalDateTime returnDepartureDatetime;

    @NotNull
    @Column(name = "hold_expires_at", nullable = false)
    private LocalDateTime holdExpiresAt; // Giữ chỗ 10 phút

    @Column(name = "booking_time", nullable = false)
    private LocalDateTime bookingTime;

    @Column(name = "confirmed_time")
    private LocalDateTime confirmedTime;

    @Column(name = "cancelled_time")
    private LocalDateTime cancelledTime;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @OneToMany(mappedBy = "trainBooking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TrainTicket> tickets = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.bookingTime == null) {
            this.bookingTime = LocalDateTime.now();
        }
        if (this.holdExpiresAt == null) {
            this.holdExpiresAt = this.bookingTime.plusMinutes(10); // 10 phút giữ chỗ
        }
    }

    public void addTicket(TrainTicket ticket) {
        tickets.add(ticket);
        ticket.setTrainBooking(this);
    }

    public TrainBooking() {
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

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public TrainStation getDepartureStation() {
        return departureStation;
    }

    public void setDepartureStation(TrainStation departureStation) {
        this.departureStation = departureStation;
    }

    public TrainStation getArrivalStation() {
        return arrivalStation;
    }

    public void setArrivalStation(TrainStation arrivalStation) {
        this.arrivalStation = arrivalStation;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public LocalDateTime getDepartureDatetime() {
        return departureDatetime;
    }

    public void setDepartureDatetime(LocalDateTime departureDatetime) {
        this.departureDatetime = departureDatetime;
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

    public Integer getTicketCount() {
        return ticketCount;
    }

    public void setTicketCount(Integer ticketCount) {
        this.ticketCount = ticketCount;
    }

    public BigDecimal getTotalOriginalFare() {
        return totalOriginalFare;
    }

    public void setTotalOriginalFare(BigDecimal totalOriginalFare) {
        this.totalOriginalFare = totalOriginalFare;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
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

    public TrainBookingStatus getStatus() {
        return status;
    }

    public void setStatus(TrainBookingStatus status) {
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

    public List<TrainTicket> getTickets() {
        return tickets;
    }

    public void setTickets(List<TrainTicket> tickets) {
        this.tickets = tickets;
    }

    public Boolean getIsRoundTrip() {
        return isRoundTrip;
    }

    public boolean isRoundTrip() {
        return Boolean.TRUE.equals(isRoundTrip);
    }

    public void setIsRoundTrip(Boolean isRoundTrip) {
        this.isRoundTrip = isRoundTrip;
    }

    public Train getReturnTrain() {
        return returnTrain;
    }

    public void setReturnTrain(Train returnTrain) {
        this.returnTrain = returnTrain;
    }

    public LocalDate getReturnDepartureDate() {
        return returnDepartureDate;
    }

    public void setReturnDepartureDate(LocalDate returnDepartureDate) {
        this.returnDepartureDate = returnDepartureDate;
    }

    public LocalDateTime getReturnDepartureDatetime() {
        return returnDepartureDatetime;
    }

    public void setReturnDepartureDatetime(LocalDateTime returnDepartureDatetime) {
        this.returnDepartureDatetime = returnDepartureDatetime;
    }
}
