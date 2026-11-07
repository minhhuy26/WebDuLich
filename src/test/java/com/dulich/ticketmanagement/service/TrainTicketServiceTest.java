package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.entity.Train;
import com.dulich.ticketmanagement.entity.TrainBooking;
import com.dulich.ticketmanagement.entity.TrainCoach;
import com.dulich.ticketmanagement.entity.TrainSeat;
import com.dulich.ticketmanagement.entity.TrainStation;
import com.dulich.ticketmanagement.entity.TrainTicket;
import com.dulich.ticketmanagement.entity.enums.CoachType;
import com.dulich.ticketmanagement.entity.enums.PassengerPriority;
import com.dulich.ticketmanagement.entity.enums.TrainBookingStatus;
import com.dulich.ticketmanagement.exception.BusinessException;
import com.dulich.ticketmanagement.repository.*;
import com.dulich.ticketmanagement.service.impl.TrainTicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("Kiểm thử nghiệp vụ vé tàu hỏa: Chồng lấn chặng & Tính giá vé (TrainTicketService)")
class TrainTicketServiceTest {

    @Mock
    private TrainRepository trainRepository;

    @Mock
    private TrainStationRepository trainStationRepository;

    @Mock
    private TrainCoachRepository trainCoachRepository;

    @Mock
    private TrainSeatRepository trainSeatRepository;

    @Mock
    private TrainBookingRepository trainBookingRepository;

    @Mock
    private TrainTicketRepository trainTicketRepository;

    private TrainTicketService trainTicketService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        trainTicketService = new TrainTicketService(
                trainRepository,
                trainStationRepository,
                trainCoachRepository,
                trainSeatRepository,
                trainBookingRepository,
                trainTicketRepository
        );
    }

    // =========================================================================
    // TEST CA 1: Kiểm tra các chặng NỐI ĐUÔI NHAU -> KHÔNG chồng lấn (Dùng chung ghế)
    // =========================================================================
    @Test
    @DisplayName("Ca 1: Kiểm tra chặng nối đuôi nhau [A, B) và [B, C) -> KHÔNG chồng lấn (bán chung 1 ghế được)")
    void testSegmentOverlap_AdjacentSegments_ShouldNotOverlap() {
        // Tuyến Bắc - Nam: Hà Nội (index 0) -> Vinh (index 4) -> Đà Nẵng (index 7) -> Sài Gòn (index 10)
        int depHanoi = 0;
        int arrVinh = 4;
        int depVinh = 4;
        int arrDanang = 7;
        int depDanang = 7;
        int arrSaigon = 10;

        // Khách 1 đi Hà Nội -> Vinh [0, 4), Khách 2 đi Vinh -> Đà Nẵng [4, 7)
        // Vì khách 1 xuống ở ga Vinh và khách 2 lên ở ga Vinh nên 2 chặng KHÔNG chồng lấn!
        boolean overlap1 = trainTicketService.isSegmentOverlapping(depHanoi, arrVinh, depVinh, arrDanang);
        assertFalse(overlap1, "Chặng [Hà Nội -> Vinh] và [Vinh -> Đà Nẵng] nối tiếp nhau phải KHÔNG chồng lấn");

        // Khách 1 đi Hà Nội -> Vinh [0, 4), Khách 3 đi Đà Nẵng -> Sài Gòn [7, 10)
        boolean overlap2 = trainTicketService.isSegmentOverlapping(depHanoi, arrVinh, depDanang, arrSaigon);
        assertFalse(overlap2, "Chặng [Hà Nội -> Vinh] và [Đà Nẵng -> Sài Gòn] cách xa nhau phải KHÔNG chồng lấn");
    }

    // =========================================================================
    // TEST CA 2: Kiểm tra các chặng GIAO THOA hoặc CHỨA NHAU -> CÓ chồng lấn (Xung đột)
    // =========================================================================
    @Test
    @DisplayName("Ca 2: Kiểm tra chặng giao thoa một phần hoặc nằm bên trong -> CÓ chồng lấn (Xung đột ghế)")
    void testSegmentOverlap_IntersectingOrContained_ShouldOverlap() {
        // Chặng gốc: Hà Nội (0) -> Đà Nẵng (7) tức [0, 7)
        int depHanoi = 0;
        int arrDanang = 7;

        // Trường hợp A: Nằm lọt bên trong chặng gốc: Thanh Hóa (3) -> Đồng Hới (5) tức [3, 5)
        boolean overlapInside = trainTicketService.isSegmentOverlapping(depHanoi, arrDanang, 3, 5);
        assertTrue(overlapInside, "Chặng [3, 5) nằm trong [0, 7) phải BỊ chồng lấn");

        // Trường hợp B: Giao thoa phần cuối: Vinh (4) -> Sài Gòn (10) tức [4, 10)
        // Giao nhau ở đoạn [4, 7)
        boolean overlapTail = trainTicketService.isSegmentOverlapping(depHanoi, arrDanang, 4, 10);
        assertTrue(overlapTail, "Chặng [0, 7) và [4, 10) giao thoa đoạn [4, 7) phải BỊ chồng lấn");

        // Trường hợp C: Giao thoa phần đầu: ga trước đó (-1 không có, nhưng giả sử [1, 5) và [0, 3) giao nhau ở [1, 3))
        boolean overlapHead = trainTicketService.isSegmentOverlapping(1, 5, 0, 3);
        assertTrue(overlapHead, "Chặng [1, 5) và [0, 3) giao thoa đoạn [1, 3) phải BỊ chồng lấn");
    }

    // =========================================================================
    // TEST CA 3: Kiểm tra chặng TRÙNG NHAU hoặc CHUNG GA ĐI/ĐẾN -> CÓ chồng lấn
    // =========================================================================
    @Test
    @DisplayName("Ca 3: Kiểm tra chặng trùng lặp hoàn toàn hoặc chung ga đi/đến -> CÓ chồng lấn")
    void testSegmentOverlap_SameOrCommonEnd_ShouldOverlap() {
        // Trùng hoàn toàn [0, 4) và [0, 4)
        boolean exactSame = trainTicketService.isSegmentOverlapping(0, 4, 0, 4);
        assertTrue(exactSame, "2 chặng trùng nhau hoàn toàn phải BỊ chồng lấn");

        // Cùng ga xuất phát: [0, 4) và [0, 2)
        boolean sameDeparture = trainTicketService.isSegmentOverlapping(0, 4, 0, 2);
        assertTrue(sameDeparture, "2 chặng có cùng ga đi phải BỊ chồng lấn");

        // Cùng ga đến: [1, 4) và [2, 4)
        boolean sameArrival = trainTicketService.isSegmentOverlapping(1, 4, 2, 4);
        assertTrue(sameArrival, "2 chặng có cùng ga đến phải BỊ chồng lấn");
    }

    // =========================================================================
    // TEST CA 4: Thuật toán tính giá vé cơ bản theo khoảng cách km và loại toa
    // =========================================================================
    @Test
    @DisplayName("Ca 4: Tính giá vé = Cự ly km × Đơn giá loại toa (Ghế mềm 700đ, Giường nằm 6 1000đ, Giường 4 VIP 1300đ)")
    void testCalculateOriginalFareByCoachType() {
        int distanceKm = 791; // Hà Nội -> Đà Nẵng: 791 km

        // 1. Toa Ghế mềm điều hòa (700 VNĐ/km)
        BigDecimal softSeatFare = trainTicketService.calculateOriginalFare(distanceKm, CoachType.SOFT_SEAT.getDefaultPricePerKm());
        // 791 * 700 = 553,700 VNĐ
        assertEquals(new BigDecimal("553700"), softSeatFare, "Giá vé ghế mềm 791km phải là 553,700 đ");

        // 2. Toa Giường nằm khoang 6 (1,000 VNĐ/km)
        BigDecimal sleeper6Fare = trainTicketService.calculateOriginalFare(distanceKm, CoachType.SLEEPER_6.getDefaultPricePerKm());
        // 791 * 1000 = 791,000 VNĐ
        assertEquals(new BigDecimal("791000"), sleeper6Fare, "Giá vé giường nằm 6 chỗ 791km phải là 791,000 đ");

        // 3. Toa Giường nằm khoang 4 VIP (1,300 VNĐ/km)
        BigDecimal sleeper4Fare = trainTicketService.calculateOriginalFare(distanceKm, CoachType.SLEEPER_4.getDefaultPricePerKm());
        // 791 * 1300 = 1,028,300 VNĐ
        assertEquals(new BigDecimal("1028300"), sleeper4Fare, "Giá vé giường nằm 4 VIP 791km phải là 1,028,300 đ");
    }

    // =========================================================================
    // TEST CA 5: Thuật toán giảm giá ưu tiên & Chính sách phí hủy vé tàu
    // =========================================================================
    @Test
    @DisplayName("Ca 5: Giảm giá ưu tiên (Trẻ em 25%, SV 10%, Người già 15%) & Phí hủy vé (>=24h: 10%, 4-24h: 20%, <4h: 30%)")
    void testDiscountsAndCancellationPolicy() {
        BigDecimal baseFare = new BigDecimal("1000000"); // 1,000,000 VNĐ

        // 1. Kiểm tra chính sách giảm giá ưu tiên
        // Người lớn tiêu chuẩn: giảm 0% -> giá 1,000,000 đ
        assertEquals(new BigDecimal("1000000"), trainTicketService.calculateFinalFare(baseFare, PassengerPriority.STANDARD));

        // Sinh viên: giảm 10% (100,000đ) -> giá 900,000 đ
        assertEquals(new BigDecimal("100000"), trainTicketService.calculateDiscountAmount(baseFare, PassengerPriority.STUDENT));
        assertEquals(new BigDecimal("900000"), trainTicketService.calculateFinalFare(baseFare, PassengerPriority.STUDENT));

        // Người cao tuổi (>= 60 tuổi): giảm 15% (150,000đ) -> giá 850,000 đ
        assertEquals(new BigDecimal("150000"), trainTicketService.calculateDiscountAmount(baseFare, PassengerPriority.SENIOR));
        assertEquals(new BigDecimal("850000"), trainTicketService.calculateFinalFare(baseFare, PassengerPriority.SENIOR));

        // Trẻ em (6 - 10 tuổi): giảm 25% (250,000đ) -> giá 750,000 đ
        assertEquals(new BigDecimal("250000"), trainTicketService.calculateDiscountAmount(baseFare, PassengerPriority.CHILD_6_10));
        assertEquals(new BigDecimal("750000"), trainTicketService.calculateFinalFare(baseFare, PassengerPriority.CHILD_6_10));

        // 2. Kiểm tra chính sách hủy vé tàu hỏa
        LocalDateTime now = LocalDateTime.of(2026, 11, 1, 8, 0);

        // Hủy trước 30 giờ (>= 24h) -> Phí 10% (100,000đ), Hoàn lại 90% (900,000đ)
        LocalDateTime dep30h = now.plusHours(30);
        assertEquals(new BigDecimal("100000"), trainTicketService.calculateCancellationFee(baseFare, now, dep30h));
        assertEquals(new BigDecimal("900000"), trainTicketService.calculateRefundAmount(baseFare, now, dep30h));

        // Hủy trước 10 giờ (từ 4 đến 24h) -> Phí 20% (200,000đ), Hoàn lại 80% (800,000đ)
        LocalDateTime dep10h = now.plusHours(10);
        assertEquals(new BigDecimal("200000"), trainTicketService.calculateCancellationFee(baseFare, now, dep10h));
        assertEquals(new BigDecimal("800000"), trainTicketService.calculateRefundAmount(baseFare, now, dep10h));

        // Hủy trước 2 giờ (< 4h) -> Phí 30% (300,000đ), Hoàn lại 70% (700,000đ)
        LocalDateTime dep2h = now.plusHours(2);
        assertEquals(new BigDecimal("300000"), trainTicketService.calculateCancellationFee(baseFare, now, dep2h));
        assertEquals(new BigDecimal("700000"), trainTicketService.calculateRefundAmount(baseFare, now, dep2h));

        // Sau giờ tàu chạy -> Phí 100% (1,000,000đ), Hoàn lại 0đ
        LocalDateTime depPast = now.minusHours(1);
        assertEquals(new BigDecimal("1000000"), trainTicketService.calculateCancellationFee(baseFare, now, depPast));
        assertEquals(BigDecimal.ZERO, trainTicketService.calculateRefundAmount(baseFare, now, depPast));
    }

    // =========================================================================
    // TEST CA TÁI HIỆN LỖI 4: Hủy đơn tàu HELD chưa thanh toán -> Hoàn tiền = 0đ, Phí hủy = 0đ
    // =========================================================================
    @Test
    @DisplayName("Lỗi 4: Hủy đơn vé tàu HELD (chưa thanh toán) phải có tiền hoàn = 0đ và phí = 0đ")
    void testCancelTrainBooking_HeldStatus_MustNotRefundMoney() {
        TrainBooking booking = new TrainBooking();
        booking.setBookingCode("TRN-HELD");
        booking.setStatus(TrainBookingStatus.HELD);
        booking.setTotalAmount(new BigDecimal("1500000"));
        booking.setDepartureDatetime(LocalDateTime.now().plusDays(3));

        when(trainBookingRepository.findByBookingCodeWithDetails("TRN-HELD")).thenReturn(Optional.of(booking));
        when(trainBookingRepository.save(any(TrainBooking.class))).thenAnswer(i -> i.getArgument(0));

        TrainBooking cancelled = trainTicketService.cancelBooking("TRN-HELD", "Hủy");

        // LỖI 4 TÁI HIỆN: Trước khi sửa, đơn HELD chưa trả tiền bị hoàn tiền ma 1,350,000đ
        assertEquals(BigDecimal.ZERO, cancelled.getRefundAmount(), "Đơn vé tàu HELD chưa thanh toán không được hoàn tiền ma");
        assertEquals(BigDecimal.ZERO, cancelled.getCancellationFee(), "Đơn vé tàu HELD hủy trong hạn không tính phí hủy");
        assertEquals(TrainBookingStatus.CANCELLED, cancelled.getStatus());
    }

    // =========================================================================
    // TEST CA TÁI HIỆN LỖI 5: Hủy đơn tàu EXPIRED -> Phải ném ngoại lệ từ chối hủy
    // =========================================================================
    @Test
    @DisplayName("Lỗi 5: Đơn vé tàu EXPIRED đã hết hạn giữ chỗ không được phép hủy")
    void testCancelTrainBooking_ExpiredStatus_MustThrowException() {
        TrainBooking booking = new TrainBooking();
        booking.setBookingCode("TRN-EXPIRED");
        booking.setStatus(TrainBookingStatus.EXPIRED);
        booking.setTotalAmount(new BigDecimal("1500000"));
        booking.setDepartureDatetime(LocalDateTime.now().plusDays(3));

        when(trainBookingRepository.findByBookingCodeWithDetails("TRN-EXPIRED")).thenReturn(Optional.of(booking));

        // LỖI 5 TÁI HIỆN: Trước khi sửa, đơn EXPIRED bị hủy và tính hoàn tiền
        BusinessException ex = assertThrows(BusinessException.class, () -> trainTicketService.cancelBooking("TRN-EXPIRED", "Hủy"));
        assertTrue(ex.getMessage().contains("hết hạn"), "Phải thông báo đơn giữ chỗ đã hết hạn");
    }

    // =========================================================================
    // TEST CA TÁI HIỆN LỖI 9: Hủy vé tàu cận giờ (< 1 giờ trước khi chạy) -> Cho phép hủy với phí 30%
    // =========================================================================
    @Test
    @DisplayName("Lỗi 9: Hủy vé tàu trước giờ chạy 30 phút (<1h) phải cho phép hủy với phí 30%, không được báo đã khởi hành")
    void testCancelTrainBooking_Within30MinutesBeforeDeparture_ShouldAllowCancellation() {
        TrainBooking booking = new TrainBooking();
        booking.setBookingCode("TRN-NEAR");
        booking.setStatus(TrainBookingStatus.ISSUED);
        booking.setTotalAmount(new BigDecimal("1000000"));
        booking.setDepartureDatetime(LocalDateTime.now().plusMinutes(30)); // 30 phút nữa tàu chạy

        when(trainBookingRepository.findByBookingCodeWithDetails("TRN-NEAR")).thenReturn(Optional.of(booking));
        when(trainBookingRepository.save(any(TrainBooking.class))).thenAnswer(i -> i.getArgument(0));

        // LỖI 9 TÁI HIỆN: Trước khi sửa, Duration.toHours() <= 0 khiến hệ thống văng ngoại lệ "Tàu đã khởi hành"
        TrainBooking cancelled = trainTicketService.cancelBooking("TRN-NEAR", "Hủy gấp");

        assertEquals(new BigDecimal("300000"), cancelled.getCancellationFee(), "Phí hủy tàu trước giờ chạy < 4h là 30%");
        assertEquals(new BigDecimal("700000"), cancelled.getRefundAmount(), "Hoàn lại 70% khi hủy trước giờ chạy");
        assertEquals(TrainBookingStatus.CANCELLED, cancelled.getStatus());
    }

    // =========================================================================
    // TEST CA TÁI HIỆN LỖI 7: Race Condition khi nhiều luồng cùng đặt 1 ghế tàu hỏa
    // =========================================================================
    @Test
    @DisplayName("Lỗi 7: Race Condition đặt trùng ghế tàu - Chạy 2 luồng cùng đặt Ghế 10, CHỈ 1 luồng được thành công")
    void testConcurrentTrainSeatBooking_RaceCondition_OnlyOneThreadSucceeds() throws Exception {
        Train train = new Train();
        train.setId(1L);
        train.setTrainCode("SE1");
        train.setDepartureTimeOfDay("08:00");

        TrainStation depStation = new TrainStation();
        depStation.setId(1L);
        depStation.setStationName("Hà Nội");
        depStation.setStationIndex(0);
        depStation.setDistanceKm(0);

        TrainStation arrStation = new TrainStation();
        arrStation.setId(2L);
        arrStation.setStationName("Vinh");
        arrStation.setStationIndex(4);
        arrStation.setDistanceKm(319);

        TrainCoach coach = new TrainCoach();
        coach.setPricePerKm(new BigDecimal("700"));

        TrainSeat seat = new TrainSeat();
        seat.setId(10L);
        seat.setSeatLabel("Ghế 10");
        seat.setCoach(coach);

        LocalDate departureDate = LocalDate.now().plusDays(5);

        List<TrainTicket> sharedActiveTickets = Collections.synchronizedList(new ArrayList<>());

        when(trainRepository.findById(1L)).thenReturn(Optional.of(train));
        when(trainStationRepository.findById(1L)).thenReturn(Optional.of(depStation));
        when(trainStationRepository.findById(2L)).thenReturn(Optional.of(arrStation));
        when(trainSeatRepository.findById(10L)).thenReturn(Optional.of(seat));
        when(trainTicketRepository.findActiveTicketsOnDate(any(), any())).thenAnswer(i -> new ArrayList<>(sharedActiveTickets));

        when(trainBookingRepository.save(any(TrainBooking.class))).thenAnswer(invocation -> {
            TrainBooking b = invocation.getArgument(0);
            // Giả lập độ trễ I/O database 50ms để kích hoạt race condition
            Thread.sleep(50);
            for (TrainTicket t : b.getTickets()) {
                sharedActiveTickets.add(t);
            }
            return b;
        });

        int numThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Cả 2 luồng cùng xuất phát đồng thời

                    com.dulich.ticketmanagement.dto.TrainBookingRequestDto req = new com.dulich.ticketmanagement.dto.TrainBookingRequestDto();
                    req.setTrainId(1L);
                    req.setDepartureStationId(1L);
                    req.setArrivalStationId(2L);
                    req.setDepartureDate(departureDate);
                    req.setCustomerName("Khách Tàu " + index);
                    req.setCustomerPhone("090123456" + index);
                    req.setCustomerEmail("khachtau" + index + "@test.com");

                    com.dulich.ticketmanagement.dto.TrainPassengerInputDto p = new com.dulich.ticketmanagement.dto.TrainPassengerInputDto();
                    p.setPassengerName("Hành Khách Tàu " + index);
                    p.setPriorityCategory(PassengerPriority.STANDARD);
                    p.setSeatId(10L); // CẢ 2 LUỒNG CÙNG ĐẶT GHẾ ID 10!

                    req.setPassengers(List.of(p));

                    trainTicketService.createHoldBooking(req);
                    successCount.incrementAndGet();
                } catch (BusinessException e) {
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // LỖI 7 TÁI HIỆN: Trước khi sửa cơ chế đồng bộ/khóa, cả 2 luồng cùng đặt ghế 10 thành công (2 thành công, 0 thất bại)
        assertEquals(1, successCount.get(), "Chỉ đúng 1 luồng được phép đặt Ghế 10 thành công!");
        assertEquals(1, failCount.get(), "Luồng còn lại phải thất bại do bị trùng/chồng lấn ghế!");
    }

    @Test
    @DisplayName("Bug 17 Test: Phát hành vé tàu sinh mã vé điện tử duy nhất, không trùng lặp và độ dài <= 30 ký tự")
    void testBug17_IssueTicket_GeneratesUniqueTicketCodesWithoutCollisions() {
        Train train = new Train();
        train.setId(1L);
        train.setTrainCode("SE1");

        TrainBooking booking = new TrainBooking();
        booking.setId(100L);
        booking.setBookingCode("SE-TRN-100");
        booking.setTrain(train);
        booking.setStatus(TrainBookingStatus.HELD);
        booking.setHoldExpiresAt(LocalDateTime.now().plusMinutes(10));

        TrainTicket t1 = new TrainTicket();
        t1.setId(1L);
        TrainTicket t2 = new TrainTicket();
        t2.setId(2L);
        booking.setTickets(new ArrayList<>(List.of(t1, t2)));

        when(trainBookingRepository.findByBookingCodeWithDetails("SE-TRN-100")).thenReturn(Optional.of(booking));
        when(trainBookingRepository.save(any(TrainBooking.class))).thenAnswer(i -> i.getArgument(0));

        TrainBooking issued = trainTicketService.confirmAndIssueTicket("SE-TRN-100");

        assertNotNull(issued);
        assertEquals(TrainBookingStatus.ISSUED, issued.getStatus());

        String code1 = issued.getTickets().get(0).getTicketCode();
        String code2 = issued.getTickets().get(1).getTicketCode();

        assertNotNull(code1);
        assertNotNull(code2);
        assertNotEquals(code1, code2, "Hai vé trong cùng một đơn không được trùng mã");
        assertTrue(code1.length() <= 30, "Mã vé phải <= 30 ký tự để phù hợp độ dài cột database (hiện tại: " + code1.length() + ")");
        assertTrue(code2.length() <= 30, "Mã vé phải <= 30 ký tự để phù hợp độ dài cột database (hiện tại: " + code2.length() + ")");
        assertTrue(code1.startsWith("TRAIN-SE1-"));
    }

    @Test
    @DisplayName("Edge Case Test: Đơn vé tàu đã CONFIRMED vẫn xuất vé thành công kể cả khi thời gian giữ chỗ ban đầu đã qua")
    void testConfirmAndIssueTicket_WhenBookingStatusIsConfirmed_DoesNotExpireEvenIfHoldTimePassed() {
        Train train = new Train();
        train.setId(1L);
        train.setTrainCode("SE1");

        TrainBooking booking = new TrainBooking();
        booking.setId(200L);
        booking.setBookingCode("SE-TRN-CONFIRMED");
        booking.setTrain(train);
        booking.setStatus(TrainBookingStatus.CONFIRMED); // Đã xác nhận / thanh toán
        booking.setHoldExpiresAt(LocalDateTime.now().minusMinutes(20)); // Thời gian giữ chỗ ban đầu đã qua
        booking.setTickets(new ArrayList<>());

        when(trainBookingRepository.findByBookingCodeWithDetails("SE-TRN-CONFIRMED")).thenReturn(Optional.of(booking));
        when(trainBookingRepository.save(any(TrainBooking.class))).thenAnswer(i -> i.getArgument(0));

        TrainBooking issued = trainTicketService.confirmAndIssueTicket("SE-TRN-CONFIRMED");

        assertNotNull(issued);
        assertEquals(TrainBookingStatus.ISSUED, issued.getStatus(), "Đơn tàu đã CONFIRMED không được bị EXPIRED khi xuất vé");
        assertNotNull(issued.getConfirmedTime());
    }

    // =========================================================================
    // TEST CA KHỨ HỒI (ROUND TRIP)
    // =========================================================================
    @Test
    @DisplayName("Khứ Hồi: Tạo đơn giữ chỗ tàu khứ hồi thành công với ghế cả 2 chiều và tính tổng tiền chuẩn xác")
    void testCreateHoldBooking_RoundTrip_Success() {
        Train outboundTrain = new Train();
        outboundTrain.setId(1L);
        outboundTrain.setTrainCode("SE1");
        outboundTrain.setDepartureTimeOfDay("06:00");

        Train returnTrain = new Train();
        returnTrain.setId(2L);
        returnTrain.setTrainCode("SE2");
        returnTrain.setDepartureTimeOfDay("06:00");

        TrainStation depStation = new TrainStation("Hà Nội - Sài Gòn", "HAN", "Ga Hà Nội", 0, 0);
        depStation.setId(1L);

        TrainStation arrStation = new TrainStation("Hà Nội - Sài Gòn", "SGO", "Ga Sài Gòn", 10, 1726);
        arrStation.setId(11L);

        TrainCoach coach1 = new TrainCoach();
        coach1.setPricePerKm(new BigDecimal("700"));
        coach1.setCoachName("Toa 1");

        TrainSeat seat1 = new TrainSeat();
        seat1.setId(10L);
        seat1.setSeatLabel("Ghế 01");
        seat1.setCoach(coach1);

        TrainCoach coach2 = new TrainCoach();
        coach2.setPricePerKm(new BigDecimal("700"));
        coach2.setCoachName("Toa 1");

        TrainSeat returnSeat1 = new TrainSeat();
        returnSeat1.setId(20L);
        returnSeat1.setSeatLabel("Ghế 02");
        returnSeat1.setCoach(coach2);

        LocalDate departureDate = LocalDate.now().plusDays(5);
        LocalDate returnDate = departureDate.plusDays(3);

        when(trainRepository.findById(1L)).thenReturn(Optional.of(outboundTrain));
        when(trainRepository.findById(2L)).thenReturn(Optional.of(returnTrain));
        when(trainStationRepository.findById(1L)).thenReturn(Optional.of(depStation));
        when(trainStationRepository.findById(11L)).thenReturn(Optional.of(arrStation));
        when(trainSeatRepository.findById(10L)).thenReturn(Optional.of(seat1));
        when(trainSeatRepository.findById(20L)).thenReturn(Optional.of(returnSeat1));
        when(trainTicketRepository.findActiveTicketsOnDate(any(), any())).thenReturn(Collections.emptyList());
        when(trainBookingRepository.save(any(TrainBooking.class))).thenAnswer(i -> i.getArgument(0));

        com.dulich.ticketmanagement.dto.TrainBookingRequestDto req = new com.dulich.ticketmanagement.dto.TrainBookingRequestDto();
        req.setTrainId(1L);
        req.setDepartureStationId(1L);
        req.setArrivalStationId(11L);
        req.setDepartureDate(departureDate);
        req.setRoundTrip(true);
        req.setReturnTrainId(2L);
        req.setReturnDate(returnDate);
        req.setCustomerName("Nguyễn Văn A");
        req.setCustomerPhone("0912345678");
        req.setCustomerEmail("nguyenvana@gmail.com");

        com.dulich.ticketmanagement.dto.TrainPassengerInputDto p = new com.dulich.ticketmanagement.dto.TrainPassengerInputDto();
        p.setPassengerName("Nguyễn Văn A");
        p.setSeatId(10L);
        p.setReturnSeatId(20L);
        p.setPriorityCategory(PassengerPriority.STUDENT); // Giảm 10% cho cả 2 chiều!
        p.setPriorityDocId("SV-12345");
        req.setPassengers(List.of(p));

        TrainBooking booking = trainTicketService.createHoldBooking(req);

        assertNotNull(booking);
        assertTrue(booking.isRoundTrip());
        assertEquals(returnTrain, booking.getReturnTrain());
        assertEquals(returnDate, booking.getReturnDepartureDate());
        assertEquals(1, booking.getTickets().size());

        TrainTicket ticket = booking.getTickets().get(0);
        assertEquals(seat1, ticket.getSeat());
        assertEquals(returnSeat1, ticket.getReturnSeat());

        // Giá gốc 1 chiều: 1726 km * 700 = 1,208,200 đ
        BigDecimal expectedOneWayFare = new BigDecimal("1208200");
        BigDecimal expectedOneWayDiscount = new BigDecimal("120820"); // 10%
        BigDecimal expectedOneWayFinal = new BigDecimal("1087380");

        assertEquals(expectedOneWayFare, ticket.getOriginalFare());
        assertEquals(expectedOneWayDiscount, ticket.getDiscountAmount());
        assertEquals(expectedOneWayFinal, ticket.getFinalFare());

        assertEquals(expectedOneWayFare, ticket.getReturnOriginalFare());
        assertEquals(expectedOneWayDiscount, ticket.getReturnDiscountAmount());
        assertEquals(expectedOneWayFinal, ticket.getReturnFinalFare());

        // Tổng cả 2 chiều:
        assertEquals(expectedOneWayFare.multiply(new BigDecimal(2)), booking.getTotalOriginalFare());
        assertEquals(expectedOneWayDiscount.multiply(new BigDecimal(2)), booking.getTotalDiscount());
        assertEquals(expectedOneWayFinal.multiply(new BigDecimal(2)), booking.getTotalAmount());
        assertEquals(TrainBookingStatus.HELD, booking.getStatus());
    }

    @Test
    @DisplayName("Khứ Hồi: Ném ngoại lệ khi chỗ ngồi chiều về đã có người khác đặt trong chặng trùng lặp")
    void testCreateHoldBooking_RoundTrip_ReturnSeatConflict_ThrowsException() {
        Train outboundTrain = new Train();
        outboundTrain.setId(1L);
        outboundTrain.setTrainCode("SE1");
        outboundTrain.setDepartureTimeOfDay("06:00");

        Train returnTrain = new Train();
        returnTrain.setId(2L);
        returnTrain.setTrainCode("SE2");
        returnTrain.setDepartureTimeOfDay("06:00");

        TrainStation depStation = new TrainStation("Hà Nội - Sài Gòn", "HAN", "Ga Hà Nội", 0, 0);
        depStation.setId(1L);

        TrainStation arrStation = new TrainStation("Hà Nội - Sài Gòn", "SGO", "Ga Sài Gòn", 10, 1726);
        arrStation.setId(11L);

        TrainCoach coach = new TrainCoach();
        coach.setPricePerKm(new BigDecimal("700"));

        TrainSeat seat1 = new TrainSeat();
        seat1.setId(10L);
        seat1.setSeatLabel("Ghế 01");
        seat1.setCoach(coach);

        TrainSeat returnSeat1 = new TrainSeat();
        returnSeat1.setId(20L);
        returnSeat1.setSeatLabel("Ghế 02");
        returnSeat1.setCoach(coach);

        LocalDate departureDate = LocalDate.now().plusDays(5);
        LocalDate returnDate = departureDate.plusDays(3);

        // Giả lập chỗ chiều về (id 20L) đã có người đặt trên tàu returnTrain vào ngày returnDate
        TrainBooking existingReturnBooking = new TrainBooking();
        existingReturnBooking.setTrain(returnTrain);
        existingReturnBooking.setDepartureDate(returnDate);
        existingReturnBooking.setStatus(TrainBookingStatus.ISSUED);
        existingReturnBooking.setDepartureStation(arrStation); // Chiều về: từ Sài Gòn (index 10)
        existingReturnBooking.setArrivalStation(depStation);    // Đến Hà Nội (index 0)

        TrainTicket existingTicket = new TrainTicket();
        existingTicket.setSeat(returnSeat1);
        existingTicket.setTrainBooking(existingReturnBooking);
        existingTicket.setDepartureStationIndex(arrStation.getStationIndex());
        existingTicket.setArrivalStationIndex(depStation.getStationIndex());

        when(trainRepository.findById(1L)).thenReturn(Optional.of(outboundTrain));
        when(trainRepository.findById(2L)).thenReturn(Optional.of(returnTrain));
        when(trainStationRepository.findById(1L)).thenReturn(Optional.of(depStation));
        when(trainStationRepository.findById(11L)).thenReturn(Optional.of(arrStation));
        when(trainSeatRepository.findById(10L)).thenReturn(Optional.of(seat1));
        when(trainSeatRepository.findById(20L)).thenReturn(Optional.of(returnSeat1));

        // findActiveTicketsOnDate cho tàu đi rỗng, nhưng cho tàu về chứa existingTicket
        when(trainTicketRepository.findActiveTicketsOnDate(1L, departureDate)).thenReturn(Collections.emptyList());
        when(trainTicketRepository.findActiveTicketsOnDate(2L, returnDate)).thenReturn(List.of(existingTicket));

        com.dulich.ticketmanagement.dto.TrainBookingRequestDto req = new com.dulich.ticketmanagement.dto.TrainBookingRequestDto();
        req.setTrainId(1L);
        req.setDepartureStationId(1L);
        req.setArrivalStationId(11L);
        req.setDepartureDate(departureDate);
        req.setRoundTrip(true);
        req.setReturnTrainId(2L);
        req.setReturnDate(returnDate);
        req.setCustomerName("Nguyễn Văn A");
        req.setCustomerPhone("0912345678");
        req.setCustomerEmail("nguyenvana@gmail.com");

        com.dulich.ticketmanagement.dto.TrainPassengerInputDto p = new com.dulich.ticketmanagement.dto.TrainPassengerInputDto();
        p.setPassengerName("Nguyễn Văn A");
        p.setSeatId(10L);
        p.setReturnSeatId(20L);
        p.setPriorityCategory(PassengerPriority.STANDARD);
        req.setPassengers(List.of(p));

        BusinessException ex = assertThrows(BusinessException.class, () -> trainTicketService.createHoldBooking(req));
        assertTrue(ex.getMessage().contains("chiều về") || ex.getMessage().contains("Ghế 02"));
    }

    @Test
    @DisplayName("Khứ Hồi: Xác nhận thanh toán & xuất vé tạo mã vé điện tử cho cả chiều đi và chiều về")
    void testConfirmAndIssueTicket_RoundTrip_IssuesBothOutboundAndReturnTicketCodes() {
        Train train = new Train();
        train.setId(1L);
        train.setTrainCode("SE1");

        Train returnTrain = new Train();
        returnTrain.setId(2L);
        returnTrain.setTrainCode("SE2");

        TrainSeat returnSeat = new TrainSeat();
        returnSeat.setId(20L);
        returnSeat.setSeatLabel("Ghế 02");

        TrainBooking booking = new TrainBooking();
        booking.setId(300L);
        booking.setBookingCode("SE-TRN-ROUND");
        booking.setTrain(train);
        booking.setReturnTrain(returnTrain);
        booking.setIsRoundTrip(true);
        booking.setStatus(TrainBookingStatus.HELD);
        booking.setHoldExpiresAt(LocalDateTime.now().plusMinutes(10));

        TrainTicket ticket = new TrainTicket();
        ticket.setId(50L);
        ticket.setReturnSeat(returnSeat);
        booking.setTickets(new ArrayList<>(List.of(ticket)));

        when(trainBookingRepository.findByBookingCodeWithDetails("SE-TRN-ROUND")).thenReturn(Optional.of(booking));
        when(trainBookingRepository.save(any(TrainBooking.class))).thenAnswer(i -> i.getArgument(0));

        TrainBooking confirmed = trainTicketService.confirmAndIssueTicket("SE-TRN-ROUND");

        assertEquals(TrainBookingStatus.ISSUED, confirmed.getStatus());
        TrainTicket confirmedTicket = confirmed.getTickets().get(0);

        assertNotNull(confirmedTicket.getTicketCode());
        assertNotNull(confirmedTicket.getReturnTicketCode());
        assertTrue(confirmedTicket.getTicketCode().startsWith("TRAIN-SE1-"));
        assertTrue(confirmedTicket.getReturnTicketCode().startsWith("TRAIN-SE2-"));
        assertTrue(confirmedTicket.getTicketCode().length() <= 30);
        assertTrue(confirmedTicket.getReturnTicketCode().length() <= 30);
    }
}
