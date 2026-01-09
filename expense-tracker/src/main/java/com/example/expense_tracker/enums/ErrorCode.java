package com.example.expense_tracker.enums;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {
    AUTHORIZATION_FAILED(123, "Authorization failed", HttpStatus.FORBIDDEN),
    AUTHENTICATION_FAILED(401, "Authentication failed", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(403, "Missing permissions", HttpStatus.FORBIDDEN),
    RESOURCE_NOT_FOUND(404, "Resource not found", HttpStatus.NOT_FOUND),
    INVALID_INPUT(422, "Input validation failed", HttpStatus.BAD_REQUEST),
    UNEXPECTED_ERROR(500, "Generic error", HttpStatus.INTERNAL_SERVER_ERROR),

    EXPIRED_ACCESS_TOKEN(1420, "Expired access token", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN(1421, "Invalid refresh token", HttpStatus.UNAUTHORIZED),
    INVALID_ACCESS_TOKEN(20103, "Invalid access token", HttpStatus.UNAUTHORIZED),
    MALFORMED_ACCESS_TOKEN(401, "Malformed access token", HttpStatus.UNAUTHORIZED),
    MALFORMED_REFRESH_TOKEN(401, "Malformed refresh token", HttpStatus.UNAUTHORIZED),

    FOREIGN_KEY_CONSTRAINT_VIOLATION(1451, "Foreign key constraint violation", HttpStatus.CONFLICT),
    DUPLICATE_ENTRY(1062, "Duplicate entry", HttpStatus.CONFLICT),
    INVALID_EMAIL(1401, "Invalid email", HttpStatus.BAD_REQUEST);

    private final int id;
    private final String description;
    private final HttpStatus httpStatus;

    ErrorCode(int id, String description, HttpStatus httpStatus) {
        this.id = id;
        this.description = description;
        this.httpStatus = httpStatus;
    }
}
