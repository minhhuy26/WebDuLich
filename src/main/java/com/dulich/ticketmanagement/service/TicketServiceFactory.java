package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.entity.enums.TicketType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory hỗ trợ lấy đúng Service xử lý cho từng loại vé:
 * FLIGHT -> FlightTicketService
 * TRAIN  -> TrainTicketService
 */
@Component
public class TicketServiceFactory {

    private final Map<TicketType, TicketService> serviceMap;

    @Autowired
    public TicketServiceFactory(List<TicketService> ticketServices) {
        this.serviceMap = ticketServices.stream()
                .collect(Collectors.toMap(TicketService::getTicketType, Function.identity()));
    }

    public TicketService getService(TicketType ticketType) {
        TicketService service = serviceMap.get(ticketType);
        if (service == null) {
            throw new IllegalArgumentException("Chưa hỗ trợ loại vé: " + ticketType);
        }
        return service;
    }
}
