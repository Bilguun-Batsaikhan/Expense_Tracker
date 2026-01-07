package com.example.expense_tracker.Services;

import java.util.List;
import java.util.UUID;
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

//todo replace getById with findById
@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseMapper expenseMapper;
    private final UserService userService;


    public ExpenseService(ExpenseRepository expenseRepository, CategoryRepository categoryRepository ,ExpenseMapper expenseMapper, UserService userService) {
        this.expenseRepository = expenseRepository;
        this.expenseMapper = expenseMapper;
        this.userService = userService;
        this.categoryRepository = categoryRepository;
    }

    public ExpenseResponseDto create(ExpenseRequestDto req) {
        Expense entity = expenseMapper.toEntity(req);
        Category category = categoryRepository.getById(req.getCategoryId());
        entity.setCategory(category);
        User user = userService.getCurrentUser();
        entity.setUser(user);
        expenseRepository.save(entity);
        return expenseMapper.toDto(entity);
    }

    public ExpenseResponseDto get(UUID id) throws UnauthorizedAccessException, ResourceNotFoundException {
        Expense entity = expenseRepository.getById(id);
        if(entity != null) {
            // compare them by id not by instance!
            if(!entity.getUser().equals(userService.getCurrentUser())) {
                throw new UnauthorizedAccessException();
            }
        } else {
            throw new ResourceNotFoundException();
        }
        return expenseMapper.toDto(entity);
    }

    public List<ExpenseResponseDto> getAll() {
        // instead of loading all expenses configure softdelete in the jpa query, at db level
        return expenseRepository.findAll().stream().filter(e -> !e.isDeleted()).map(expenseMapper::toDto).toList();
    }
    //todo: make validate user a method
    public ExpenseResponseDto update(ExpenseRequestDto req, UUID id) throws UnauthorizedAccessException, ResourceNotFoundException {
        Expense entity = expenseRepository.getById(id);
        if(entity != null) {
            if(!entity.getUser().equals(userService.getCurrentUser())) {
                throw new UnauthorizedAccessException();
            }
        } else {
            throw new ResourceNotFoundException();
        }
        entity.setAmount(req.getAmount());
        entity.setCurrency(req.getCurrency());
        entity.setDescription(req.getDescription());
        entity.setExpenseDate(req.getExpenseDate());
        if(!entity.getCategory().getId().equals(req.getCategoryId())) {
            Category category = categoryRepository.getById(req.getCategoryId());
            entity.setCategory(category);
        }
        return expenseMapper.toDto(entity);
    }
}
