package com.dulich.ticketmanagement.service.impl;

import com.dulich.ticketmanagement.entity.FlightBooking;
import com.dulich.ticketmanagement.entity.FlightPassenger;
import com.dulich.ticketmanagement.entity.TrainBooking;
import com.dulich.ticketmanagement.entity.TrainTicket;
import com.dulich.ticketmanagement.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${app.mail.from:VietTravel Hub <support@viettravelhub.vn>}")
    private String mailFrom;

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0 VNĐ";
        return String.format("%,.0f VNĐ", amount);
    }

    private void sendHtmlEmailAsync(String toEmail, String subject, String htmlContent) {
        if (toEmail == null || toEmail.trim().isEmpty() || !toEmail.contains("@")) {
            log.warn("Bỏ qua gửi email: Địa chỉ email người nhận không hợp lệ '{}'", toEmail);
            return;
        }

        CompletableFuture.runAsync(() -> {
            boolean hasCredentials = mailSender != null
                    && mailUsername != null && !mailUsername.trim().isEmpty()
                    && mailPassword != null && !mailPassword.trim().isEmpty();

            if (hasCredentials) {
                try {
                    MimeMessage message = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                    helper.setFrom(mailFrom);
                    helper.setTo(toEmail.trim());
                    helper.setSubject(subject);
                    helper.setText(htmlContent, true);

                    mailSender.send(message);
                    log.info(">>> [SMTP GMAIL THÀNH CÔNG] Đã gửi email '{}' đến: {}", subject, toEmail);
                    return;
                } catch (Exception ex) {
                    log.warn(">>> [SMTP GMAIL CẢNH BÁO] Không thể gửi email trực tiếp qua SMTP ({}). Chuyển sang ghi log nội dung thông tin vé.", ex.getMessage());
                }
            }

            // Fallback: Ghi log định dạng thông báo khi chưa cài mật khẩu ứng dụng Gmail SMTP
            log.info("=======================================================================");
            log.info(">>> [THÔNG BÁO VÉ GỬI VỀ GMAIL: {}]", toEmail);
            log.info(">>> TIÊU ĐỀ: {}", subject);
            log.info(">>> HỆ THỐNG: VietTravel Hub - Dịch vụ đặt vé Du lịch & Đường sắt");
            log.info("=======================================================================");
        });
    }

    // =========================================================================
    // 1. EMAIL VÉ MÁY BAY
    // =========================================================================

    @Override
    public void sendFlightBookingHeldEmail(FlightBooking booking) {
        String subject = "✈️ [VietTravel Hub] Xác nhận giữ chỗ vé máy bay: " + booking.getBookingCode();
        String html = buildFlightEmailTemplate(booking, "GIỮ CHỖ THÀNH CÔNG (CẦN THANH TOÁN)", "#0284c7");
        sendHtmlEmailAsync(booking.getCustomerEmail(), subject, html);
    }

    @Override
    public void sendFlightBookingConfirmedEmail(FlightBooking booking) {
        String subject = "✈️ [VietTravel Hub] Vé máy bay điện tử chính thức: " + booking.getBookingCode();
        String html = buildFlightEmailTemplate(booking, "XÁC NHẬN PHÁT HÀNH VÉ ĐIỆN TỬ", "#16a34a");
        sendHtmlEmailAsync(booking.getCustomerEmail(), subject, html);
    }

    @Override
    public void sendFlightBookingCancelledEmail(FlightBooking booking) {
        String subject = "✈️ [VietTravel Hub] Xác nhận hủy vé máy bay: " + booking.getBookingCode();
        String html = buildFlightEmailTemplate(booking, "ĐƠN VÉ ĐÃ ĐƯỢC HỦY", "#dc2626");
        sendHtmlEmailAsync(booking.getCustomerEmail(), subject, html);
    }

    private String buildFlightEmailTemplate(FlightBooking booking, String statusTitle, String badgeColor) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head>");
        sb.append("<body style='font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Helvetica, Arial, sans-serif; line-height: 1.6; color: #1e293b; background-color: #f8fafc; padding: 20px 0; margin: 0;'>");
        sb.append("<div style='max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); border: 1px solid #e2e8f0;'>");

        // Header
        sb.append("<div style='background: linear-gradient(135deg, #0284c7 0%, #0369a1 100%); padding: 24px; text-align: center; color: #ffffff;'>");
        sb.append("<div style='font-size: 22px; font-weight: 800; letter-spacing: 0.5px;'>VietTravel Hub</div>");
        sb.append("<div style='font-size: 13px; opacity: 0.9; margin-top: 4px;'>Dịch vụ vé máy bay nội địa chính hãng</div>");
        sb.append("</div>");

        // Body content
        sb.append("<div style='padding: 24px;'>");
        sb.append("<div style='display: inline-block; padding: 6px 14px; border-radius: 9999px; background: ").append(badgeColor).append("; color: #ffffff; font-weight: 700; font-size: 12px; margin-bottom: 16px;'>")
          .append(statusTitle).append("</div>");

        sb.append("<h2 style='margin: 0 0 8px 0; font-size: 18px; color: #0f172a;'>Xin chào <strong>").append(booking.getCustomerName()).append("</strong>,</h2>");
        sb.append("<p style='margin: 0 0 16px 0; font-size: 14px; color: #475569;'>Cảm ơn quý khách đã tin tưởng lựa chọn VietTravel Hub. Dưới đây là thông tin chi tiết đơn vé máy bay của quý khách:</p>");

        // Booking Code Highlight Box
        sb.append("<div style='background: #f0f9ff; border: 1px dashed #0284c7; border-radius: 8px; padding: 14px; text-align: center; margin-bottom: 20px;'>");
        sb.append("<div style='font-size: 12px; color: #0369a1; text-transform: uppercase; font-weight: 700;'>Mã đặt chỗ (Booking Code)</div>");
        sb.append("<div style='font-size: 24px; font-weight: 800; color: #0284c7; letter-spacing: 1px; margin-top: 4px;'>").append(booking.getBookingCode()).append("</div>");
        if (booking.getStatus().name().equals("HELD") && booking.getHoldExpiresAt() != null) {
            sb.append("<div style='font-size: 12px; color: #dc2626; margin-top: 4px; font-weight: 600;'>Thời hạn giữ chỗ đến: ").append(booking.getHoldExpiresAt().format(DATETIME_FMT)).append("</div>");
        }
        sb.append("</div>");

        // Itinerary Details
        sb.append("<div style='margin-bottom: 20px;'>");
        sb.append("<div style='font-weight: 700; font-size: 14px; color: #0f172a; border-bottom: 2px solid #f1f5f9; padding-bottom: 6px; margin-bottom: 10px;'>✈️ Thông tin hành trình bay</div>");
        sb.append("<table style='width: 100%; border-collapse: collapse; font-size: 13px;'>");
        sb.append("<tr><td style='padding: 6px 0; color: #64748b;'>Loại vé:</td><td style='padding: 6px 0; font-weight: 700; text-align: right; color: ").append(booking.isRoundTrip() ? "#059669" : "#0284c7").append(";'>").append(booking.isRoundTrip() ? "Khứ hồi (2 chiều)" : "Một chiều").append("</td></tr>");
        if (booking.getFlight() != null) {
            sb.append("<tr><td style='padding: 6px 0; color: #64748b; font-weight: 600;'>[Chiều đi] Chuyến bay:</td><td style='padding: 6px 0; font-weight: 700; text-align: right;'>").append(booking.getFlight().getFlightNumber()).append(" (").append(booking.getFlight().getAirline()).append(") - ").append(booking.getSeatClass().getDisplayName()).append("</td></tr>");
            sb.append("<tr><td style='padding: 6px 0; color: #64748b;'>Chặng đi:</td><td style='padding: 6px 0; font-weight: 700; text-align: right;'>").append(booking.getFlight().getDepartureCity()).append(" (").append(booking.getFlight().getDepartureAirport()).append(") &rarr; ").append(booking.getFlight().getArrivalCity()).append(" (").append(booking.getFlight().getArrivalAirport()).append(")</td></tr>");
            if (booking.getFlight().getDepartureTime() != null) {
                sb.append("<tr><td style='padding: 6px 0; color: #64748b;'>Giờ cất cánh đi:</td><td style='padding: 6px 0; font-weight: 700; text-align: right; color: #0284c7;'>").append(booking.getFlight().getDepartureTime().format(DATETIME_FMT)).append("</td></tr>");
            }
        }
        if (booking.isRoundTrip() && booking.getReturnFlight() != null) {
            String retSeatClassName = booking.getReturnSeatClass() != null ? booking.getReturnSeatClass().getDisplayName() : booking.getSeatClass().getDisplayName();
            sb.append("<tr style='border-top: 1px dashed #e2e8f0;'><td style='padding: 6px 0; color: #64748b; font-weight: 600;'>[Chiều về] Chuyến bay:</td><td style='padding: 6px 0; font-weight: 700; text-align: right;'>").append(booking.getReturnFlight().getFlightNumber()).append(" (").append(booking.getReturnFlight().getAirline()).append(") - ").append(retSeatClassName).append("</td></tr>");
            sb.append("<tr><td style='padding: 6px 0; color: #64748b;'>Chặng về:</td><td style='padding: 6px 0; font-weight: 700; text-align: right;'>").append(booking.getReturnFlight().getDepartureCity()).append(" (").append(booking.getReturnFlight().getDepartureAirport()).append(") &rarr; ").append(booking.getReturnFlight().getArrivalCity()).append(" (").append(booking.getReturnFlight().getArrivalAirport()).append(")</td></tr>");
            if (booking.getReturnFlight().getDepartureTime() != null) {
                sb.append("<tr><td style='padding: 6px 0; color: #64748b;'>Giờ cất cánh về:</td><td style='padding: 6px 0; font-weight: 700; text-align: right; color: #059669;'>").append(booking.getReturnFlight().getDepartureTime().format(DATETIME_FMT)).append("</td></tr>");
            }
        }
        sb.append("<tr><td style='padding: 6px 0; color: #64748b;'>Số lượng hành khách:</td><td style='padding: 6px 0; font-weight: 700; text-align: right;'>").append(booking.getPassengerCount()).append(" khách</td></tr>");
        sb.append("</table>");
        sb.append("</div>");

        // Passengers
        if (booking.getPassengers() != null && !booking.getPassengers().isEmpty()) {
            sb.append("<div style='margin-bottom: 20px;'>");
            sb.append("<div style='font-weight: 700; font-size: 14px; color: #0f172a; border-bottom: 2px solid #f1f5f9; padding-bottom: 6px; margin-bottom: 10px;'>👤 Danh sách hành khách &amp; Chỗ ngồi</div>");
            sb.append("<table style='width: 100%; border-collapse: collapse; font-size: 13px;'>");
            sb.append("<tr style='background: #f8fafc; font-weight: 600; text-align: left;'><th style='padding: 8px;'>Hành khách</th><th style='padding: 8px;'>Ghế ngồi</th><th style='padding: 8px;'>Hành lý</th><th style='padding: 8px; text-align: right;'>Mã vé</th></tr>");
            for (FlightPassenger p : booking.getPassengers()) {
                sb.append("<tr style='border-bottom: 1px solid #f1f5f9;'>");
                sb.append("<td style='padding: 8px;'><strong>").append(p.getFullName()).append("</strong><br/><small style='color: #64748b;'>").append(p.getPassengerType().getDisplayName()).append("</small></td>");
                
                // Seats
                sb.append("<td style='padding: 8px;'>");
                sb.append("<span style='background: #e0f2fe; color: #0369a1; padding: 2px 6px; border-radius: 4px; font-weight: 700; font-size: 12px;'>Đi: ").append(p.getSeatNumber() != null ? p.getSeatNumber() : "Tự động").append("</span>");
                if (booking.isRoundTrip()) {
                    sb.append("<br/><span style='background: #dcfce7; color: #15803d; padding: 2px 6px; border-radius: 4px; font-weight: 700; font-size: 12px; margin-top: 3px; display: inline-block;'>Về: ").append(p.getReturnSeatNumber() != null ? p.getReturnSeatNumber() : "Tự động").append("</span>");
                }
                sb.append("</td>");

                sb.append("<td style='padding: 8px;'>+").append(p.getBaggageKg()).append(" kg</td>");
                
                // Ticket codes
                sb.append("<td style='padding: 8px; text-align: right;'>");
                sb.append("<code style='color: #0284c7; font-weight: 700; font-size: 12px;'>").append(p.getTicketCode() != null ? p.getTicketCode() : "Đang cấp").append("</code>");
                if (booking.isRoundTrip() && p.getReturnTicketCode() != null) {
                    sb.append("<br/><code style='color: #059669; font-weight: 700; font-size: 12px;'>").append(p.getReturnTicketCode()).append("</code>");
                }
                sb.append("</td>");

                sb.append("</tr>");
            }
            sb.append("</table>");
            sb.append("</div>");
        }

        // Total amount
        sb.append("<div style='background: #f8fafc; border-radius: 8px; padding: 14px; margin-bottom: 20px;'>");
        sb.append("<div style='display: flex; justify-content: space-between; font-size: 15px;'>");
        sb.append("<span><strong>Tổng tiền thanh toán:</strong></span>");
        sb.append("<span style='color: #ea580c; font-size: 18px; font-weight: 800; float: right;'>").append(formatMoney(booking.getTotalAmount())).append("</span>");
        sb.append("</div>");
        if (booking.getRefundAmount() != null && booking.getRefundAmount().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("<div style='font-size: 13px; color: #16a34a; margin-top: 4px; text-align: right;'>Tiền hoàn lại: <strong>").append(formatMoney(booking.getRefundAmount())).append("</strong></div>");
        }
        sb.append("</div>");

        // Guidance
        sb.append("<div style='font-size: 12px; color: #64748b; line-height: 1.5; border-top: 1px solid #e2e8f0; padding-top: 14px;'>");
        sb.append("<strong>📌 Lưu ý quan trọng:</strong>");
        sb.append("<ul style='margin: 4px 0 0 16px; padding: 0;'>");
        sb.append("<li>Quý khách vui lòng có mặt tại sân bay trước giờ khởi hành ít nhất <strong>90 phút</strong> để làm thủ tục check-in.</li>");
        sb.append("<li>Mang theo bản gốc CCCD/Hộ chiếu hoặc Giấy khai sinh (với trẻ em) khớp với thông tin đã đăng ký.</li>");
        sb.append("<li>Quý khách có thể xem và quản lý đơn vé mọi lúc tại website VietTravel Hub.</li>");
        sb.append("</ul>");
        sb.append("</div>");

        sb.append("</div>"); // end body

        // Footer
        sb.append("<div style='background: #f1f5f9; padding: 16px; text-align: center; font-size: 12px; color: #64748b;'>");
        sb.append("VietTravel Hub &copy; 2026. Hỗ trợ khách hàng 24/7 qua Hotline: <strong>1900 6868</strong>");
        sb.append("</div>");

        sb.append("</div></body></html>");
        return sb.toString();
    }

    // =========================================================================
    // 2. EMAIL VÉ TÀU HỎA
    // =========================================================================

    @Override
    public void sendTrainBookingHeldEmail(TrainBooking booking) {
        String subject = "🚆 [VietTravel Hub] Xác nhận giữ chỗ vé tàu hỏa: " + booking.getBookingCode();
        String html = buildTrainEmailTemplate(booking, "GIỮ CHỖ TÀU HỎA (10 PHÚT)", "#d97706");
        sendHtmlEmailAsync(booking.getCustomerEmail(), subject, html);
    }

    @Override
    public void sendTrainBookingConfirmedEmail(TrainBooking booking) {
        String subject = "🚆 [VietTravel Hub] Vé tàu hỏa điện tử chính thức: " + booking.getBookingCode();
        String html = buildTrainEmailTemplate(booking, "XÁC NHẬN PHÁT HÀNH THÀNH CÔNG", "#16a34a");
        sendHtmlEmailAsync(booking.getCustomerEmail(), subject, html);
    }

    @Override
    public void sendTrainBookingCancelledEmail(TrainBooking booking) {
        String subject = "🚆 [VietTravel Hub] Xác nhận trả/hủy vé tàu hỏa: " + booking.getBookingCode();
        String html = buildTrainEmailTemplate(booking, "ĐÃ TRẢ / HỦY VÉ TÀU", "#dc2626");
        sendHtmlEmailAsync(booking.getCustomerEmail(), subject, html);
    }

    private String buildTrainEmailTemplate(TrainBooking booking, String statusTitle, String badgeColor) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head>");
        sb.append("<body style='font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Helvetica, Arial, sans-serif; line-height: 1.6; color: #1e293b; background-color: #f8fafc; padding: 20px 0; margin: 0;'>");
        sb.append("<div style='max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); border: 1px solid #e2e8f0;'>");

        // Header
        sb.append("<div style='background: linear-gradient(135deg, #d97706 0%, #b45309 100%); padding: 24px; text-align: center; color: #ffffff;'>");
        sb.append("<div style='font-size: 22px; font-weight: 800; letter-spacing: 0.5px;'>VietTravel Hub</div>");
        sb.append("<div style='font-size: 13px; opacity: 0.9; margin-top: 4px;'>Dịch vụ vé tàu hỏa đường sắt Bắc - Nam</div>");
        sb.append("</div>");

        // Body content
        sb.append("<div style='padding: 24px;'>");
        sb.append("<div style='display: inline-block; padding: 6px 14px; border-radius: 9999px; background: ").append(badgeColor).append("; color: #ffffff; font-weight: 700; font-size: 12px; margin-bottom: 16px;'>")
          .append(statusTitle).append("</div>");

        sb.append("<h2 style='margin: 0 0 8px 0; font-size: 18px; color: #0f172a;'>Xin chào <strong>").append(booking.getCustomerName()).append("</strong>,</h2>");
        sb.append("<p style='margin: 0 0 16px 0; font-size: 14px; color: #475569;'>Cảm ơn quý khách đã đặt vé tàu hỏa tại VietTravel Hub. Thông tin hành trình và mã vé của quý khách như sau:</p>");

        // Booking Code Highlight Box
        sb.append("<div style='background: #fffbeb; border: 1px dashed #d97706; border-radius: 8px; padding: 14px; text-align: center; margin-bottom: 20px;'>");
        sb.append("<div style='font-size: 12px; color: #b45309; text-transform: uppercase; font-weight: 700;'>Mã đặt vé tàu (Booking Code)</div>");
        sb.append("<div style='font-size: 24px; font-weight: 800; color: #d97706; letter-spacing: 1px; margin-top: 4px;'>").append(booking.getBookingCode()).append("</div>");
        if (booking.getStatus().name().equals("HELD") && booking.getHoldExpiresAt() != null) {
            sb.append("<div style='font-size: 12px; color: #dc2626; margin-top: 4px; font-weight: 600;'>Thời hạn giữ chỗ đến: ").append(booking.getHoldExpiresAt().format(DATETIME_FMT)).append("</div>");
        }
        sb.append("</div>");

        // Itinerary Details
        sb.append("<div style='margin-bottom: 20px;'>");
        sb.append("<div style='font-weight: 700; font-size: 14px; color: #0f172a; border-bottom: 2px solid #f1f5f9; padding-bottom: 6px; margin-bottom: 10px;'>🚆 Lịch trình chuyến tàu</div>");
        sb.append("<table style='width: 100%; border-collapse: collapse; font-size: 13px;'>");
        sb.append("<tr><td style='padding: 6px 0; color: #64748b;'>Loại vé:</td><td style='padding: 6px 0; font-weight: 700; text-align: right; color: ").append(booking.isRoundTrip() ? "#059669" : "#d97706").append(";'>").append(booking.isRoundTrip() ? "Khứ hồi (2 chiều)" : "Một chiều").append("</td></tr>");
        if (booking.getTrain() != null) {
            sb.append("<tr><td style='padding: 6px 0; color: #64748b;'>Đoàn tàu chiều đi:</td><td style='padding: 6px 0; font-weight: 700; text-align: right; color: #d97706;'>").append(booking.getTrain().getTrainCode()).append(" (").append(booking.getTrain().getRouteName()).append(")</td></tr>");
        }
        if (booking.getDepartureStation() != null && booking.getArrivalStation() != null) {
            sb.append("<tr><td style='padding: 6px 0; color: #64748b;'>Chặng đi:</td><td style='padding: 6px 0; font-weight: 700; text-align: right;'>").append(booking.getDepartureStation().getStationName()).append(" &rarr; ").append(booking.getArrivalStation().getStationName()).append("</td></tr>");
        }
        if (booking.getDepartureDatetime() != null) {
            sb.append("<tr><td style='padding: 6px 0; color: #64748b;'>Khởi hành chiều đi:</td><td style='padding: 6px 0; font-weight: 700; text-align: right;'>").append(booking.getDepartureDatetime().format(DATETIME_FMT)).append("</td></tr>");
        } else if (booking.getDepartureDate() != null) {
            sb.append("<tr><td style='padding: 6px 0; color: #64748b;'>Ngày khởi hành chiều đi:</td><td style='padding: 6px 0; font-weight: 700; text-align: right;'>").append(booking.getDepartureDate().format(DATE_FMT)).append("</td></tr>");
        }

        if (booking.isRoundTrip() && booking.getReturnTrain() != null) {
            sb.append("<tr style='border-top: 1px dashed #e2e8f0;'><td style='padding: 6px 0; color: #64748b;'>Đoàn tàu chiều về:</td><td style='padding: 6px 0; font-weight: 700; text-align: right; color: #0284c7;'>").append(booking.getReturnTrain().getTrainCode()).append(" (").append(booking.getReturnTrain().getRouteName()).append(")</td></tr>");
            if (booking.getDepartureStation() != null && booking.getArrivalStation() != null) {
                sb.append("<tr><td style='padding: 6px 0; color: #64748b;'>Chặng về:</td><td style='padding: 6px 0; font-weight: 700; text-align: right;'>").append(booking.getArrivalStation().getStationName()).append(" &rarr; ").append(booking.getDepartureStation().getStationName()).append("</td></tr>");
            }
            if (booking.getReturnDepartureDatetime() != null) {
                sb.append("<tr><td style='padding: 6px 0; color: #64748b;'>Khởi hành chiều về:</td><td style='padding: 6px 0; font-weight: 700; text-align: right;'>").append(booking.getReturnDepartureDatetime().format(DATETIME_FMT)).append("</td></tr>");
            } else if (booking.getReturnDepartureDate() != null) {
                sb.append("<tr><td style='padding: 6px 0; color: #64748b;'>Ngày khởi hành chiều về:</td><td style='padding: 6px 0; font-weight: 700; text-align: right;'>").append(booking.getReturnDepartureDate().format(DATE_FMT)).append("</td></tr>");
            }
        }

        sb.append("<tr><td style='padding: 6px 0; color: #64748b;'>Số lượng khách:</td><td style='padding: 6px 0; font-weight: 700; text-align: right;'>").append(booking.getTicketCount()).append(" người</td></tr>");
        sb.append("</table>");
        sb.append("</div>");

        // Tickets & Seats
        if (booking.getTickets() != null && !booking.getTickets().isEmpty()) {
            sb.append("<div style='margin-bottom: 20px;'>");
            sb.append("<div style='font-weight: 700; font-size: 14px; color: #0f172a; border-bottom: 2px solid #f1f5f9; padding-bottom: 6px; margin-bottom: 10px;'>🎟️ Chi tiết vé &amp; Chỗ ngồi</div>");
            sb.append("<table style='width: 100%; border-collapse: collapse; font-size: 13px;'>");
            if (booking.isRoundTrip()) {
                sb.append("<tr style='background: #f8fafc; font-weight: 600; text-align: left;'><th style='padding: 8px;'>Hành khách</th><th style='padding: 8px;'>Chỗ Chiều Đi</th><th style='padding: 8px;'>Chỗ Chiều Về</th><th style='padding: 8px;'>Đối tượng</th><th style='padding: 8px; text-align: right;'>Mã vé</th></tr>");
            } else {
                sb.append("<tr style='background: #f8fafc; font-weight: 600; text-align: left;'><th style='padding: 8px;'>Hành khách</th><th style='padding: 8px;'>Toa / Ghế</th><th style='padding: 8px;'>Đối tượng</th><th style='padding: 8px; text-align: right;'>Mã vé</th></tr>");
            }
            for (TrainTicket t : booking.getTickets()) {
                sb.append("<tr style='border-bottom: 1px solid #f1f5f9;'>");
                sb.append("<td style='padding: 8px;'><strong>").append(t.getPassengerName()).append("</strong></td>");
                String seatInfo = (t.getSeat() != null && t.getSeat().getCoach() != null)
                        ? "Toa " + t.getSeat().getCoach().getCoachNumber() + " - " + t.getSeat().getSeatLabel()
                        : "Chỗ tự động";
                sb.append("<td style='padding: 8px;'><span style='background: #fef3c7; color: #b45309; padding: 2px 6px; border-radius: 4px; font-weight: 700;'>").append(seatInfo).append("</span></td>");
                if (booking.isRoundTrip()) {
                    String retSeatInfo = (t.getReturnSeat() != null && t.getReturnSeat().getCoach() != null)
                            ? "Toa " + t.getReturnSeat().getCoach().getCoachNumber() + " - " + t.getReturnSeat().getSeatLabel()
                            : "Chỗ tự động";
                    sb.append("<td style='padding: 8px;'><span style='background: #e0f2fe; color: #0369a1; padding: 2px 6px; border-radius: 4px; font-weight: 700;'>").append(retSeatInfo).append("</span></td>");
                }
                sb.append("<td style='padding: 8px;'>").append(t.getPriorityCategory() != null ? t.getPriorityCategory().getDisplayName() : "Tiêu chuẩn").append("</td>");
                sb.append("<td style='padding: 8px; text-align: right;'>");
                sb.append("<code style='color: #d97706; font-weight: 700;'>").append(t.getTicketCode() != null ? t.getTicketCode() : "Đang cấp").append("</code>");
                if (booking.isRoundTrip() && t.getReturnTicketCode() != null) {
                    sb.append("<br/><code style='color: #0284c7; font-weight: 700;'>").append(t.getReturnTicketCode()).append("</code>");
                }
                sb.append("</td></tr>");
            }
            sb.append("</table>");
            sb.append("</div>");
        }

        // Total amount
        sb.append("<div style='background: #f8fafc; border-radius: 8px; padding: 14px; margin-bottom: 20px;'>");
        sb.append("<div style='display: flex; justify-content: space-between; font-size: 15px;'>");
        sb.append("<span><strong>Tổng tiền thanh toán:</strong></span>");
        sb.append("<span style='color: #ea580c; font-size: 18px; font-weight: 800; float: right;'>").append(formatMoney(booking.getTotalAmount())).append("</span>");
        sb.append("</div>");
        if (booking.getRefundAmount() != null && booking.getRefundAmount().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("<div style='font-size: 13px; color: #16a34a; margin-top: 4px; text-align: right;'>Tiền hoàn lại: <strong>").append(formatMoney(booking.getRefundAmount())).append("</strong></div>");
        }
        sb.append("</div>");

        // Guidance
        sb.append("<div style='font-size: 12px; color: #64748b; line-height: 1.5; border-top: 1px solid #e2e8f0; padding-top: 14px;'>");
        sb.append("<strong>📌 Quy định lên tàu:</strong>");
        sb.append("<ul style='margin: 4px 0 0 16px; padding: 0;'>");
        sb.append("<li>Quý khách vui lòng có mặt tại ga đường sắt trước giờ tàu chạy ít nhất <strong>30-45 phút</strong>.</li>");
        sb.append("<li>Xuất trình vé điện tử (trên email hoặc điện thoại) kèm CCCD/Hộ chiếu khi qua cổng soát vé.</li>");
        sb.append("<li>Quý khách có thể quản lý, đổi chặng hoặc trả vé trực tuyến theo quy định ngành đường sắt.</li>");
        sb.append("</ul>");
        sb.append("</div>");

        sb.append("</div>"); // end body

        // Footer
        sb.append("<div style='background: #f1f5f9; padding: 16px; text-align: center; font-size: 12px; color: #64748b;'>");
        sb.append("VietTravel Hub &copy; 2026. Hỗ trợ khách hàng đường sắt 24/7 qua Hotline: <strong>1900 6868</strong>");
        sb.append("</div>");

        sb.append("</div></body></html>");
        return sb.toString();
    }
}
