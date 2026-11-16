package com.dulich.ticketmanagement.service;

import com.dulich.ticketmanagement.dto.TicketSearchDto;
import com.dulich.ticketmanagement.entity.Ticket;
import com.dulich.ticketmanagement.entity.enums.TicketStatus;
import com.dulich.ticketmanagement.entity.enums.TransportType;
import com.dulich.ticketmanagement.repository.TicketRepository;
import com.dulich.ticketmanagement.service.impl.TicketCatalogServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Tra cứu danh mục & chuẩn hóa địa điểm (TicketCatalogService)")
class TicketCatalogServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketCatalogServiceImpl ticketCatalogService;

    @Test
    @DisplayName("Chọn 'Sài Gòn (SGN)' tự chuẩn hóa thành mã SGN để khớp cả TP. Hồ Chí Minh (SGN) và Sài Gòn (SGN)")
    void testSearchNormalizesAirportCodeFromQuickList() {
        TicketSearchDto dto = new TicketSearchDto();
        dto.setTransportType(TransportType.AIR);
        dto.setDeparturePlace("Hà Nội (HAN)");
        dto.setArrivalPlace("Sài Gòn (SGN)");

        Ticket t = new Ticket();
        t.setDeparturePlace("Hà Nội (HAN)");
        t.setArrivalPlace("TP. Hồ Chí Minh (SGN)");
        t.setDepartureTime(LocalDateTime.now().plusDays(3));

        when(ticketRepository.searchAvailableTickets(
                eq(TransportType.AIR), eq("HAN"), isNull(), eq("SGN"), isNull(), any(), any()))
                .thenReturn(List.of(t));

        List<Ticket> results = ticketCatalogService.searchTickets(dto);

        assertEquals(1, results.size());
        assertTrue(ticketCatalogService.isExactMatchResult(dto, results));
    }

    @Test
    @DisplayName("Tab 'Tất Cả Chuyến' (transportType == null) tìm trên cả Máy bay và Tàu hỏa")
    void testSearchWithNullTransportTypeQueriesAllTypes() {
        TicketSearchDto dto = new TicketSearchDto();
        dto.setTransportType(null);
        dto.setDeparturePlace("Ga Hà Nội");

        Ticket train = new Ticket();
        train.setTransportType(TransportType.TRAIN);
        train.setDeparturePlace("Ga Hà Nội");
        train.setArrivalPlace("Ga Đà Nẵng");
        train.setDepartureTime(LocalDateTime.now().plusDays(2));

        when(ticketRepository.searchAvailableTickets(
                isNull(), eq("Ga Hà Nội"), isNull(), isNull(), isNull(), any(), any()))
                .thenReturn(List.of(train));

        List<Ticket> results = ticketCatalogService.searchTickets(dto);

        assertEquals(1, results.size());
        assertEquals(TransportType.TRAIN, results.get(0).getTransportType());
        assertTrue(ticketCatalogService.isExactMatchResult(dto, results));
    }

    @Test
    @DisplayName("Phát hiện chính xác khi kết quả là danh sách gợi ý fallback")
    void testDetectsFallbackResults() {
        TicketSearchDto dto = new TicketSearchDto();
        dto.setTransportType(TransportType.TRAIN);
        dto.setArrivalPlace("Ga Vinh");

        Ticket fallback = new Ticket();
        fallback.setTransportType(TransportType.TRAIN);
        fallback.setDeparturePlace("Ga Hà Nội");
        fallback.setArrivalPlace("Ga Đà Nẵng");
        fallback.setDepartureTime(LocalDateTime.now().plusDays(2));

        when(ticketRepository.searchAvailableTickets(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(ticketRepository.findByTransportTypeAndStatus(TransportType.TRAIN, TicketStatus.AVAILABLE))
                .thenReturn(List.of(fallback));

        List<Ticket> results = ticketCatalogService.searchTickets(dto);

        assertEquals(1, results.size());
        assertFalse(ticketCatalogService.isExactMatchResult(dto, results));
    }

    @Test
    @DisplayName("Lấy danh sách điểm khởi hành/đến khả dụng phân loại theo phương tiện")
    void testGetAvailablePlacesByType() {
        when(ticketRepository.findDistinctAvailableDeparturesWithType())
                .thenReturn(List.of(
                        new Object[]{TransportType.AIR, "Hà Nội (HAN)"},
                        new Object[]{TransportType.TRAIN, "Ga Hà Nội"}
                ));

        List<String> airDeps = ticketCatalogService.getAvailablePlaces(TransportType.AIR, true);
        List<String> allDeps = ticketCatalogService.getAvailablePlaces(null, true);

        assertEquals(List.of("Hà Nội (HAN)"), airDeps);
        assertEquals(List.of("Hà Nội (HAN)", "Ga Hà Nội"), allDeps);
    }

    @Test
    @DisplayName("Tự động sinh số ghế liên tiếp cho vé máy bay và tàu hỏa khi đặt nhiều khách")
    void testGenerateConsecutiveSeat() {
        // Máy bay: 12A -> 12B, 12C, 12D...
        assertEquals("12A", ticketCatalogService.generateConsecutiveSeat("12A", 0, TransportType.AIR));
        assertEquals("12B", ticketCatalogService.generateConsecutiveSeat("12A", 1, TransportType.AIR));
        assertEquals("12C", ticketCatalogService.generateConsecutiveSeat("12A", 2, TransportType.AIR));

        // Tàu hỏa: Toa 2 - Ghế 15 -> Toa 2 - Ghế 16, Toa 2 - Ghế 17...
        assertEquals("Toa 2 - Ghế 15", ticketCatalogService.generateConsecutiveSeat("Toa 2 - Ghế 15", 0, TransportType.TRAIN));
        assertEquals("Toa 2 - Ghế 16", ticketCatalogService.generateConsecutiveSeat("Toa 2 - Ghế 15", 1, TransportType.TRAIN));
        assertEquals("Toa 2 - Ghế 17", ticketCatalogService.generateConsecutiveSeat("Toa 2 - Ghế 15", 2, TransportType.TRAIN));
    }
}
