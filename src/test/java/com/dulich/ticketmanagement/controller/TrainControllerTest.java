package com.dulich.ticketmanagement.controller;

import com.dulich.ticketmanagement.dto.TrainSearchDto;
import com.dulich.ticketmanagement.entity.Train;
import com.dulich.ticketmanagement.entity.TrainStation;
import com.dulich.ticketmanagement.exception.BusinessException;
import com.dulich.ticketmanagement.repository.*;
import com.dulich.ticketmanagement.service.impl.TrainTicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("Kiểm thử TrainController")
class TrainControllerTest {

    private TrainController trainController;
    private TrainRepository trainRepository;
    private TrainStationRepository trainStationRepository;
    private TrainCoachRepository trainCoachRepository;
    private TrainSeatRepository trainSeatRepository;
    private TrainBookingRepository trainBookingRepository;
    private UserRepository userRepository;
    private TrainTicketService trainTicketService;
    private com.dulich.ticketmanagement.service.EmailService emailService;

    @BeforeEach
    void setUp() {
        trainRepository = Mockito.mock(TrainRepository.class);
        trainStationRepository = Mockito.mock(TrainStationRepository.class);
        trainCoachRepository = Mockito.mock(TrainCoachRepository.class);
        trainSeatRepository = Mockito.mock(TrainSeatRepository.class);
        trainBookingRepository = Mockito.mock(TrainBookingRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        trainTicketService = Mockito.mock(TrainTicketService.class);
        emailService = Mockito.mock(com.dulich.ticketmanagement.service.EmailService.class);

        trainController = new TrainController(
                trainTicketService,
                trainRepository,
                trainStationRepository,
                trainCoachRepository,
                trainSeatRepository,
                trainBookingRepository,
                userRepository,
                emailService
        );
    }

    @Test
    @DisplayName("Bug 20 Test: showBookingForm với trainId không tồn tại phải ném BusinessException thay vì NoSuchElementException")
    void testBug20_ShowBookingForm_InvalidTrainId_ThrowsBusinessException() {
        when(trainRepository.findById(999L)).thenReturn(Optional.empty());

        Model model = new ExtendedModelMap();
        BusinessException ex = assertThrows(BusinessException.class, () ->
                trainController.showBookingForm(999L, 1L, 2L, "2026-11-01", List.of(10L), null, null, null, model)
        );
        assertTrue(ex.getMessage().contains("Không tìm thấy thông tin đoàn tàu"));
    }

    @Test
    @DisplayName("Bug 20 Test: showBookingForm với ngày sai định dạng phải ném BusinessException thay vì DateTimeParseException")
    void testBug20_ShowBookingForm_InvalidDateFormat_ThrowsBusinessException() {
        when(trainRepository.findById(1L)).thenReturn(Optional.of(new Train()));
        when(trainStationRepository.findById(1L)).thenReturn(Optional.of(new TrainStation()));
        when(trainStationRepository.findById(2L)).thenReturn(Optional.of(new TrainStation()));

        Model model = new ExtendedModelMap();
        BusinessException ex = assertThrows(BusinessException.class, () ->
                trainController.showBookingForm(1L, 1L, 2L, "invalid-date", List.of(10L), null, null, null, model)
        );
        assertTrue(ex.getMessage().contains("Định dạng ngày khởi hành không hợp lệ"));
    }

    @Test
    @DisplayName("Test: listAllTrainBookings khi khách vãng lai chưa đăng nhập và chưa tra cứu thì không hiển thị đơn")
    void testListAllTrainBookings_WhenAnonymousWithoutSearch_ReturnsEmptyBookings() {
        Model model = new ExtendedModelMap();
        String view = trainController.listAllTrainBookings(null, model, null);
        assertEquals("train/train_booking_list", view);
        assertEquals(false, model.getAttribute("isManager"));
        assertEquals(false, model.getAttribute("hasSearched"));
        @SuppressWarnings("unchecked")
        List<com.dulich.ticketmanagement.entity.TrainBooking> bookings =
                (List<com.dulich.ticketmanagement.entity.TrainBooking>) model.getAttribute("bookings");
        assertNotNull(bookings);
        assertTrue(bookings.isEmpty());
    }

    @Test
    @DisplayName("Test: listAllTrainBookings khi khách vãng lai tra cứu với SĐT hoặc mã đơn thì trả về đơn khớp")
    void testListAllTrainBookings_WhenAnonymousWithSearch_ReturnsMatchingBookings() {
        com.dulich.ticketmanagement.entity.TrainBooking b1 = new com.dulich.ticketmanagement.entity.TrainBooking();
        b1.setBookingCode("SE-TRN-12345");
        b1.setCustomerPhone("0987654321");

        when(trainBookingRepository.findAllWithDetails()).thenReturn(List.of(b1));

        Model model = new ExtendedModelMap();
        String view = trainController.listAllTrainBookings("0987654321", model, null);
        assertEquals("train/train_booking_list", view);
        assertEquals(false, model.getAttribute("isManager"));
        assertEquals(true, model.getAttribute("hasSearched"));
        @SuppressWarnings("unchecked")
        List<com.dulich.ticketmanagement.entity.TrainBooking> bookings =
                (List<com.dulich.ticketmanagement.entity.TrainBooking>) model.getAttribute("bookings");
        assertEquals(1, bookings.size());
        assertEquals("SE-TRN-12345", bookings.get(0).getBookingCode());
    }

    @Test
    @DisplayName("Test: listAllTrainBookings khi đăng nhập với ROLE_ADMIN trả về trang quản lý vé tàu")
    void testListAllTrainBookings_WhenRoleAdmin_ReturnsTrainBookingList() {
        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        GrantedAuthority authority = () -> "ROLE_ADMIN";
        Mockito.doReturn(List.of(authority)).when(auth).getAuthorities();
        when(trainBookingRepository.findAllWithDetails()).thenReturn(List.of());

        Model model = new ExtendedModelMap();
        String view = trainController.listAllTrainBookings(null, model, auth);
        assertEquals("train/train_booking_list", view);
        assertEquals(true, model.getAttribute("isManager"));
        assertNotNull(model.getAttribute("bookings"));
    }

    @Test
    @DisplayName("Test: searchTrains khi không có tham số thì mặc định hiển thị toàn tuyến Bắc - Nam và danh sách tàu sẵn sàng")
    void testSearchTrains_WhenNoParams_DefaultsToFullRouteAndLoadsTrains() {
        TrainStation hn = new TrainStation("Hà Nội - Sài Gòn", "HAN", "Ga Hà Nội", 0, 0);
        hn.setId(1L);
        TrainStation sg = new TrainStation("Hà Nội - Sài Gòn", "SGO", "Ga Sài Gòn", 10, 1726);
        sg.setId(11L);

        when(trainStationRepository.findAllByOrderByStationIndexAsc()).thenReturn(List.of(hn, sg));
        Train se1 = new Train();
        se1.setTrainCode("SE1");
        se1.setStatus("ACTIVE");
        when(trainRepository.findByStatus("ACTIVE")).thenReturn(List.of(se1));
        when(trainTicketService.searchTrains(Mockito.any())).thenReturn(List.of(se1));

        TrainSearchDto searchDto = new TrainSearchDto();
        Model model = new ExtendedModelMap();

        String view = trainController.searchTrains(searchDto, model);

        assertEquals("train/train_list", view);
        assertEquals(1L, searchDto.getDepartureStationId());
        assertEquals(11L, searchDto.getArrivalStationId());
        assertEquals(hn, model.getAttribute("depStation"));
        assertEquals(sg, model.getAttribute("arrStation"));
        assertEquals(1726, model.getAttribute("distanceKm"));
        @SuppressWarnings("unchecked")
        List<Train> trains = (List<Train>) model.getAttribute("trains");
        assertNotNull(trains);
        assertEquals(1, trains.size());
    }

    @Test
    @DisplayName("Test: searchTrains khi chọn ga đi trùng ga đến thì thông báo lỗi")
    void testSearchTrains_WhenSameStation_SetsErrorMessage() {
        TrainStation hn = new TrainStation("Hà Nội - Sài Gòn", "HAN", "Ga Hà Nội", 0, 0);
        hn.setId(1L);

        when(trainStationRepository.findAllByOrderByStationIndexAsc()).thenReturn(List.of(hn));
        when(trainStationRepository.findById(1L)).thenReturn(Optional.of(hn));

        TrainSearchDto searchDto = new TrainSearchDto();
        searchDto.setDepartureStationId(1L);
        searchDto.setArrivalStationId(1L);
        Model model = new ExtendedModelMap();

        String view = trainController.searchTrains(searchDto, model);

        assertEquals("train/train_list", view);
        assertNotNull(model.getAttribute("errorMessage"));
        assertTrue(model.getAttribute("errorMessage").toString().contains("không được trùng nhau"));
        @SuppressWarnings("unchecked")
        List<Train> trains = (List<Train>) model.getAttribute("trains");
        assertTrue(trains.isEmpty());
    }

    @Test
    @DisplayName("Khứ Hồi: searchTrains khi roundTrip=true gọi tìm kiếm đoàn tàu chiều về")
    void testSearchTrains_WhenRoundTrip_PopulatesReturnTrains() {
        TrainStation hn = new TrainStation("Hà Nội - Sài Gòn", "HAN", "Ga Hà Nội", 0, 0);
        hn.setId(1L);
        TrainStation sg = new TrainStation("Hà Nội - Sài Gòn", "SGO", "Ga Sài Gòn", 10, 1726);
        sg.setId(11L);

        when(trainStationRepository.findAllByOrderByStationIndexAsc()).thenReturn(List.of(hn, sg));
        when(trainStationRepository.findById(1L)).thenReturn(Optional.of(hn));
        when(trainStationRepository.findById(11L)).thenReturn(Optional.of(sg));

        Train se1 = new Train();
        se1.setId(1L);
        se1.setTrainCode("SE1");
        Train se2 = new Train();
        se2.setId(2L);
        se2.setTrainCode("SE2");

        when(trainTicketService.searchTrains(Mockito.any())).thenReturn(List.of(se1));
        when(trainTicketService.searchReturnTrains(Mockito.any())).thenReturn(List.of(se2));

        TrainSearchDto searchDto = new TrainSearchDto();
        searchDto.setDepartureStationId(1L);
        searchDto.setArrivalStationId(11L);
        searchDto.setRoundTrip(true);
        searchDto.setDepartureDate(java.time.LocalDate.now().plusDays(2));
        searchDto.setReturnDate(java.time.LocalDate.now().plusDays(5));

        Model model = new ExtendedModelMap();
        String view = trainController.searchTrains(searchDto, model);

        assertEquals("train/train_list", view);
        @SuppressWarnings("unchecked")
        List<Train> returnTrains = (List<Train>) model.getAttribute("returnTrains");
        assertNotNull(returnTrains);
        assertEquals(1, returnTrains.size());
        assertEquals("SE2", returnTrains.get(0).getTrainCode());
    }

    @Test
    @DisplayName("Khứ Hồi: selectReturnCoachSeat trả về view sơ đồ ghế chiều về với cờ isSelectingReturn=true")
    void testSelectReturnCoachSeat_ValidParams_RendersReturnSeatMap() {
        Train outbound = new Train();
        outbound.setId(1L);
        outbound.setTrainCode("SE1");

        Train returnTrain = new Train();
        returnTrain.setId(2L);
        returnTrain.setTrainCode("SE2");

        TrainStation depStation = new TrainStation("Hà Nội - Sài Gòn", "HAN", "Ga Hà Nội", 0, 0);
        depStation.setId(1L);
        TrainStation arrStation = new TrainStation("Hà Nội - Sài Gòn", "SGO", "Ga Sài Gòn", 10, 1726);
        arrStation.setId(11L);

        com.dulich.ticketmanagement.entity.TrainCoach coach = new com.dulich.ticketmanagement.entity.TrainCoach();
        coach.setId(100L);
        coach.setCoachName("Toa 1");
        coach.setCoachType(com.dulich.ticketmanagement.entity.enums.CoachType.SOFT_SEAT);
        coach.setPricePerKm(new java.math.BigDecimal("700"));

        com.dulich.ticketmanagement.entity.TrainSeat seat1 = new com.dulich.ticketmanagement.entity.TrainSeat();
        seat1.setId(10L);
        seat1.setSeatLabel("Ghế 01");
        seat1.setCoach(coach);

        when(trainRepository.findById(1L)).thenReturn(Optional.of(outbound));
        when(trainRepository.findById(2L)).thenReturn(Optional.of(returnTrain));
        when(trainStationRepository.findById(1L)).thenReturn(Optional.of(depStation));
        when(trainStationRepository.findById(11L)).thenReturn(Optional.of(arrStation));
        when(trainSeatRepository.findById(10L)).thenReturn(Optional.of(seat1));
        when(trainCoachRepository.findByTrainIdOrderByCoachNumberAsc(2L)).thenReturn(List.of(coach));
        when(trainTicketService.getCoachSeatsAvailability(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of());
        when(trainTicketService.calculateOriginalFare(Mockito.anyInt(), Mockito.any())).thenReturn(new java.math.BigDecimal("1200000"));

        Model model = new ExtendedModelMap();
        String view = trainController.selectReturnCoachSeat(
                2L, 1L, 1L, 11L, "2026-11-10", "2026-11-15", List.of(10L), null, null, model
        );

        assertEquals("train/train_seat_map", view);
        assertEquals(true, model.getAttribute("isSelectingReturn"));
        assertEquals(true, model.getAttribute("isRoundTrip"));
        assertEquals(1, model.getAttribute("requiredSeatCount"));
        assertEquals(returnTrain, model.getAttribute("returnTrain"));
    }

    @Test
    @DisplayName("Khứ Hồi: showBookingForm ném ngoại lệ khi số lượng ghế chiều về không khớp chiều đi")
    void testShowBookingForm_RoundTrip_MismatchedSeatCount_ThrowsBusinessException() {
        Model model = new ExtendedModelMap();
        BusinessException ex = assertThrows(BusinessException.class, () ->
                trainController.showBookingForm(
                        1L, 1L, 11L, "2026-11-10", List.of(10L, 11L), 2L, "2026-11-15", List.of(20L), model
                )
        );
        assertTrue(ex.getMessage().contains("phải khớp"));
    }
}
