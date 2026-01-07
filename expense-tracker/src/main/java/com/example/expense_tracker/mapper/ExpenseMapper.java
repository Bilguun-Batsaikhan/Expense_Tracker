package com.example.expense_tracker.mapper;

import org.springframework.stereotype.Component;

import com.example.expense_tracker.dto.expense.ExpenseRequestDto;
import com.example.expense_tracker.dto.expense.ExpenseResponseDto;
import com.example.expense_tracker.entities.Expense;

@Component
public class ExpenseMapper {
    public ExpenseResponseDto toDto(Expense expense) {
        ExpenseResponseDto expenseResponseDto = new ExpenseResponseDto();
        expenseResponseDto.setId(expense.getId());
        expenseResponseDto.setAmount(expense.getAmount());
        expenseResponseDto.setCurrency(expense.getCurrency());
        expenseResponseDto.setDescription(expense.getDescription());
        expenseResponseDto.setExpenseDate(expense.getExpenseDate());
        expenseResponseDto.setCategoryId(expense.getCategory().getId());
        expenseResponseDto.setCategoryName(expense.getCategory().getName());
        expenseResponseDto.setCreatedAt(expense.getCreatedAt());
        return expenseResponseDto;
    }

    public Expense toEntity(ExpenseRequestDto expenseRequestDto) {
        Expense expense = new Expense();
        expense.setAmount(expenseRequestDto.getAmount());
        expense.setDescription(expenseRequestDto.getDescription());
        expense.setExpenseDate(expenseRequestDto.getExpenseDate());
        expense.setCurrency(expenseRequestDto.getCurrency());
        expense.setDeleted(false);
        return expense;
    }
}
