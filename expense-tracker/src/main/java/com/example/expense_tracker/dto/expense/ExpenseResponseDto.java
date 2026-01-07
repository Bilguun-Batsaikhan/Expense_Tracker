package com.example.expense_tracker.dto.expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseResponseDto {
    private UUID id;
    private BigDecimal amount;
    private String currency;
    private String description;
    private LocalDate expenseDate;

    private UUID categoryId;
    private String categoryName;

    private LocalDateTime createdAt;
}

