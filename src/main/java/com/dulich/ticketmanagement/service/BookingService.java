package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.entity.Booking;

import java.util.List;

public interface BookingService {
    Booking createBooking(Booking booking);
    List<Booking> getAllBookings();
    Booking getById(Long id);
    Booking getByCode(String code);
    long count();
}
