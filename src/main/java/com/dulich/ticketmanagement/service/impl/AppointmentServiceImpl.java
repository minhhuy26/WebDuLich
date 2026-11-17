package com.dulich.ticketmanagement.service.impl;

import com.dulich.ticketmanagement.entity.Reservation;
import com.dulich.ticketmanagement.entity.User;
import com.dulich.ticketmanagement.entity.enums.AppointmentStatus;
import com.dulich.ticketmanagement.exception.BusinessException;
import com.dulich.ticketmanagement.exception.ResourceNotFoundException;
import com.dulich.ticketmanagement.repository.ReservationRepository;
import com.dulich.ticketmanagement.repository.UserRepository;
import com.dulich.ticketmanagement.service.AppointmentService;
import com.dulich.ticketmanagement.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Autowired
    public AppointmentServiceImpl(ReservationRepository reservationRepository,
                                  UserRepository userRepository,
                                  EmailService emailService) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    public Reservation scheduleConsultation(String bookingCode, LocalDateTime preferredDate, String notes) {
        if (bookingCode == null || bookingCode.trim().isEmpty()) {
            throw new BusinessException("Mã đặt vé không được để trống khi đăng ký lịch hẹn tư vấn!");
        }

        Reservation reservation = reservationRepository.findByBookingCode(bookingCode.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt vé với mã: " + bookingCode));

        // 1. Kiểm tra ngày giờ hẹn trong tương lai
        if (preferredDate == null || preferredDate.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Thời gian hẹn tư vấn phải là một thời điểm trong tương lai!");
        }

        // 2. Kiểm tra giờ làm việc hành chính (08:00 - 17:30)
        LocalTime time = preferredDate.toLocalTime();
        if (time.isBefore(LocalTime.of(8, 0)) || time.isAfter(LocalTime.of(17, 30))) {
            throw new BusinessException("Vui lòng chọn khung giờ hẹn làm việc từ 08:00 đến 17:30!");
        }

        // 3. Kiểm tra ngày làm việc (không nhận Chủ Nhật)
        if (preferredDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new BusinessException("Văn phòng không làm việc vào ngày Chủ Nhật, vui lòng chọn từ Thứ 2 đến Thứ 7!");
        }

        // 4. Kiểm tra giới hạn số lượt hẹn trong ngày (tối đa 20 lượt hẹn/ngày)
        LocalDate date = preferredDate.toLocalDate();
        long existingCount = reservationRepository.countConsultationsOnDate(date.atStartOfDay(), date.atTime(23, 59, 59));
        if (existingCount >= 20) {
            throw new BusinessException("Ngày " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " đã đủ số lượt hẹn tối đa (20 lượt). Vui lòng chọn ngày khác!");
        }

        // 5. Cập nhật thông tin tư vấn gắn liền với đơn đặt vé
        reservation.setHasConsultation(true);
        reservation.setConsultationDate(preferredDate);
        reservation.setConsultationNotes(notes != null ? notes.trim() : null);
        reservation.setConsultationStatus(AppointmentStatus.PENDING);

        Reservation saved = reservationRepository.save(reservation);

        try {
            emailService.sendConsultationConfirmedEmail(saved);
        } catch (Exception ex) {
            log.warn("Lỗi gửi email xác nhận lịch hẹn tư vấn: {}", ex.getMessage());
        }

        return saved;
    }

    @Override
    public Reservation updateStatus(Long reservationId, AppointmentStatus status, Long operatorId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt vé với ID: " + reservationId));

        if (!Boolean.TRUE.equals(reservation.getHasConsultation())) {
            throw new BusinessException("Đơn đặt vé này hiện chưa đăng ký hẹn tư vấn!");
        }

        reservation.setConsultationStatus(status != null ? status : AppointmentStatus.CONFIRMED);

        if (operatorId != null) {
            User operator = userRepository.findById(operatorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên điều hành với ID: " + operatorId));
            reservation.setAssignedConsultant(operator);
        }

        return reservationRepository.save(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findAll() {
        return reservationRepository.findByHasConsultationTrueOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findByPhone(String phone) {
        return reservationRepository.findByHasConsultationTrueAndCustomerPhoneOrderByCreatedAtDesc(phone);
    }

    @Override
    @Transactional(readOnly = true)
    public Reservation findByBookingCode(String bookingCode) {
        return reservationRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt vé với mã: " + bookingCode));
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return reservationRepository.countByHasConsultationTrue();
    }
}
