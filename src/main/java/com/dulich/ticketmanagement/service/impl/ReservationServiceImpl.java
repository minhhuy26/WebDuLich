package com.dulich.ticketmanagement.service.impl;

import com.dulich.ticketmanagement.dto.PassengerDto;
import com.dulich.ticketmanagement.dto.ReservationRequestDto;
import com.dulich.ticketmanagement.entity.Reservation;
import com.dulich.ticketmanagement.entity.Ticket;
import com.dulich.ticketmanagement.entity.TransDetail;
import com.dulich.ticketmanagement.entity.User;
import com.dulich.ticketmanagement.entity.enums.PaymentMethod;
import com.dulich.ticketmanagement.entity.enums.PaymentStatus;
import com.dulich.ticketmanagement.entity.enums.ReservationStatus;
import com.dulich.ticketmanagement.entity.enums.TicketStatus;
import com.dulich.ticketmanagement.exception.BusinessException;
import com.dulich.ticketmanagement.exception.ResourceNotFoundException;
import com.dulich.ticketmanagement.repository.ReservationRepository;
import com.dulich.ticketmanagement.repository.TicketRepository;
import com.dulich.ticketmanagement.repository.TransDetailRepository;
import com.dulich.ticketmanagement.service.EmailService;
import com.dulich.ticketmanagement.service.QrCodeService;
import com.dulich.ticketmanagement.service.ReservationService;
import com.dulich.ticketmanagement.service.TicketCatalogService;
import com.dulich.ticketmanagement.service.strategy.TicketStrategy;
import com.dulich.ticketmanagement.service.strategy.TicketStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationRepository reservationRepository;
    private final TicketRepository ticketRepository;
    private final TransDetailRepository transDetailRepository;
    private final TicketStrategyFactory strategyFactory;
    private final QrCodeService qrCodeService;
    private final EmailService emailService;
    private final TicketCatalogService ticketCatalogService;

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  TicketRepository ticketRepository,
                                  TransDetailRepository transDetailRepository,
                                  TicketStrategyFactory strategyFactory,
                                  QrCodeService qrCodeService,
                                  EmailService emailService,
                                  TicketCatalogService ticketCatalogService) {
        this.reservationRepository = reservationRepository;
        this.ticketRepository = ticketRepository;
        this.transDetailRepository = transDetailRepository;
        this.strategyFactory = strategyFactory;
        this.qrCodeService = qrCodeService;
        this.emailService = emailService;
        this.ticketCatalogService = ticketCatalogService;
    }

    @Override
    public Reservation createHoldReservation(ReservationRequestDto dto, User currentUser) {
        TicketStrategy strategy = strategyFactory.getStrategy(dto.getTransportType());

        Reservation reservation = new Reservation();
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String randomSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        reservation.setBookingCode("RES-" + datePrefix + "-" + randomSuffix);

        reservation.setUser(currentUser);
        reservation.setCustomerName(dto.getCustomerName().trim());
        reservation.setCustomerPhone(dto.getCustomerPhone().trim());
        reservation.setCustomerEmail(dto.getCustomerEmail().trim());
        reservation.setTransportType(dto.getTransportType());
        reservation.setDeparturePlace(dto.getDeparturePlace().trim());
        reservation.setArrivalPlace(dto.getArrivalPlace().trim());
        reservation.setDepartureDate(dto.getDepartureDate().atTime(8, 0));
        if (dto.getReturnDate() != null) {
            reservation.setReturnDate(dto.getReturnDate().atTime(17, 0));
        }
        reservation.setIsRoundTrip(dto.isRoundTrip());
        reservation.setPassengerCount(dto.getPassengers().size());
        reservation.setStatus(ReservationStatus.CREATED);
        reservation.setHoldExpiresAt(LocalDateTime.now().plusMinutes(15));

        // Tạo danh sách tickets cho từng hành khách
        List<Ticket> tickets = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // Lấy thông tin vé mẫu từ kho nếu có
        Ticket baseSampleTicket = null;
        if (dto.getTicketId() != null) {
            baseSampleTicket = ticketRepository.findById(dto.getTicketId()).orElse(null);
        }

        if (baseSampleTicket != null && baseSampleTicket.getStatus() != TicketStatus.AVAILABLE) {
            throw new BusinessException("Chuyến đi hoặc chỗ ngồi này hiện không còn khả dụng (đang được giữ chỗ hoặc đã bán)! Vui lòng chọn chuyến khác.");
        }

        for (int i = 0; i < dto.getPassengers().size(); i++) {
            PassengerDto p = dto.getPassengers().get(i);
            Ticket t;
            if (i == 0 && baseSampleTicket != null) {
                // Khóa vé mẫu được chọn trong kho
                t = baseSampleTicket;
            } else {
                t = new Ticket();
            }

            t.setReservation(reservation);
            t.setTransportType(dto.getTransportType());
            t.setVehicleCode(dto.getVehicleCode());
            t.setVehicleName(dto.getVehicleName() != null ? dto.getVehicleName() : dto.getVehicleCode());
            t.setRouteInfo(dto.getDeparturePlace() + " ➔ " + dto.getArrivalPlace());
            t.setDeparturePlace(dto.getDeparturePlace());
            t.setArrivalPlace(dto.getArrivalPlace());
            t.setDepartureTime(reservation.getDepartureDate());
            t.setArrivalTime(reservation.getDepartureDate().plusHours(3));

            String baseSeatStr = (baseSampleTicket != null && baseSampleTicket.getSeatNumber() != null)
                    ? baseSampleTicket.getSeatNumber() : "12A";
            String seat = (p.getSeatNumber() != null && !p.getSeatNumber().trim().isEmpty())
                    ? p.getSeatNumber().trim()
                    : ticketCatalogService.generateConsecutiveSeat(baseSeatStr, i, dto.getTransportType());
            t.setSeatNumber(seat);
            t.setSeatClass(dto.getSeatClass());
            t.setPassengerName(p.getFullName().trim());
            t.setIdCard(p.getIdCard() != null ? p.getIdCard().trim() : null);
            t.setPassengerType(p.getPassengerType());

            BigDecimal basePrice = (baseSampleTicket != null && baseSampleTicket.getBasePrice() != null)
                    ? baseSampleTicket.getBasePrice() : new BigDecimal("500000");
            t.setBasePrice(basePrice);
            t.setExtraFee(p.getExtraFee() != null ? p.getExtraFee() : BigDecimal.ZERO);

            // Tính giá bằng Strategy Pattern đa hình
            BigDecimal finalPrice = strategy.calculatePrice(t, p.getPassengerType(), t.getExtraFee());
            t.setFinalPrice(finalPrice);
            t.setStatus(TicketStatus.HELD);

            tickets.add(t);
            totalAmount = totalAmount.add(finalPrice);
        }

        // 1.1. Chặn trùng lặp vị trí ghế giữa các hành khách trong cùng một đơn
        java.util.Set<String> chosenSeats = new java.util.HashSet<>();
        for (Ticket t : tickets) {
            String seatUpper = t.getSeatNumber().trim().toUpperCase();
            if (!chosenSeats.add(seatUpper)) {
                throw new BusinessException("Trùng lặp vị trí ghế (" + t.getSeatNumber() + ") giữa các hành khách trong cùng đơn! Vui lòng chọn ghế riêng biệt.");
            }
        }

        // 1.2. Chặn xung đột với các ghế đã có người đặt trước trên chuyến đi
        List<String> occupiedSeats = ticketCatalogService.getOccupiedSeats(dto.getVehicleCode());
        if (occupiedSeats != null && !occupiedSeats.isEmpty()) {
            for (Ticket t : tickets) {
                if (occupiedSeats.contains(t.getSeatNumber().trim())) {
                    if (baseSampleTicket == null || !t.getSeatNumber().trim().equalsIgnoreCase(baseSampleTicket.getSeatNumber().trim())) {
                        throw new BusinessException("Vị trí ghế " + t.getSeatNumber() + " trên chuyến " + dto.getVehicleCode() + " đã có hành khách khác đặt! Vui lòng chọn vị trí ghế khác.");
                    }
                }
            }
        }

        // Thẩm định bằng Strategy Pattern
        strategy.validateReservation(reservation, tickets);

        reservation.setTotalAmount(totalAmount);
        reservation.setTickets(tickets);

        Reservation saved = reservationRepository.save(reservation);
        log.info("Tạo đơn giữ chỗ thành công: {} cho khách {}", saved.getBookingCode(), saved.getCustomerName());

        try {
            emailService.sendReservationHeldEmail(saved);
        } catch (Exception ex) {
            log.warn("Lỗi gửi email xác nhận giữ chỗ: {}", ex.getMessage());
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Reservation getByBookingCode(String bookingCode) {
        Reservation reservation = reservationRepository.findByBookingCodeWithTickets(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt vé: " + bookingCode));
        if (reservation.getTransDetails() != null) {
            reservation.getTransDetails().size(); // Khởi tạo an toàn trong phiên giao dịch Hibernate
        }
        return reservation;
    }

    @Override
    public Reservation confirmAndIssue(String bookingCode, PaymentMethod paymentMethod, String transactionRef, String note) {
        Reservation reservation = getByBookingCode(bookingCode);

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            return reservation;
        }

        if (reservation.getStatus() != ReservationStatus.CREATED) {
            throw new BusinessException("Đơn đặt vé ở trạng thái '" + reservation.getStatus().getDisplayName() + "' không thể thanh toán!");
        }

        if (LocalDateTime.now().isAfter(reservation.getHoldExpiresAt())) {
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
            throw new BusinessException("Đơn đặt vé đã quá hạn giữ chỗ (15 phút)! Vui lòng tiến hành đặt vé mới.");
        }

        // 1. Cập nhật trạng thái đơn sang CONFIRMED
        reservation.setStatus(ReservationStatus.CONFIRMED);

        // 2. Phát hành mã vé điện tử & sinh ảnh QR Code cho từng vé
        String dateSuffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        int idx = 1;
        for (Ticket ticket : reservation.getTickets()) {
            ticket.setStatus(TicketStatus.ISSUED);
            String ticketCode = "ETK-" + reservation.getTransportType().name() + "-" + dateSuffix + "-" + String.format("%03d", idx++);
            ticket.setTicketCode(ticketCode);

            // Tạo chuỗi mã hóa QR Code quét được
            String qrContent = String.format("ETICKET:%s|BOOKING:%s|PASSENGER:%s|SEAT:%s|ROUTE:%s",
                    ticketCode, reservation.getBookingCode(), ticket.getPassengerName(), ticket.getSeatNumber(), ticket.getRouteInfo());
            String qrBase64 = qrCodeService.generateBase64QrCode(qrContent, 200, 200);
            ticket.setQrCodeData(qrBase64);
        }

        // 3. Ghi nhận giao dịch vào bảng trans_details
        String payCode = "PAY-" + dateSuffix + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        TransDetail transDetail = new TransDetail(
                reservation,
                payCode,
                reservation.getTotalAmount(),
                paymentMethod != null ? paymentMethod : PaymentMethod.VNPAY,
                PaymentStatus.SUCCESS,
                transactionRef != null ? transactionRef : "REF-" + System.currentTimeMillis() % 1000000,
                note != null ? note : "Thanh toán thành công đơn đặt chỗ " + reservation.getBookingCode()
        );
        transDetailRepository.save(transDetail);
        reservation.getTransDetails().add(transDetail);

        Reservation saved = reservationRepository.save(reservation);
        log.info("Xác nhận thanh toán và phát hành vé điện tử thành công: {}", saved.getBookingCode());

        try {
            emailService.sendReservationConfirmedEmail(saved);
        } catch (Exception ex) {
            log.warn("Lỗi gửi email xác nhận vé điện tử: {}", ex.getMessage());
        }

        return saved;
    }

    @Override
    public Reservation cancelReservation(String bookingCode, String reason) {
        Reservation reservation = getByBookingCode(bookingCode);

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return reservation;
        }

        TicketStrategy strategy = strategyFactory.getStrategy(reservation.getTransportType());
        boolean wasConfirmed = (reservation.getStatus() == ReservationStatus.CONFIRMED);
        BigDecimal refundAmount = BigDecimal.ZERO;
        BigDecimal fee = BigDecimal.ZERO;

        if (wasConfirmed) {
            fee = strategy.calculateCancellationFee(reservation);
            refundAmount = reservation.getTotalAmount().subtract(fee);
            if (refundAmount.compareTo(BigDecimal.ZERO) < 0) {
                refundAmount = BigDecimal.ZERO;
            }
            log.info("Hủy đơn vé {}: Phí hủy áp dụng theo Strategy = {} VNĐ, Số tiền hoàn trả = {} VNĐ", bookingCode, fee, refundAmount);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelReason(reason != null ? reason : "Khách hàng yêu cầu hủy đơn");

        // Nhả lại trạng thái vé trong kho và xóa thông tin vé điện tử/hành khách
        for (Ticket ticket : reservation.getTickets()) {
            ticket.setStatus(TicketStatus.AVAILABLE);
            ticket.setReservation(null);
            ticket.setPassengerName(null);
            ticket.setIdCard(null);
            ticket.setTicketCode(null);
            ticket.setQrCodeData(null);
            ticketRepository.save(ticket);
        }

        // 2.1. Ghi nhận giao dịch hoàn tiền vào bảng trans_details nếu đơn trước đó đã thanh toán
        if (wasConfirmed && refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            String dateSuffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
            String refundCode = "REFUND-" + dateSuffix + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            TransDetail refundTrans = new TransDetail(
                    reservation,
                    refundCode,
                    refundAmount,
                    PaymentMethod.VNPAY,
                    PaymentStatus.REFUNDED,
                    "REFUND-FEE-" + fee,
                    "Hoàn trả hủy vé: Tổng tiền " + reservation.getTotalAmount() + " đ, trừ phí phạt hủy " + fee + " đ"
            );
            transDetailRepository.save(refundTrans);
            reservation.getTransDetails().add(refundTrans);
        }

        Reservation saved = reservationRepository.save(reservation);

        try {
            emailService.sendReservationCancelledEmail(saved);
        } catch (Exception ex) {
            log.warn("Lỗi gửi email thông báo hủy vé: {}", ex.getMessage());
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAllWithDetails();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByPhone(String phone) {
        return reservationRepository.findByCustomerPhoneOrderByCreatedAtDesc(phone);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByUserId(Long userId) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Scheduled(fixedRate = 60000)
    public void releaseExpiredHoldReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredList = reservationRepository.findExpiredReservations(ReservationStatus.CREATED, now);
        for (Reservation res : expiredList) {
            res.setStatus(ReservationStatus.EXPIRED);
            for (Ticket t : res.getTickets()) {
                t.setStatus(TicketStatus.AVAILABLE);
                t.setReservation(null);
                t.setPassengerName(null);
                t.setIdCard(null);
                t.setTicketCode(null);
                t.setQrCodeData(null);
                ticketRepository.save(t);
            }
            reservationRepository.save(res);
            log.info("Tự động giải phóng đơn giữ chỗ quá hạn 15 phút: {}", res.getBookingCode());
            try {
                emailService.sendReservationCancelledEmail(res);
            } catch (Exception ex) {
                log.warn("Lỗi gửi email thông báo hủy đơn quá hạn: {}", ex.getMessage());
            }
        }
    }
}
