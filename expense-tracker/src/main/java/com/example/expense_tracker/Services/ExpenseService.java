package com.example.expense_tracker.Services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.expense_tracker.Repositories.ExpenseRepository;
import com.example.expense_tracker.Exceptions.ResourceNotFoundException;
import com.example.expense_tracker.Exceptions.UnauthorizedAccessException;
import com.example.expense_tracker.Repositories.CategoryRepository;
import com.example.expense_tracker.dto.expense.ExpenseRequestDto;
import com.example.expense_tracker.dto.expense.ExpenseResponseDto;
import com.example.expense_tracker.entities.Category;
import com.example.expense_tracker.entities.Expense;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.mapper.ExpenseMapper;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseMapper expenseMapper;
    private final UserService userService;

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
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + req.getCategoryId()));
        entity.setCategory(category);
        User user = userService.getCurrentUser();
        entity.setUser(user);
        expenseRepository.save(entity);
        return expenseMapper.toDto(entity);
    }

    public ExpenseResponseDto get(UUID id) {
        Expense entity = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        if (!validateUser(entity.getUser()))
            throw new UnauthorizedAccessException("User with id: " + entity.getUser().getId() + "is not authorized");
        return expenseMapper.toDto(entity);
    }

    public Page<ExpenseResponseDto> getAll(Pageable pageable) {
        Pageable safePageable = PageRequest.of(
                Math.max(pageable.getPageNumber(), 0),
                Math.min(pageable.getPageSize(), 50),
                Sort.by("expenseDate").descending());

        // for now retrieve all regardless of the user, like an admin. Future return
        // only for users.
        Page<Expense> page = expenseRepository.findByDeletedFalse(safePageable);
        // return page.getContent().stream().map(expenseMapper::toDto).toList();
        return page.map(expenseMapper::toDto);
    }

    public ExpenseResponseDto update(ExpenseRequestDto req, UUID id)
            throws UnauthorizedAccessException, ResourceNotFoundException {
        Expense entity = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        if (!validateUser(entity.getUser()))
            throw new UnauthorizedAccessException("User with id: " + entity.getUser().getId() + "is not authorized");

        entity.setAmount(req.getAmount());
        entity.setCurrency(req.getCurrency());
        entity.setDescription(req.getDescription());
        entity.setExpenseDate(req.getExpenseDate());
        if (!entity.getCategory().getId().equals(req.getCategoryId())) {
            Optional<Category> category = categoryRepository.findById(req.getCategoryId());
            if (category.isEmpty())
                throw new ResourceNotFoundException();
            entity.setCategory(category.get());
        }
        return expenseMapper.toDto(entity);
    }

    public boolean validateUser(User user) {
        return user.getId().equals(userService.getCurrentUser().getId());
    }
}
