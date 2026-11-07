package com.dulich.ticketmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class AdminStatisticsDto {
    private int totalFlightTicketsSold = 0;
    private int totalTrainTicketsSold = 0;
    private int totalTicketsSold = 0;

    private BigDecimal flightRevenue = BigDecimal.ZERO;
    private BigDecimal trainRevenue = BigDecimal.ZERO;
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    private int totalConsultations = 0;
    private int pendingConsultations = 0;

    private Map<LocalDate, BigDecimal> revenueByDate = new LinkedHashMap<>();
    private Map<String, BigDecimal> revenueByMonth = new LinkedHashMap<>();

    public AdminStatisticsDto() {
    }

    public int getTotalFlightTicketsSold() {
        return totalFlightTicketsSold;
    }

    public void setTotalFlightTicketsSold(int totalFlightTicketsSold) {
        this.totalFlightTicketsSold = totalFlightTicketsSold;
    }

    public int getTotalTrainTicketsSold() {
        return totalTrainTicketsSold;
    }

    public void setTotalTrainTicketsSold(int totalTrainTicketsSold) {
        this.totalTrainTicketsSold = totalTrainTicketsSold;
    }

    public int getTotalTicketsSold() {
        return totalTicketsSold;
    }

    public void setTotalTicketsSold(int totalTicketsSold) {
        this.totalTicketsSold = totalTicketsSold;
    }

    public BigDecimal getFlightRevenue() {
        return flightRevenue;
    }

    public void setFlightRevenue(BigDecimal flightRevenue) {
        this.flightRevenue = flightRevenue;
    }

    public BigDecimal getTrainRevenue() {
        return trainRevenue;
    }

    public void setTrainRevenue(BigDecimal trainRevenue) {
        this.trainRevenue = trainRevenue;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public int getTotalConsultations() {
        return totalConsultations;
    }

    public void setTotalConsultations(int totalConsultations) {
        this.totalConsultations = totalConsultations;
    }

    public int getPendingConsultations() {
        return pendingConsultations;
    }

    public void setPendingConsultations(int pendingConsultations) {
        this.pendingConsultations = pendingConsultations;
    }

    public Map<LocalDate, BigDecimal> getRevenueByDate() {
        return revenueByDate;
    }

    public void setRevenueByDate(Map<LocalDate, BigDecimal> revenueByDate) {
        this.revenueByDate = revenueByDate;
    }

    public Map<String, BigDecimal> getRevenueByMonth() {
        return revenueByMonth;
    }

    public void setRevenueByMonth(Map<String, BigDecimal> revenueByMonth) {
        this.revenueByMonth = revenueByMonth;
    }
}
