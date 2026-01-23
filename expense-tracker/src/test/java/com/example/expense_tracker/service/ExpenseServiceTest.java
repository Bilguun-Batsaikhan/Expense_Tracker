package com.example.expense_tracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.expense_tracker.Repositories.CategoryRepository;
import com.example.expense_tracker.Repositories.ExpenseRepository;
import com.example.expense_tracker.Services.ExpenseService;
import com.example.expense_tracker.Services.UserService;
import com.example.expense_tracker.dto.CustomUserDetails;
import com.example.expense_tracker.dto.expense.ExpenseRequestDto;
import com.example.expense_tracker.dto.expense.ExpenseResponseDto;
import com.example.expense_tracker.entities.Category;
import com.example.expense_tracker.entities.Expense;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.enums.ErrorCode;
import com.example.expense_tracker.exceptions.ApiException;
import com.example.expense_tracker.mapper.ExpenseMapper;

import jakarta.persistence.EntityManager;

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
        @Mock
        private EntityManager entityManager;
        @InjectMocks
        private ExpenseService expenseService;

        // create when missing Category
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

        // create success
        @Test
        void create_whenSuccess_returnsExpenseResponseDto() {
                // Arrange
                UUID expenseId = UUID.randomUUID();
                UUID categoryId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();

                ExpenseRequestDto expenseRequestDto = new ExpenseRequestDto();
                expenseRequestDto.setCategoryId(categoryId);

                Expense entity = new Expense();
                entity.setId(expenseId);
                when(expenseMapper.toEntity(expenseRequestDto)).thenReturn(entity);
                Category category = new Category();
                category.setId(categoryId);
                when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
                entity.setCategory(category);
                CustomUserDetails customUserDetails = mock(CustomUserDetails.class);
                when(userService.getCurrentUserDetails()).thenReturn(customUserDetails);
                when(customUserDetails.getId()).thenReturn(userId);
                User userRef = new User();
                userRef.setId(userId);
                when(entityManager.getReference(User.class, userId)).thenReturn(userRef);
                entity.setUser(userRef);
                when(expenseRepository.save(entity)).thenReturn(entity);
                ExpenseResponseDto eDto = new ExpenseResponseDto();
                eDto.setId(expenseId);
                eDto.setCategoryId(categoryId);
                when(expenseMapper.toDto(entity)).thenReturn(eDto);
                // Act
                ExpenseResponseDto result = expenseService.create(expenseRequestDto);
                // Assert
                assertNotNull(result);
                assertEquals(result.getId(), entity.getId());
                assertEquals(result.getCategoryId(), category.getId());
        }

        // get when wrong id
        @Test
        void getExpenseForCurrentUser_whenExpenseMissing_throwsApiExceptionWithErrorCodeResourceNotFound() {
                UUID id = UUID.randomUUID();
                UUID userId = UUID.randomUUID();
                CustomUserDetails userDetails = mock(CustomUserDetails.class);
                when(userService.getCurrentUserDetails()).thenReturn(userDetails);
                when(userDetails.getId()).thenReturn(userId);
                when(expenseRepository.findByIdAndUserIdAndDeletedFalse(id, userId)).thenReturn(Optional.empty());
                // Execute the method, Expect it to fail, Capture the thrown exception
                ApiException ex = assertThrows(ApiException.class, () -> expenseService.getExpenseForCurrentUser(id));

                assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        void getExpenseForCurrentUser_whenSuccess_returnsExpenseResponseDto() {
                UUID userId = UUID.randomUUID();
                UUID expenseId = UUID.randomUUID();

                CustomUserDetails userDetails = mock(CustomUserDetails.class);
                when(userDetails.getId()).thenReturn(userId);
                when(userService.getCurrentUserDetails()).thenReturn(userDetails);

                Expense entity = new Expense();
                entity.setId(expenseId);

                when(expenseRepository.findByIdAndUserIdAndDeletedFalse(expenseId, userId))
                                .thenReturn(Optional.of(entity));

                ExpenseResponseDto mappedDto = new ExpenseResponseDto();
                mappedDto.setId(expenseId);
                when(expenseMapper.toDto(entity)).thenReturn(mappedDto);

                ExpenseResponseDto result = expenseService.getExpenseForCurrentUser(expenseId);

                assertNotNull(result);
                assertEquals(expenseId, result.getId());

                verify(userService).getCurrentUserDetails();
                verify(expenseRepository).findByIdAndUserIdAndDeletedFalse(expenseId, userId);
                verify(expenseMapper).toDto(entity);
        }

        @Test
        void getAll_whenSuccess_returnsPagedExpenseResponseDto() {
                // arrange
                UUID userId = UUID.randomUUID();
                UUID expenseId = UUID.randomUUID();

                CustomUserDetails userDetails = mock(CustomUserDetails.class);
                when(userDetails.getId()).thenReturn(userId);
                when(userDetails.getEmail()).thenReturn("test@example.com");
                when(userService.getCurrentUserDetails()).thenReturn(userDetails);

                Expense entity = new Expense();
                entity.setId(expenseId);

                ExpenseResponseDto dto = new ExpenseResponseDto();
                dto.setId(expenseId);
                when(expenseMapper.toDto(entity)).thenReturn(dto);

                // input pageable (could be unsafe, service will clamp it anyway)
                Pageable input = PageRequest.of(0, 50);

                Pageable safe = PageRequest.of(0, 50, Sort.by("expenseDate").descending());
                Page<Expense> repoPage = new PageImpl<>(List.of(entity), safe, 1);

                when(expenseRepository.findByUserIdAndDeletedFalse(userId, safe))
                                .thenReturn(repoPage);

                // act
                Page<ExpenseResponseDto> result = expenseService.getAll(input);

                // assert
                assertNotNull(result);
                assertEquals(1, result.getTotalElements());
                assertEquals(1, result.getContent().size());
                assertEquals(expenseId, result.getContent().get(0).getId());

                verify(userService).getCurrentUserDetails();
                verify(expenseRepository).findByUserIdAndDeletedFalse(userId, safe);
                verify(expenseMapper).toDto(entity);
        }

        @Test
        void update_whenExpenseNotFound_throwsApiExceptionWithErrorCodeResourceNotFound() {
                // Arrange
                CustomUserDetails customUserDetails = mock(CustomUserDetails.class);
                when(userService.getCurrentUserDetails()).thenReturn(customUserDetails);

                UUID expenseId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();
                when(customUserDetails.getId()).thenReturn(userId);

                Expense entity = new Expense();
                entity.setId(expenseId);
                when(expenseRepository.findByIdAndUserIdAndDeletedFalse(expenseId, userId))
                                .thenReturn(Optional.empty());

                ExpenseRequestDto requestDto = new ExpenseRequestDto();
                // Act
                ApiException ex = assertThrows(ApiException.class, () -> expenseService.update(requestDto, expenseId));

                assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
                verify(expenseRepository).findByIdAndUserIdAndDeletedFalse(expenseId, userId);
                verifyNoInteractions(categoryRepository, expenseMapper); // if expense doesn’t exist, you should not be
                                                                         // hitting category lookup or mapping.
        }

        @Test
        void update_whenCategoryNotFound_throwsApiExceptionWithErrorCodeResourceNotFound() {
                // Arrange
                CustomUserDetails customUserDetails = mock(CustomUserDetails.class);
                when(userService.getCurrentUserDetails()).thenReturn(customUserDetails);

                UUID expenseId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();
                when(customUserDetails.getId()).thenReturn(userId);

                Expense entity = new Expense();
                entity.setId(expenseId);
                when(expenseRepository.findByIdAndUserIdAndDeletedFalse(expenseId, userId))
                                .thenReturn(Optional.of(entity));
                ExpenseRequestDto requestDto = new ExpenseRequestDto();
                UUID categoryId = UUID.randomUUID();
                requestDto.setCategoryId(categoryId);
                when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());
                // Act
                ApiException ex = assertThrows(ApiException.class, () -> expenseService.update(requestDto, expenseId));
                assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
                verify(expenseRepository).findByIdAndUserIdAndDeletedFalse(expenseId, userId);
                verify(categoryRepository).findById(categoryId);

                verifyNoInteractions(expenseMapper);
                verifyNoMoreInteractions(expenseRepository, categoryRepository);
        }

        @Test
        void update_whenSuccess_returnsUpdatedExpenseResponseDto() {
                // arrange
                UUID userId = UUID.randomUUID();
                UUID expenseId = UUID.randomUUID();
                UUID categoryIdOriginal = UUID.randomUUID();
                UUID categoryIdUpdated = UUID.randomUUID();

                CustomUserDetails userDetails = mock(CustomUserDetails.class);
                when(userDetails.getId()).thenReturn(userId);
                when(userService.getCurrentUserDetails()).thenReturn(userDetails);

                Category categoryOriginal = new Category();
                categoryOriginal.setId(categoryIdOriginal);

                Expense entity = new Expense();
                entity.setId(expenseId);
                entity.setCategory(categoryOriginal);
                entity.setAmount(BigDecimal.ZERO);
                entity.setCurrency("EUR");
                entity.setDescription("Test_1");
                entity.setExpenseDate(LocalDate.of(1997, 3, 6));

                when(expenseRepository.findByIdAndUserIdAndDeletedFalse(expenseId, userId))
                                .thenReturn(Optional.of(entity));

                Category categoryUpdated = new Category();
                categoryUpdated.setId(categoryIdUpdated);
                when(categoryRepository.findById(categoryIdUpdated)).thenReturn(Optional.of(categoryUpdated));

                ExpenseRequestDto requestDto = new ExpenseRequestDto();
                requestDto.setCategoryId(categoryIdUpdated);
                requestDto.setAmount(BigDecimal.ONE);
                requestDto.setCurrency("USD");
                requestDto.setDescription("Test_2");
                LocalDate newDate = LocalDate.now();
                requestDto.setExpenseDate(newDate);

                ExpenseResponseDto responseDto = new ExpenseResponseDto();
                responseDto.setId(expenseId);
                when(expenseMapper.toDto(entity)).thenReturn(responseDto);

                // act
                ExpenseResponseDto result = expenseService.update(requestDto, expenseId);

                // assert (returned dto)
                assertNotNull(result);
                assertEquals(expenseId, result.getId());

                // assert (entity updated)
                assertEquals(categoryIdUpdated, entity.getCategory().getId());
                assertEquals(BigDecimal.ONE, entity.getAmount());
                assertEquals("USD", entity.getCurrency());
                assertEquals("Test_2", entity.getDescription());
                assertEquals(newDate, entity.getExpenseDate());

                verify(expenseRepository).findByIdAndUserIdAndDeletedFalse(expenseId, userId);
                verify(categoryRepository).findById(categoryIdUpdated);
                verify(expenseMapper).toDto(entity);
        }

        @Test
        void delete_whenExpenseNotFound_throwApiExceptionWithErrorCodeResourceNotFound() {
                UUID expenseId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();

                CustomUserDetails userDetails = mock(CustomUserDetails.class);
                when(userDetails.getId()).thenReturn(userId);
                when(userService.getCurrentUserDetails()).thenReturn(userDetails);

                when(expenseRepository.findByIdAndUserIdAndDeletedFalse(expenseId, userId))
                                .thenReturn(Optional.empty());

                ApiException ex = assertThrows(ApiException.class, () -> expenseService.delete(expenseId));

                assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
                verify(userService).getCurrentUserDetails();
                verify(expenseRepository).findByIdAndUserIdAndDeletedFalse(expenseId, userId);
                verifyNoInteractions(expenseMapper);
        }

        @Test
        void delete_whenSuccess_returnsDeletedExpenseResponseDto() {
                // arrange
                UUID expenseId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();

                CustomUserDetails userDetails = mock(CustomUserDetails.class);
                when(userDetails.getId()).thenReturn(userId);
                when(userService.getCurrentUserDetails()).thenReturn(userDetails);

                Expense entity = new Expense();
                entity.setId(expenseId);
                entity.setDeleted(false);

                when(expenseRepository.findByIdAndUserIdAndDeletedFalse(expenseId, userId))
                                .thenReturn(Optional.of(entity));

                ExpenseResponseDto responseDto = new ExpenseResponseDto();
                responseDto.setId(expenseId);
                when(expenseMapper.toDto(entity)).thenReturn(responseDto);

                // act
                ExpenseResponseDto result = expenseService.delete(expenseId);

                // assert
                assertNotNull(result);
                assertEquals(expenseId, result.getId());
                assertTrue(entity.isDeleted());

                verify(expenseRepository).findByIdAndUserIdAndDeletedFalse(expenseId, userId);
                verify(expenseMapper).toDto(entity);
        }

}
