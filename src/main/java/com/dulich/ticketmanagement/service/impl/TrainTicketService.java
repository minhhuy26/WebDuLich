package com.dulich.ticketmanagement.service.impl;

import com.dulich.ticketmanagement.dto.SeatAvailabilityDto;
import com.dulich.ticketmanagement.dto.TrainBookingRequestDto;
import com.dulich.ticketmanagement.dto.TrainPassengerInputDto;
import com.dulich.ticketmanagement.dto.TrainSearchDto;
import com.dulich.ticketmanagement.entity.*;
import com.dulich.ticketmanagement.entity.enums.PassengerPriority;
import com.dulich.ticketmanagement.entity.enums.TicketType;
import com.dulich.ticketmanagement.entity.enums.TrainBookingStatus;
import com.dulich.ticketmanagement.exception.BusinessException;
import com.dulich.ticketmanagement.exception.ResourceNotFoundException;
import com.dulich.ticketmanagement.repository.*;
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
import java.time.LocalTime;
import java.util.*;

/**
 * Xử lý nghiệp vụ ĐẶC THÙ CHO VÉ TÀU HỎA:
 * - Thuật toán kiểm tra chỗ trống THEO CHẶNG (Segment Overlap Check): một ghế bán được cho nhiều khách nếu các chặng không chồng lấn.
 * - Thuật toán tính giá: Khoảng cách km × Đơn giá loại toa (Ghế mềm 700đ/km, Giường 6 chỗ 1000đ/km, Giường 4 VIP 1300đ/km).
 * - Chính sách giảm giá đối tượng ưu tiên: Trẻ em 6-10 tuổi (giảm 25%), Sinh viên (giảm 10%), Người cao tuổi (giảm 15%).
 * - Chính sách hoàn hủy vé tàu: trước >= 24h phí 10%, 4 - 24h phí 20%, < 4h phí 30%, sau giờ chạy không hoàn tiền.
 * - Quy trình: Chọn ga/ngày -> Chọn tàu -> Chọn toa & vị trí trên sơ đồ -> Nhập thông tin & đối tượng ưu tiên -> Giữ chỗ 10 phút -> Phát hành vé / Hủy vé.
 */
@Service("trainTicketService")
@Transactional
public class TrainTicketService implements TicketService {

    private static final Logger log = LoggerFactory.getLogger(TrainTicketService.class);

    public static final int HOLD_EXPIRATION_MINUTES = 10; // Giữ chỗ 10 phút
    public static final int MAX_TICKETS_PER_BOOKING = 10; // Tối đa 10 vé mỗi lần đặt
    public static final int MAX_BOOKING_DAYS_IN_ADVANCE = 30; // Chỉ đặt trong 30 ngày tới

    private final TrainRepository trainRepository;
    private final TrainStationRepository trainStationRepository;
    private final TrainCoachRepository trainCoachRepository;
    private final TrainSeatRepository trainSeatRepository;
    private final TrainBookingRepository trainBookingRepository;
    private final TrainTicketRepository trainTicketRepository;
    private final Map<Long, Object> trainLocks = new java.util.concurrent.ConcurrentHashMap<>();

    @Autowired
    public TrainTicketService(TrainRepository trainRepository,
                              TrainStationRepository trainStationRepository,
                              TrainCoachRepository trainCoachRepository,
                              TrainSeatRepository trainSeatRepository,
                              TrainBookingRepository trainBookingRepository,
                              TrainTicketRepository trainTicketRepository) {
        this.trainRepository = trainRepository;
        this.trainStationRepository = trainStationRepository;
        this.trainCoachRepository = trainCoachRepository;
        this.trainSeatRepository = trainSeatRepository;
        this.trainBookingRepository = trainBookingRepository;
        this.trainTicketRepository = trainTicketRepository;
    }

    @Override
    public TicketType getTicketType() {
        return TicketType.TRAIN;
    }

    // =========================================================================
    // 1. THUẬT TOÁN KIỂM TRA CHỒNG LẤN CHẶNG (SEGMENT OVERLAP CHECK)
    // =========================================================================

    /**
     * THUẬT TOÁN KIỂM TRA CHỒNG LẤN CHẶNG (Segment Overlap Check):
     * Trong đường sắt, hành trình từ Ga A (chỉ số depIdx) đến Ga B (chỉ số arrIdx)
     * tương ứng với khoảng ga nửa mở [depIdx, arrIdx).
     * Điểm đến arrIdx là điểm khách xuống tàu, vì vậy hành khách tiếp theo hoàn toàn có thể
     * lên tàu tại chính ga arrIdx để đi tiếp mà KHÔNG bị xung đột ghế.
     *
     * Về mặt toán học, hai khoảng nửa mở [depIdx1, arrIdx1) và [depIdx2, arrIdx2)
     * CHỒNG LẤN NHAU khi và chỉ khi:
     *     max(depIdx1, depIdx2) < min(arrIdx1, arrIdx2)
     *
     * Ngược lại, hai chặng KHÔNG CHỒNG LẤN NHAU (nghĩa là dùng chung cùng một ghế được) khi:
     *     arrIdx1 <= depIdx2  HOẶC  arrIdx2 <= depIdx1
     *
     * Ví dụ thực tế:
     * - Khách 1 đi từ Ga Hà Nội (index 0) đến Ga Vinh (index 4) -> khoảng [0, 4)
     * - Khách 2 đi từ Ga Vinh (index 4) đến Ga Đà Nẵng (index 7) -> khoảng [4, 7)
     * -> arrIdx1 (4) <= depIdx2 (4) -> Không chồng lấn -> Cùng ngồi chung một ghế được!
     *
     * @param depIdx1 Ga đi của chặng 1
     * @param arrIdx1 Ga đến của chặng 1
     * @param depIdx2 Ga đi của chặng 2
     * @param arrIdx2 Ga đến của chặng 2
     * @return true nếu 2 chặng bị chồng lấn (xung đột chỗ); false nếu không chồng lấn (hợp lệ)
     */
    public boolean isSegmentOverlapping(int depIdx1, int arrIdx1, int depIdx2, int arrIdx2) {
        int start1 = Math.min(depIdx1, arrIdx1);
        int end1 = Math.max(depIdx1, arrIdx1);
        int start2 = Math.min(depIdx2, arrIdx2);
        int end2 = Math.max(depIdx2, arrIdx2);
        return Math.max(start1, start2) < Math.min(end1, end2);
    }

    // =========================================================================
    // 2. THUẬT TOÁN TÍNH GIÁ VÉ TÀU HỎA & GIẢM GIÁ ƯU TIÊN
    // =========================================================================

    /**
     * Thuật toán tính giá vé tàu gốc:
     * Giá vé chuẩn = Khoảng cách km × Đơn giá theo loại toa
     */
    public BigDecimal calculateOriginalFare(int distanceKm, BigDecimal pricePerKm) {
        if (distanceKm <= 0) {
            throw new BusinessException("Khoảng cách di chuyển bằng tàu hỏa phải lớn hơn 0 km!");
        }
        if (pricePerKm == null || pricePerKm.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Đơn giá loại toa phải lớn hơn 0!");
        }
        return pricePerKm.multiply(BigDecimal.valueOf(distanceKm)).setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Tính mức tiền giảm giá theo đối tượng ưu tiên:
     * - Trẻ em 6 - 10 tuổi: giảm 25% (0.25)
     * - Sinh viên: giảm 10% (0.10)
     * - Người cao tuổi (>= 60 tuổi): giảm 15% (0.15)
     * - Người lớn chuẩn: giảm 0% (0.00)
     */
    public BigDecimal calculateDiscountAmount(BigDecimal originalFare, PassengerPriority priority) {
        if (originalFare == null || priority == null) {
            return BigDecimal.ZERO;
        }
        return originalFare.multiply(priority.getDiscountRate()).setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Giá vé cuối cùng = Giá vé chuẩn - Tiền giảm giá
     */
    public BigDecimal calculateFinalFare(BigDecimal originalFare, PassengerPriority priority) {
        BigDecimal discount = calculateDiscountAmount(originalFare, priority);
        return originalFare.subtract(discount);
    }

    // =========================================================================
    // 3. CHÍNH SÁCH PHÍ HỦY VÉ TÀU HỎA
    // =========================================================================

    /**
     * Chính sách phí hủy vé tàu hỏa:
     * - Hủy trước giờ tàu chạy >= 24 giờ: phí 10%
     * - Từ 4 đến 24 giờ: phí 20%
     * - Dưới 4 giờ: phí 30%
     * - Sau giờ tàu chạy: không hoàn tiền (phí 100%)
     */
    public BigDecimal calculateCancellationFee(BigDecimal totalAmount, LocalDateTime cancelTime, LocalDateTime departureDatetime) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (cancelTime.isAfter(departureDatetime)) {
            return totalAmount; // 100% sau giờ tàu chạy
        }

        long hoursBeforeDeparture = Duration.between(cancelTime, departureDatetime).toHours();

        BigDecimal feePercent;
        if (hoursBeforeDeparture >= 24) {
            feePercent = new BigDecimal("0.10"); // 10%
        } else if (hoursBeforeDeparture >= 4) {
            feePercent = new BigDecimal("0.20"); // 20%
        } else {
            feePercent = new BigDecimal("0.30"); // 30% (< 4h)
        }

        return totalAmount.multiply(feePercent).setScale(0, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateRefundAmount(BigDecimal totalAmount, LocalDateTime cancelTime, LocalDateTime departureDatetime) {
        BigDecimal fee = calculateCancellationFee(totalAmount, cancelTime, departureDatetime);
        return totalAmount.subtract(fee).max(BigDecimal.ZERO);
    }

    // =========================================================================
    // 3.5. TÌM KIẾM ĐOÀN TÀU (CHIỀU ĐI & CHIỀU VỀ)
    // =========================================================================
    @Transactional(readOnly = true)
    public List<Train> searchTrains(TrainSearchDto searchDto) {
        if (searchDto.getDepartureStationId() == null || searchDto.getArrivalStationId() == null) {
            return trainRepository.findActiveByDepartureKeyword("Hà Nội");
        }
        TrainStation depStation = trainStationRepository.findById(searchDto.getDepartureStationId()).orElse(null);
        TrainStation arrStation = trainStationRepository.findById(searchDto.getArrivalStationId()).orElse(null);
        if (depStation == null || arrStation == null || depStation.getId().equals(arrStation.getId())) {
            return Collections.emptyList();
        }
        if (depStation.getStationIndex() < arrStation.getStationIndex()) {
            return trainRepository.findActiveByDepartureKeyword("Hà Nội");
        } else {
            return trainRepository.findActiveByDepartureKeyword("Sài Gòn");
        }
    }

    @Transactional(readOnly = true)
    public List<Train> searchReturnTrains(TrainSearchDto searchDto) {
        if (!searchDto.isRoundTrip() || searchDto.getReturnDate() == null) {
            return Collections.emptyList();
        }
        if (searchDto.getDepartureStationId() == null || searchDto.getArrivalStationId() == null) {
            return Collections.emptyList();
        }
        TrainStation depStation = trainStationRepository.findById(searchDto.getDepartureStationId()).orElse(null);
        TrainStation arrStation = trainStationRepository.findById(searchDto.getArrivalStationId()).orElse(null);
        if (depStation == null || arrStation == null || depStation.getId().equals(arrStation.getId())) {
            return Collections.emptyList();
        }
        // Chiều về chạy ngược lại: nếu chiều đi là Bắc -> Nam thì chiều về là Nam -> Bắc (Sài Gòn -> Hà Nội)
        if (depStation.getStationIndex() < arrStation.getStationIndex()) {
            return trainRepository.findActiveByDepartureKeyword("Sài Gòn");
        } else {
            return trainRepository.findActiveByDepartureKeyword("Hà Nội");
        }
    }

    // =========================================================================
    // 4. KIỂM TRA CHỖ TRỐNG SƠ ĐỒ GHẾ CỦA TOA THEO CHẶNG
    // =========================================================================
    @Transactional(readOnly = true)
    public List<SeatAvailabilityDto> getCoachSeatsAvailability(Long coachId, Long trainId, LocalDate departureDate,
                                                              int depStationIdx, int arrStationIdx) {
        TrainCoach coach = trainCoachRepository.findByIdWithSeats(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy toa tàu ID: " + coachId));

        // Lấy tất cả các vé đang có hiệu lực (HELD, CONFIRMED, ISSUED) trên tàu vào ngày đó
        List<TrainTicket> activeTickets = trainTicketRepository.findActiveTicketsOnDate(trainId, departureDate);

        List<SeatAvailabilityDto> result = new ArrayList<>();

        for (TrainSeat seat : coach.getSeats()) {
            boolean isOccupied = false;
            String reason = null;

            // Kiểm tra xem ghế này có vé nào đang chiếm chặng chồng lấn không (cả vé đi và vé khứ hồi)
            for (TrainTicket ticket : activeTickets) {
                boolean matchesOutbound = ticket.getSeat() != null
                        && ticket.getSeat().getId().equals(seat.getId())
                        && ticket.getTrainBooking().getTrain().getId().equals(trainId)
                        && ticket.getTrainBooking().getDepartureDate().equals(departureDate);

                boolean matchesReturn = ticket.getReturnSeat() != null
                        && ticket.getReturnSeat().getId().equals(seat.getId())
                        && ticket.getTrainBooking().isRoundTrip()
                        && ticket.getTrainBooking().getReturnTrain() != null
                        && ticket.getTrainBooking().getReturnTrain().getId().equals(trainId)
                        && ticket.getTrainBooking().getReturnDepartureDate() != null
                        && ticket.getTrainBooking().getReturnDepartureDate().equals(departureDate);

                if (matchesOutbound) {
                    if (isSegmentOverlapping(depStationIdx, arrStationIdx,
                            ticket.getDepartureStationIndex(), ticket.getArrivalStationIndex())) {
                        isOccupied = true;
                        reason = "Đã có khách đặt chặng [" + ticket.getTrainBooking().getDepartureStation().getStationName() +
                                " -> " + ticket.getTrainBooking().getArrivalStation().getStationName() + "]";
                        break;
                    }
                } else if (matchesReturn) {
                    if (isSegmentOverlapping(depStationIdx, arrStationIdx,
                            ticket.getArrivalStationIndex(), ticket.getDepartureStationIndex())) {
                        isOccupied = true;
                        reason = "Đã có khách đặt vé về chặng [" + ticket.getTrainBooking().getArrivalStation().getStationName() +
                                " -> " + ticket.getTrainBooking().getDepartureStation().getStationName() + "]";
                        break;
                    }
                }
            }

            result.add(new SeatAvailabilityDto(
                    seat.getId(),
                    seat.getSeatNumber(),
                    seat.getSeatLabel(),
                    seat.getPositionNote(),
                    !isOccupied,
                    reason
            ));
        }

        return result;
    }

    // =========================================================================
    // 5. QUY TRÌNH ĐẶT VÉ VÀ GIỮ CHỖ (HOLD RESERVATION - 10 PHÚT)
    // =========================================================================
    public TrainBooking createHoldBooking(TrainBookingRequestDto request) {
        Object lock1 = trainLocks.computeIfAbsent(request.getTrainId(), k -> new Object());
        Object lock2 = (request.isRoundTrip() && request.getReturnTrainId() != null)
                ? trainLocks.computeIfAbsent(request.getReturnTrainId(), k -> new Object())
                : new Object();

        Object firstLock = (request.getReturnTrainId() == null || request.getTrainId() <= request.getReturnTrainId()) ? lock1 : lock2;
        Object secondLock = (firstLock == lock1) ? lock2 : lock1;

        synchronized (firstLock) {
            synchronized (secondLock) {
                LocalDateTime now = LocalDateTime.now();

                // 1. Kiểm tra tồn tại đoàn tàu & 2 ga (khóa dòng bi quan trên CSDL)
                Train train = trainRepository.findByIdForUpdate(request.getTrainId())
                        .orElseGet(() -> trainRepository.findById(request.getTrainId())
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đoàn tàu ID: " + request.getTrainId())));

                Train returnTrain = null;
                if (request.isRoundTrip() && request.getReturnTrainId() != null) {
                    returnTrain = trainRepository.findByIdForUpdate(request.getReturnTrainId())
                            .orElseGet(() -> trainRepository.findById(request.getReturnTrainId())
                                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đoàn tàu chiều về ID: " + request.getReturnTrainId())));
                }

                TrainStation depStation = trainStationRepository.findById(request.getDepartureStationId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ga đi!"));

                TrainStation arrStation = trainStationRepository.findById(request.getArrivalStationId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ga đến!"));

                // 2. Tính ngày giờ khởi hành chính xác
                LocalTime depTimeOfDay = LocalTime.parse(train.getDepartureTimeOfDay());
                LocalDateTime departureDatetime = request.getDepartureDate().atTime(depTimeOfDay);

                LocalDateTime returnDepartureDatetime = null;
                if (request.isRoundTrip() && returnTrain != null && request.getReturnDate() != null) {
                    LocalTime retTimeOfDay = LocalTime.parse(returnTrain.getDepartureTimeOfDay());
                    returnDepartureDatetime = request.getReturnDate().atTime(retTimeOfDay);
                }

                // 3. Validation tổng thể
                validateTrainBookingRequest(train, depStation, arrStation, request, departureDatetime, now);

                if (request.isRoundTrip() && returnDepartureDatetime != null && returnDepartureDatetime.isBefore(departureDatetime)) {
                    throw new BusinessException("Giờ tàu chiều về (" + returnDepartureDatetime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")) +
                            ") phải sau giờ tàu chiều đi (" + departureDatetime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")) + ")!");
                }

                int distanceKm = Math.abs(arrStation.getDistanceKm() - depStation.getDistanceKm());

                // 4. Lấy danh sách các vé đang hoạt động trên tàu để kiểm tra chồng lấn ghế
                List<TrainTicket> activeTickets = trainTicketRepository.findActiveTicketsOnDate(train.getId(), request.getDepartureDate());

                List<TrainTicket> returnActiveTickets = (request.isRoundTrip() && returnTrain != null && request.getReturnDate() != null)
                        ? trainTicketRepository.findActiveTicketsOnDate(returnTrain.getId(), request.getReturnDate())
                        : Collections.emptyList();

                Set<Long> chosenSeatIds = new HashSet<>();
                Set<Long> chosenReturnSeatIds = new HashSet<>();

                TrainBooking booking = new TrainBooking();
                booking.setBookingCode("SE-TRN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
                booking.setTrain(train);
                booking.setDepartureStation(depStation);
                booking.setArrivalStation(arrStation);
                booking.setDepartureDate(request.getDepartureDate());
                booking.setDepartureDatetime(departureDatetime);
                booking.setCustomerName(request.getCustomerName().trim());
                booking.setCustomerPhone(request.getCustomerPhone().trim());
                booking.setCustomerEmail(request.getCustomerEmail().trim());
                booking.setTicketCount(request.getPassengers().size());
                booking.setStatus(TrainBookingStatus.HELD);
                booking.setBookingTime(now);
                booking.setHoldExpiresAt(now.plusMinutes(HOLD_EXPIRATION_MINUTES)); // Giữ chỗ 10 phút

                if (request.isRoundTrip() && returnTrain != null) {
                    booking.setIsRoundTrip(true);
                    booking.setReturnTrain(returnTrain);
                    booking.setReturnDepartureDate(request.getReturnDate());
                    booking.setReturnDepartureDatetime(returnDepartureDatetime);
                } else {
                    booking.setIsRoundTrip(false);
                }

                BigDecimal totalOriginal = BigDecimal.ZERO;
                BigDecimal totalDiscount = BigDecimal.ZERO;

                int reqDepIdx = depStation.getStationIndex();
                int reqArrIdx = arrStation.getStationIndex();

                for (TrainPassengerInputDto pDto : request.getPassengers()) {
                    TrainSeat seat = trainSeatRepository.findById(pDto.getSeatId())
                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chỗ ngồi ID: " + pDto.getSeatId()));

                    // Kiểm tra trùng ghế trong cùng đơn
                    if (!chosenSeatIds.add(seat.getId())) {
                        throw new BusinessException("Ghế " + seat.getSeatLabel() + " bị chọn trùng lặp trong cùng đơn đặt!");
                    }

                    // Kiểm tra chồng lấn chặng với các vé khác đã bán/đang giữ
                    for (TrainTicket activeTicket : activeTickets) {
                        boolean matchSeat = (activeTicket.getSeat() != null && activeTicket.getSeat().getId().equals(seat.getId())
                                && activeTicket.getTrainBooking().getTrain().getId().equals(train.getId())
                                && activeTicket.getTrainBooking().getDepartureDate().equals(request.getDepartureDate()))
                                || (activeTicket.getReturnSeat() != null && activeTicket.getReturnSeat().getId().equals(seat.getId())
                                && activeTicket.getTrainBooking().isRoundTrip()
                                && activeTicket.getTrainBooking().getReturnTrain() != null
                                && activeTicket.getTrainBooking().getReturnTrain().getId().equals(train.getId())
                                && activeTicket.getTrainBooking().getReturnDepartureDate() != null
                                && activeTicket.getTrainBooking().getReturnDepartureDate().equals(request.getDepartureDate()));

                        if (matchSeat) {
                            int tDep = (activeTicket.getDepartureStationIndex() != null) ? activeTicket.getDepartureStationIndex() :
                                    (activeTicket.getTrainBooking() != null && activeTicket.getTrainBooking().getDepartureStation() != null ? activeTicket.getTrainBooking().getDepartureStation().getStationIndex() : 0);
                            int tArr = (activeTicket.getArrivalStationIndex() != null) ? activeTicket.getArrivalStationIndex() :
                                    (activeTicket.getTrainBooking() != null && activeTicket.getTrainBooking().getArrivalStation() != null ? activeTicket.getTrainBooking().getArrivalStation().getStationIndex() : 0);
                            if (activeTicket.getReturnSeat() != null && activeTicket.getReturnSeat().getId().equals(seat.getId())) {
                                int temp = tDep;
                                tDep = tArr;
                                tArr = temp;
                            }
                            if (isSegmentOverlapping(reqDepIdx, reqArrIdx, tDep, tArr)) {
                                throw new BusinessException("Ghế " + seat.getSeatLabel() +
                                        " đã có khách đặt trên chặng [" +
                                        activeTicket.getTrainBooking().getDepartureStation().getStationName() + " -> " +
                                        activeTicket.getTrainBooking().getArrivalStation().getStationName() +
                                        "]. Chặng bạn chọn bị chồng lấn chỗ!");
                            }
                        }
                    }

                    // Tính tiền vé chiều đi
                    BigDecimal pricePerKm = seat.getCoach().getPricePerKm();
                    BigDecimal originalFare = calculateOriginalFare(distanceKm, pricePerKm);
                    BigDecimal discount = calculateDiscountAmount(originalFare, pDto.getPriorityCategory());
                    BigDecimal finalFare = originalFare.subtract(discount);

                    TrainTicket ticket = new TrainTicket();
                    ticket.setSeat(seat);
                    ticket.setPassengerName(pDto.getPassengerName().trim());
                    ticket.setPassengerIdCard(pDto.getPassengerIdCard() != null ? pDto.getPassengerIdCard().trim() : null);
                    ticket.setPriorityCategory(pDto.getPriorityCategory());
                    ticket.setPriorityDocId(pDto.getPriorityDocId() != null ? pDto.getPriorityDocId().trim() : null);
                    ticket.setDepartureStationIndex(reqDepIdx);
                    ticket.setArrivalStationIndex(reqArrIdx);
                    ticket.setDistanceKm(distanceKm);
                    ticket.setOriginalFare(originalFare);
                    ticket.setDiscountAmount(discount);
                    ticket.setFinalFare(finalFare);

                    totalOriginal = totalOriginal.add(originalFare);
                    totalDiscount = totalDiscount.add(discount);

                    // Xử lý vé chiều về nếu là khứ hồi
                    if (request.isRoundTrip() && returnTrain != null && pDto.getReturnSeatId() != null) {
                        TrainSeat returnSeat = trainSeatRepository.findById(pDto.getReturnSeatId())
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chỗ ngồi chiều về ID: " + pDto.getReturnSeatId()));

                        if (!chosenReturnSeatIds.add(returnSeat.getId())) {
                            throw new BusinessException("Ghế chiều về " + returnSeat.getSeatLabel() + " bị chọn trùng lặp!");
                        }

                        // Kiểm tra chồng lấn chặng chiều về (chặng về: arrStation -> depStation)
                        for (TrainTicket activeTicket : returnActiveTickets) {
                            boolean matchSeat = (activeTicket.getSeat() != null && activeTicket.getSeat().getId().equals(returnSeat.getId())
                                    && activeTicket.getTrainBooking().getTrain().getId().equals(returnTrain.getId())
                                    && activeTicket.getTrainBooking().getDepartureDate().equals(request.getReturnDate()))
                                    || (activeTicket.getReturnSeat() != null && activeTicket.getReturnSeat().getId().equals(returnSeat.getId())
                                    && activeTicket.getTrainBooking().isRoundTrip()
                                    && activeTicket.getTrainBooking().getReturnTrain() != null
                                    && activeTicket.getTrainBooking().getReturnTrain().getId().equals(returnTrain.getId())
                                    && activeTicket.getTrainBooking().getReturnDepartureDate() != null
                                    && activeTicket.getTrainBooking().getReturnDepartureDate().equals(request.getReturnDate()));

                            if (matchSeat) {
                                int tDep = (activeTicket.getDepartureStationIndex() != null) ? activeTicket.getDepartureStationIndex() :
                                        (activeTicket.getTrainBooking() != null && activeTicket.getTrainBooking().getDepartureStation() != null ? activeTicket.getTrainBooking().getDepartureStation().getStationIndex() : 0);
                                int tArr = (activeTicket.getArrivalStationIndex() != null) ? activeTicket.getArrivalStationIndex() :
                                        (activeTicket.getTrainBooking() != null && activeTicket.getTrainBooking().getArrivalStation() != null ? activeTicket.getTrainBooking().getArrivalStation().getStationIndex() : 0);
                                if (activeTicket.getReturnSeat() != null && activeTicket.getReturnSeat().getId().equals(returnSeat.getId())) {
                                    int temp = tDep;
                                    tDep = tArr;
                                    tArr = temp;
                                }
                                if (isSegmentOverlapping(reqArrIdx, reqDepIdx, tDep, tArr)) {
                                    throw new BusinessException("Ghế chiều về " + returnSeat.getSeatLabel() +
                                            " đã có khách đặt trên chặng [" +
                                            activeTicket.getTrainBooking().getDepartureStation().getStationName() + " -> " +
                                            activeTicket.getTrainBooking().getArrivalStation().getStationName() +
                                            "]. Chặng về bị chồng lấn chỗ!");
                                }
                            }
                        }

                        BigDecimal returnPricePerKm = returnSeat.getCoach().getPricePerKm();
                        BigDecimal returnOriginalFare = calculateOriginalFare(distanceKm, returnPricePerKm);
                        BigDecimal returnDiscount = calculateDiscountAmount(returnOriginalFare, pDto.getPriorityCategory());
                        BigDecimal returnFinalFare = returnOriginalFare.subtract(returnDiscount);

                        ticket.setReturnSeat(returnSeat);
                        ticket.setReturnOriginalFare(returnOriginalFare);
                        ticket.setReturnDiscountAmount(returnDiscount);
                        ticket.setReturnFinalFare(returnFinalFare);

                        totalOriginal = totalOriginal.add(returnOriginalFare);
                        totalDiscount = totalDiscount.add(returnDiscount);
                    }

                    booking.addTicket(ticket);
                }

                booking.setTotalOriginalFare(totalOriginal);
                booking.setTotalDiscount(totalDiscount);
                booking.setTotalAmount(totalOriginal.subtract(totalDiscount));

                log.info("Tạo đơn giữ chỗ vé tàu thành công: {} ({} khách, Khứ hồi: {}). Hết hạn giữ chỗ lúc {}",
                        booking.getBookingCode(), booking.getTicketCount(), booking.isRoundTrip(), booking.getHoldExpiresAt());

                return trainBookingRepository.save(booking);
            }
        }
    }

    // =========================================================================
    // 6. XÁC NHẬN THANH TOÁN & PHÁT HÀNH VÉ TÀU ĐIỆN TỬ
    // =========================================================================
    public TrainBooking confirmAndIssueTicket(String bookingCode) {
        TrainBooking booking = trainBookingRepository.findByBookingCodeWithDetails(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt vé tàu: " + bookingCode));

        LocalDateTime now = LocalDateTime.now();

        if (booking.getStatus() == TrainBookingStatus.ISSUED) {
            return booking;
        }

        if (booking.getStatus() != TrainBookingStatus.HELD && booking.getStatus() != TrainBookingStatus.CONFIRMED) {
            throw new BusinessException("Đơn đặt chỗ ở trạng thái " + booking.getStatus().getDisplayName() +
                    ", không thể xác nhận xuất vé!");
        }

        if (booking.getStatus() == TrainBookingStatus.HELD && booking.getHoldExpiresAt() != null && now.isAfter(booking.getHoldExpiresAt())) {
            booking.setStatus(TrainBookingStatus.EXPIRED);
            trainBookingRepository.save(booking);
            throw new BusinessException("Thời gian giữ chỗ 10 phút đã hết hạn! Chỗ ngồi đã được nhả lại cho hành khách khác.");
        }

        booking.setStatus(TrainBookingStatus.ISSUED);
        booking.setConfirmedTime(now);

        long counter = 1;
        for (TrainTicket t : booking.getTickets()) {
            t.setTicketCode("TRAIN-" + booking.getTrain().getTrainCode() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase() + "-" + counter);
            if (booking.isRoundTrip() && booking.getReturnTrain() != null) {
                t.setReturnTicketCode("TRAIN-" + booking.getReturnTrain().getTrainCode() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase() + "-" + counter);
            }
            counter++;
        }

        log.info("Phát hành vé tàu điện tử thành công cho đơn {} (Khứ hồi: {})", bookingCode, booking.isRoundTrip());
        return trainBookingRepository.save(booking);
    }

    // =========================================================================
    // 7. HỦY VÉ TÀU HỎA
    // =========================================================================
    public TrainBooking cancelBooking(String bookingCode, String reason) {
        TrainBooking booking = trainBookingRepository.findByBookingCodeWithDetails(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt vé tàu: " + bookingCode));

        if (booking.getStatus() == TrainBookingStatus.EXPIRED) {
            throw new BusinessException("Đơn giữ chỗ đã hết hạn, không thể hủy!");
        }

        if (booking.getStatus() == TrainBookingStatus.CANCELLED) {
            throw new BusinessException("Đơn đặt vé tàu này đã được hủy trước đó!");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departureTime = booking.getDepartureDatetime();

        if (now.isAfter(departureTime) && (booking.getStatus() == TrainBookingStatus.ISSUED 
                || booking.getStatus() == TrainBookingStatus.CONFIRMED)) {
            throw new BusinessException("Tàu đã khởi hành, theo quy định đường sắt không thể trả/hủy vé!");
        }

        BigDecimal cancellationFee = BigDecimal.ZERO;
        BigDecimal refundAmount = BigDecimal.ZERO;

        // Chỉ tính phí hủy và hoàn tiền khi đơn đã thanh toán
        if (booking.getStatus() == TrainBookingStatus.ISSUED 
                || booking.getStatus() == TrainBookingStatus.CONFIRMED) {
            cancellationFee = calculateCancellationFee(booking.getTotalAmount(), now, departureTime);
            refundAmount = calculateRefundAmount(booking.getTotalAmount(), now, departureTime);
        }

        booking.setCancellationFee(cancellationFee);
        booking.setRefundAmount(refundAmount);
        booking.setStatus(TrainBookingStatus.CANCELLED);
        booking.setCancelledTime(now);
        booking.setCancelReason(reason != null ? reason : "Hành khách yêu cầu hủy/trả vé");

        log.info("Hủy vé tàu thành công cho mã {}. Phí hủy: {} VNĐ, Hoàn lại: {} VNĐ",
                bookingCode, cancellationFee, refundAmount);

        return trainBookingRepository.save(booking);
    }

    // =========================================================================
    // 8. SCHEDULED TASK: NHẢ CHỖ HẾT HẠN (10 PHÚT)
    // =========================================================================
    public int releaseExpiredHoldBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<TrainBooking> expiredBookings = trainBookingRepository.findExpiredBookings(TrainBookingStatus.HELD, now);

        int count = 0;
        for (TrainBooking booking : expiredBookings) {
            try {
                log.info("Scheduled Task: Đơn giữ chỗ vé tàu {} hết hạn lúc {}, thực hiện nhả chỗ!",
                        booking.getBookingCode(), booking.getHoldExpiresAt());
                booking.setStatus(TrainBookingStatus.EXPIRED);
                trainBookingRepository.save(booking);
                count++;
            } catch (Exception ex) {
                log.error("Scheduled Task: Lỗi khi nhả chỗ cho đơn vé tàu {}: {}", 
                        booking != null ? booking.getBookingCode() : "null", ex.getMessage());
            }
        }
        return count;
    }

    // =========================================================================
    // 9. VALIDATION NGHIỆP VỤ VÉ TÀU HỎA
    // =========================================================================
    public void validateTrainBookingRequest(Train train, TrainStation depStation, TrainStation arrStation,
                                           TrainBookingRequestDto request, LocalDateTime departureDatetime, LocalDateTime now) {
        // 1. Ga đi khác ga đến và đúng thứ tự tuyến
        if (depStation.getId().equals(arrStation.getId())) {
            throw new BusinessException("Ga xuất phát và Ga đến không được trùng nhau!");
        }
        boolean isNorthToSouthTrain = train.getDepartureStationName() != null && train.getDepartureStationName().contains("Hà Nội");
        if (isNorthToSouthTrain && depStation.getStationIndex() >= arrStation.getStationIndex()) {
            throw new BusinessException("Đoàn tàu " + train.getTrainCode() + " chạy chiều Bắc -> Nam. Ga đi (" + depStation.getStationName() + ") phải đứng trước Ga đến (" + arrStation.getStationName() + ")!");
        }
        boolean isSouthToNorthTrain = train.getDepartureStationName() != null && train.getDepartureStationName().contains("Sài Gòn");
        if (isSouthToNorthTrain && depStation.getStationIndex() <= arrStation.getStationIndex()) {
            throw new BusinessException("Đoàn tàu " + train.getTrainCode() + " chạy chiều Nam -> Bắc. Ga đi (" + depStation.getStationName() + ") phải đứng sau Ga đến (" + arrStation.getStationName() + ")!");
        }

        if (request.isRoundTrip()) {
            if (request.getReturnDate() == null) {
                throw new BusinessException("Vé khứ hồi bắt buộc phải chọn ngày về!");
            }
            if (request.getReturnDate().isBefore(request.getDepartureDate())) {
                throw new BusinessException("Ngày về (" + request.getReturnDate() + ") không được trước ngày đi (" + request.getDepartureDate() + ")!");
            }
            if (request.getReturnTrainId() == null) {
                throw new BusinessException("Chưa chọn đoàn tàu cho chiều về!");
            }
        }

        // 2. Chỉ đặt trong vòng 30 ngày tới
        LocalDate today = now.toLocalDate();
        if (request.getDepartureDate().isBefore(today)) {
            throw new BusinessException("Ngày đi không được ở quá khứ!");
        }
        if (request.getDepartureDate().isAfter(today.plusDays(MAX_BOOKING_DAYS_IN_ADVANCE))) {
            throw new BusinessException("Đường sắt chỉ mở bán vé trong vòng 30 ngày tới (tối đa đến ngày " +
                    today.plusDays(MAX_BOOKING_DAYS_IN_ADVANCE) + ")!");
        }

        // 3. Giờ tàu chạy không ở quá khứ
        if (departureDatetime.isBefore(now)) {
            throw new BusinessException("Giờ tàu khởi hành đã qua!");
        }

        // 4. Tối đa 10 vé mỗi lần đặt
        List<TrainPassengerInputDto> passengers = request.getPassengers();
        if (passengers == null || passengers.isEmpty()) {
            throw new BusinessException("Danh sách hành khách không được để trống!");
        }
        if (passengers.size() > MAX_TICKETS_PER_BOOKING) {
            throw new BusinessException("Mỗi lần đặt vé tàu chỉ được tối đa 10 vé (hiện có: " + passengers.size() + ")!");
        }

        // 5. Kiểm tra đối tượng ưu tiên & giấy tờ chứng minh
        boolean hasAdult = false;
        int childCount = 0;

        for (TrainPassengerInputDto p : passengers) {
            if (p.getPassengerName() == null || p.getPassengerName().trim().isEmpty()) {
                throw new BusinessException("Họ tên hành khách không được để trống!");
            }

            if (p.getPriorityCategory() == PassengerPriority.STANDARD ||
                p.getPriorityCategory() == PassengerPriority.STUDENT ||
                p.getPriorityCategory() == PassengerPriority.SENIOR) {
                hasAdult = true;
            }

            if (p.getPriorityCategory() == PassengerPriority.CHILD_6_10) {
                childCount++;
            }

            // Đối tượng ưu tiên phải có thông tin giấy tờ
            if (p.getPriorityCategory() != PassengerPriority.STANDARD) {
                if (p.getPriorityDocId() == null || p.getPriorityDocId().trim().isEmpty()) {
                    throw new BusinessException("Hành khách " + p.getPassengerName() + " thuộc diện ưu tiên [" +
                            p.getPriorityCategory().getDisplayName() + "] bắt buộc phải nhập " +
                            p.getPriorityCategory().getRequiredDocNote() + "!");
                }
            }

            if (request.isRoundTrip() && p.getReturnSeatId() == null) {
                throw new BusinessException("Hành khách " + p.getPassengerName() + " chưa được chọn chỗ ngồi chiều về!");
            }
        }

        // 6. Trẻ em dưới 10 tuổi phải đi cùng người lớn
        if (childCount > 0 && !hasAdult) {
            throw new BusinessException("Quy định ngành đường sắt: Trẻ em dưới 10 tuổi bắt buộc phải có người lớn đi kèm bảo hộ!");
        }
    }

    // =========================================================================
    // CÁC HÀM IMPLEMENT TỪ TICKETSERVICE CHUNG (TƯƠNG THÍCH ĐA HÌNH)
    // =========================================================================
    @Override
    public BigDecimal calculateTotalFare(BigDecimal basePrice, String seatClass, int passengerCount, BigDecimal extraServices) {
        BigDecimal unitPrice = "SLEEPER_4".equalsIgnoreCase(seatClass) ? new BigDecimal("1300")
                : "SLEEPER_6".equalsIgnoreCase(seatClass) ? new BigDecimal("1000")
                : new BigDecimal("700");
        int defaultDistance = 791; // Hà Nội - Đà Nẵng
        BigDecimal singleFare = unitPrice.multiply(BigDecimal.valueOf(defaultDistance));
        BigDecimal total = singleFare.multiply(BigDecimal.valueOf(passengerCount));
        if (extraServices != null) {
            total = total.add(extraServices);
        }
        return total;
    }

    @Override
    public void validateBooking(Booking booking) {
        if (booking.getDepartureDate() == null) {
            throw new BusinessException("Vé tàu hỏa bắt buộc phải có ngày xuất phát!");
        }
        if (booking.getDeparturePlace() != null && booking.getDeparturePlace().equalsIgnoreCase(booking.getArrivalPlace())) {
            throw new BusinessException("Ga xuất phát và Ga đến của tàu hỏa không được trùng nhau!");
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
            throw new BusinessException("Tàu đã khởi hành, không thể hủy vé!");
        }
        booking.setStatus(com.dulich.ticketmanagement.entity.enums.BookingStatus.CANCELLED);
    }
}
