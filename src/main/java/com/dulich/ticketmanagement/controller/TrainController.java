package com.dulich.ticketmanagement.controller;

import com.dulich.ticketmanagement.dto.SeatAvailabilityDto;
import com.dulich.ticketmanagement.dto.TrainBookingRequestDto;
import com.dulich.ticketmanagement.dto.TrainPassengerInputDto;
import com.dulich.ticketmanagement.dto.TrainSearchDto;
import com.dulich.ticketmanagement.entity.*;
import com.dulich.ticketmanagement.entity.User;
import com.dulich.ticketmanagement.entity.enums.PassengerPriority;
import com.dulich.ticketmanagement.exception.BusinessException;
import com.dulich.ticketmanagement.repository.*;
import com.dulich.ticketmanagement.service.EmailService;
import com.dulich.ticketmanagement.service.impl.TrainTicketService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Controller
@RequestMapping("/trains")
public class TrainController {

    private final TrainTicketService trainTicketService;
    private final TrainRepository trainRepository;
    private final TrainStationRepository trainStationRepository;
    private final TrainCoachRepository trainCoachRepository;
    private final TrainSeatRepository trainSeatRepository;
    private final TrainBookingRepository trainBookingRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Autowired
    public TrainController(TrainTicketService trainTicketService,
                           TrainRepository trainRepository,
                           TrainStationRepository trainStationRepository,
                           TrainCoachRepository trainCoachRepository,
                           TrainSeatRepository trainSeatRepository,
                           TrainBookingRepository trainBookingRepository,
                           UserRepository userRepository,
                           EmailService emailService) {
        this.trainTicketService = trainTicketService;
        this.trainRepository = trainRepository;
        this.trainStationRepository = trainStationRepository;
        this.trainCoachRepository = trainCoachRepository;
        this.trainSeatRepository = trainSeatRepository;
        this.trainBookingRepository = trainBookingRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    private void validateBookingAccess(TrainBooking booking, String inputPhone, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() 
                && !(auth instanceof AnonymousAuthenticationToken);

        if (isAuthenticated) {
            boolean isStaffOrAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_NHANVIEN"));
            if (isStaffOrAdmin) {
                return;
            }

            User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
            if (currentUser != null && currentUser.getPhone() != null 
                    && currentUser.getPhone().trim().equals(booking.getCustomerPhone().trim())) {
                return;
            }

            throw new BusinessException("Bạn không có quyền thực hiện thao tác trên đơn vé tàu này!");
        }

        // Khách vãng lai: Kiểm tra session đã lưu mã đơn lúc đặt giữ chỗ hoặc SĐT xác thực
        String sessionBookingKey = "TRAIN_AUTH_" + booking.getBookingCode();
        if (Boolean.TRUE.equals(session.getAttribute(sessionBookingKey))) {
            return;
        }

        if (inputPhone != null && inputPhone.trim().equals(booking.getCustomerPhone().trim())) {
            session.setAttribute(sessionBookingKey, true);
            return;
        }

        throw new BusinessException("Vui lòng cung cấp đúng số điện thoại đặt vé để xác thực quyền thao tác!");
    }

    // 1. TÌM KIẾM VÉ TÀU HỎA (HỖ TRỢ MỘT CHIỀU & KHỨ HỒI)
    @GetMapping
    public String searchTrains(@ModelAttribute("searchDto") TrainSearchDto searchDto, Model model) {
        List<TrainStation> stations = trainStationRepository.findAllByOrderByStationIndexAsc();
        model.addAttribute("stations", stations);

        if (searchDto.getDepartureDate() == null) {
            searchDto.setDepartureDate(LocalDate.now().plusDays(1));
        }

        TrainStation depStation = null;
        TrainStation arrStation = null;

        if (searchDto.getDepartureStationId() != null && searchDto.getArrivalStationId() != null) {
            depStation = trainStationRepository.findById(searchDto.getDepartureStationId()).orElse(null);
            arrStation = trainStationRepository.findById(searchDto.getArrivalStationId()).orElse(null);
        } else if (!stations.isEmpty()) {
            // Mặc định: Tuyến Bắc - Nam (Hà Nội -> Sài Gòn)
            depStation = stations.get(0);
            arrStation = stations.get(stations.size() - 1);
            searchDto.setDepartureStationId(depStation.getId());
            searchDto.setArrivalStationId(arrStation.getId());
        }

        if (searchDto.isRoundTrip() && searchDto.getReturnDate() == null) {
            searchDto.setReturnDate(searchDto.getDepartureDate().plusDays(3));
        }

        int distanceKm = 0;
        List<Train> trains = new ArrayList<>();
        List<Train> returnTrains = Collections.emptyList();

        if (depStation != null && arrStation != null) {
            if (depStation.getId().equals(arrStation.getId())) {
                model.addAttribute("errorMessage", "Ga khởi hành và Ga đến không được trùng nhau!");
            } else {
                distanceKm = Math.abs(arrStation.getDistanceKm() - depStation.getDistanceKm());
                trains = trainTicketService.searchTrains(searchDto);
                if (searchDto.isRoundTrip()) {
                    returnTrains = trainTicketService.searchReturnTrains(searchDto);
                }
            }
        }

        model.addAttribute("trains", trains);
        model.addAttribute("returnTrains", returnTrains);
        model.addAttribute("distanceKm", distanceKm);
        model.addAttribute("depStation", depStation);
        model.addAttribute("arrStation", arrStation);
        model.addAttribute("searchDto", searchDto);

        return "train/train_list";
    }

    // 2. CHỌN TOA VÀ XEM SƠ ĐỒ GHẾ CHIỀU ĐI
    @GetMapping("/{trainId}/select-coach")
    public String selectCoachSeat(@PathVariable("trainId") Long trainId,
                                  @RequestParam("depStationId") Long depStationId,
                                  @RequestParam("arrStationId") Long arrStationId,
                                  @RequestParam("depDate") String depDateStr,
                                  @RequestParam(value = "returnTrainId", required = false) Long returnTrainId,
                                  @RequestParam(value = "returnDate", required = false) String returnDateStr,
                                  @RequestParam(value = "coachId", required = false) Long coachId,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đoàn tàu ID: " + trainId));

        TrainStation depStation = trainStationRepository.findById(depStationId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy ga đi!"));

        TrainStation arrStation = trainStationRepository.findById(arrStationId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy ga đến!"));

        LocalDate departureDate;
        try {
            departureDate = LocalDate.parse(depDateStr);
        } catch (DateTimeParseException e) {
            throw new BusinessException("Định dạng ngày khởi hành không hợp lệ (chuẩn: YYYY-MM-DD)!");
        }

        List<TrainCoach> coaches = trainCoachRepository.findByTrainIdOrderByCoachNumberAsc(trainId);
        if (coaches.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đoàn tàu " + train.getTrainCode() + " hiện đang bảo trì kỹ thuật hoặc chưa mở bán toa vé. Quý khách vui lòng chọn đoàn tàu khác!");
            return "redirect:/trains";
        }

        TrainCoach selectedCoach = coaches.get(0);
        if (coachId != null) {
            for (TrainCoach c : coaches) {
                if (c.getId().equals(coachId)) {
                    selectedCoach = c;
                    break;
                }
            }
        }

        // Lấy sơ đồ ghế và tình trạng trống THEO CHẶNG
        List<SeatAvailabilityDto> seats = trainTicketService.getCoachSeatsAvailability(
                selectedCoach.getId(), trainId, departureDate, depStation.getStationIndex(), arrStation.getStationIndex()
        );

        int distanceKm = Math.abs(arrStation.getDistanceKm() - depStation.getDistanceKm());
        BigDecimal estimatedPrice = trainTicketService.calculateOriginalFare(distanceKm, selectedCoach.getPricePerKm());

        boolean isRoundTrip = (returnTrainId != null && returnDateStr != null && !returnDateStr.isBlank());
        if (isRoundTrip) {
            Train returnTrain = trainRepository.findById(returnTrainId).orElse(null);
            model.addAttribute("isRoundTrip", true);
            model.addAttribute("returnTrain", returnTrain);
            model.addAttribute("returnTrainId", returnTrainId);
            model.addAttribute("returnDate", returnDateStr);
        } else {
            model.addAttribute("isRoundTrip", false);
        }

        model.addAttribute("isSelectingReturn", false);
        model.addAttribute("train", train);
        model.addAttribute("depStation", depStation);
        model.addAttribute("arrStation", arrStation);
        model.addAttribute("departureDate", departureDate);
        model.addAttribute("coaches", coaches);
        model.addAttribute("selectedCoach", selectedCoach);
        model.addAttribute("seats", seats);
        model.addAttribute("distanceKm", distanceKm);
        model.addAttribute("estimatedPrice", estimatedPrice);

        return "train/train_seat_map";
    }

    // 2B. CHỌN TOA VÀ SƠ ĐỒ GHẾ CHIỀU VỀ (VÉ KHỨ HỒI)
    @RequestMapping(value = "/{returnTrainId}/select-return-coach", method = {RequestMethod.GET, RequestMethod.POST})
    public String selectReturnCoachSeat(@PathVariable("returnTrainId") Long returnTrainId,
                                        @RequestParam("outboundTrainId") Long outboundTrainId,
                                        @RequestParam("depStationId") Long depStationId,
                                        @RequestParam("arrStationId") Long arrStationId,
                                        @RequestParam("depDate") String depDateStr,
                                        @RequestParam("returnDate") String returnDateStr,
                                        @RequestParam("selectedSeatIds") List<Long> outboundSeatIds,
                                        @RequestParam(value = "coachId", required = false) Long coachId,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {
        if (outboundSeatIds == null || outboundSeatIds.isEmpty()) {
            throw new BusinessException("Vui lòng chọn ít nhất 1 chỗ ngồi chiều đi!");
        }

        Train returnTrain = trainRepository.findById(returnTrainId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đoàn tàu chiều về ID: " + returnTrainId));

        Train outboundTrain = trainRepository.findById(outboundTrainId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đoàn tàu chiều đi ID: " + outboundTrainId));

        TrainStation depStation = trainStationRepository.findById(depStationId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy ga đi!"));

        TrainStation arrStation = trainStationRepository.findById(arrStationId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy ga đến!"));

        LocalDate departureDate = LocalDate.parse(depDateStr);
        LocalDate returnDate = LocalDate.parse(returnDateStr);

        List<TrainSeat> outboundSeats = new ArrayList<>();
        for (Long sId : outboundSeatIds) {
            trainSeatRepository.findById(sId).ifPresent(outboundSeats::add);
        }

        List<TrainCoach> coaches = trainCoachRepository.findByTrainIdOrderByCoachNumberAsc(returnTrainId);
        if (coaches.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đoàn tàu về " + returnTrain.getTrainCode() + " hiện chưa mở bán toa vé!");
            return "redirect:/trains";
        }

        TrainCoach selectedCoach = coaches.get(0);
        if (coachId != null) {
            for (TrainCoach c : coaches) {
                if (c.getId().equals(coachId)) {
                    selectedCoach = c;
                    break;
                }
            }
        }

        // Chặng chiều về chạy ngược lại: arrStation -> depStation
        List<SeatAvailabilityDto> seats = trainTicketService.getCoachSeatsAvailability(
                selectedCoach.getId(), returnTrainId, returnDate, arrStation.getStationIndex(), depStation.getStationIndex()
        );

        int distanceKm = Math.abs(arrStation.getDistanceKm() - depStation.getDistanceKm());
        BigDecimal estimatedPrice = trainTicketService.calculateOriginalFare(distanceKm, selectedCoach.getPricePerKm());

        model.addAttribute("isSelectingReturn", true);
        model.addAttribute("isRoundTrip", true);
        model.addAttribute("train", returnTrain);
        model.addAttribute("returnTrain", returnTrain);
        model.addAttribute("returnTrainId", returnTrainId);
        model.addAttribute("outboundTrain", outboundTrain);
        model.addAttribute("outboundTrainId", outboundTrainId);
        model.addAttribute("outboundSeats", outboundSeats);
        model.addAttribute("outboundSeatIds", outboundSeatIds);
        model.addAttribute("requiredSeatCount", outboundSeatIds.size());
        model.addAttribute("depStation", depStation);
        model.addAttribute("arrStation", arrStation);
        model.addAttribute("departureDate", departureDate);
        model.addAttribute("returnDate", returnDateStr);
        model.addAttribute("coaches", coaches);
        model.addAttribute("selectedCoach", selectedCoach);
        model.addAttribute("seats", seats);
        model.addAttribute("distanceKm", distanceKm);
        model.addAttribute("estimatedPrice", estimatedPrice);

        return "train/train_seat_map";
    }

    // 3. FORM NHẬP THÔNG TIN HÀNH KHÁCH (HỖ TRỢ MỘT CHIỀU VÀ KHỨ HỒI)
    @PostMapping("/booking-form")
    public String showBookingForm(@RequestParam("trainId") Long trainId,
                                  @RequestParam("depStationId") Long depStationId,
                                  @RequestParam("arrStationId") Long arrStationId,
                                  @RequestParam("departureDate") String depDateStr,
                                  @RequestParam("selectedSeatIds") List<Long> selectedSeatIds,
                                  @RequestParam(value = "returnTrainId", required = false) Long returnTrainId,
                                  @RequestParam(value = "returnDate", required = false) String returnDateStr,
                                  @RequestParam(value = "returnSelectedSeatIds", required = false) List<Long> returnSelectedSeatIds,
                                  Model model) {
        if (selectedSeatIds == null || selectedSeatIds.isEmpty()) {
            throw new BusinessException("Vui lòng chọn ít nhất 1 chỗ ngồi/giường trên sơ đồ!");
        }
        if (selectedSeatIds.size() > 10) {
            throw new BusinessException("Mỗi lần đặt vé chỉ được tối đa 10 chỗ!");
        }

        boolean isRoundTrip = (returnTrainId != null && returnDateStr != null && returnSelectedSeatIds != null && !returnSelectedSeatIds.isEmpty());
        if (isRoundTrip && returnSelectedSeatIds.size() != selectedSeatIds.size()) {
            throw new BusinessException("Số lượng chỗ chiều về (" + returnSelectedSeatIds.size() + ") phải khớp với số lượng chỗ chiều đi (" + selectedSeatIds.size() + ")!");
        }

        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thông tin đoàn tàu ID: " + trainId));
        TrainStation depStation = trainStationRepository.findById(depStationId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy ga đi ID: " + depStationId));
        TrainStation arrStation = trainStationRepository.findById(arrStationId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy ga đến ID: " + arrStationId));
        LocalDate departureDate;
        try {
            departureDate = LocalDate.parse(depDateStr);
        } catch (DateTimeParseException e) {
            throw new BusinessException("Định dạng ngày khởi hành không hợp lệ (chuẩn: YYYY-MM-DD)!");
        }

        Train returnTrain = null;
        LocalDate returnDate = null;
        if (isRoundTrip) {
            returnTrain = trainRepository.findById(returnTrainId)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy thông tin đoàn tàu chiều về ID: " + returnTrainId));
            returnDate = LocalDate.parse(returnDateStr);
        }

        TrainBookingRequestDto bookingRequest = new TrainBookingRequestDto();
        bookingRequest.setTrainId(trainId);
        bookingRequest.setDepartureStationId(depStationId);
        bookingRequest.setArrivalStationId(arrStationId);
        bookingRequest.setDepartureDate(departureDate);

        if (isRoundTrip) {
            bookingRequest.setRoundTrip(true);
            bookingRequest.setReturnTrainId(returnTrainId);
            bookingRequest.setReturnDate(returnDate);
        }

        // Ưu đãi thành viên: Tự động lưu sẵn thông tin cho khách đã đăng nhập (đỡ cần điền lại)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
            if (currentUser != null) {
                bookingRequest.setCustomerName(currentUser.getFullName());
                bookingRequest.setCustomerPhone(currentUser.getPhone());
                bookingRequest.setCustomerEmail(currentUser.getEmail());
                model.addAttribute("isMember", true);
                model.addAttribute("memberUser", currentUser);
            }
        }

        List<TrainPassengerInputDto> passengers = new ArrayList<>();
        List<TrainSeat> seats = new ArrayList<>();
        List<TrainSeat> returnSeats = new ArrayList<>();

        for (int i = 0; i < selectedSeatIds.size(); i++) {
            Long seatId = selectedSeatIds.get(i);
            TrainSeat seat = trainSeatRepository.findById(seatId)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy thông tin chỗ ngồi ID: " + seatId));
            seats.add(seat);

            TrainPassengerInputDto p = new TrainPassengerInputDto();
            p.setSeatId(seatId);
            p.setPriorityCategory(PassengerPriority.STANDARD);

            if (isRoundTrip) {
                Long retSeatId = returnSelectedSeatIds.get(i);
                TrainSeat retSeat = trainSeatRepository.findById(retSeatId)
                        .orElseThrow(() -> new BusinessException("Không tìm thấy thông tin chỗ ngồi chiều về ID: " + retSeatId));
                returnSeats.add(retSeat);
                p.setReturnSeatId(retSeatId);
            }

            if (i == 0 && bookingRequest.getCustomerName() != null) {
                p.setPassengerName(bookingRequest.getCustomerName());
            }
            passengers.add(p);
        }
        bookingRequest.setPassengers(passengers);

        int distanceKm = Math.abs(arrStation.getDistanceKm() - depStation.getDistanceKm());

        model.addAttribute("train", train);
        model.addAttribute("returnTrain", returnTrain);
        model.addAttribute("isRoundTrip", isRoundTrip);
        model.addAttribute("depStation", depStation);
        model.addAttribute("arrStation", arrStation);
        model.addAttribute("departureDate", departureDate);
        model.addAttribute("returnDepartureDate", returnDate);
        model.addAttribute("distanceKm", distanceKm);
        model.addAttribute("seats", seats);
        model.addAttribute("returnSeats", returnSeats);
        model.addAttribute("bookingRequest", bookingRequest);
        model.addAttribute("priorities", PassengerPriority.values());

        return "train/train_booking_form";
    }

    // 4. XỬ LÝ ĐẶT GIỮ CHỖ (HOLD 10 PHÚT)
    @PostMapping("/hold")
    public String holdTrainBooking(@Valid @ModelAttribute("bookingRequest") TrainBookingRequestDto bookingRequest,
                                   BindingResult bindingResult,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateBookingFormModel(bookingRequest, model);
            return "train/train_booking_form";
        }

        try {
            TrainBooking booking = trainTicketService.createHoldBooking(bookingRequest);
            session.setAttribute("TRAIN_AUTH_" + booking.getBookingCode(), true);

            // Lưu vào danh sách đơn của phiên (session) để khách vãng lai xem lại đơn đã đặt
            @SuppressWarnings("unchecked")
            List<String> guestTrainBookings = (List<String>) session.getAttribute("GUEST_TRAIN_BOOKINGS");
            if (guestTrainBookings == null) {
                guestTrainBookings = new ArrayList<>();
            }
            if (!guestTrainBookings.contains(booking.getBookingCode())) {
                guestTrainBookings.add(booking.getBookingCode());
            }
            session.setAttribute("GUEST_TRAIN_BOOKINGS", guestTrainBookings);

            emailService.sendTrainBookingHeldEmail(booking);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Giữ chỗ thành công! Thông tin vé tàu đã được chuyển về Gmail: " + booking.getCustomerEmail() + ". Chỗ ngồi được giữ trong 10 phút.");
            return "redirect:/trains/booking/" + booking.getBookingCode();
        } catch (BusinessException ex) {
            populateBookingFormModel(bookingRequest, model);
            model.addAttribute("errorMessage", ex.getMessage());
            return "train/train_booking_form";
        }
    }

    private void populateBookingFormModel(TrainBookingRequestDto bookingRequest, Model model) {
        Train train = trainRepository.findById(bookingRequest.getTrainId()).orElse(null);
        TrainStation depStation = trainStationRepository.findById(bookingRequest.getDepartureStationId()).orElse(null);
        TrainStation arrStation = trainStationRepository.findById(bookingRequest.getArrivalStationId()).orElse(null);
        int distanceKm = (depStation != null && arrStation != null) ? Math.abs(arrStation.getDistanceKm() - depStation.getDistanceKm()) : 0;

        List<TrainSeat> seats = new ArrayList<>();
        List<TrainSeat> returnSeats = new ArrayList<>();
        if (bookingRequest.getPassengers() != null) {
            for (TrainPassengerInputDto p : bookingRequest.getPassengers()) {
                if (p.getSeatId() != null) {
                    trainSeatRepository.findById(p.getSeatId()).ifPresent(seats::add);
                }
                if (p.getReturnSeatId() != null) {
                    trainSeatRepository.findById(p.getReturnSeatId()).ifPresent(returnSeats::add);
                }
            }
        }

        Train returnTrain = null;
        if (bookingRequest.isRoundTrip() && bookingRequest.getReturnTrainId() != null) {
            returnTrain = trainRepository.findById(bookingRequest.getReturnTrainId()).orElse(null);
        }

        model.addAttribute("train", train);
        model.addAttribute("returnTrain", returnTrain);
        model.addAttribute("isRoundTrip", bookingRequest.isRoundTrip() && returnTrain != null);
        model.addAttribute("depStation", depStation);
        model.addAttribute("arrStation", arrStation);
        model.addAttribute("departureDate", bookingRequest.getDepartureDate());
        model.addAttribute("returnDepartureDate", bookingRequest.getReturnDate());
        model.addAttribute("distanceKm", distanceKm);
        model.addAttribute("seats", seats);
        model.addAttribute("returnSeats", returnSeats);
        model.addAttribute("priorities", PassengerPriority.values());
    }

    // 5. CHI TIẾT ĐƠN VÉ TÀU
    @GetMapping("/booking/{code}")
    public String viewTrainBooking(@PathVariable("code") String bookingCode,
                                   @RequestParam(value = "phone", required = false) String phone,
                                   HttpSession session,
                                   Model model) {
        TrainBooking booking = trainBookingRepository.findByBookingCodeWithDetails(bookingCode)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn đặt vé tàu: " + bookingCode));
        try {
            validateBookingAccess(booking, phone, session);
        } catch (BusinessException ex) {
            model.addAttribute("bookingCode", bookingCode);
            model.addAttribute("lookupAction", "/trains/booking/" + bookingCode);
            model.addAttribute("ticketLabel", "vé tàu");
            return "booking_access";
        }

        model.addAttribute("booking", booking);
        model.addAttribute("now", LocalDateTime.now());
        return "train/train_booking_detail";
    }

    // 6. XÁC NHẬN THANH TOÁN & XUẤT VÉ ĐIỆN TỬ
    @PostMapping("/booking/{code}/confirm")
    public String confirmTrainBooking(@PathVariable("code") String bookingCode,
                                      @RequestParam(value = "phone", required = false) String phone,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        try {
            TrainBooking booking = trainBookingRepository.findByBookingCodeWithDetails(bookingCode)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy đơn đặt vé tàu: " + bookingCode));
            validateBookingAccess(booking, phone, session);
            TrainBooking confirmed = trainTicketService.confirmAndIssueTicket(bookingCode);
            emailService.sendTrainBookingConfirmedEmail(confirmed);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Xác nhận thanh toán & phát hành vé tàu điện tử thành công cho mã đơn " + booking.getBookingCode() + "! Vé tàu đã được gửi về Gmail: " + booking.getCustomerEmail());
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/trains/booking/" + bookingCode;
    }

    // 7. HỦY VÉ TÀU HỎA
    @PostMapping("/booking/{code}/cancel")
    public String cancelTrainBooking(@PathVariable("code") String bookingCode,
                                     @RequestParam(value = "reason", required = false) String reason,
                                     @RequestParam(value = "phone", required = false) String phone,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        try {
            TrainBooking booking = trainBookingRepository.findByBookingCodeWithDetails(bookingCode)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy đơn đặt vé tàu: " + bookingCode));
            validateBookingAccess(booking, phone, session);
            TrainBooking cancelled = trainTicketService.cancelBooking(bookingCode, reason);
            emailService.sendTrainBookingCancelledEmail(cancelled);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Hủy vé tàu thành công! Thông báo hủy vé đã được gửi về Gmail: " + booking.getCustomerEmail() +
                    " - Tiền hoàn lại: " + String.format("%,.0f VNĐ", cancelled.getRefundAmount()));
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/trains/booking/" + bookingCode;
    }

    // 8. TRA CỨU & QUẢN LÝ ĐƠN ĐẶT VÉ TÀU (Xem đơn đã đặt & Quản lý cho Admin/Nhân viên)
    @GetMapping("/bookings")
    public String listAllTrainBookings(@RequestParam(value = "searchKeyword", required = false) String searchKeyword,
                                       Model model,
                                       Authentication auth) {
        boolean isStaffOrAdmin = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)
                && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_NHANVIEN"));

        boolean hasSearched = (searchKeyword != null && !searchKeyword.isBlank());
        List<TrainBooking> bookings;

        if (isStaffOrAdmin) {
            // Quản lý / Nhân viên: Xem toàn bộ đơn hệ thống hoặc lọc theo từ khóa
            if (hasSearched) {
                String kw = searchKeyword.trim().toLowerCase();
                bookings = trainBookingRepository.findAllWithDetails().stream()
                        .filter(b -> (b.getBookingCode() != null && b.getBookingCode().toLowerCase().contains(kw)) ||
                                     (b.getCustomerPhone() != null && b.getCustomerPhone().contains(kw)) ||
                                     (b.getCustomerName() != null && b.getCustomerName().toLowerCase().contains(kw)) ||
                                     (b.getTrain() != null && b.getTrain().getTrainCode() != null && b.getTrain().getTrainCode().toLowerCase().contains(kw)))
                        .toList();
            } else {
                bookings = trainBookingRepository.findAllWithDetails();
            }
        } else {
            // Khách chưa đăng nhập: Bảo mật thông tin hành khách, BẮT BUỘC phải tra cứu (SĐT hoặc Mã đơn) mới ra đơn của mình
            if (hasSearched) {
                String kw = searchKeyword.trim().toLowerCase();
                bookings = trainBookingRepository.findAllWithDetails().stream()
                        .filter(b -> (b.getBookingCode() != null && b.getBookingCode().toLowerCase().contains(kw)) ||
                                     (b.getCustomerPhone() != null && b.getCustomerPhone().contains(kw)))
                        .toList();
            } else {
                bookings = java.util.Collections.emptyList();
            }
        }

        model.addAttribute("isManager", isStaffOrAdmin);
        model.addAttribute("hasSearched", hasSearched);
        model.addAttribute("bookings", bookings);
        model.addAttribute("searchKeyword", searchKeyword);
        return "train/train_booking_list";
    }
}
