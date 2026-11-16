package com.dulich.ticketmanagement.service.impl;

import com.dulich.ticketmanagement.dto.TicketSearchDto;
import com.dulich.ticketmanagement.entity.Ticket;
import com.dulich.ticketmanagement.entity.enums.TicketStatus;
import com.dulich.ticketmanagement.entity.enums.TransportType;
import com.dulich.ticketmanagement.exception.ResourceNotFoundException;
import com.dulich.ticketmanagement.repository.TicketRepository;
import com.dulich.ticketmanagement.service.TicketCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class TicketCatalogServiceImpl implements TicketCatalogService {

    private static final Pattern AIRPORT_CODE_PATTERN = Pattern.compile("\\(([A-Z]{3})\\)");

    private final TicketRepository ticketRepository;

    @Autowired
    public TicketCatalogServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public List<Ticket> searchTickets(TicketSearchDto searchDto) {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusMonths(6);

        if (searchDto.getDepartureDate() != null) {
            LocalDate d = searchDto.getDepartureDate();
            start = d.atStartOfDay();
            end = d.atTime(23, 59, 59);
            if (start.isBefore(LocalDateTime.now())) {
                start = LocalDateTime.now();
            }
        }

        TransportType type = searchDto.getTransportType();
        String[] depTerms = resolveSearchTerms(searchDto.getDeparturePlace(), type);
        String[] arrTerms = resolveSearchTerms(searchDto.getArrivalPlace(), type);

        // 1. Thử tìm kiếm theo đúng tiêu chí ngày
        List<Ticket> results = ticketRepository.searchAvailableTickets(
                type, depTerms[0], depTerms[1], arrTerms[0], arrTerms[1], start, end);

        // 2. Nếu không có vé trong ngày đó, nới rộng tìm kiếm các chuyến gần nhất (trong vòng 3 tháng tới)
        if (results.isEmpty() && (depTerms[0] != null || arrTerms[0] != null)) {
            results = ticketRepository.searchAvailableTickets(
                    type, depTerms[0], depTerms[1], arrTerms[0], arrTerms[1],
                    LocalDateTime.now(), LocalDateTime.now().plusMonths(3));
        }

        // 3. Nếu vẫn không thấy, trả về toàn bộ vé khả dụng phù hợp loại phương tiện để người dùng có lựa chọn
        if (results.isEmpty()) {
            results = (type != null)
                    ? ticketRepository.findByTransportTypeAndStatus(type, TicketStatus.AVAILABLE)
                    : ticketRepository.findByStatusOrderByDepartureTimeAsc(TicketStatus.AVAILABLE);
        }

        return results;
    }

    private String[] resolveSearchTerms(String term, TransportType type) {
        if (term == null) {
            return new String[]{null, null};
        }
        String trimmed = term.trim();
        if (trimmed.isEmpty() || trimmed.equalsIgnoreCase("all") || trimmed.equals("--")) {
            return new String[]{null, null};
        }

        String lower = trimmed.toLowerCase(Locale.ROOT);
        Matcher codeMatcher = AIRPORT_CODE_PATTERN.matcher(trimmed);
        if (codeMatcher.find()) {
            String code = codeMatcher.group(1);
            String cityPart = trimmed.substring(0, codeMatcher.start()).trim();
            if (type == TransportType.TRAIN && !cityPart.isEmpty()) {
                if (cityPart.toLowerCase(Locale.ROOT).contains("hồ chí minh") || cityPart.toLowerCase(Locale.ROOT).contains("sài gòn")) {
                    return new String[]{"Sài Gòn", null};
                }
                return new String[]{cityPart, null};
            }
            if (code.equals("HAN") || code.equals("SGN") || code.equals("DAD")
                    || code.equals("PQC") || code.equals("CXR") || code.equals("HUI") || code.equals("HPH")) {
                return new String[]{code, null};
            }
            return new String[]{trimmed, code};
        }

        if (lower.equals("han") || lower.equals("sgn") || lower.equals("dad")
                || lower.equals("pqc") || lower.equals("cxr") || lower.equals("hui") || lower.equals("hph")) {
            return new String[]{trimmed.toUpperCase(Locale.ROOT), null};
        }

        if (lower.startsWith("ga ") && type == TransportType.AIR) {
            String city = trimmed.substring(3).trim();
            if (city.toLowerCase(Locale.ROOT).contains("sài gòn") || city.toLowerCase(Locale.ROOT).contains("hồ chí minh")) {
                return new String[]{"SGN", "Sài Gòn"};
            }
            return new String[]{city, null};
        }

        if (!lower.startsWith("ga ") && (lower.contains("sài gòn") || lower.contains("hồ chí minh")
                || lower.equals("tp.hcm") || lower.equals("tphcm") || lower.equals("tp hcm"))) {
            if (type == TransportType.TRAIN) {
                return new String[]{"Sài Gòn", null};
            }
            if (type == TransportType.AIR) {
                return new String[]{"SGN", "Sài Gòn"};
            }
            return new String[]{"Sài Gòn", "SGN"};
        }

        return new String[]{trimmed, null};
    }

    @Override
    public List<String> getAvailablePlaces(TransportType transportType, boolean isDeparture) {
        List<Object[]> rows = isDeparture
                ? ticketRepository.findDistinctAvailableDeparturesWithType()
                : ticketRepository.findDistinctAvailableArrivalsWithType();
        List<String> result = new ArrayList<>();
        for (Object[] row : rows) {
            if (row == null || row.length < 2 || row[1] == null) continue;
            TransportType rowType = (TransportType) row[0];
            String place = row[1].toString();
            if ((transportType == null || transportType == rowType) && !result.contains(place)) {
                result.add(place);
            }
        }
        return result;
    }

    @Override
    public boolean isExactMatchResult(TicketSearchDto searchDto, List<Ticket> results) {
        if (results == null || results.isEmpty()) {
            return false;
        }
        TransportType type = searchDto.getTransportType();
        String[] depTerms = resolveSearchTerms(searchDto.getDeparturePlace(), type);
        String[] arrTerms = resolveSearchTerms(searchDto.getArrivalPlace(), type);
        LocalDate filterDate = searchDto.getDepartureDate();

        if (depTerms[0] == null && arrTerms[0] == null && filterDate == null) {
            return true;
        }

        for (Ticket ticket : results) {
            boolean matchesDep = depTerms[0] == null || matchesPlace(ticket.getDeparturePlace(), depTerms);
            boolean matchesArr = arrTerms[0] == null || matchesPlace(ticket.getArrivalPlace(), arrTerms);
            boolean matchesDate = filterDate == null
                    || (ticket.getDepartureTime() != null && ticket.getDepartureTime().toLocalDate().equals(filterDate));
            if (matchesDep && matchesArr && matchesDate) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesPlace(String candidate, String[] terms) {
        if (candidate == null) return false;
        String lowerCandidate = candidate.toLowerCase(Locale.ROOT);
        if (terms[0] != null && lowerCandidate.contains(terms[0].toLowerCase(Locale.ROOT))) {
            return true;
        }
        return terms[1] != null && lowerCandidate.contains(terms[1].toLowerCase(Locale.ROOT));
    }

    @Override
    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin vé với ID: " + id));
    }

    @Override
    public List<Ticket> getTicketsByVehicle(String vehicleCode, LocalDateTime depTime) {
        return ticketRepository.findByVehicleAndDepartureTime(vehicleCode, depTime);
    }

    @Override
    public List<String> getOccupiedSeats(String vehicleCode) {
        return ticketRepository.findOccupiedSeatNumbers(vehicleCode);
    }

    @Override
    public List<Ticket> getAvailableTicketsByType(TransportType transportType) {
        return ticketRepository.findByTransportTypeAndStatus(transportType, TicketStatus.AVAILABLE);
    }

    @Override
    public String generateConsecutiveSeat(String baseSeat, int index, TransportType transportType) {
        if (index == 0 && baseSeat != null && !baseSeat.isBlank()) {
            return baseSeat.trim();
        }
        if (baseSeat == null || baseSeat.isBlank()) {
            return transportType == TransportType.AIR
                    ? ("12" + (char) ('A' + (index % 6)))
                    : ("Toa 1 - Ghế " + String.format("%02d", index + 1));
        }

        String seat = baseSeat.trim();

        if (transportType == TransportType.AIR) {
            Pattern pattern = Pattern.compile("^(\\d+)([A-Fa-f])$");
            Matcher m = pattern.matcher(seat);
            if (m.matches()) {
                int row = Integer.parseInt(m.group(1));
                char col = Character.toUpperCase(m.group(2).charAt(0));
                int colOffset = (col - 'A') + index;
                int newRow = row + (colOffset / 6);
                char newCol = (char) ('A' + (colOffset % 6));
                return String.format("%02d%c", newRow, newCol);
            }
            return seat + "-" + (index + 1);
        } else {
            Pattern pattern = Pattern.compile("^(.*?-\\s*(?:Ghế|Giường)\\s*)(\\d+)(.*)$", Pattern.CASE_INSENSITIVE);
            Matcher m = pattern.matcher(seat);
            if (m.matches()) {
                String prefix = m.group(1);
                int seatNum = Integer.parseInt(m.group(2)) + index;
                String suffix = m.group(3);
                return prefix + String.format("%02d", seatNum) + suffix;
            }
            return seat + " (Chỗ " + (index + 1) + ")";
        }
    }
}
