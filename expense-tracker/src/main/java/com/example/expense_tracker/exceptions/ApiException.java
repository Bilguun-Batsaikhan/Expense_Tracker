package com.example.expense_tracker.exceptions;

import com.example.expense_tracker.enums.ErrorCode;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final ErrorCode apiExceptionsEnum;
    private Exception exception;

    public ApiException(ErrorCode apiExceptionsEnum) {
        super();
        this.apiExceptionsEnum = apiExceptionsEnum;
    }

    public ApiException(ErrorCode apiExceptionsEnum, Exception exception) {
        super();
        this.apiExceptionsEnum = apiExceptionsEnum;
        this.exception = exception;
    }
}
