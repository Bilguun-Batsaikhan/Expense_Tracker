package com.example.expense_tracker.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.expense_tracker.dto.ApiError;
import com.example.expense_tracker.exceptions.ApiException;

@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiExceptions(ApiException exception) {
        return ResponseEntity.status(exception.getApiExceptionsEnum().getHttpStatus())
                .body(ApiError.of(exception.getApiExceptionsEnum()));
    }
}
