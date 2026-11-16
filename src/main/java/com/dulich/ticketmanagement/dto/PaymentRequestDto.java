package com.dulich.ticketmanagement.dto;

import com.dulich.ticketmanagement.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class PaymentRequestDto {

    @NotBlank(message = "Mã đơn đặt vé không được để trống")
    private String bookingCode;

    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    private PaymentMethod paymentMethod = PaymentMethod.VNPAY;

    private BigDecimal amount;

    private String customerPhone;

    private String note;

    public PaymentRequestDto() {
    }

    public PaymentRequestDto(String bookingCode, PaymentMethod paymentMethod, BigDecimal amount) {
        this.bookingCode = bookingCode;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
