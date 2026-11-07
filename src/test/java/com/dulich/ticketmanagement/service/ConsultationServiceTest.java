package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.entity.Consultation;
import com.dulich.ticketmanagement.entity.enums.ConsultationStatus;
import com.dulich.ticketmanagement.entity.enums.TicketType;
import com.dulich.ticketmanagement.exception.BusinessException;
import com.dulich.ticketmanagement.repository.ConsultationRepository;
import com.dulich.ticketmanagement.service.impl.ConsultationServiceImpl;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConsultationServiceTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @InjectMocks
    private ConsultationServiceImpl consultationService;

    private LocalDateTime nextWorkingDayAt9AM;

    @BeforeEach
    void setUp() {
        // Tìm thứ 2 hoặc ngày thường tiếp theo lúc 09:00
        LocalDateTime candidate = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0).withSecond(0);
        if (candidate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            candidate = candidate.plusDays(1);
        }
        nextWorkingDayAt9AM = candidate;
    }

    @Test
    @DisplayName("Test 1: Đăng ký lịch hẹn hợp lệ thành công (Trong giờ làm việc, SĐT VN hợp lệ, sinh mã CS-xxx)")
    void testCreateConsultation_Success() {
        Consultation consultation = new Consultation();
        consultation.setCustomerName("Nguyễn Văn A");
        consultation.setPhone("0912345678");
        consultation.setEmail("an@example.com");
        consultation.setTicketType(TicketType.FLIGHT);
        consultation.setPreferredDate(nextWorkingDayAt9AM);

        when(consultationRepository.countAppointmentsInSlot(any(), any(), eq(ConsultationStatus.CANCELLED), any())).thenReturn(1L);
        when(consultationRepository.save(any(Consultation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Consultation result = consultationService.createConsultation(consultation);

        assertNotNull(result);
        assertNotNull(result.getConsultationCode());
        assertTrue(result.getConsultationCode().startsWith("CS-"));
        assertEquals(ConsultationStatus.PENDING, result.getStatus());
        verify(consultationRepository, times(1)).save(any(Consultation.class));
    }

    @Test
    @DisplayName("Test 2: Validation SĐT không hợp lệ (Không đủ 10 số hoặc sai đầu số VN) -> Báo lỗi")
    void testCreateConsultation_InvalidPhone_ThrowsException() {
        Consultation consultation = new Consultation();
        consultation.setCustomerName("Nguyễn Văn A");
        consultation.setPhone("123456"); // Sai định dạng
        consultation.setPreferredDate(nextWorkingDayAt9AM);

        BusinessException ex = assertThrows(BusinessException.class, () -> consultationService.createConsultation(consultation));
        assertTrue(ex.getMessage().contains("Số điện thoại không hợp lệ"));
    }

    @Test
    @DisplayName("Test 3: Validation ngày hẹn rơi vào Chủ Nhật -> Báo lỗi nghỉ làm việc")
    void testCreateConsultation_Sunday_ThrowsException() {
        // Chọn Chủ Nhật kế tiếp
        LocalDateTime sunday = LocalDateTime.now().plusWeeks(1).with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).withHour(10).withMinute(0);

        Consultation consultation = new Consultation();
        consultation.setCustomerName("Nguyễn Văn A");
        consultation.setPhone("0987654321");
        consultation.setPreferredDate(sunday);

        BusinessException ex = assertThrows(BusinessException.class, () -> consultationService.createConsultation(consultation));
        assertTrue(ex.getMessage().contains("nghỉ Chủ nhật"));
    }

    @Test
    @DisplayName("Test 4: Validation ngoài khung giờ làm việc (sau 17h hoặc trước 8h) -> Báo lỗi")
    void testCreateConsultation_OutsideWorkingHours_ThrowsException() {
        LocalDateTime eveningSlot = nextWorkingDayAt9AM.withHour(19).withMinute(30);

        Consultation consultation = new Consultation();
        consultation.setCustomerName("Nguyễn Văn A");
        consultation.setPhone("0987654321");
        consultation.setPreferredDate(eveningSlot);

        BusinessException ex = assertThrows(BusinessException.class, () -> consultationService.createConsultation(consultation));
        assertTrue(ex.getMessage().contains("08:00 đến 17:00"));
    }

    @Test
    @DisplayName("Test 5: Validation mỗi khung giờ tối đa 3 lịch hẹn -> Lịch thứ 4 bị từ chối")
    void testCreateConsultation_ExceedMax3PerSlot_ThrowsException() {
        Consultation consultation = new Consultation();
        consultation.setCustomerName("Nguyễn Văn D");
        consultation.setPhone("0912345678");
        consultation.setPreferredDate(nextWorkingDayAt9AM);

        // Khung giờ đã có 3 lịch hẹn đang hiệu lực
        when(consultationRepository.countAppointmentsInSlot(any(), any(), eq(ConsultationStatus.CANCELLED), isNull())).thenReturn(3L);

        BusinessException ex = assertThrows(BusinessException.class, () -> consultationService.createConsultation(consultation));
        assertTrue(ex.getMessage().contains("đã đạt tối đa 3 lịch hẹn"));
        verify(consultationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Bug 10 Test: Form đăng ký tư vấn chưa có consultationCode nhưng validation form không được báo lỗi consultationCode")
    void testBug10_ConsultationFormValidation_ConsultationCodeCanBeNullBeforeCreation() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Consultation formSubmission = new Consultation();
        formSubmission.setCustomerName("Trần Văn B");
        formSubmission.setPhone("0901234567");
        formSubmission.setEmail("tranvanb@example.com");
        formSubmission.setTicketType(TicketType.FLIGHT);
        formSubmission.setPreferredDate(nextWorkingDayAt9AM);
        // consultationCode is intentionally null, as it is generated by service later

        var violations = validator.validate(formSubmission);
        boolean hasConsultationCodeViolation = violations.stream()
                .anyMatch(v -> "consultationCode".equals(v.getPropertyPath().toString()));

        assertFalse(hasConsultationCodeViolation, "consultationCode không được có annotation @NotBlank vì được sinh tự động sau khi submit");
    }

    @Test
    @DisplayName("Bug 14 Test: Đổi giờ hẹn trong cùng khung giờ loại trừ chính ID đang đổi để không bị tính trùng đạt tối đa 3")
    void testBug14_RescheduleAppointment_ExcludesCurrentAppointmentIdFromSlotCount() {
        Long consultationId = 10L;
        Consultation existing = new Consultation();
        existing.setId(consultationId);
        existing.setConsultationCode("CS-999999");
        existing.setCustomerName("Nguyễn Văn A");
        existing.setPhone("0912345678");
        existing.setStatus(ConsultationStatus.PENDING);
        existing.setPreferredDate(nextWorkingDayAt9AM);

        when(consultationRepository.findById(consultationId)).thenReturn(Optional.of(existing));
        // Khi loại trừ ID 10L, số lượng còn lại trong slot là 2 (hợp lệ)
        when(consultationRepository.countAppointmentsInSlot(any(), any(), eq(ConsultationStatus.CANCELLED), eq(consultationId)))
                .thenReturn(2L);
        when(consultationRepository.save(any(Consultation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime newTimeInSameSlot = nextWorkingDayAt9AM.plusMinutes(15);
        Consultation updated = consultationService.reschedule(consultationId, newTimeInSameSlot);

        assertNotNull(updated);
        assertEquals(newTimeInSameSlot, updated.getPreferredDate());
        verify(consultationRepository).countAppointmentsInSlot(any(), any(), eq(ConsultationStatus.CANCELLED), eq(consultationId));
    }

    @Test
    @DisplayName("Bug 18 Test: Không được chuyển trạng thái tùy tiện khi lịch hẹn đã COMPLETED hoặc CANCELLED")
    void testBug18_UpdateStatus_CannotChangeCompletedOrCancelledAppointment() {
        Consultation completed = new Consultation();
        completed.setId(1L);
        completed.setStatus(ConsultationStatus.COMPLETED);

        Consultation cancelled = new Consultation();
        cancelled.setId(2L);
        cancelled.setStatus(ConsultationStatus.CANCELLED);

        Consultation pending = new Consultation();
        pending.setId(3L);
        pending.setStatus(ConsultationStatus.PENDING);

        when(consultationRepository.findById(1L)).thenReturn(Optional.of(completed));
        when(consultationRepository.findById(2L)).thenReturn(Optional.of(cancelled));
        when(consultationRepository.findById(3L)).thenReturn(Optional.of(pending));
        when(consultationRepository.save(any(Consultation.class))).thenAnswer(i -> i.getArgument(0));

        // 1. COMPLETED không được đổi sang bất kỳ trạng thái nào khác
        BusinessException ex1 = assertThrows(BusinessException.class,
                () -> consultationService.updateStatus(1L, ConsultationStatus.CONFIRMED));
        assertTrue(ex1.getMessage().contains("không thể thay đổi") || ex1.getMessage().contains("không thể chuyển"));

        // 2. CANCELLED không được đổi sang bất kỳ trạng thái nào khác
        BusinessException ex2 = assertThrows(BusinessException.class,
                () -> consultationService.updateStatus(2L, ConsultationStatus.PENDING));
        assertTrue(ex2.getMessage().contains("không thể thay đổi") || ex2.getMessage().contains("không thể chuyển"));

        // 3. PENDING được đổi sang CONFIRMED
        Consultation confirmed = consultationService.updateStatus(3L, ConsultationStatus.CONFIRMED);
        assertEquals(ConsultationStatus.CONFIRMED, confirmed.getStatus());
    }
}
