package com.dulich.ticketmanagement.service.impl;

import com.dulich.ticketmanagement.entity.Reservation;
import com.dulich.ticketmanagement.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Autowired(required = false)
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void sendReservationHeldEmail(Reservation reservation) {
        if (reservation == null || reservation.getCustomerEmail() == null || reservation.getCustomerEmail().isBlank()) {
            return;
        }
        String subject = "【VietTravel Hub】Xác nhận giữ chỗ thành công mã: " + reservation.getBookingCode();
        String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; line-height: 1.6; color: #333;'>" +
                "<h2 style='color: #0f2942;'>Kính chào Quý khách " + reservation.getCustomerName() + "!</h2>" +
                "<p>Quý khách vừa thực hiện giữ chỗ thành công trên hệ thống <b>VietTravel Hub</b>:</p>" +
                "<div style='background: #f1f5f9; padding: 16px; border-radius: 8px; margin: 16px 0;'>" +
                "<p style='margin: 4px 0;'>Mã đơn đặt chỗ: <b style='color: #0062d2; font-size: 16px;'>" + reservation.getBookingCode() + "</b></p>" +
                "<p style='margin: 4px 0;'>Hành trình: <b>" + reservation.getDeparturePlace() + " ➔ " + reservation.getArrivalPlace() + "</b></p>" +
                "<p style='margin: 4px 0;'>Phương tiện: <b>" + reservation.getTransportType().getFullName() + "</b></p>" +
                "<p style='margin: 4px 0;'>Số lượng hành khách: <b>" + reservation.getPassengerCount() + " khách</b></p>" +
                "<p style='margin: 4px 0;'>Tổng tiền thanh toán: <b style='color: #d9531e; font-size: 16px;'>" + String.format("%,.0f", reservation.getTotalAmount()) + " VNĐ</b></p>" +
                "</div>" +
                "<p style='color: #b45309; font-weight: bold;'>⚠️ Thời hạn giữ chỗ: 15 phút (đến lúc " + reservation.getHoldExpiresAt() + ").</p>" +
                "<p>Vui lòng hoàn tất thanh toán trước khi thời hạn kết thúc để vé và mã QR Code được phát hành chính thức.</p>" +
                "<p>Trân trọng,<br><b>Đội ngũ VietTravel Hub</b></p>" +
                "</div>";
        sendHtmlEmail(reservation.getCustomerEmail(), subject, content);
    }

    @Override
    @Async
    public void sendReservationConfirmedEmail(Reservation reservation) {
        if (reservation == null || reservation.getCustomerEmail() == null || reservation.getCustomerEmail().isBlank()) {
            return;
        }
        String subject = "【VietTravel Hub】Vé điện tử chính thức mã: " + reservation.getBookingCode();
        String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; line-height: 1.6; color: #333;'>" +
                "<h2 style='color: #059669;'>Chúc mừng Quý khách " + reservation.getCustomerName() + "!</h2>" +
                "<p>Đơn đặt vé <b>" + reservation.getBookingCode() + "</b> đã được thanh toán thành công và vé điện tử chính thức đã được kích hoạt.</p>" +
                "<div style='background: #f8fafc; padding: 16px; border: 1px solid #cbd5e1; border-radius: 8px; margin: 16px 0;'>" +
                "<p style='margin: 4px 0;'>Hành trình: <b>" + reservation.getDeparturePlace() + " ➔ " + reservation.getArrivalPlace() + "</b></p>" +
                "<p style='margin: 4px 0;'>Ngày khởi hành: <b>" + reservation.getDepartureDate() + "</b></p>" +
                "<p style='margin: 4px 0;'>Trạng thái: <b style='color: #059669;'>ĐÃ XÁC NHẬN & PHÁT HÀNH VÉ</b></p>" +
                "</div>" +
                "<p>Quý khách có thể xem và tải về mã QR Code lên tàu/máy bay tại đường dẫn tra cứu của chúng tôi.</p>" +
                "<p>Chúc Quý khách có một hành trình an toàn và trọn vẹn!</p>" +
                "<p>Trân trọng,<br><b>Đội ngũ VietTravel Hub</b></p>" +
                "</div>";
        sendHtmlEmail(reservation.getCustomerEmail(), subject, content);
    }

    @Override
    @Async
    public void sendReservationCancelledEmail(Reservation reservation) {
        if (reservation == null || reservation.getCustomerEmail() == null || reservation.getCustomerEmail().isBlank()) {
            return;
        }
        String subject = "【VietTravel Hub】Thông báo hủy đơn vé: " + reservation.getBookingCode();
        String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; line-height: 1.6; color: #333;'>" +
                "<h2 style='color: #dc2626;'>Kính gửi Quý khách " + reservation.getCustomerName() + ",</h2>" +
                "<p>Đơn đặt vé <b>" + reservation.getBookingCode() + "</b> (" + reservation.getDeparturePlace() + " ➔ " + reservation.getArrivalPlace() + ") đã được hủy trên hệ thống.</p>" +
                "<p>Lý do: " + (reservation.getCancelReason() != null ? reservation.getCancelReason() : "Theo yêu cầu của khách hàng hoặc hết hạn giữ chỗ") + ".</p>" +
                "<p>Nếu cần hỗ trợ thêm thông tin, vui lòng liên hệ hotline VietTravel Hub.</p>" +
                "<p>Trân trọng,<br><b>Đội ngũ VietTravel Hub</b></p>" +
                "</div>";
        sendHtmlEmail(reservation.getCustomerEmail(), subject, content);
    }

    @Override
    @Async
    public void sendConsultationConfirmedEmail(Reservation reservation) {
        if (reservation == null || reservation.getCustomerEmail() == null || reservation.getCustomerEmail().isBlank()) {
            return;
        }
        String subject = "【VietTravel Hub】Xác nhận đăng ký lịch hẹn tư vấn đơn vé: " + reservation.getBookingCode();
        String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; line-height: 1.6; color: #333;'>" +
                "<h2 style='color: #0062d2;'>Kính chào Quý khách " + reservation.getCustomerName() + "!</h2>" +
                "<p>Hệ thống <b>VietTravel Hub</b> đã tiếp nhận yêu cầu đặt lịch hẹn tư vấn cho chuyến đi của Quý khách:</p>" +
                "<div style='background: #f0fdf4; padding: 16px; border: 1px solid #bbf7d0; border-radius: 8px; margin: 16px 0;'>" +
                "<p style='margin: 4px 0;'>Mã đơn vé liên quan: <b style='color: #15803d; font-size: 16px;'>" + reservation.getBookingCode() + "</b></p>" +
                "<p style='margin: 4px 0;'>Hành trình: <b>" + reservation.getDeparturePlace() + " ➔ " + reservation.getArrivalPlace() + "</b></p>" +
                "<p style='margin: 4px 0;'>Thời gian hẹn tư vấn: <b>" + reservation.getConsultationDate() + "</b></p>" +
                "<p style='margin: 4px 0;'>Số điện thoại liên hệ: <b>" + reservation.getCustomerPhone() + "</b></p>" +
                "<p style='margin: 4px 0;'>Ghi chú yêu cầu: <i>" + (reservation.getConsultationNotes() != null ? reservation.getConsultationNotes() : "Tư vấn chuyến đi") + "</i></p>" +
                "</div>" +
                "<p>Chuyên viên điều hành của chúng tôi sẽ liên hệ lại với Quý khách trước giờ hẹn.</p>" +
                "<p>Trân trọng cảm ơn Quý khách,<br><b>Phòng Điều Hành Dịch Vụ - VietTravel Hub</b></p>" +
                "</div>";
        sendHtmlEmail(reservation.getCustomerEmail(), subject, content);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        if (mailSender == null || mailPassword == null || mailPassword.isBlank()) {
            log.info("[MOCK MAIL / DEV MODE] Giả lập gửi email thành công tới {}: Tiêu đề: {}", toEmail, subject);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            String sender = (fromEmail != null && !fromEmail.isBlank() && !fromEmail.startsWith("${"))
                    ? fromEmail : "support@viettravelhub.vn";
            helper.setFrom(sender);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Đã gửi email SMTP thành công tới {}", toEmail);
        } catch (Exception ex) {
            log.warn("Không thể gửi email tới {}: {}", toEmail, ex.getMessage());
        }
    }
}
