package com.example.expense_tracker.controllers;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.expense_tracker.dto.ApiError;
import com.example.expense_tracker.enums.ErrorCode;
import com.example.expense_tracker.exceptions.ApiException;

@RestControllerAdvice
public class ExceptionController {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionController.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiExceptions(ApiException exception) {
        logger.warn(
                "ApiException [{}] correlationId={}",
                exception.getErrorCode().getHttpStatus().value(),
                MDC.get("X-Correlation-Id"),
                exception);

        String correlationId = MDC.get("X-Correlation-Id");

        ApiError error = ApiError.builder()
                .code(exception.getErrorCode().name())
                .message(exception.getErrorCode().getDescription())
                .status(exception.getErrorCode().getHttpStatus().value())
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity
                .status(error.getStatus())
                .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (a, b) -> a));

        ApiError error = ApiError.builder()
                .code(ErrorCode.INVALID_INPUT.name())
                .message("Validation failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .details(errors)
                .correlationId(MDC.get("X-Correlation-Id"))
                .build();

        logger.warn("Validation failed: {}", errors);

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {

        logger.error("Unexpected error", ex);

        ApiError error = ApiError.builder()
                .code("UNEXPECTED_ERROR")
                .message("Something went wrong")
                .status(500)
                .correlationId(MDC.get("X-Correlation-Id"))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

}
