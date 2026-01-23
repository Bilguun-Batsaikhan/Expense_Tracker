package com.example.expense_tracker.Services;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.expense_tracker.Repositories.ExpenseRepository;
import com.example.expense_tracker.Repositories.CategoryRepository;
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

@Service
public class ExpenseService {
    private final EntityManager entityManager;
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseMapper expenseMapper;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(ExpenseService.class);

    public ExpenseService(ExpenseRepository expenseRepository, CategoryRepository categoryRepository,
            ExpenseMapper expenseMapper, UserService userService, EntityManager entityManager) {
        this.expenseRepository = expenseRepository;
        this.expenseMapper = expenseMapper;
        this.userService = userService;
        this.categoryRepository = categoryRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public ExpenseResponseDto create(ExpenseRequestDto req) {
        Expense entity = expenseMapper.toEntity(req);
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
        entity.setCategory(category);
        CustomUserDetails currentUser = userService.getCurrentUserDetails();
        UUID userId = currentUser.getId();
        User userRef = entityManager.getReference(User.class, userId);
        entity.setUser(userRef);

        expenseRepository.save(entity);
        logger.info("Expense is created for for user {}", currentUser.getEmail());
        return expenseMapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public ExpenseResponseDto getExpenseForCurrentUser(UUID id) {
        CustomUserDetails currentUser = userService.getCurrentUserDetails();
        Expense entity = expenseRepository.findByIdAndUserIdAndDeletedFalse(id, currentUser.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
        logger.info("Fetching expense with id={}", id);
        return expenseMapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponseDto> getAll(Pageable pageable) {
        CustomUserDetails currentUser = userService.getCurrentUserDetails();
        String currentUserEmail = currentUser.getEmail();

        Pageable safePageable = PageRequest.of(
                Math.max(pageable.getPageNumber(), 0),
                Math.min(pageable.getPageSize(), 50),
                Sort.by("expenseDate").descending());

        Page<Expense> page = expenseRepository.findByUserIdAndDeletedFalse(currentUser.getId(), safePageable);

        logger.info("Fetching paged expenses for user: {}", currentUserEmail);
        return page.map(expenseMapper::toDto);
    }

    @Transactional
    public ExpenseResponseDto update(ExpenseRequestDto req, UUID id) {
        CustomUserDetails currentuser = userService.getCurrentUserDetails();
        Expense entity = expenseRepository.findByIdAndUserIdAndDeletedFalse(id, currentuser.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));

        entity.setAmount(req.getAmount());
        entity.setCurrency(req.getCurrency());
        entity.setDescription(req.getDescription());
        entity.setExpenseDate(req.getExpenseDate());

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
        entity.setCategory(category);

        logger.info("Expense with id={} is updated", entity.getId());
        return expenseMapper.toDto(entity);
    }

    @Transactional
    public ExpenseResponseDto delete(UUID id) {
        CustomUserDetails currentuser = userService.getCurrentUserDetails();
        Expense entity = expenseRepository.findByIdAndUserIdAndDeletedFalse(id, currentuser.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));

        ExpenseResponseDto responseDto = expenseMapper.toDto(entity);
        entity.setDeleted(true);
        logger.info("Expense with id={} is deleted", responseDto.getId());
        return responseDto;
    }
}
