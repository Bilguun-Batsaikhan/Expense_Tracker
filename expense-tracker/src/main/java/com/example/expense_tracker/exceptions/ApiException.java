package com.example.expense_tracker.exceptions;

import com.example.expense_tracker.enums.ErrorCode;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;
    private Exception exception;

    public ApiException(ErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public ApiException(ErrorCode errorCode, Exception exception) {
        super();
        this.errorCode = errorCode;
        this.exception = exception;
    }
}
