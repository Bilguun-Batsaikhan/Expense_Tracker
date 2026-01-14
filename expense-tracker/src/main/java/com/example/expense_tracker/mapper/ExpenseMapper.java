package com.example.expense_tracker.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.expense_tracker.dto.expense.ExpenseRequestDto;
import com.example.expense_tracker.dto.expense.ExpenseResponseDto;
import com.example.expense_tracker.entities.Expense;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    public ExpenseResponseDto toDto(Expense expense);

    public Expense toEntity(ExpenseRequestDto expenseRequestDto);
}
