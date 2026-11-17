package com.dulich.ticketmanagement.entity;

import com.dulich.ticketmanagement.entity.enums.AppointmentStatus;
import com.dulich.ticketmanagement.entity.enums.ReservationStatus;
import com.dulich.ticketmanagement.entity.enums.TransportType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "DonDatCho")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Mã đặt chỗ không được để trống")
    @Column(name = "ma_dat_cho", nullable = false, unique = true, length = 30)
    private String bookingCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_dung_id")
    private User user;

    @NotBlank(message = "Tên khách hàng không được để trống")
    @Column(name = "ten_khach_hang", nullable = false, length = 100)
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Column(name = "so_dien_thoai", nullable = false, length = 20)
    private String customerPhone;

    @NotBlank(message = "Email không được để trống")
    @Column(name = "email", nullable = false, length = 100)
    private String customerEmail;

    @NotNull(message = "Loại phương tiện không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(name = "loai_phuong_tien", nullable = false, length = 20)
    private TransportType transportType;

    @NotBlank(message = "Điểm khởi hành không được để trống")
    @Column(name = "diem_di", nullable = false, length = 100)
    private String departurePlace;

    @NotBlank(message = "Điểm đến không được để trống")
    @Column(name = "diem_den", nullable = false, length = 100)
    private String arrivalPlace;

    @NotNull(message = "Ngày khởi hành không được để trống")
    @Column(name = "ngay_di", nullable = false)
    private LocalDateTime departureDate;

    @Column(name = "ngay_ve")
    private LocalDateTime returnDate;

    @Column(name = "la_khu_hoi", nullable = false)
    private Boolean isRoundTrip = false;

    @Min(value = 1, message = "Số lượng hành khách tối thiểu là 1")
    @Column(name = "so_luong_khach", nullable = false)
    private Integer passengerCount = 1;

    @NotNull
    @Column(name = "tong_tien", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.CREATED;

    @NotNull
    @Column(name = "han_giu_cho", nullable = false)
    private LocalDateTime holdExpiresAt;

    @Column(name = "ly_do_huy", length = 255)
    private String cancelReason;

    @Column(name = "co_hen_tu_van", nullable = false)
    private Boolean hasConsultation = false;

    @Column(name = "ngay_hen_tu_van")
    private LocalDateTime consultationDate;

    @Column(name = "ghi_chu_tu_van", length = 500)
    private String consultationNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai_tu_van", nullable = false, length = 20)
    private AppointmentStatus consultationStatus = AppointmentStatus.NONE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_tu_van_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User assignedConsultant;

    @Column(name = "ngay_tao", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "reservation", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TransDetail> transDetails = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.holdExpiresAt == null) {
            this.holdExpiresAt = LocalDateTime.now().plusMinutes(15);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Reservation() {
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public TransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
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

    public Boolean getIsRoundTrip() {
        return isRoundTrip;
    }

    public void setIsRoundTrip(Boolean isRoundTrip) {
        this.isRoundTrip = isRoundTrip;
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

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
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

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public List<TransDetail> getTransDetails() {
        return transDetails;
    }

    public void setTransDetails(List<TransDetail> transDetails) {
        this.transDetails = transDetails;
    }

    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
        ticket.setReservation(this);
    }

    public void addTransDetail(TransDetail detail) {
        transDetails.add(detail);
        detail.setReservation(this);
    }

    public Boolean getHasConsultation() {
        return hasConsultation;
    }

    public void setHasConsultation(Boolean hasConsultation) {
        this.hasConsultation = hasConsultation;
    }

    public LocalDateTime getConsultationDate() {
        return consultationDate;
    }

    public void setConsultationDate(LocalDateTime consultationDate) {
        this.consultationDate = consultationDate;
    }

    public String getConsultationNotes() {
        return consultationNotes;
    }

    public void setConsultationNotes(String consultationNotes) {
        this.consultationNotes = consultationNotes;
    }

    public AppointmentStatus getConsultationStatus() {
        return consultationStatus;
    }

    public void setConsultationStatus(AppointmentStatus consultationStatus) {
        this.consultationStatus = consultationStatus;
    }

    public User getAssignedConsultant() {
        return assignedConsultant;
    }

    public void setAssignedConsultant(User assignedConsultant) {
        this.assignedConsultant = assignedConsultant;
    }
}
