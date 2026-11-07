package com.dulich.ticketmanagement.service.impl;

import com.dulich.ticketmanagement.entity.Consultation;
import com.dulich.ticketmanagement.entity.enums.ConsultationStatus;
import com.dulich.ticketmanagement.exception.BusinessException;
import com.dulich.ticketmanagement.repository.ConsultationRepository;
import com.dulich.ticketmanagement.service.ConsultationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Transactional
public class ConsultationServiceImpl implements ConsultationService {

    private static final Pattern VN_PHONE_PATTERN = Pattern.compile("^(0[35789][0-9]{8}|09[0-9]{8}|02[0-9]{9})$");
    private static final int MAX_APPOINTMENTS_PER_SLOT = 3;

    private final ConsultationRepository consultationRepository;

    @Autowired
    public ConsultationServiceImpl(ConsultationRepository consultationRepository) {
        this.consultationRepository = consultationRepository;
    }

    @Override
    public Consultation createConsultation(Consultation consultation) {
        // 1. Validate số điện thoại Việt Nam
        if (consultation.getPhone() == null || !VN_PHONE_PATTERN.matcher(consultation.getPhone().trim()).matches()) {
            throw new BusinessException("Số điện thoại không hợp lệ! Vui lòng nhập đúng số điện thoại di động Việt Nam (10 số, bắt đầu bằng 03, 05, 07, 08, 09).");
        }

        // 2. Validate thời gian hẹn tư vấn
        validateAppointmentTime(consultation.getPreferredDate(), null);

        // 3. Khởi tạo mã lịch hẹn CS-XXXXXX và trạng thái ban đầu
        String code = "CS-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        consultation.setConsultationCode(code);
        consultation.setStatus(ConsultationStatus.PENDING);

        return consultationRepository.save(consultation);
    }

    @Override
    public void validateAppointmentTime(LocalDateTime dateTime, Long excludeConsultationId) {
        if (dateTime == null) {
            throw new BusinessException("Thời gian hẹn tư vấn không được để trống!");
        }

        LocalDateTime now = LocalDateTime.now();
        // Phải ở tương lai (ít nhất cách thời điểm hiện tại 15 phút)
        if (!dateTime.isAfter(now.plusMinutes(15))) {
            throw new BusinessException("Thời gian hẹn tư vấn phải ở tương lai (ít nhất sau thời điểm hiện tại 15 phút)!");
        }

        // Trong giờ làm việc từ thứ 2 đến thứ 7 (Chủ nhật nghỉ)
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            throw new BusinessException("Bộ phận tư vấn chỉ làm việc từ Thứ 2 đến Thứ 7 (nghỉ Chủ nhật). Vui lòng chọn ngày khác!");
        }

        // Khung giờ làm việc từ 08:00 đến 17:00
        int hour = dateTime.getHour();
        int minute = dateTime.getMinute();
        if (hour < 8 || (hour > 17) || (hour == 17 && minute > 0)) {
            throw new BusinessException("Giờ hẹn tư vấn phải nằm trong khung giờ làm việc: 08:00 đến 17:00!");
        }

        // Mỗi khung giờ 1 tiếng tối đa 3 lịch hẹn
        LocalDateTime slotStart = dateTime.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime slotEnd = slotStart.plusHours(1);

        long count = consultationRepository.countAppointmentsInSlot(slotStart, slotEnd, ConsultationStatus.CANCELLED, excludeConsultationId);
        if (count >= MAX_APPOINTMENTS_PER_SLOT) {
            throw new BusinessException(String.format("Khung giờ %02d:00 - %02d:00 ngày %s đã đạt tối đa %d lịch hẹn! Vui lòng chọn khung giờ khác.",
                    slotStart.getHour(), slotEnd.getHour(), slotStart.toLocalDate(), MAX_APPOINTMENTS_PER_SLOT));
        }
    }

    @Override
    public Consultation reschedule(Long id, LocalDateTime newDateTime) {
        Consultation consultation = findById(id);
        if (consultation.getStatus() == ConsultationStatus.COMPLETED) {
            throw new BusinessException("Lịch hẹn đã hoàn thành, không thể đổi giờ!");
        }
        if (consultation.getStatus() == ConsultationStatus.CANCELLED) {
            throw new BusinessException("Lịch hẹn đã hủy, không thể đổi giờ!");
        }

        validateAppointmentTime(newDateTime, id);
        consultation.setPreferredDate(newDateTime);
        return consultationRepository.save(consultation);
    }

    @Override
    public Consultation updateStatus(Long id, ConsultationStatus status) {
        Consultation consultation = findById(id);
        if (consultation.getStatus() == status) {
            return consultation;
        }
        if (consultation.getStatus() == ConsultationStatus.COMPLETED) {
            throw new BusinessException("Lịch hẹn đã hoàn thành, không thể thay đổi trạng thái!");
        }
        if (consultation.getStatus() == ConsultationStatus.CANCELLED) {
            throw new BusinessException("Lịch hẹn đã bị hủy, không thể thay đổi trạng thái!");
        }
        consultation.setStatus(status);
        return consultationRepository.save(consultation);
    }

    @Override
    @Transactional(readOnly = true)
    public Consultation findById(Long id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy lịch hẹn tư vấn ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Consultation findByCode(String code) {
        return consultationRepository.findByConsultationCode(code)
                .orElseThrow(() -> new BusinessException("Không tìm thấy lịch hẹn tư vấn có mã: " + code));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Consultation> findAll() {
        return consultationRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Consultation> findByPhone(String phone) {
        return consultationRepository.findByPhone(phone);
    }
}
