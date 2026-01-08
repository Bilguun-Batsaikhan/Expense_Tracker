package com.example.expense_tracker.dto.pagination;

import java.util.List;

public record PagedResponse<T>(
        List<T> data,
        PaginationMetaData pagination
) {}

