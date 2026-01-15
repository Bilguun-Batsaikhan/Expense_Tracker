package com.example.expense_tracker.enums;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Identity Errors (401)
    AUTHENTICATION_FAILED(4010, "Authentication failed", HttpStatus.UNAUTHORIZED),
    INVALID_ACCESS_TOKEN(4011, "Invalid access token", HttpStatus.UNAUTHORIZED),
    EXPIRED_ACCESS_TOKEN(4012, "Expired access token", HttpStatus.UNAUTHORIZED),

    // Permission Errors (403)
    FORBIDDEN(4030, "You do not have permission to access this resource", HttpStatus.FORBIDDEN),
    AUTHORIZATION_FAILED(4031, "Authorization check failed", HttpStatus.FORBIDDEN),
    USER_DISABLED(4032, "User account is disabled", HttpStatus.FORBIDDEN),

    // Request Errors (400 / 404 / 409)
    RESOURCE_NOT_FOUND(4040, "Resource not found", HttpStatus.NOT_FOUND),
    INVALID_INPUT(4000, "The provided input is invalid", HttpStatus.BAD_REQUEST),
    DUPLICATE_ENTRY(4090, "This resource already exists", HttpStatus.CONFLICT),
    FOREIGN_KEY_VIOLATION(4091, "Resource is currently in use and cannot be deleted", HttpStatus.CONFLICT),

    // Server Errors (500)
    UNEXPECTED_ERROR(5000, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int id;
    private final String description;
    private final HttpStatus httpStatus;

    ErrorCode(int id, String description, HttpStatus httpStatus) {
        this.id = id;
        this.description = description;
        this.httpStatus = httpStatus;
    }
}
