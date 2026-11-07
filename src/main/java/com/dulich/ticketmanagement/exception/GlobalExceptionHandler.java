package com.dulich.ticketmanagement.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ModelAndView handleBusinessException(BusinessException ex) {
        log.warn("Nghiệp vụ không hợp lệ: {}", ex.getMessage());
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("timestamp", LocalDateTime.now());
        mav.addObject("status", 400);
        mav.addObject("error", "Lỗi nghiệp vụ đặt vé");
        mav.addObject("message", ex.getMessage());
        return mav;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleNotFoundException(ResourceNotFoundException ex) {
        log.warn("Không tìm thấy tài nguyên: {}", ex.getMessage());
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("timestamp", LocalDateTime.now());
        mav.addObject("status", 404);
        mav.addObject("error", "Không tìm thấy dữ liệu");
        mav.addObject("message", ex.getMessage());
        return mav;
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ModelAndView handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        log.warn("Đường dẫn không tồn tại (404): {}", ex.getResourcePath());
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("timestamp", LocalDateTime.now());
        mav.addObject("status", 404);
        mav.addObject("error", "Đường dẫn không tồn tại");
        mav.addObject("message", "Trang hoặc tài nguyên '" + ex.getResourcePath() + "' không tồn tại trên hệ thống.");
        return mav;
    }

    @ExceptionHandler(java.time.format.DateTimeParseException.class)
    public ModelAndView handleDateTimeParseException(java.time.format.DateTimeParseException ex) {
        log.warn("Định dạng ngày tháng không hợp lệ: {}", ex.getMessage());
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("timestamp", LocalDateTime.now());
        mav.addObject("status", 400);
        mav.addObject("error", "Định dạng ngày không hợp lệ");
        mav.addObject("message", "Ngày tháng gửi lên không đúng định dạng chuẩn (YYYY-MM-DD). Vui lòng kiểm tra lại!");
        return mav;
    }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ModelAndView handleNoSuchElementException(java.util.NoSuchElementException ex) {
        log.warn("Không tìm thấy phần tử yêu cầu: {}", ex.getMessage());
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("timestamp", LocalDateTime.now());
        mav.addObject("status", 404);
        mav.addObject("error", "Không tìm thấy dữ liệu");
        mav.addObject("message", "Dữ liệu yêu cầu không tồn tại trên hệ thống hoặc đã bị xóa!");
        return mav;
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ModelAndView handleValidationException(Exception ex) {
        log.warn("Dữ liệu nhập vào không hợp lệ: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();

        if (ex instanceof MethodArgumentNotValidException manve) {
            for (FieldError fieldError : manve.getBindingResult().getFieldErrors()) {
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        } else if (ex instanceof BindException be) {
            for (FieldError fieldError : be.getBindingResult().getFieldErrors()) {
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        }

        ModelAndView mav = new ModelAndView("error");
        mav.addObject("timestamp", LocalDateTime.now());
        mav.addObject("status", 422);
        mav.addObject("error", "Dữ liệu không hợp lệ");
        mav.addObject("message", "Vui lòng kiểm tra lại thông tin các trường đã nhập.");
        mav.addObject("validationErrors", errors);
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGenericException(Exception ex) {
        log.error("Lỗi hệ thống: ", ex);
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("timestamp", LocalDateTime.now());
        mav.addObject("status", 500);
        mav.addObject("error", "Lỗi hệ thống");
        // Do not expose exception details, database information, or stack-trace
        // fragments to an end user. The exception remains in the server-side log.
        mav.addObject("message", "Hệ thống đang gặp sự cố. Vui lòng thử lại sau hoặc liên hệ bộ phận hỗ trợ.");
        return mav;
    }
}
