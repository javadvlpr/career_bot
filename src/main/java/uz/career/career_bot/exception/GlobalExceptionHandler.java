package uz.career.career_bot.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uz.career.career_bot.controller.ApiController;
import uz.career.career_bot.dto.ErrorResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Exception handler for REST API — catches only exceptions coming from ApiController.
 * Returns a response in JSON format.
 */
@Slf4j
@RestControllerAdvice(assignableTypes = ApiController.class)
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        log.warn("Not found: {} | path={}", ex.getMessage(), request.getRequestURI());
        return buildResponse(ex.getStatus(), "Not Found", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExists(AlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Already exists: {} | path={}", ex.getMessage(), request.getRequestURI());
        return buildResponse(ex.getStatus(), "Conflict", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex, HttpServletRequest request) {
        log.warn("Validation failed: {} | path={}", ex.getMessage(), request.getRequestURI());
        return buildResponse(ex.getStatus(), "Bad Request", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        log.warn("Business error: {} | path={}", ex.getMessage(), request.getRequestURI());
        return buildResponse(ex.getStatus(), ex.getStatus().getReasonPhrase(),
                ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleArgumentValidation(MethodArgumentNotValidException ex,
                                                                  HttpServletRequest request) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors().stream()
                .map(this::formatFieldError)
                .toList();
        log.warn("Validation errors: {} | path={}", errors, request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Failed",
                "Ma'lumotlar noto'g'ri", request.getRequestURI(), errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                               HttpServletRequest request) {
        log.warn("Illegal argument: {} | path={}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request",
                ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {}: ", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Kutilmagan xatolik yuz berdi", request.getRequestURI(), null);
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error,
                                                        String message, String path,
                                                        List<String> validationErrors) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path)
                .validationErrors(validationErrors)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}