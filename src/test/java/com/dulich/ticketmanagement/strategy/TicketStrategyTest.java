package com.dulich.ticketmanagement.strategy;

import com.dulich.ticketmanagement.entity.Ticket;
import com.dulich.ticketmanagement.entity.enums.PassengerType;
import com.dulich.ticketmanagement.entity.enums.TransportType;
import com.dulich.ticketmanagement.service.strategy.AirTicketStrategy;
import com.dulich.ticketmanagement.service.strategy.TicketStrategy;
import com.dulich.ticketmanagement.service.strategy.TicketStrategyFactory;
import com.dulich.ticketmanagement.service.strategy.TrainTicketStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Chiến lược định giá vé (Strategy Pattern) cho Vé Máy Bay & Tàu Hỏa")
class TicketStrategyTest {

    private TicketStrategyFactory strategyFactory;
    private AirTicketStrategy airStrategy;
    private TrainTicketStrategy trainStrategy;

    @BeforeEach
    void setUp() {
        airStrategy = new AirTicketStrategy();
        trainStrategy = new TrainTicketStrategy();
        strategyFactory = new TicketStrategyFactory(List.of(airStrategy, trainStrategy));
    }

    @Test
    @DisplayName("Factory phải phân giải chính xác Strategy theo loại phương tiện")
    void testStrategyFactoryResolution() {
        TicketStrategy resolvedAir = strategyFactory.getStrategy(TransportType.AIR);
        assertNotNull(resolvedAir);
        assertEquals(TransportType.AIR, resolvedAir.getTransportType());

        TicketStrategy resolvedTrain = strategyFactory.getStrategy(TransportType.TRAIN);
        assertNotNull(resolvedTrain);
        assertEquals(TransportType.TRAIN, resolvedTrain.getTransportType());
    }

    @Test
    @DisplayName("Vé máy bay: Tính giá vé cho Người lớn (100% giá + 120k phí sân bay)")
    void testAirPricingAdult() {
        Ticket ticket = new Ticket();
        ticket.setTransportType(TransportType.AIR);
        ticket.setBasePrice(new BigDecimal("1500000"));

        BigDecimal finalPrice = airStrategy.calculatePrice(ticket, PassengerType.ADULT, BigDecimal.ZERO);
        // 1,500,000 * 1.0 + 120,000 = 1,620,000
        assertEquals(new BigDecimal("1620000"), finalPrice);
    }

    @Test
    @DisplayName("Vé máy bay: Trẻ em được giảm 25% giá cơ bản (75% + 120k phí)")
    void testAirPricingChild() {
        Ticket ticket = new Ticket();
        ticket.setTransportType(TransportType.AIR);
        ticket.setBasePrice(new BigDecimal("1000000"));

        BigDecimal finalPrice = airStrategy.calculatePrice(ticket, PassengerType.CHILD, BigDecimal.ZERO);
        // 1,000,000 * 0.75 + 120,000 = 870,000
        assertEquals(new BigDecimal("870000"), finalPrice);
    }

    @Test
    @DisplayName("Vé tàu hỏa: Người lớn (100% giá + 10k bảo hiểm đường sắt)")
    void testTrainPricingAdult() {
        Ticket ticket = new Ticket();
        ticket.setTransportType(TransportType.TRAIN);
        ticket.setBasePrice(new BigDecimal("600000"));

        BigDecimal finalPrice = trainStrategy.calculatePrice(ticket, PassengerType.ADULT, BigDecimal.ZERO);
        // 600,000 * 1.0 + 10,000 = 610,000
        assertEquals(new BigDecimal("610000"), finalPrice);
    }

    @Test
    @DisplayName("Vé tàu hỏa: Học sinh/Sinh viên được giảm 10% (90% + 10k bảo hiểm)")
    void testTrainPricingStudent() {
        Ticket ticket = new Ticket();
        ticket.setTransportType(TransportType.TRAIN);
        ticket.setBasePrice(new BigDecimal("500000"));

        BigDecimal finalPrice = trainStrategy.calculatePrice(ticket, PassengerType.STUDENT, BigDecimal.ZERO);
        // 500,000 * 0.90 + 10,000 = 460,000
        assertEquals(new BigDecimal("460000"), finalPrice);
    }

    @Test
    @DisplayName("Chính sách nghiệp vụ mô tả đúng loại phương tiện")
    void testPolicyDescription() {
        assertNotNull(airStrategy.getPolicyDescription());
        assertTrue(airStrategy.getPolicyDescription().contains("máy bay") || airStrategy.getPolicyDescription().contains("Hàng không"));

        assertNotNull(trainStrategy.getPolicyDescription());
        assertTrue(trainStrategy.getPolicyDescription().contains("tàu hỏa") || trainStrategy.getPolicyDescription().contains("Đường sắt"));
    }
}
