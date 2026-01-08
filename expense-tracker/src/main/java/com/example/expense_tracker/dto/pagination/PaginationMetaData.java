package com.example.expense_tracker.dto.pagination;

public record PaginationMetaData(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {}
