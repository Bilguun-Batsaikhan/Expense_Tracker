package com.example.expense_tracker.exceptions;

import com.example.expense_tracker.enums.ApiExceptionsEnum;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final ApiExceptionsEnum apiExceptionsEnum;
    private Exception exception;

    public ApiException(ApiExceptionsEnum apiExceptionsEnum) {
        super();
        this.apiExceptionsEnum = apiExceptionsEnum;
    }

    public ApiException(ApiExceptionsEnum apiExceptionsEnum, Exception exception) {
        super();
        this.apiExceptionsEnum = apiExceptionsEnum;
        this.exception = exception;
    }
}
