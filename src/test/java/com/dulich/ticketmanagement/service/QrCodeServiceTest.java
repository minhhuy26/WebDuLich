package com.dulich.ticketmanagement.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Dịch vụ sinh mã QR Code vé điện tử (ZXing)")
class QrCodeServiceTest {

    private final QrCodeService qrCodeService = new QrCodeService();

    @Test
    @DisplayName("Tạo mã QR thành công ở định dạng Base64 Data URI")
    void testGenerateBase64QrCode() {
        String testPayload = "VERIFY:ETK-AIR-100201|RES-202611-0001|VN210|12A";
        String qrImage = qrCodeService.generateBase64QrCode(testPayload, 200, 200);

        assertNotNull(qrImage);
        assertTrue(qrImage.startsWith("data:image/png;base64,"));
        assertTrue(qrImage.length() > 100);
    }

    @Test
    @DisplayName("Nội dung rỗng hoặc null phải trả về chuỗi rỗng an toàn")
    void testGenerateEmptyPayload() {
        assertEquals("", qrCodeService.generateBase64QrCode("", 200, 200));
        assertEquals("", qrCodeService.generateBase64QrCode(null, 200, 200));
    }
}
