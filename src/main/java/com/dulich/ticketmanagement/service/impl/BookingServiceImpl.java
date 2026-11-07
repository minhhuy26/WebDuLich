package com.dulich.ticketmanagement.service.impl;

import com.dulich.ticketmanagement.entity.Booking;
import com.dulich.ticketmanagement.exception.ResourceNotFoundException;
import com.dulich.ticketmanagement.repository.BookingRepository;
import com.dulich.ticketmanagement.service.BookingService;
import com.dulich.ticketmanagement.service.TicketService;
import com.dulich.ticketmanagement.service.TicketServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final TicketServiceFactory ticketServiceFactory;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, TicketServiceFactory ticketServiceFactory) {
        this.bookingRepository = bookingRepository;
        this.ticketServiceFactory = ticketServiceFactory;
    }

    @Override
    public Booking createBooking(Booking booking) {
        // Áp dụng validation và quy trình của từng loại vé tương ứng
        TicketService specificTicketService = ticketServiceFactory.getService(booking.getTicketType());
        specificTicketService.validateBooking(booking);
        specificTicketService.processBooking(booking);

        if (booking.getBookingCode() == null || booking.getBookingCode().isEmpty()) {
            booking.setBookingCode("BKG-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        }

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAllWithDetails();
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt vé với ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getByCode(String code) {
        return bookingRepository.findByBookingCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt vé với mã: " + code));
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return bookingRepository.count();
    }
}
