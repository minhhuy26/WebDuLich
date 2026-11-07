package com.dulich.ticketmanagement.service.impl;

import com.dulich.ticketmanagement.dto.ChangeFlightRequestDto;
import com.dulich.ticketmanagement.dto.FlightBookingRequestDto;
import com.dulich.ticketmanagement.dto.FlightSearchDto;
import com.dulich.ticketmanagement.dto.PassengerInputDto;
import com.dulich.ticketmanagement.entity.Booking;
import com.dulich.ticketmanagement.entity.Flight;
import com.dulich.ticketmanagement.entity.FlightBooking;
import com.dulich.ticketmanagement.entity.FlightPassenger;
import com.dulich.ticketmanagement.entity.enums.FlightBookingStatus;
import com.dulich.ticketmanagement.entity.enums.FlightSeatClass;
import com.dulich.ticketmanagement.entity.enums.PassengerType;
import com.dulich.ticketmanagement.entity.enums.TicketType;
import com.dulich.ticketmanagement.exception.BusinessException;
import com.dulich.ticketmanagement.exception.ResourceNotFoundException;
import com.dulich.ticketmanagement.repository.FlightBookingRepository;
import com.dulich.ticketmanagement.repository.FlightPassengerRepository;
import com.dulich.ticketmanagement.repository.FlightRepository;
import com.dulich.ticketmanagement.service.TicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Xử lý đầy đủ nghiệp vụ VÉ MÁY BAY:
 * - Quy trình: Tìm chuyến -> Chọn hạng ghế -> Nhập hành khách -> Giữ chỗ 15p -> Xác nhận phát hành vé
 * - Thuật toán tính giá vé, hệ số thời điểm đặt, hệ số độ tuổi, phụ phí hành lý quá cước, thuế phí sân bay
 * - Chính sách hoàn hủy vé theo khung thời gian & đổi chuyến
 * - Scheduled Task tự động nhả ghế giữ chỗ hết hạn
 * - Validation nghiêm ngặt danh tính CCCD/Hộ chiếu, tỉ lệ em bé/người lớn, ghế trống
 */
@Service("flightTicketService")
@Transactional
public class FlightTicketService implements TicketService {

    private static final Logger log = LoggerFactory.getLogger(FlightTicketService.class);

    // Hằng số nghiệp vụ
    public static final BigDecimal AIRPORT_TAX_FEE = new BigDecimal("120000"); // 120,000 VNĐ / khách (trừ em bé)
    public static final BigDecimal EXCESS_BAGGAGE_FEE_PER_KG = new BigDecimal("50000"); // 50,000 VNĐ / kg quá cước
    public static final BigDecimal FIXED_CHANGE_FEE = new BigDecimal("350000"); // 350,000 VNĐ phí đổi cố định / khách
    public static final int HOLD_EXPIRATION_MINUTES = 15; // Giữ chỗ 15 phút
    public static final int MAX_PASSENGERS_PER_BOOKING = 9; // Tối đa 9 khách

    // Regex kiểm tra số CCCD 12 số hoặc Hộ chiếu (1 chữ cái hoa + 7-8 số)
    private static final Pattern CCCD_PATTERN = Pattern.compile("^[0-9]{12}$");
    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[A-Z][0-9]{7,8}$");

    private final FlightRepository flightRepository;
    private final FlightBookingRepository flightBookingRepository;
    private final FlightPassengerRepository flightPassengerRepository;
    private final Map<Long, Object> flightLocks = new java.util.concurrent.ConcurrentHashMap<>();

    @Autowired
    public FlightTicketService(FlightRepository flightRepository,
                               FlightBookingRepository flightBookingRepository,
                               FlightPassengerRepository flightPassengerRepository) {
        this.flightRepository = flightRepository;
        this.flightBookingRepository = flightBookingRepository;
        this.flightPassengerRepository = flightPassengerRepository;
    }

    @Override
    public TicketType getTicketType() {
        return TicketType.FLIGHT;
    }

    // =========================================================================
    // 1. TÌM KIẾM CHUYẾN BAY (SEARCH FLIGHTS)
    // =========================================================================
    @Transactional(readOnly = true)
    public List<Flight> searchFlights(FlightSearchDto searchDto) {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = null;

        if (searchDto.getDepartureDate() != null) {
            LocalDate date = searchDto.getDepartureDate();
            startTime = date.atStartOfDay();
            endTime = date.atTime(23, 59, 59);
            if (startTime.isBefore(LocalDateTime.now())) {
                startTime = LocalDateTime.now();
            }
        }

        String dep = (searchDto.getDepartureAirport() != null && !searchDto.getDepartureAirport().trim().isEmpty())
                ? searchDto.getDepartureAirport().trim() : null;
        String arr = (searchDto.getArrivalAirport() != null && !searchDto.getArrivalAirport().trim().isEmpty())
                ? searchDto.getArrivalAirport().trim() : null;

        return flightRepository.searchFlights(dep, arr, startTime, endTime);
    }

    @Transactional(readOnly = true)
    public List<Flight> searchReturnFlights(FlightSearchDto searchDto) {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = null;

        if (searchDto.getReturnDate() != null) {
            LocalDate date = searchDto.getReturnDate();
            startTime = date.atStartOfDay();
            endTime = date.atTime(23, 59, 59);
            if (startTime.isBefore(LocalDateTime.now())) {
                startTime = LocalDateTime.now();
            }
        } else if (searchDto.getDepartureDate() != null) {
            LocalDate date = searchDto.getDepartureDate();
            startTime = date.atStartOfDay();
            if (startTime.isBefore(LocalDateTime.now())) {
                startTime = LocalDateTime.now();
            }
        }

        String dep = (searchDto.getArrivalAirport() != null && !searchDto.getArrivalAirport().trim().isEmpty())
                ? searchDto.getArrivalAirport().trim() : null;
        String arr = (searchDto.getDepartureAirport() != null && !searchDto.getDepartureAirport().trim().isEmpty())
                ? searchDto.getDepartureAirport().trim() : null;

        return flightRepository.searchFlights(dep, arr, startTime, endTime);
    }

    // =========================================================================
    // 2. THUẬT TOÁN TÍNH GIÁ VÉ MÁY BAY
    // =========================================================================

    /**
     * Tính hệ số thời điểm đặt vé theo số ngày còn lại đến giờ bay:
     * - Còn trên 14 ngày: hệ số 1.0
     * - Từ 3 đến 14 ngày: hệ số 1.1
     * - Dưới 3 ngày: hệ số 1.3
     */
    public BigDecimal calculateBookingLeadTimeMultiplier(LocalDateTime bookingTime, LocalDateTime departureTime) {
        long daysUntilDeparture = Duration.between(bookingTime, departureTime).toDays();
        if (daysUntilDeparture > 14) {
            return new BigDecimal("1.0");
        } else if (daysUntilDeparture >= 3) {
            return new BigDecimal("1.1");
        } else {
            return new BigDecimal("1.3");
        }
    }

    /**
     * Tính giá vé cơ bản của từng hành khách:
     * Giá = Giá cơ bản × Hệ số hạng × Hệ số thời điểm đặt × Hệ số độ tuổi
     */
    public BigDecimal calculatePassengerBaseFare(BigDecimal basePrice,
                                                FlightSeatClass seatClass,
                                                LocalDateTime bookingTime,
                                                LocalDateTime departureTime,
                                                PassengerType passengerType) {
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Giá vé máy bay cơ bản phải lớn hơn 0");
        }

        BigDecimal classMultiplier = seatClass.getMultiplier();
        BigDecimal leadTimeMultiplier = calculateBookingLeadTimeMultiplier(bookingTime, departureTime);

        // Giá vé người lớn tiêu chuẩn
        BigDecimal adultFare = basePrice.multiply(classMultiplier).multiply(leadTimeMultiplier);

        // Hệ số độ tuổi: Trẻ em 75%, Em bé 10%, Người lớn 100%
        BigDecimal ageMultiplier = switch (passengerType) {
            case ADULT -> BigDecimal.ONE;
            case CHILD -> new BigDecimal("0.75");
            case INFANT -> new BigDecimal("0.10");
        };

        return adultFare.multiply(ageMultiplier).setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Tính phí hành lý quá cước:
     * Quá cước = max(0, baggageKg - freeBaggageKg) × 50,000 VNĐ
     */
    public BigDecimal calculateExcessBaggageFee(FlightSeatClass seatClass, int baggageKg) {
        int freeKg = seatClass.getFreeBaggageKg();
        if (baggageKg <= freeKg) {
            return BigDecimal.ZERO;
        }
        int excessKg = baggageKg - freeKg;
        return EXCESS_BAGGAGE_FEE_PER_KG.multiply(BigDecimal.valueOf(excessKg));
    }

    /**
     * Tính thuế phí sân bay theo hành khách:
     * Người lớn & Trẻ em: 120,000 VNĐ; Em bé: 0 VNĐ
     */
    public BigDecimal calculateAirportTax(PassengerType passengerType) {
        return passengerType == PassengerType.INFANT ? BigDecimal.ZERO : AIRPORT_TAX_FEE;
    }

    // =========================================================================
    // 3. QUY TRÌNH GIỮ CHỖ (HOLD RESERVATION - HẾT HẠN SAU 15 PHÚT)
    // =========================================================================
    public FlightBooking createHoldBooking(FlightBookingRequestDto request) {
        Long id1 = request.getFlightId();
        Long id2 = (request.isRoundTrip() || request.getReturnFlightId() != null) ? request.getReturnFlightId() : null;
        if (id2 != null && id1 > id2) {
            Long temp = id1;
            id1 = id2;
            id2 = temp;
        }

        Object lock1 = flightLocks.computeIfAbsent(id1, k -> new Object());
        Object lock2 = (id2 != null && !id2.equals(id1)) ? flightLocks.computeIfAbsent(id2, k -> new Object()) : new Object();

        synchronized (lock1) {
            synchronized (lock2) {
                // 1. Kiểm tra tồn tại chuyến bay đi
                Flight flight = flightRepository.findByIdForUpdate(request.getFlightId())
                        .orElseGet(() -> flightRepository.findById(request.getFlightId())
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến bay đi ID: " + request.getFlightId())));

                boolean isRoundTrip = request.isRoundTrip() || request.getReturnFlightId() != null;
                Flight returnFlight = null;
                FlightSeatClass returnSeatClass = request.getReturnSeatClass() != null ? request.getReturnSeatClass() : request.getSeatClass();

                if (isRoundTrip) {
                    if (request.getReturnFlightId() == null) {
                        throw new BusinessException("Vé khứ hồi bắt buộc phải chọn chuyến bay về!");
                    }
                    if (request.getReturnFlightId().equals(request.getFlightId())) {
                        throw new BusinessException("Chuyến bay về không thể trùng với chuyến bay đi!");
                    }
                    returnFlight = flightRepository.findByIdForUpdate(request.getReturnFlightId())
                            .orElseGet(() -> flightRepository.findById(request.getReturnFlightId())
                                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến bay về ID: " + request.getReturnFlightId())));
                }

                LocalDateTime now = LocalDateTime.now();

                // 2. Validate nghiệp vụ chuyến bay và danh sách hành khách
                validateFlightBookingRequest(flight, request, now);

                if (isRoundTrip && returnFlight != null) {
                    if (returnFlight.getDepartureTime().isBefore(now.plusHours(2))) {
                        throw new BusinessException("Chuyến bay về phải khởi hành sau thời điểm hiện tại ít nhất 2 tiếng!");
                    }
                    if (returnFlight.getDepartureTime().isBefore(flight.getDepartureTime().plusHours(2))) {
                        throw new BusinessException("Chuyến bay về phải khởi hành sau chuyến bay đi ít nhất 2 tiếng!");
                    }
                }

                // 3. Kiểm tra và trừ số ghế trống trên chuyến bay đi
                int seatOccupyingCount = 0;
                for (PassengerInputDto p : request.getPassengers()) {
                    if (p.getPassengerType() != PassengerType.INFANT) {
                        seatOccupyingCount++;
                    }
                }
                if (flight.getAvailableSeats(request.getSeatClass()) < seatOccupyingCount) {
                    throw new BusinessException("Chuyến bay đi - Hạng ghế " + request.getSeatClass().getDisplayName() +
                            " chỉ còn " + flight.getAvailableSeats(request.getSeatClass()) +
                            " chỗ trống, không đủ cho " + seatOccupyingCount + " hành khách!");
                }

                // Kiểm tra và trừ số ghế trống trên chuyến bay về (nếu có)
                if (isRoundTrip && returnFlight != null) {
                    if (returnFlight.getAvailableSeats(returnSeatClass) < seatOccupyingCount) {
                        throw new BusinessException("Chuyến bay về - Hạng ghế " + returnSeatClass.getDisplayName() +
                                " chỉ còn " + returnFlight.getAvailableSeats(returnSeatClass) +
                                " chỗ trống, không đủ cho " + seatOccupyingCount + " hành khách!");
                    }

                    // Kiểm tra ghế chọn chiều về không bị trùng
                    Set<String> occupiedReturnSeats = getOccupiedSeats(returnFlight.getId());
                    Set<String> chosenReturnSeats = new HashSet<>();
                    for (PassengerInputDto p : request.getPassengers()) {
                        if (p.getPassengerType() != PassengerType.INFANT && p.getReturnSeatNumber() != null && !p.getReturnSeatNumber().trim().isEmpty()) {
                            String s = p.getReturnSeatNumber().trim().toUpperCase();
                            if (!chosenReturnSeats.add(s)) {
                                throw new BusinessException("Số ghế chiều về " + s + " bị chọn trùng giữa các hành khách!");
                            }
                            if (occupiedReturnSeats.contains(s)) {
                                throw new BusinessException("Số ghế chiều về " + s + " trên chuyến bay về đã có người đặt. Vui lòng chọn ghế khác!");
                            }
                        }
                    }
                }

                flight.reduceAvailableSeats(request.getSeatClass(), seatOccupyingCount);
                flightRepository.save(flight);

                if (isRoundTrip && returnFlight != null) {
                    returnFlight.reduceAvailableSeats(returnSeatClass, seatOccupyingCount);
                    flightRepository.save(returnFlight);
                }

                // 4. Tạo entity FlightBooking
                FlightBooking booking = new FlightBooking();
                booking.setBookingCode("VN-AIR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
                booking.setFlight(flight);
                if (isRoundTrip) {
                    booking.setReturnFlight(returnFlight);
                    booking.setIsRoundTrip(true);
                    booking.setReturnSeatClass(returnSeatClass);
                } else {
                    booking.setIsRoundTrip(false);
                }
                booking.setCustomerName(request.getCustomerName().trim());
                booking.setCustomerPhone(request.getCustomerPhone().trim());
                booking.setCustomerEmail(request.getCustomerEmail().trim());
                booking.setSeatClass(request.getSeatClass());
                booking.setPassengerCount(request.getPassengers().size());
                booking.setStatus(FlightBookingStatus.HELD);
                booking.setBookingTime(now);
                booking.setHoldExpiresAt(now.plusMinutes(HOLD_EXPIRATION_MINUTES));

                BigDecimal totalFare = BigDecimal.ZERO;
                BigDecimal totalTaxFee = BigDecimal.ZERO;
                BigDecimal totalBaggageFee = BigDecimal.ZERO;

                // 5. Tạo danh sách hành khách và tính toán chi phí chi tiết
                for (PassengerInputDto pDto : request.getPassengers()) {
                    FlightPassenger passenger = new FlightPassenger();
                    passenger.setFullName(pDto.getFullName().trim());
                    passenger.setIdCard(pDto.getIdCard() != null ? pDto.getIdCard().trim() : null);
                    passenger.setPassengerType(pDto.getPassengerType());
                    passenger.setBaggageKg(pDto.getBaggageKg() != null ? pDto.getBaggageKg() : 0);

                    // Em bé không chiếm ghế
                    if (pDto.getPassengerType() == PassengerType.INFANT) {
                        passenger.setSeatNumber(null);
                        passenger.setReturnSeatNumber(null);
                    } else {
                        passenger.setSeatNumber(pDto.getSeatNumber() != null && !pDto.getSeatNumber().trim().isEmpty() ? pDto.getSeatNumber().trim().toUpperCase() : null);
                        passenger.setReturnSeatNumber(pDto.getReturnSeatNumber() != null && !pDto.getReturnSeatNumber().trim().isEmpty() ? pDto.getReturnSeatNumber().trim().toUpperCase() : null);
                    }

                    // Chiều đi
                    BigDecimal outboundBaseFare = calculatePassengerBaseFare(flight.getBasePrice(), request.getSeatClass(), now, flight.getDepartureTime(), pDto.getPassengerType());
                    BigDecimal outboundTax = calculateAirportTax(pDto.getPassengerType());
                    BigDecimal outboundBaggage = calculateExcessBaggageFee(request.getSeatClass(), passenger.getBaggageKg());

                    BigDecimal pBaseFare = outboundBaseFare;
                    BigDecimal pTax = outboundTax;
                    BigDecimal pBaggage = outboundBaggage;

                    // Chiều về (nếu có)
                    if (isRoundTrip && returnFlight != null) {
                        BigDecimal returnBaseFare = calculatePassengerBaseFare(returnFlight.getBasePrice(), returnSeatClass, now, returnFlight.getDepartureTime(), pDto.getPassengerType());
                        BigDecimal returnTax = calculateAirportTax(pDto.getPassengerType());
                        BigDecimal returnBaggage = calculateExcessBaggageFee(returnSeatClass, passenger.getBaggageKg());

                        pBaseFare = pBaseFare.add(returnBaseFare);
                        pTax = pTax.add(returnTax);
                        pBaggage = pBaggage.add(returnBaggage);
                    }

                    BigDecimal finalPrice = pBaseFare.add(pTax).add(pBaggage);

                    passenger.setBaseFare(pBaseFare);
                    passenger.setAirportTax(pTax);
                    passenger.setExcessBaggageFee(pBaggage);
                    passenger.setFinalPrice(finalPrice);

                    booking.addPassenger(passenger);

                    totalFare = totalFare.add(pBaseFare);
                    totalTaxFee = totalTaxFee.add(pTax);
                    totalBaggageFee = totalBaggageFee.add(pBaggage);
                }

                booking.setTotalFare(totalFare);
                booking.setTotalTaxFee(totalTaxFee);
                booking.setTotalBaggageFee(totalBaggageFee);
                booking.setTotalAmount(totalFare.add(totalTaxFee).add(totalBaggageFee));

                log.info("Tạo đơn giữ chỗ thành công: {} cho {} hành khách (Khứ hồi: {}). Hết hạn lúc {}",
                        booking.getBookingCode(), booking.getPassengerCount(), booking.isRoundTrip(), booking.getHoldExpiresAt());

                return flightBookingRepository.save(booking);
            }
        }
    }

    // =========================================================================
    // 4. XÁC NHẬN THANH TOÁN & PHÁT HÀNH VÉ (CONFIRM & ISSUE TICKET)
    // =========================================================================
    public FlightBooking confirmAndIssueTicket(String bookingCode) {
        FlightBooking booking = flightBookingRepository.findByBookingCodeWithDetails(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt vé: " + bookingCode));

        LocalDateTime now = LocalDateTime.now();

        if (booking.getStatus() == FlightBookingStatus.ISSUED) {
            return booking;
        }

        if (booking.getStatus() != FlightBookingStatus.HELD && booking.getStatus() != FlightBookingStatus.CONFIRMED) {
            throw new BusinessException("Đơn đặt chỗ ở trạng thái " + booking.getStatus().getDisplayName() +
                    ", không thể xác nhận phát hành vé!");
        }

        if (booking.getStatus() == FlightBookingStatus.HELD && booking.getHoldExpiresAt() != null && now.isAfter(booking.getHoldExpiresAt())) {
            booking.setStatus(FlightBookingStatus.EXPIRED);
            int seatOccupyingCount = 0;
            for (FlightPassenger p : booking.getPassengers()) {
                if (p.getPassengerType() != PassengerType.INFANT) {
                    seatOccupyingCount++;
                }
            }
            booking.getFlight().restoreAvailableSeats(booking.getSeatClass(), seatOccupyingCount);
            flightRepository.save(booking.getFlight());
            if (booking.isRoundTrip() && booking.getReturnFlight() != null) {
                FlightSeatClass rsc = booking.getReturnSeatClass() != null ? booking.getReturnSeatClass() : booking.getSeatClass();
                booking.getReturnFlight().restoreAvailableSeats(rsc, seatOccupyingCount);
                flightRepository.save(booking.getReturnFlight());
            }
            flightBookingRepository.save(booking);
            throw new BusinessException("Thời gian giữ chỗ 15 phút đã hết hạn! Vui lòng thực hiện đặt lại.");
        }

        booking.setStatus(FlightBookingStatus.ISSUED);
        booking.setConfirmedTime(now);

        // Sinh mã vé điện tử (E-ticket) cho từng hành khách (cả chiều đi và chiều về nếu là khứ hồi)
        long counter = 1;
        for (FlightPassenger p : booking.getPassengers()) {
            p.setTicketCode("ETK-" + booking.getFlight().getFlightNumber() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase() + "-" + counter);
            if (booking.isRoundTrip() && booking.getReturnFlight() != null) {
                p.setReturnTicketCode("ETK-" + booking.getReturnFlight().getFlightNumber() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase() + "-" + counter);
            }
            counter++;
        }

        log.info("Phát hành vé điện tử thành công cho đơn {}", booking.getBookingCode());
        return flightBookingRepository.save(booking);
    }

    // =========================================================================
    // 5. CHÍNH SÁCH HỦY VÉ VÀ HOÀN TIỀN
    // =========================================================================

    /**
     * Tính phí hủy vé:
     * - Hủy trước > 72 giờ: phí 10%
     * - Từ 24 đến 72 giờ: phí 30%
     * - Dưới 24 giờ: phí 70%
     * - Sau giờ bay: phí 100% (không hoàn tiền)
     */
    public BigDecimal calculateCancellationFee(BigDecimal totalAmount, LocalDateTime cancelTime, LocalDateTime departureTime) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (cancelTime.isAfter(departureTime)) {
            return totalAmount; // 100% sau giờ bay
        }

        long hoursBeforeDeparture = Duration.between(cancelTime, departureTime).toHours();

        BigDecimal feePercent;
        if (hoursBeforeDeparture >= 72) {
            feePercent = new BigDecimal("0.10"); // 10%
        } else if (hoursBeforeDeparture >= 24) {
            feePercent = new BigDecimal("0.30"); // 30%
        } else {
            feePercent = new BigDecimal("0.70"); // 70% (< 24h)
        }

        return totalAmount.multiply(feePercent).setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Số tiền hoàn lại = Tổng tiền - Phí hủy
     */
    public BigDecimal calculateRefundAmount(BigDecimal totalAmount, LocalDateTime cancelTime, LocalDateTime departureTime) {
        BigDecimal fee = calculateCancellationFee(totalAmount, cancelTime, departureTime);
        return totalAmount.subtract(fee).max(BigDecimal.ZERO);
    }

    public FlightBooking cancelBooking(String bookingCode, String reason) {
        FlightBooking booking = flightBookingRepository.findByBookingCodeWithDetails(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt vé: " + bookingCode));

        if (booking.getStatus() == FlightBookingStatus.EXPIRED) {
            throw new BusinessException("Đơn giữ chỗ đã hết hạn, không thể hủy!");
        }

        if (booking.getStatus() == FlightBookingStatus.CANCELLED) {
            throw new BusinessException("Đơn đặt vé này đã được hủy trước đó!");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departureTime = booking.getFlight().getDepartureTime();

        if (now.isAfter(departureTime) && (booking.getStatus() == FlightBookingStatus.ISSUED 
                || booking.getStatus() == FlightBookingStatus.CONFIRMED 
                || booking.getStatus() == FlightBookingStatus.CHANGED)) {
            throw new BusinessException("Chuyến bay đã cất cánh, theo chính sách hãng hàng không không thể hoàn hủy vé!");
        }

        BigDecimal cancellationFee = BigDecimal.ZERO;
        BigDecimal refundAmount = BigDecimal.ZERO;

        // Chỉ tính phí hủy và tiền hoàn cho các đơn đã thanh toán
        if (booking.getStatus() == FlightBookingStatus.ISSUED 
                || booking.getStatus() == FlightBookingStatus.CONFIRMED 
                || booking.getStatus() == FlightBookingStatus.CHANGED) {
            cancellationFee = calculateCancellationFee(booking.getTotalAmount(), now, departureTime);
            refundAmount = calculateRefundAmount(booking.getTotalAmount(), now, departureTime);
        }

        booking.setCancellationFee(cancellationFee);
        booking.setRefundAmount(refundAmount);
        booking.setStatus(FlightBookingStatus.CANCELLED);
        booking.setCancelledTime(now);
        booking.setCancelReason(reason != null ? reason : "Khách hàng yêu cầu hủy vé");

        // Nhả lại ghế cho chuyến bay
        int seatOccupyingCount = 0;
        for (FlightPassenger p : booking.getPassengers()) {
            if (p.getPassengerType() != PassengerType.INFANT) {
                seatOccupyingCount++;
            }
        }
        booking.getFlight().restoreAvailableSeats(booking.getSeatClass(), seatOccupyingCount);
        flightRepository.save(booking.getFlight());

        if (booking.isRoundTrip() && booking.getReturnFlight() != null) {
            FlightSeatClass rsc = booking.getReturnSeatClass() != null ? booking.getReturnSeatClass() : booking.getSeatClass();
            booking.getReturnFlight().restoreAvailableSeats(rsc, seatOccupyingCount);
            flightRepository.save(booking.getReturnFlight());
        }

        log.info("Hủy vé thành công cho đơn {}. Phí hủy: {} VNĐ, Hoàn lại: {} VNĐ",
                bookingCode, cancellationFee, refundAmount);

        return flightBookingRepository.save(booking);
    }

    // =========================================================================
    // 6. CHÍNH SÁCH ĐỔI CHUYẾN BAY (CHANGE FLIGHT)
    // =========================================================================
    public FlightBooking changeFlight(String bookingCode, ChangeFlightRequestDto request) {
        FlightBooking oldBooking = flightBookingRepository.findByBookingCodeWithDetails(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn vé: " + bookingCode));

        if (oldBooking.getStatus() != FlightBookingStatus.ISSUED 
                && oldBooking.getStatus() != FlightBookingStatus.CONFIRMED 
                && oldBooking.getStatus() != FlightBookingStatus.CHANGED) {
            throw new BusinessException("Chỉ đơn vé đã phát hành hoặc đã xác nhận mới có thể đổi chuyến!");
        }

        Flight newFlight = flightRepository.findById(request.getNewFlightId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến bay mới!"));

        LocalDateTime now = LocalDateTime.now();
        if (newFlight.getDepartureTime().isBefore(now.plusHours(2))) {
            throw new BusinessException("Chuyến bay mới phải khởi hành sau thời điểm hiện tại ít nhất 2 tiếng!");
        }

        // Đếm số ghế chiếm chỗ
        int seatOccupyingCount = 0;
        for (FlightPassenger p : oldBooking.getPassengers()) {
            if (p.getPassengerType() != PassengerType.INFANT) {
                seatOccupyingCount++;
            }
        }

        if (newFlight.getAvailableSeats(request.getNewSeatClass()) < seatOccupyingCount) {
            throw new BusinessException("Chuyến bay mới không còn đủ " + seatOccupyingCount + " chỗ trống hạng " +
                    request.getNewSeatClass().getDisplayName());
        }

        // Tính giá vé của chuyến bay mới và tính lại cước hành lý theo hạng ghế mới
        BigDecimal newTotalFare = BigDecimal.ZERO;
        BigDecimal newTotalBaggageFee = BigDecimal.ZERO;
        for (FlightPassenger p : oldBooking.getPassengers()) {
            BigDecimal fare = calculatePassengerBaseFare(newFlight.getBasePrice(), request.getNewSeatClass(), now, newFlight.getDepartureTime(), p.getPassengerType());
            p.setBaseFare(fare);

            BigDecimal baggageFee = calculateExcessBaggageFee(request.getNewSeatClass(), p.getBaggageKg());
            p.setExcessBaggageFee(baggageFee);

            BigDecimal airportTax = p.getAirportTax() != null ? p.getAirportTax() : BigDecimal.ZERO;
            p.setFinalPrice(fare.add(airportTax).add(baggageFee));

            newTotalFare = newTotalFare.add(fare);
            newTotalBaggageFee = newTotalBaggageFee.add(baggageFee);
        }

        // Chênh lệch giá vé: max(0, giá vé mới - giá vé cũ)
        BigDecimal fareDifference = newTotalFare.subtract(oldBooking.getTotalFare()).max(BigDecimal.ZERO);

        // Phí đổi cố định theo số lượng khách chiếm ghế
        BigDecimal totalFixedChangeFee = FIXED_CHANGE_FEE.multiply(BigDecimal.valueOf(Math.max(1, seatOccupyingCount)));
        BigDecimal totalChangeCost = totalFixedChangeFee.add(fareDifference);

        // Hoàn lại ghế chuyến bay cũ
        oldBooking.getFlight().restoreAvailableSeats(oldBooking.getSeatClass(), seatOccupyingCount);
        flightRepository.save(oldBooking.getFlight());

        // Trừ ghế chuyến bay mới
        newFlight.reduceAvailableSeats(request.getNewSeatClass(), seatOccupyingCount);
        flightRepository.save(newFlight);

        // Cập nhật đơn đặt vé sang chuyến mới (bao gồm phí đổi cố định vào tổng tiền đơn)
        oldBooking.setFlight(newFlight);
        oldBooking.setSeatClass(request.getNewSeatClass());
        oldBooking.setTotalFare(newTotalFare);
        oldBooking.setTotalBaggageFee(newTotalBaggageFee);
        oldBooking.setTotalAmount(newTotalFare.add(oldBooking.getTotalTaxFee()).add(newTotalBaggageFee).add(totalFixedChangeFee));
        oldBooking.setStatus(FlightBookingStatus.CHANGED);

        // Cập nhật số ghế mới nếu có
        if (request.getNewSeatNumbers() != null && !request.getNewSeatNumbers().isEmpty()) {
            int seatIdx = 0;
            for (FlightPassenger p : oldBooking.getPassengers()) {
                if (p.getPassengerType() != PassengerType.INFANT && seatIdx < request.getNewSeatNumbers().size()) {
                    p.setSeatNumber(request.getNewSeatNumbers().get(seatIdx++));
                }
            }
        }

        log.info("Đổi chuyến thành công cho mã {}. Phí đổi: {} VNĐ (Phí cố định: {} VNĐ, Chênh lệch: {} VNĐ)",
                bookingCode, totalChangeCost, totalFixedChangeFee, fareDifference);

        return flightBookingRepository.save(oldBooking);
    }

    // =========================================================================
    // 7. SCHEDULED TASK: TỰ ĐỘNG NHẢ GHẾ GIỮ CHỖ HẾT HẠN (15 PHÚT)
    // =========================================================================
    public int releaseExpiredHoldBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<FlightBooking> expiredBookings = flightBookingRepository.findExpiredBookings(FlightBookingStatus.HELD, now);

        int count = 0;
        for (FlightBooking booking : expiredBookings) {
            try {
                log.info("Scheduled Task: Đơn giữ chỗ {} hết hạn lúc {}, thực hiện nhả ghế!",
                        booking.getBookingCode(), booking.getHoldExpiresAt());

                booking.setStatus(FlightBookingStatus.EXPIRED);

                int seatOccupyingCount = 0;
                if (booking.getPassengers() != null) {
                    for (FlightPassenger p : booking.getPassengers()) {
                        if (p.getPassengerType() != PassengerType.INFANT) {
                            seatOccupyingCount++;
                        }
                    }
                }

                if (booking.getFlight() != null) {
                    booking.getFlight().restoreAvailableSeats(booking.getSeatClass(), seatOccupyingCount);
                    flightRepository.save(booking.getFlight());
                }
                if (booking.isRoundTrip() && booking.getReturnFlight() != null) {
                    FlightSeatClass rsc = booking.getReturnSeatClass() != null ? booking.getReturnSeatClass() : booking.getSeatClass();
                    booking.getReturnFlight().restoreAvailableSeats(rsc, seatOccupyingCount);
                    flightRepository.save(booking.getReturnFlight());
                }
                flightBookingRepository.save(booking);
                count++;
            } catch (Exception ex) {
                log.error("Scheduled Task: Lỗi khi nhả ghế cho đơn giữ chỗ {}: {}", 
                        booking != null ? booking.getBookingCode() : "null", ex.getMessage());
            }
        }
        return count;
    }

    // =========================================================================
    // 8. CÁC HÀM VALIDATION NGHIỆP VỤ
    // =========================================================================
    public void validateFlightBookingRequest(Flight flight, FlightBookingRequestDto request, LocalDateTime now) {
        // 1. Ngày bay không ở quá khứ, đặt muộn nhất 2 giờ trước giờ bay
        if (flight.getDepartureTime().isBefore(now)) {
            throw new BusinessException("Chuyến bay đã khởi hành, không thể đặt vé!");
        }
        if (flight.getDepartureTime().isBefore(now.plusHours(2))) {
            throw new BusinessException("Vé máy bay phải được đặt trước giờ khởi hành tối thiểu 2 tiếng!");
        }

        // 2. Tối đa 9 hành khách mỗi lần đặt
        List<PassengerInputDto> passengers = request.getPassengers();
        if (passengers == null || passengers.isEmpty()) {
            throw new BusinessException("Danh sách hành khách không được để trống!");
        }
        if (passengers.size() > MAX_PASSENGERS_PER_BOOKING) {
            throw new BusinessException("Mỗi lần đặt vé chỉ được tối đa 9 hành khách (hiện có: " + passengers.size() + ")!");
        }

        // 3. Kiểm tra số lượng người lớn và em bé: Mỗi em bé phải đi kèm 1 người lớn
        int adultCount = 0;
        int infantCount = 0;
        Set<String> chosenSeats = new HashSet<>();

        Set<String> occupiedSeats = flightPassengerRepository.findOccupiedSeatNumbers(flight.getId());

        for (PassengerInputDto p : passengers) {
            // Họ tên không rỗng
            if (p.getFullName() == null || p.getFullName().trim().isEmpty()) {
                throw new BusinessException("Họ tên hành khách không được để trống!");
            }

            if (p.getPassengerType() == PassengerType.ADULT) {
                adultCount++;
                // Bắt buộc CCCD 12 số hoặc Hộ chiếu hợp lệ
                validateIdentityDocument(p.getFullName(), p.getIdCard());
            } else if (p.getPassengerType() == PassengerType.INFANT) {
                infantCount++;
            }

            // Kiểm tra ghế ngồi (Em bé không chiếm ghế)
            if (p.getPassengerType() != PassengerType.INFANT && p.getSeatNumber() != null && !p.getSeatNumber().trim().isEmpty()) {
                String seat = p.getSeatNumber().trim().toUpperCase();

                // Kiểm tra trùng ghế trong cùng đơn
                if (!chosenSeats.add(seat)) {
                    throw new BusinessException("Ghế " + seat + " bị chọn trùng lặp giữa các hành khách trong cùng đơn đặt!");
                }

                // Kiểm tra ghế đã có người đặt chưa
                if (occupiedSeats.contains(seat)) {
                    throw new BusinessException("Ghế " + seat + " đã được hành khách khác đặt trên chuyến bay này. Vui lòng chọn ghế khác!");
                }
            }
        }

        if (adultCount == 0 && !passengers.isEmpty()) {
            throw new BusinessException("Đơn đặt vé phải có ít nhất 1 hành khách người lớn!");
        }

        if (infantCount > adultCount) {
            throw new BusinessException("Quy định an toàn bay: Mỗi em bé (dưới 2 tuổi) phải có 1 người lớn đi kèm bảo bọc! (Hiện có: " +
                    infantCount + " em bé, nhưng chỉ có " + adultCount + " người lớn).");
        }
    }

    /**
     * Kiểm tra giấy tờ tùy thân: CCCD 12 chữ số hoặc số Hộ chiếu hợp lệ
     */
    public void validateIdentityDocument(String passengerName, String idCard) {
        if (idCard == null || idCard.trim().isEmpty()) {
            throw new BusinessException("Hành khách người lớn (" + passengerName + ") bắt buộc phải nhập số CCCD hoặc Hộ chiếu!");
        }
        String cleanId = idCard.trim().toUpperCase();
        boolean isCccd = CCCD_PATTERN.matcher(cleanId).matches();
        boolean isPassport = PASSPORT_PATTERN.matcher(cleanId).matches();

        if (!isCccd && !isPassport) {
            throw new BusinessException("Số giấy tờ tùy thân của hành khách " + passengerName +
                    " (" + cleanId + ") không hợp lệ! Vui lòng nhập đúng CCCD (12 chữ số) hoặc số Hộ chiếu (1 chữ cái theo sau bởi 7-8 số).");
        }
    }

    @Transactional(readOnly = true)
    public Set<String> getOccupiedSeats(Long flightId) {
        if (flightId == null) {
            return Collections.emptySet();
        }
        Set<String> seats = new HashSet<>(flightPassengerRepository.findOccupiedSeatNumbers(flightId));
        seats.addAll(flightPassengerRepository.findOccupiedReturnSeatNumbers(flightId));
        return seats;
    }

    // =========================================================================
    // CÁC HÀM IMPLEMENT TỪ TICKETSERVICE CHUNG (TƯƠNG THÍCH ĐƠN TỔNG THỂ)
    // =========================================================================
    @Override
    public BigDecimal calculateTotalFare(BigDecimal basePrice, String seatClass, int passengerCount, BigDecimal extraServices) {
        FlightSeatClass sc = FlightSeatClass.valueOf(seatClass != null ? seatClass.toUpperCase() : "ECONOMY");
        BigDecimal singlePassengerBase = calculatePassengerBaseFare(basePrice, sc, LocalDateTime.now(), LocalDateTime.now().plusDays(10), PassengerType.ADULT);
        BigDecimal total = singlePassengerBase.add(AIRPORT_TAX_FEE).multiply(BigDecimal.valueOf(passengerCount));
        if (extraServices != null) {
            total = total.add(extraServices);
        }
        return total;
    }

    @Override
    public void validateBooking(Booking booking) {
        if (booking.getDepartureDate() == null) {
            throw new BusinessException("Vé máy bay bắt buộc phải có ngày giờ bay!");
        }
        if (booking.getDepartureDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BusinessException("Vé máy bay phải được đặt trước giờ khởi hành tối thiểu 2 tiếng!");
        }
    }

    @Override
    public void processBooking(Booking booking) {
        validateBooking(booking);
        booking.setStatus(com.dulich.ticketmanagement.entity.enums.BookingStatus.CONFIRMED);
    }

    @Override
    public void cancelBooking(Booking booking, String reason) {
        LocalDateTime departure = booking.getDepartureDate();
        if (departure != null && departure.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Chuyến bay đã cất cánh, không thể hủy vé!");
        }
        booking.setStatus(com.dulich.ticketmanagement.entity.enums.BookingStatus.CANCELLED);
    }
}
