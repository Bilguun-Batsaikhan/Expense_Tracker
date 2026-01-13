package com.example.expense_tracker.Services;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.expense_tracker.Repositories.ExpenseRepository;
import com.example.expense_tracker.Repositories.CategoryRepository;
import com.example.expense_tracker.dto.expense.ExpenseRequestDto;
import com.example.expense_tracker.dto.expense.ExpenseResponseDto;
import com.example.expense_tracker.entities.Category;
import com.example.expense_tracker.entities.Expense;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.enums.ErrorCode;
import com.example.expense_tracker.exceptions.ApiException;
import com.example.expense_tracker.mapper.ExpenseMapper;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseMapper expenseMapper;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(ExpenseService.class);

    public ExpenseService(ExpenseRepository expenseRepository, CategoryRepository categoryRepository,
            ExpenseMapper expenseMapper, UserService userService) {
        this.expenseRepository = expenseRepository;
        this.expenseMapper = expenseMapper;
        this.userService = userService;
        this.categoryRepository = categoryRepository;
    }

    public ExpenseResponseDto create(ExpenseRequestDto req) {
        Expense entity = expenseMapper.toEntity(req);
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
        entity.setCategory(category);
        User user = userService.getCurrentUser();
        entity.setUser(user);
        expenseRepository.save(entity);
        logger.info("Expense is created for for user {}", userService.getCurrentUser().getId());
        return expenseMapper.toDto(entity);
    }

    public ExpenseResponseDto get(UUID id) {
        Expense entity = expenseRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
        if (!validateUser(entity.getUser()))
            throw new ApiException(ErrorCode.AUTHORIZATION_FAILED);
        logger.info("Fetching expense with id={}", id);
        return expenseMapper.toDto(entity);
    }

    public Page<ExpenseResponseDto> getAll(Pageable pageable) {
        User user = userService.getCurrentUser();
        String currentUserEmail = user.getEmail();

        Pageable safePageable = PageRequest.of(
                Math.max(pageable.getPageNumber(), 0),
                Math.min(pageable.getPageSize(), 50),
                Sort.by("expenseDate").descending());

        Page<Expense> page = expenseRepository.findByUserEmailAndDeletedFalse(currentUserEmail, safePageable);

        logger.info("Fetching paged expenses for user: {}", currentUserEmail);
        return page.map(expenseMapper::toDto);
    }

    public ExpenseResponseDto update(ExpenseRequestDto req, UUID id) {
        Expense entity = expenseRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
        if (!validateUser(entity.getUser()))
            throw new ApiException(ErrorCode.AUTHORIZATION_FAILED);

        entity.setAmount(req.getAmount());
        entity.setCurrency(req.getCurrency());
        entity.setDescription(req.getDescription());
        entity.setExpenseDate(req.getExpenseDate());
        if (!entity.getCategory().getId().equals(req.getCategoryId())) {
            Optional<Category> category = categoryRepository.findById(req.getCategoryId());
            if (category.isEmpty())
                throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
            entity.setCategory(category.get());
        }
        logger.info("Expense with id={} is updated", entity.getId());
        return expenseMapper.toDto(entity);
    }

    public ExpenseResponseDto delete(UUID id) {
        Expense entity = expenseRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
        if (!validateUser(entity.getUser()))
            throw new ApiException(ErrorCode.AUTHORIZATION_FAILED);
        ExpenseResponseDto responseDto = expenseMapper.toDto(entity);
        expenseRepository.delete(entity);
        logger.info("Expense with id={} is deleted", responseDto.getId());
        return responseDto;
    }

    public boolean validateUser(User user) {
        return user.getId().equals(userService.getCurrentUser().getId());
    }
}
