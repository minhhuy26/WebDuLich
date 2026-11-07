package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.entity.Flight;
import com.dulich.ticketmanagement.entity.FlightBooking;
import com.dulich.ticketmanagement.entity.FlightPassenger;
import com.dulich.ticketmanagement.entity.enums.FlightBookingStatus;
import com.dulich.ticketmanagement.entity.enums.FlightSeatClass;
import com.dulich.ticketmanagement.entity.enums.PassengerType;
import com.dulich.ticketmanagement.exception.BusinessException;
import com.dulich.ticketmanagement.repository.FlightBookingRepository;
import com.dulich.ticketmanagement.repository.FlightPassengerRepository;
import com.dulich.ticketmanagement.repository.FlightRepository;
import com.dulich.ticketmanagement.service.impl.FlightTicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử nghiệp vụ tính giá và hủy vé máy bay (FlightTicketService)")
class FlightTicketServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private FlightBookingRepository flightBookingRepository;

    @Mock
    private FlightPassengerRepository flightPassengerRepository;

    private FlightTicketService flightTicketService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        flightTicketService = new FlightTicketService(flightRepository, flightBookingRepository, flightPassengerRepository);
    }

    // =========================================================================
    // TEST CA 1: Thuật toán tính giá vé theo hệ số thời điểm đặt (Lead Time)
    // =========================================================================
    @Test
    @DisplayName("Ca 1: Tính giá vé theo hệ số thời điểm đặt (>14 ngày: 1.0; 3-14 ngày: 1.1; <3 ngày: 1.3)")
    void testCalculateFareByLeadTime() {
        BigDecimal basePrice = new BigDecimal("1000000"); // 1,000,000 VNĐ
        LocalDateTime bookingTime = LocalDateTime.of(2026, 10, 1, 10, 0);

        // Trường hợp 1: Đặt trước 20 ngày (>14 ngày) -> Hệ số 1.0
        LocalDateTime depOver14Days = bookingTime.plusDays(20);
        BigDecimal fareOver14 = flightTicketService.calculatePassengerBaseFare(
                basePrice, FlightSeatClass.ECONOMY, bookingTime, depOver14Days, PassengerType.ADULT);
        assertEquals(new BigDecimal("1000000"), fareOver14, "Giá vé đặt trước >14 ngày với hệ số 1.0 phải là 1,000,000 đ");

        // Trường hợp 2: Đặt trước 7 ngày (3 đến 14 ngày) -> Hệ số 1.1
        LocalDateTime depBetween3And14 = bookingTime.plusDays(7);
        BigDecimal fare3To14 = flightTicketService.calculatePassengerBaseFare(
                basePrice, FlightSeatClass.ECONOMY, bookingTime, depBetween3And14, PassengerType.ADULT);
        assertEquals(new BigDecimal("1100000"), fare3To14, "Giá vé đặt trước 3-14 ngày với hệ số 1.1 phải là 1,100,000 đ");

        // Trường hợp 3: Đặt trước 1 ngày (<3 ngày) -> Hệ số 1.3
        LocalDateTime depUnder3Days = bookingTime.plusDays(1);
        BigDecimal fareUnder3 = flightTicketService.calculatePassengerBaseFare(
                basePrice, FlightSeatClass.ECONOMY, bookingTime, depUnder3Days, PassengerType.ADULT);
        assertEquals(new BigDecimal("1300000"), fareUnder3, "Giá vé đặt cận ngày (<3 ngày) với hệ số 1.3 phải là 1,300,000 đ");
    }

    // =========================================================================
    // TEST CA 2: Thuật toán tính giá vé theo độ tuổi (Adult 100%, Child 75%, Infant 10%)
    // =========================================================================
    @Test
    @DisplayName("Ca 2: Phân loại giá vé hành khách (Người lớn 100%, Trẻ em 75%, Em bé 10%)")
    void testCalculateFareByPassengerType() {
        BigDecimal basePrice = new BigDecimal("1000000"); // 1,000,000 VNĐ
        LocalDateTime bookingTime = LocalDateTime.of(2026, 10, 1, 10, 0);
        LocalDateTime departureTime = bookingTime.plusDays(20); // >14 ngày -> hệ số lead time 1.0

        // Hạng ghế PREMIUM: Hệ số 1.5 -> Giá người lớn = 1,000,000 * 1.5 * 1.0 = 1,500,000 VNĐ
        BigDecimal adultFare = flightTicketService.calculatePassengerBaseFare(
                basePrice, FlightSeatClass.PREMIUM, bookingTime, departureTime, PassengerType.ADULT);
        assertEquals(new BigDecimal("1500000"), adultFare, "Giá người lớn hạng Premium phải là 1,500,000 đ");

        // Trẻ em (2 - 11 tuổi): 75% giá người lớn -> 1,500,000 * 0.75 = 1,125,000 VNĐ
        BigDecimal childFare = flightTicketService.calculatePassengerBaseFare(
                basePrice, FlightSeatClass.PREMIUM, bookingTime, departureTime, PassengerType.CHILD);
        assertEquals(new BigDecimal("1125000"), childFare, "Giá trẻ em (75%) hạng Premium phải là 1,125,000 đ");

        // Em bé (< 2 tuổi): 10% giá người lớn -> 1,500,000 * 0.10 = 150,000 VNĐ
        BigDecimal infantFare = flightTicketService.calculatePassengerBaseFare(
                basePrice, FlightSeatClass.PREMIUM, bookingTime, departureTime, PassengerType.INFANT);
        assertEquals(new BigDecimal("1500000").multiply(new BigDecimal("0.10")).setScale(0), infantFare, "Giá em bé (10%) hạng Premium phải là 150,000 đ");
        assertEquals(BigDecimal.ZERO, flightTicketService.calculateAirportTax(PassengerType.INFANT), "Em bé không phải nộp phí sân bay");
    }

    // =========================================================================
    // TEST CA 3: Thuật toán tính phí hành lý quá cước theo định mức từng hạng
    // =========================================================================
    @Test
    @DisplayName("Ca 3: Tính phí hành lý quá cước theo định mức (Eco 20kg, Biz 40kg, quá cước 50.000đ/kg)")
    void testCalculateExcessBaggageFee() {
        // Hạng ECONOMY: Miễn cước 20kg
        // Khách mang 20kg -> không quá cước
        BigDecimal ecoFeeNormal = flightTicketService.calculateExcessBaggageFee(FlightSeatClass.ECONOMY, 20);
        assertEquals(BigDecimal.ZERO, ecoFeeNormal, "Mang đúng 20kg hạng Economy không bị tính phí quá cước");

        // Khách mang 25kg -> vượt 5kg -> 5 * 50,000 = 250,000 VNĐ
        BigDecimal ecoFeeExcess = flightTicketService.calculateExcessBaggageFee(FlightSeatClass.ECONOMY, 25);
        assertEquals(new BigDecimal("250000"), ecoFeeExcess, "Mang 25kg hạng Economy vượt 5kg phải tính 250,000 đ");

        // Hạng BUSINESS: Miễn cước 40kg
        // Khách mang 38kg -> không quá cước
        BigDecimal bizFeeNormal = flightTicketService.calculateExcessBaggageFee(FlightSeatClass.BUSINESS, 38);
        assertEquals(BigDecimal.ZERO, bizFeeNormal, "Mang 38kg hạng Business không bị tính phí");

        // Khách mang 46kg -> vượt 6kg -> 6 * 50,000 = 300,000 VNĐ
        BigDecimal bizFeeExcess = flightTicketService.calculateExcessBaggageFee(FlightSeatClass.BUSINESS, 46);
        assertEquals(new BigDecimal("300000"), bizFeeExcess, "Mang 46kg hạng Business vượt 6kg phải tính 300,000 đ");
    }

    // =========================================================================
    // TEST CA 4: Chính sách phí hủy vé & hoàn tiền khi hủy trước giờ bay > 72 giờ
    // =========================================================================
    @Test
    @DisplayName("Ca 4: Hủy vé trước giờ bay > 72 giờ (Phí hủy 10%, Hoàn tiền 90%)")
    void testCancelFlightMoreThan72Hours() {
        BigDecimal totalAmount = new BigDecimal("2000000"); // Đơn vé 2,000,000 VNĐ
        LocalDateTime cancelTime = LocalDateTime.of(2026, 11, 1, 8, 0);
        LocalDateTime departureTime = cancelTime.plusHours(96); // 96 giờ trước giờ bay (>72h)

        // Phí hủy: 10% * 2,000,000 = 200,000 VNĐ
        BigDecimal cancelFee = flightTicketService.calculateCancellationFee(totalAmount, cancelTime, departureTime);
        assertEquals(new BigDecimal("200000"), cancelFee, "Phí hủy trước >72h phải là 10% (200,000 đ)");

        // Hoàn lại: 90% * 2,000,000 = 1,800,000 VNĐ
        BigDecimal refund = flightTicketService.calculateRefundAmount(totalAmount, cancelTime, departureTime);
        assertEquals(new BigDecimal("1800000"), refund, "Tiền hoàn lại trước >72h phải là 90% (1,800,000 đ)");
    }

    // =========================================================================
    // TEST CA 5: Chính sách phí hủy vé trong khung 24-72h, dưới 24h và sau giờ bay
    // =========================================================================
    @Test
    @DisplayName("Ca 5: Hủy vé trong các khung 24-72h (phí 30%), <24h (phí 70%), và sau giờ bay (phí 100%)")
    void testCancelFlightOtherTimeframes() {
        BigDecimal totalAmount = new BigDecimal("1000000"); // Đơn vé 1,000,000 VNĐ
        LocalDateTime cancelTime = LocalDateTime.of(2026, 11, 1, 8, 0);

        // Khung 1: Hủy trước 48 giờ (trong khoảng 24h - 72h) -> Phí 30%, hoàn 70%
        LocalDateTime dep48h = cancelTime.plusHours(48);
        BigDecimal fee48h = flightTicketService.calculateCancellationFee(totalAmount, cancelTime, dep48h);
        BigDecimal refund48h = flightTicketService.calculateRefundAmount(totalAmount, cancelTime, dep48h);
        assertEquals(new BigDecimal("300000"), fee48h, "Phí hủy trong khung 24-72h là 30% (300,000 đ)");
        assertEquals(new BigDecimal("700000"), refund48h, "Hoàn lại trong khung 24-72h là 70% (700,000 đ)");

        // Khung 2: Hủy trước 12 giờ (<24h) -> Phí 70%, hoàn 30%
        LocalDateTime dep12h = cancelTime.plusHours(12);
        BigDecimal fee12h = flightTicketService.calculateCancellationFee(totalAmount, cancelTime, dep12h);
        BigDecimal refund12h = flightTicketService.calculateRefundAmount(totalAmount, cancelTime, dep12h);
        assertEquals(new BigDecimal("700000"), fee12h, "Phí hủy dưới 24h là 70% (700,000 đ)");
        assertEquals(new BigDecimal("300000"), refund12h, "Hoàn lại dưới 24h là 30% (300,000 đ)");

        // Khung 3: Chuyến bay đã cất cánh (sau giờ bay) -> Phí 100%, hoàn 0đ
        LocalDateTime depPast = cancelTime.minusHours(2);
        BigDecimal feePast = flightTicketService.calculateCancellationFee(totalAmount, cancelTime, depPast);
        BigDecimal refundPast = flightTicketService.calculateRefundAmount(totalAmount, cancelTime, depPast);
        assertEquals(new BigDecimal("1000000"), feePast, "Hủy sau giờ bay phí 100% (1,000,000 đ)");
        assertEquals(BigDecimal.ZERO, refundPast, "Hủy sau giờ bay không hoàn tiền (0 đ)");
    }

    // =========================================================================
    // TEST CA TÁI HIỆN LỖI 1: Khách xác nhận vé trễ sau khi hết hạn giữ chỗ -> Phải hoàn trả ghế
    // =========================================================================
    @Test
    @DisplayName("Lỗi 1: Xác nhận vé sau khi hết hạn giữ chỗ phải phục hồi số ghế trống cho chuyến bay")
    void testLateConfirmation_MustRestoreSeatsToFlight() {
        Flight flight = new Flight();
        flight.setEconomySeatsTotal(150);
        flight.setEconomySeatsAvailable(148); // Đã bị trừ 2 ghế

        FlightBooking booking = new FlightBooking();
        booking.setBookingCode("VN-AIR-TEST1");
        booking.setFlight(flight);
        booking.setSeatClass(FlightSeatClass.ECONOMY);
        booking.setStatus(FlightBookingStatus.HELD);
        booking.setHoldExpiresAt(LocalDateTime.now().minusMinutes(5)); // Đã quá hạn 5 phút

        FlightPassenger p1 = new FlightPassenger();
        p1.setPassengerType(PassengerType.ADULT);
        booking.addPassenger(p1);

        FlightPassenger p2 = new FlightPassenger();
        p2.setPassengerType(PassengerType.ADULT);
        booking.addPassenger(p2);

        when(flightBookingRepository.findByBookingCodeWithDetails("VN-AIR-TEST1")).thenReturn(Optional.of(booking));
        when(flightBookingRepository.save(any(FlightBooking.class))).thenAnswer(i -> i.getArgument(0));
        when(flightRepository.save(any(Flight.class))).thenAnswer(i -> i.getArgument(0));

        assertThrows(BusinessException.class, () -> flightTicketService.confirmAndIssueTicket("VN-AIR-TEST1"));

        // LỖI 1 TÁI HIỆN: Hiện tại code không hoàn ghế nên available vẫn là 148 thay vì được phục hồi về 150
        assertEquals(150, flight.getAvailableSeats(FlightSeatClass.ECONOMY), "Số ghế trống phải được phục hồi về 150 khi đơn hết hạn");
    }

    // =========================================================================
    // TEST CA TÁI HIỆN LỖI 4: Hủy đơn HELD chưa thanh toán -> Hoàn tiền = 0đ, Phí hủy = 0đ
    // =========================================================================
    @Test
    @DisplayName("Lỗi 4: Hủy đơn HELD (chưa thanh toán) phải có tiền hoàn = 0đ và phí = 0đ")
    void testCancelBooking_HeldStatus_MustNotRefundMoney() {
        Flight flight = new Flight();
        flight.setEconomySeatsTotal(150);
        flight.setEconomySeatsAvailable(149);
        flight.setDepartureTime(LocalDateTime.now().plusDays(5));

        FlightBooking booking = new FlightBooking();
        booking.setBookingCode("VN-AIR-HELD");
        booking.setFlight(flight);
        booking.setSeatClass(FlightSeatClass.ECONOMY);
        booking.setStatus(FlightBookingStatus.HELD);
        booking.setTotalAmount(new BigDecimal("2000000"));

        FlightPassenger p = new FlightPassenger();
        p.setPassengerType(PassengerType.ADULT);
        booking.addPassenger(p);

        when(flightBookingRepository.findByBookingCodeWithDetails("VN-AIR-HELD")).thenReturn(Optional.of(booking));
        when(flightBookingRepository.save(any(FlightBooking.class))).thenAnswer(i -> i.getArgument(0));
        when(flightRepository.save(any(Flight.class))).thenAnswer(i -> i.getArgument(0));

        FlightBooking cancelled = flightTicketService.cancelBooking("VN-AIR-HELD", "Khách đổi ý");

        // LỖI 4 TÁI HIỆN: Trước khi sửa, hệ thống hoàn tiền 1,800,000đ cho đơn chưa thanh toán
        assertEquals(BigDecimal.ZERO, cancelled.getRefundAmount(), "Đơn HELD chưa thanh toán không được hoàn tiền ma");
        assertEquals(BigDecimal.ZERO, cancelled.getCancellationFee(), "Đơn HELD hủy trong hạn không tính phí hủy");
        assertEquals(FlightBookingStatus.CANCELLED, cancelled.getStatus());
        assertEquals(150, flight.getAvailableSeats(FlightSeatClass.ECONOMY), "Ghế phải được nhả lại cho chuyến bay");
    }

    // =========================================================================
    // TEST CA TÁI HIỆN LỖI 5: Hủy đơn EXPIRED (đã hết hạn) -> Phải ném ngoại lệ từ chối hủy
    // =========================================================================
    @Test
    @DisplayName("Lỗi 5: Đơn EXPIRED đã hết hạn giữ chỗ không được phép hủy lại (tránh double restore ghế)")
    void testCancelBooking_ExpiredStatus_MustThrowException() {
        Flight flight = new Flight();
        flight.setEconomySeatsTotal(150);
        flight.setEconomySeatsAvailable(150);
        flight.setDepartureTime(LocalDateTime.now().plusDays(5));

        FlightBooking booking = new FlightBooking();
        booking.setBookingCode("VN-AIR-EXPIRED");
        booking.setFlight(flight);
        booking.setSeatClass(FlightSeatClass.ECONOMY);
        booking.setStatus(FlightBookingStatus.EXPIRED);
        booking.setTotalAmount(new BigDecimal("2000000"));

        when(flightBookingRepository.findByBookingCodeWithDetails("VN-AIR-EXPIRED")).thenReturn(Optional.of(booking));

        // LỖI 5 TÁI HIỆN: Trước khi sửa, đơn EXPIRED bị hủy và cộng thêm ghế lần 2 (150 -> 151)
        BusinessException ex = assertThrows(BusinessException.class, () -> flightTicketService.cancelBooking("VN-AIR-EXPIRED", "Hủy"));
        assertTrue(ex.getMessage().contains("hết hạn"), "Phải thông báo đơn giữ chỗ đã hết hạn");
    }

    // =========================================================================
    // TEST CA TÁI HIỆN LỖI 9: Hủy vé cận giờ (< 1 giờ trước khi bay) -> Phải cho phép hủy với phí 70%
    // =========================================================================
    @Test
    @DisplayName("Lỗi 9: Hủy vé trước giờ bay 30 phút (<1h) phải cho phép hủy với phí 70%, không được báo đã cất cánh")
    void testCancelBooking_Within30MinutesBeforeDeparture_ShouldAllowCancellation() {
        Flight flight = new Flight();
        flight.setEconomySeatsTotal(150);
        flight.setEconomySeatsAvailable(149);
        // Chuyến bay khởi hành sau 30 phút nữa (chưa cất cánh!)
        flight.setDepartureTime(LocalDateTime.now().plusMinutes(30));

        FlightBooking booking = new FlightBooking();
        booking.setBookingCode("VN-AIR-NEAR");
        booking.setFlight(flight);
        booking.setSeatClass(FlightSeatClass.ECONOMY);
        booking.setStatus(FlightBookingStatus.ISSUED);
        booking.setTotalAmount(new BigDecimal("1000000"));

        FlightPassenger p = new FlightPassenger();
        p.setPassengerType(PassengerType.ADULT);
        booking.addPassenger(p);

        when(flightBookingRepository.findByBookingCodeWithDetails("VN-AIR-NEAR")).thenReturn(Optional.of(booking));
        when(flightBookingRepository.save(any(FlightBooking.class))).thenAnswer(i -> i.getArgument(0));
        when(flightRepository.save(any(Flight.class))).thenAnswer(i -> i.getArgument(0));

        // LỖI 9 TÁI HIỆN: Trước khi sửa, Duration.toHours() <= 0 khiến hệ thống văng ngoại lệ "Chuyến bay đã cất cánh"
        FlightBooking cancelled = flightTicketService.cancelBooking("VN-AIR-NEAR", "Bận việc đột xuất");

        assertEquals(new BigDecimal("700000"), cancelled.getCancellationFee(), "Phí hủy trước giờ bay < 24h là 70%");
        assertEquals(new BigDecimal("300000"), cancelled.getRefundAmount(), "Hoàn lại 30% khi hủy trước giờ bay");
        assertEquals(FlightBookingStatus.CANCELLED, cancelled.getStatus());
    }

    // =========================================================================
    // TEST CA TÁI HIỆN LỖI 6: Race Condition khi nhiều luồng cùng đặt 1 ghế máy bay
    // =========================================================================
    @Test
    @DisplayName("Lỗi 6: Race Condition đặt trùng ghế - Chạy 2 luồng cùng đặt ghế 12A, CHỈ 1 luồng được thành công")
    void testConcurrentSeatBooking_RaceCondition_OnlyOneThreadSucceeds() throws Exception {
        Flight flight = new Flight();
        flight.setId(1L);
        flight.setFlightNumber("VN-999");
        flight.setBasePrice(new BigDecimal("1000000"));
        flight.setDepartureTime(LocalDateTime.now().plusDays(5));
        flight.setEconomySeatsTotal(150);
        flight.setEconomySeatsAvailable(150);

        java.util.Set<String> sharedOccupiedSeats = java.util.Collections.synchronizedSet(new java.util.HashSet<>());

        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(flightPassengerRepository.findOccupiedSeatNumbers(1L)).thenAnswer(i -> new java.util.HashSet<>(sharedOccupiedSeats));

        when(flightBookingRepository.save(any(FlightBooking.class))).thenAnswer(invocation -> {
            FlightBooking b = invocation.getArgument(0);
            // Giả lập độ trễ I/O database 50ms để kích hoạt race condition
            Thread.sleep(50);
            for (FlightPassenger p : b.getPassengers()) {
                if (p.getSeatNumber() != null) {
                    sharedOccupiedSeats.add(p.getSeatNumber());
                }
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

                    com.dulich.ticketmanagement.dto.FlightBookingRequestDto req = new com.dulich.ticketmanagement.dto.FlightBookingRequestDto();
                    req.setFlightId(1L);
                    req.setCustomerName("Khách Hàng " + index);
                    req.setCustomerPhone("091234567" + index);
                    req.setCustomerEmail("khach" + index + "@test.com");
                    req.setSeatClass(FlightSeatClass.ECONOMY);

                    com.dulich.ticketmanagement.dto.PassengerInputDto p = new com.dulich.ticketmanagement.dto.PassengerInputDto();
                    p.setFullName("Hành Khách " + index);
                    p.setIdCard("01234567890" + index);
                    p.setPassengerType(PassengerType.ADULT);
                    p.setSeatNumber("12A"); // CẢ 2 LUỒNG CÙNG ĐẶT GHẾ 12A!

                    req.setPassengers(java.util.List.of(p));

                    flightTicketService.createHoldBooking(req);
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

        startLatch.countDown(); // Bắn pháo lệnh cho các luồng chạy
        doneLatch.await();
        executor.shutdown();

        // LỖI 6 TÁI HIỆN: Trước khi sửa cơ chế đồng bộ/khóa, cả 2 luồng đều kiểm tra lúc ghế trống và cùng thành công (2 thành công, 0 thất bại)
        assertEquals(1, successCount.get(), "Chỉ đúng 1 luồng được phép đặt ghế 12A thành công!");
        assertEquals(1, failCount.get(), "Luồng còn lại phải thất bại với BusinessException do trùng ghế!");
    }

    @Test
    @DisplayName("Bug 11 Test: Đổi chuyến bay phải cập nhật giá hành khách, tính lại phí hành lý theo hạng ghế mới và cộng phí đổi cố định vào tổng tiền")
    void testBug11_ChangeFlight_UpdatesPassengerPricesRecalculatesBaggageIncludesChangeFee() {
        Flight oldFlight = new Flight();
        oldFlight.setId(1L);
        oldFlight.setFlightNumber("VN101");
        oldFlight.setBasePrice(new BigDecimal("1000000"));
        oldFlight.setDepartureTime(LocalDateTime.now().plusDays(3));
        oldFlight.setEconomySeatsAvailable(50);
        oldFlight.setBusinessSeatsAvailable(10);

        Flight newFlight = new Flight();
        newFlight.setId(2L);
        newFlight.setFlightNumber("VN202");
        newFlight.setBasePrice(new BigDecimal("2000000"));
        newFlight.setDepartureTime(LocalDateTime.now().plusDays(20));
        newFlight.setEconomySeatsAvailable(50);
        newFlight.setBusinessSeatsAvailable(10);

        FlightPassenger p = new FlightPassenger();
        p.setId(101L);
        p.setFullName("Nguyen Van Test");
        p.setPassengerType(PassengerType.ADULT);
        p.setBaggageKg(25); // 25kg: trong Economy (miễn 20kg) vượt 5kg -> 250.000; trong Business (miễn 40kg) vượt 0kg -> 0d
        p.setBaseFare(new BigDecimal("1000000"));
        p.setAirportTax(new BigDecimal("100000"));
        p.setExcessBaggageFee(new BigDecimal("250000"));
        p.setFinalPrice(new BigDecimal("1350000"));
        p.setSeatNumber("15A");

        FlightBooking oldBooking = new FlightBooking();
        oldBooking.setId(1L);
        oldBooking.setBookingCode("VN-AIR-TEST11");
        oldBooking.setFlight(oldFlight);
        oldBooking.setStatus(FlightBookingStatus.CONFIRMED);
        oldBooking.setSeatClass(FlightSeatClass.ECONOMY);
        oldBooking.setPassengerCount(1);
        oldBooking.setTotalFare(new BigDecimal("1000000"));
        oldBooking.setTotalTaxFee(new BigDecimal("100000"));
        oldBooking.setTotalBaggageFee(new BigDecimal("250000"));
        oldBooking.setTotalAmount(new BigDecimal("1350000"));
        p.setFlightBooking(oldBooking);
        oldBooking.setPassengers(new java.util.ArrayList<>(java.util.List.of(p)));

        when(flightBookingRepository.findByBookingCodeWithDetails("VN-AIR-TEST11")).thenReturn(Optional.of(oldBooking));
        when(flightRepository.findById(2L)).thenReturn(Optional.of(newFlight));
        when(flightBookingRepository.save(any(FlightBooking.class))).thenAnswer(i -> i.getArgument(0));

        com.dulich.ticketmanagement.dto.ChangeFlightRequestDto req = new com.dulich.ticketmanagement.dto.ChangeFlightRequestDto();
        req.setNewFlightId(2L);
        req.setNewSeatClass(FlightSeatClass.BUSINESS);
        req.setNewSeatNumbers(java.util.List.of("1A"));

        FlightBooking changed = flightTicketService.changeFlight("VN-AIR-TEST11", req);

        // 1. Phí đổi cố định 350.000 phải được cộng vào tổng tiền booking
        // Giá vé Business: 2.000.000 * 2.5 * 1.0 (trước 5 ngày = 1.0) = 5.000.000
        // Thuế: 100.000
        // Hành lý mới (Business miễn cước 40kg, khách mang 25kg): 0 đ
        // Phí đổi cố định: 350.000 đ
        // Tổng tiền mới mong đợi = 5.000.000 + 100.000 + 0 + 350.000 = 5.450.000
        assertEquals(0, changed.getTotalFare().compareTo(new BigDecimal("5000000")));
        assertEquals(0, changed.getTotalBaggageFee().compareTo(BigDecimal.ZERO), "Hành lý phải được tính lại theo hạn mức hạng Business (miễn cước 40kg)");
        assertEquals(0, changed.getTotalAmount().compareTo(new BigDecimal("5450000")), "Tổng tiền phải bao gồm phí đổi cố định 350.000 VNĐ");

        // 2. Thông tin giá trên entity hành khách phải được cập nhật
        FlightPassenger updatedP = changed.getPassengers().get(0);
        assertEquals(0, updatedP.getBaseFare().compareTo(new BigDecimal("5000000")), "baseFare của hành khách phải được cập nhật sang giá chuyến mới");
        assertEquals(0, updatedP.getExcessBaggageFee().compareTo(BigDecimal.ZERO), "excessBaggageFee của hành khách phải được tính lại");
        assertEquals(0, updatedP.getFinalPrice().compareTo(new BigDecimal("5100000")), "finalPrice của hành khách phải được cập nhật (fare + tax + baggage)");

        // 3. Đơn đã đổi chuyến (trạng thái CHANGED) phải được phép đổi tiếp nếu có nhu cầu
        assertEquals(FlightBookingStatus.CHANGED, changed.getStatus());
        // Thử đổi tiếp khi đã ở trạng thái CHANGED
        when(flightRepository.findById(1L)).thenReturn(Optional.of(oldFlight));
        com.dulich.ticketmanagement.dto.ChangeFlightRequestDto req2 = new com.dulich.ticketmanagement.dto.ChangeFlightRequestDto();
        req2.setNewFlightId(1L);
        req2.setNewSeatClass(FlightSeatClass.ECONOMY);
        assertDoesNotThrow(() -> flightTicketService.changeFlight("VN-AIR-TEST11", req2),
                "Đơn có trạng thái CHANGED phải được phép đổi chuyến tiếp");
    }

    @Test
    @DisplayName("Bug 17 Test: Phát hành vé máy bay sinh mã vé điện tử duy nhất, không trùng lặp và độ dài <= 30 ký tự")
    void testBug17_IssueTicket_GeneratesUniqueTicketCodesWithoutCollisions() {
        Flight flight = new Flight();
        flight.setId(1L);
        flight.setFlightNumber("VN123");

        FlightBooking booking = new FlightBooking();
        booking.setId(100L);
        booking.setBookingCode("VN-AIR-100");
        booking.setFlight(flight);
        booking.setStatus(FlightBookingStatus.HELD);
        booking.setHoldExpiresAt(LocalDateTime.now().plusMinutes(15));

        FlightPassenger p1 = new FlightPassenger();
        p1.setId(1L);
        FlightPassenger p2 = new FlightPassenger();
        p2.setId(2L);
        booking.setPassengers(new java.util.ArrayList<>(java.util.List.of(p1, p2)));

        when(flightBookingRepository.findByBookingCodeWithDetails("VN-AIR-100")).thenReturn(Optional.of(booking));
        when(flightBookingRepository.save(any(FlightBooking.class))).thenAnswer(i -> i.getArgument(0));

        FlightBooking issued = flightTicketService.confirmAndIssueTicket("VN-AIR-100");

        assertNotNull(issued);
        assertEquals(FlightBookingStatus.ISSUED, issued.getStatus());

        String code1 = issued.getPassengers().get(0).getTicketCode();
        String code2 = issued.getPassengers().get(1).getTicketCode();

        assertNotNull(code1);
        assertNotNull(code2);
        assertNotEquals(code1, code2, "Hai vé máy bay trong cùng một đơn không được trùng mã");
        assertTrue(code1.length() <= 30, "Mã vé máy bay phải <= 30 ký tự (hiện tại: " + code1.length() + ")");
        assertTrue(code2.length() <= 30, "Mã vé máy bay phải <= 30 ký tự (hiện tại: " + code2.length() + ")");
        assertTrue(code1.startsWith("ETK-VN123-"));
    }

    @Test
    @DisplayName("Bug 21 Test: Scheduler nhả ghế hết hạn khi gặp một đơn lỗi dữ liệu không được làm gián đoạn các đơn còn lại")
    void testBug21_ReleaseExpiredHoldBookings_OneFaultyBookingDoesNotAbortOthers() {
        Flight flight = new Flight();
        flight.setId(1L);
        flight.setEconomySeatsAvailable(10);

        // Đơn 1: Hợp lệ
        FlightBooking b1 = new FlightBooking();
        b1.setId(1L);
        b1.setBookingCode("VN-AIR-EX1");
        b1.setFlight(flight);
        b1.setSeatClass(FlightSeatClass.ECONOMY);
        b1.setStatus(FlightBookingStatus.HELD);
        b1.setPassengers(new java.util.ArrayList<>());

        // Đơn 2: Bị lỗi dữ liệu (flight == null làm null pointer exception nếu không try-catch)
        FlightBooking b2 = new FlightBooking();
        b2.setId(2L);
        b2.setBookingCode("VN-AIR-FAULTY");
        b2.setFlight(null); // flight null
        b2.setSeatClass(FlightSeatClass.ECONOMY);
        b2.setStatus(FlightBookingStatus.HELD);
        b2.setPassengers(new java.util.ArrayList<>());

        // Đơn 3: Hợp lệ đứng sau đơn lỗi
        FlightBooking b3 = new FlightBooking();
        b3.setId(3L);
        b3.setBookingCode("VN-AIR-EX3");
        b3.setFlight(flight);
        b3.setSeatClass(FlightSeatClass.ECONOMY);
        b3.setStatus(FlightBookingStatus.HELD);
        b3.setPassengers(new java.util.ArrayList<>());

        when(flightBookingRepository.findExpiredBookings(eq(FlightBookingStatus.HELD), any(LocalDateTime.class)))
                .thenReturn(java.util.List.of(b1, b2, b3));
        when(flightBookingRepository.save(b2)).thenThrow(new RuntimeException("Lỗi CSDL khi lưu đơn b2"));

        // Trước khi sửa: b2 gây NullPointerException làm dừng vòng lặp, b3 không được xử lý
        int processedCount = flightTicketService.releaseExpiredHoldBookings();

        assertEquals(2, processedCount, "Phải nhả ghế thành công cho 2 đơn hợp lệ b1 và b3 dù b2 bị lỗi");
        assertEquals(FlightBookingStatus.EXPIRED, b1.getStatus());
        assertEquals(FlightBookingStatus.EXPIRED, b3.getStatus());
    }

    @Test
    @DisplayName("Edge Case Test: Đơn vé đã CONFIRMED vẫn xuất vé thành công kể cả khi thời gian giữ chỗ ban đầu đã qua")
    void testConfirmAndIssueTicket_WhenBookingStatusIsConfirmed_DoesNotExpireEvenIfHoldTimePassed() {
        Flight flight = new Flight();
        flight.setId(1L);
        flight.setFlightNumber("VN123");

        FlightBooking booking = new FlightBooking();
        booking.setId(200L);
        booking.setBookingCode("VN-AIR-CONFIRMED");
        booking.setFlight(flight);
        booking.setStatus(FlightBookingStatus.CONFIRMED); // Đã thanh toán / xác nhận
        booking.setHoldExpiresAt(LocalDateTime.now().minusMinutes(20)); // Thời gian giữ chỗ 15p ban đầu đã qua
        booking.setPassengers(new java.util.ArrayList<>());

        when(flightBookingRepository.findByBookingCodeWithDetails("VN-AIR-CONFIRMED")).thenReturn(Optional.of(booking));
        when(flightBookingRepository.save(any(FlightBooking.class))).thenAnswer(i -> i.getArgument(0));

        FlightBooking issued = flightTicketService.confirmAndIssueTicket("VN-AIR-CONFIRMED");

        assertNotNull(issued);
        assertEquals(FlightBookingStatus.ISSUED, issued.getStatus(), "Đơn đã CONFIRMED không được bị EXPIRED khi xuất vé");
        assertNotNull(issued.getConfirmedTime());
    }

    // =========================================================================
    // ROUND-TRIP TESTS (VÉ MÁY BAY KHỨ HỒI)
    // =========================================================================
    @Test
    @DisplayName("Nghiệp vụ vé khứ hồi: Tính tổng tiền 2 chặng, trừ ghế cả 2 chuyến và cấp mã vé 2 chiều")
    void testCreateHoldBooking_RoundTrip_Success() {
        Flight outboundFlight = new Flight();
        outboundFlight.setId(10L);
        outboundFlight.setFlightNumber("VN215");
        outboundFlight.setBasePrice(new BigDecimal("1000000"));
        outboundFlight.setDepartureTime(LocalDateTime.now().plusDays(10));
        outboundFlight.setEconomySeatsAvailable(50);

        Flight returnFlight = new Flight();
        returnFlight.setId(20L);
        returnFlight.setFlightNumber("VN216");
        returnFlight.setBasePrice(new BigDecimal("1200000"));
        returnFlight.setDepartureTime(LocalDateTime.now().plusDays(20));
        returnFlight.setEconomySeatsAvailable(40);

        when(flightRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(outboundFlight));
        when(flightRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(returnFlight));
        when(flightBookingRepository.save(any(FlightBooking.class))).thenAnswer(i -> i.getArgument(0));

        com.dulich.ticketmanagement.dto.FlightBookingRequestDto request = new com.dulich.ticketmanagement.dto.FlightBookingRequestDto();
        request.setFlightId(10L);
        request.setReturnFlightId(20L);
        request.setRoundTrip(true);
        request.setCustomerName("Nguyen Van A");
        request.setCustomerPhone("0912345678");
        request.setCustomerEmail("customer@gmail.com");
        request.setSeatClass(FlightSeatClass.ECONOMY);
        request.setReturnSeatClass(FlightSeatClass.ECONOMY);

        com.dulich.ticketmanagement.dto.PassengerInputDto p = new com.dulich.ticketmanagement.dto.PassengerInputDto();
        p.setFullName("Nguyen Van A");
        p.setIdCard("001200001234");
        p.setPassengerType(PassengerType.ADULT);
        p.setSeatNumber("12A");
        p.setReturnSeatNumber("14B");
        p.setBaggageKg(0);
        request.setPassengers(java.util.List.of(p));

        FlightBooking booking = flightTicketService.createHoldBooking(request);

        assertNotNull(booking);
        assertTrue(booking.isRoundTrip());
        assertEquals(outboundFlight, booking.getFlight());
        assertEquals(returnFlight, booking.getReturnFlight());
        assertEquals(49, outboundFlight.getEconomySeatsAvailable(), "Chuyến đi phải bị trừ 1 ghế");
        assertEquals(39, returnFlight.getEconomySeatsAvailable(), "Chuyến về phải bị trừ 1 ghế");

        // Outbound base fare (1,000,000 * 1.0 (economy) * 1.1 (10 days) * 1.0 (adult) = 1,100,000)
        // Return base fare (1,200,000 * 1.0 (economy) * 1.0 (15 days) * 1.0 (adult) = 1,200,000)
        // Total fare = 2,300,000
        // Airport tax = 120,000 * 2 = 240,000
        // Total amount = 2,540,000
        assertEquals(new BigDecimal("2300000"), booking.getTotalFare());
        assertEquals(new BigDecimal("240000"), booking.getTotalTaxFee());
        assertEquals(new BigDecimal("2540000"), booking.getTotalAmount());
        assertEquals("12A", booking.getPassengers().get(0).getSeatNumber());
        assertEquals("14B", booking.getPassengers().get(0).getReturnSeatNumber());
    }

    @Test
    @DisplayName("Nghiệp vụ vé khứ hồi: Hủy vé hoàn trả ghế trên CẢ chuyến bay đi và chuyến bay về")
    void testCancelBooking_RoundTrip_RestoresBothFlightsSeats() {
        Flight outboundFlight = new Flight();
        outboundFlight.setId(10L);
        outboundFlight.setFlightNumber("VN215");
        outboundFlight.setDepartureTime(LocalDateTime.now().plusDays(5));
        outboundFlight.setEconomySeatsAvailable(10);

        Flight returnFlight = new Flight();
        returnFlight.setId(20L);
        returnFlight.setFlightNumber("VN216");
        returnFlight.setDepartureTime(LocalDateTime.now().plusDays(10));
        returnFlight.setEconomySeatsAvailable(15);

        FlightBooking booking = new FlightBooking();
        booking.setId(300L);
        booking.setBookingCode("VN-AIR-ROUND");
        booking.setFlight(outboundFlight);
        booking.setReturnFlight(returnFlight);
        booking.setIsRoundTrip(true);
        booking.setSeatClass(FlightSeatClass.ECONOMY);
        booking.setReturnSeatClass(FlightSeatClass.ECONOMY);
        booking.setStatus(FlightBookingStatus.HELD);
        booking.setTotalAmount(new BigDecimal("2540000"));

        FlightPassenger p = new FlightPassenger();
        p.setPassengerType(PassengerType.ADULT);
        booking.addPassenger(p);

        when(flightBookingRepository.findByBookingCodeWithDetails("VN-AIR-ROUND")).thenReturn(Optional.of(booking));
        when(flightBookingRepository.save(any(FlightBooking.class))).thenAnswer(i -> i.getArgument(0));

        FlightBooking cancelled = flightTicketService.cancelBooking("VN-AIR-ROUND", "Hủy vé thử nghiệm");

        assertEquals(FlightBookingStatus.CANCELLED, cancelled.getStatus());
        assertEquals(11, outboundFlight.getEconomySeatsAvailable(), "Chuyến đi phải được hoàn trả 1 ghế");
        assertEquals(16, returnFlight.getEconomySeatsAvailable(), "Chuyến về phải được hoàn trả 1 ghế");
    }
}
