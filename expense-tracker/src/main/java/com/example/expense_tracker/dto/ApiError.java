package com.example.expense_tracker.dto;

import java.time.Instant;

import org.springframework.http.HttpStatus;

import com.example.expense_tracker.enums.ApiExceptionsEnum;

public record ApiError(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp) {
    public static ApiError of(HttpStatus status, String message) {
        return new ApiError(
                status.value(),
                status.getReasonPhrase(),
                message,
                null,
                Instant.now());
    }

    public static ApiError of(ApiExceptionsEnum exceptionsEnum) {
        return new ApiError(exceptionsEnum.getHttpStatus().value(), exceptionsEnum.getHttpStatus().getReasonPhrase(),
                exceptionsEnum.getDescription(), null, Instant.now());
    }
}
