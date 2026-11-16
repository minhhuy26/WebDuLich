package com.dulich.ticketmanagement.service.impl;

import com.dulich.ticketmanagement.entity.Appointment;
import com.dulich.ticketmanagement.entity.User;
import com.dulich.ticketmanagement.entity.enums.AppointmentStatus;
import com.dulich.ticketmanagement.exception.BusinessException;
import com.dulich.ticketmanagement.exception.ResourceNotFoundException;
import com.dulich.ticketmanagement.repository.AppointmentRepository;
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

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Autowired
    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  UserRepository userRepository,
                                  EmailService emailService) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    public Appointment createAppointment(Appointment appointment) {
        // 1. Kiểm tra ngày giờ hẹn trong tương lai
        if (appointment.getPreferredDate() == null || appointment.getPreferredDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Thời gian hẹn tư vấn phải là một thời điểm trong tương lai!");
        }

        // 2. Kiểm tra giờ làm việc hành chính (08:00 - 17:30)
        LocalTime time = appointment.getPreferredDate().toLocalTime();
        if (time.isBefore(LocalTime.of(8, 0)) || time.isAfter(LocalTime.of(17, 30))) {
            throw new BusinessException("Vui lòng chọn khung giờ hẹn làm việc từ 08:00 đến 17:30!");
        }

        // 3. Kiểm tra ngày làm việc (không nhận Chủ Nhật)
        if (appointment.getPreferredDate().getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new BusinessException("Văn phòng không làm việc vào ngày Chủ Nhật, vui lòng chọn từ Thứ 2 đến Thứ 7!");
        }

        // 4. Kiểm tra giới hạn số lượt hẹn trong ngày (tối đa 20 lượt hẹn/ngày)
        LocalDate date = appointment.getPreferredDate().toLocalDate();
        long existingCount = appointmentRepository.countAppointmentsOnDate(date.atStartOfDay(), date.atTime(23, 59, 59));
        if (existingCount >= 20) {
            throw new BusinessException("Ngày " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " đã đủ số lượt hẹn tối đa (20 lượt). Vui lòng chọn ngày khác!");
        }

        // 5. Sinh mã lịch hẹn
        if (appointment.getAppointmentCode() == null || appointment.getAppointmentCode().trim().isEmpty()) {
            String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
            appointment.setAppointmentCode("APT-" + datePrefix + "-" + String.format("%04d", (existingCount + 1)));
        }

        appointment.setStatus(AppointmentStatus.PENDING);
        Appointment saved = appointmentRepository.save(appointment);

        try {
            emailService.sendAppointmentConfirmedEmail(saved);
        } catch (Exception ex) {
            log.warn("Lỗi gửi email xác nhận lịch hẹn tư vấn: {}", ex.getMessage());
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Appointment findByCode(String code) {
        return appointmentRepository.findByAppointmentCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin cuộc hẹn với mã: " + code));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findAll() {
        return appointmentRepository.findAllWithDetails();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findByPhone(String phone) {
        return appointmentRepository.findByPhoneOrderByCreatedAtDesc(phone);
    }

    @Override
    public Appointment updateStatus(Long id, AppointmentStatus status, Long operatorId) {
        Appointment apt = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hẹn ID: " + id));
        apt.setStatus(status);
        if (operatorId != null) {
            User op = userRepository.findById(operatorId).orElse(null);
            apt.setAssignedOperator(op);
        }
        return appointmentRepository.save(apt);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return appointmentRepository.count();
    }
}
