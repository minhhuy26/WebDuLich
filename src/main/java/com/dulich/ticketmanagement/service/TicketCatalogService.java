package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.dto.TicketSearchDto;
import com.dulich.ticketmanagement.entity.Ticket;
import com.dulich.ticketmanagement.entity.enums.TransportType;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketCatalogService {

    List<Ticket> searchTickets(TicketSearchDto searchDto);

    Ticket getTicketById(Long id);

    List<Ticket> getTicketsByVehicle(String vehicleCode, LocalDateTime depTime);

    List<String> getOccupiedSeats(String vehicleCode);

    List<Ticket> getAvailableTicketsByType(TransportType transportType);

    List<String> getAvailablePlaces(TransportType transportType, boolean isDeparture);

    boolean isExactMatchResult(TicketSearchDto searchDto, List<Ticket> results);

    String generateConsecutiveSeat(String baseSeat, int index, TransportType transportType);
}
