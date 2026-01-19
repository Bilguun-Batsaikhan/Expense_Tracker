package com.example.expense_tracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.expense_tracker.Repositories.CategoryRepository;
import com.example.expense_tracker.Repositories.ExpenseRepository;
import com.example.expense_tracker.Services.ExpenseService;
import com.example.expense_tracker.Services.UserService;
import com.example.expense_tracker.dto.CustomUserDetails;
import com.example.expense_tracker.dto.expense.ExpenseRequestDto;
import com.example.expense_tracker.dto.expense.ExpenseResponseDto;
import com.example.expense_tracker.entities.Category;
import com.example.expense_tracker.entities.Expense;
import com.example.expense_tracker.enums.ErrorCode;
import com.example.expense_tracker.exceptions.ApiException;
import com.example.expense_tracker.mapper.ExpenseMapper;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {
    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private ExpenseMapper expenseMapper;
    @Mock
    private UserService userService;
    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private ExpenseService expenseService;

    // create(req)
    @Test
    void create_whenMissingCategory_throwsApiExceptionWithErrorCodeResourceNotFound() {
        ExpenseRequestDto mockDto = new ExpenseRequestDto();
        UUID missingCategoryId = UUID.randomUUID();
        mockDto.setCategoryId(missingCategoryId);

        when(categoryRepository.findById(missingCategoryId))
                .thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> expenseService.create(mockDto));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void create_whenSuccess_returnsExpenseResponseDto() {
        // arrange
        ExpenseRequestDto mockDto = new ExpenseRequestDto();
        UUID categoryId = UUID.randomUUID();
        mockDto.setCategoryId(categoryId);

        Category category = new Category();
        category.setId(categoryId);

        UUID userId = UUID.randomUUID();
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(userId);
        when(userDetails.getUsername()).thenReturn("test@test.com");

        Expense entity = new Expense();
        ExpenseResponseDto responseDto = new ExpenseResponseDto();

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(userService.getCurrentUserDetails())
                .thenReturn(userDetails);

        when(expenseMapper.toEntity(mockDto))
                .thenReturn(entity);

        when(expenseMapper.toDto(entity))
                .thenReturn(responseDto);

        when(expenseRepository.save(entity))
                .thenReturn(entity);

        // act
        ExpenseResponseDto result = expenseService.create(mockDto);

        // assert
        assertNotNull(result);
        assertSame(responseDto, result);

        assertEquals(category, entity.getCategory());
        assertNotNull(entity.getUser());
        assertEquals(userId, entity.getUser().getId());

        verify(expenseRepository).save(entity);
    }

    @Test
    void getExpenseForCurrentUser_whenMissingId_throwsApiExceptionWithErrorCodeResourceNotFound() {
        UUID id = UUID.randomUUID();
        // When the service asks the repository for this ID, pretend the DB has no
        // record.
        UUID userId = UUID.randomUUID();
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(userId);
        when(expenseRepository.findByIdAndUserIdAndDeletedFalse(id, userId)).thenReturn(Optional.empty());
        // Execute the method, Expect it to fail, Capture the thrown exception
        ApiException ex = assertThrows(ApiException.class, () -> expenseService.getExpenseForCurrentUser(id));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    // @Test
    // void getExpenseForCurrentUser_whenSuccess_returnsExpenseResponseDto() {
    // UUID id = UUID.randomUUID();

    // }
}
