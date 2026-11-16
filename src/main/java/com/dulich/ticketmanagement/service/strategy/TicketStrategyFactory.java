package com.dulich.ticketmanagement.service.strategy;

import com.dulich.ticketmanagement.entity.enums.TransportType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory hỗ trợ tự động tìm và tiêm (inject) đúng Strategy tương ứng
 * dựa trên mã loại phương tiện (AIR, TRAIN).
 */
@Component
public class TicketStrategyFactory {

    private final Map<TransportType, TicketStrategy> strategyMap;

    @Autowired
    public TicketStrategyFactory(List<TicketStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(TicketStrategy::getTransportType, Function.identity()));
    }

    public TicketStrategy getStrategy(TransportType type) {
        TicketStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Chưa hỗ trợ giải thuật cho loại phương tiện: " + type);
        }
        return strategy;
    }
}
