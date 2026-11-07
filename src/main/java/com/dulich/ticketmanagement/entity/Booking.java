package com.dulich.ticketmanagement.entity;

import com.dulich.ticketmanagement.entity.enums.BookingStatus;
import com.dulich.ticketmanagement.entity.enums.TicketType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Mã đặt vé không được để trống")
    @Column(name = "booking_code", nullable = false, unique = true, length = 30)
    private String bookingCode;

    @NotNull(message = "Khách hàng không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull(message = "Loại vé không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type", nullable = false, length = 20)
    private TicketType ticketType;

    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate;

    @NotNull(message = "Ngày khởi hành không được để trống")
    @Column(name = "departure_date", nullable = false)
    private LocalDateTime departureDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @NotBlank(message = "Điểm khởi hành không được để trống")
    @Column(name = "departure_place", nullable = false, length = 100)
    private String departurePlace;

    @NotBlank(message = "Điểm đến không được để trống")
    @Column(name = "arrival_place", nullable = false, length = 100)
    private String arrivalPlace;

    @Min(value = 1, message = "Số lượng hành khách tối thiểu là 1")
    @Column(name = "passenger_count", nullable = false)
    private Integer passengerCount = 1;

    @NotNull(message = "Tổng tiền không được để trống")
    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @NotNull(message = "Trạng thái đơn không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "special_requests", length = 500)
    private String specialRequests;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TicketDetail> ticketDetails = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.bookingDate == null) {
            this.bookingDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Booking() {
    }

    public void addTicketDetail(TicketDetail detail) {
        ticketDetails.add(detail);
        detail.setBooking(this);
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setBooking(this);
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

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public void setTicketType(TicketType ticketType) {
        this.ticketType = ticketType;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalDateTime getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDateTime departureDate) {
        this.departureDate = departureDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
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

    public Integer getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(Integer passengerCount) {
        this.passengerCount = passengerCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<TicketDetail> getTicketDetails() {
        return ticketDetails;
    }

    public void setTicketDetails(List<TicketDetail> ticketDetails) {
        this.ticketDetails = ticketDetails;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }
}
