package com.example.expense_tracker.controllers;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.expense_tracker.Services.ExpenseService;
import com.example.expense_tracker.dto.expense.ExpenseRequestDto;
import com.example.expense_tracker.dto.expense.ExpenseResponseDto;
import com.example.expense_tracker.dto.pagination.PagedResponse;
import com.example.expense_tracker.dto.pagination.PaginationMetaData;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/expense")
public class ExpenseController {
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponseDto> createExpense(
            @Valid @RequestBody ExpenseRequestDto request) {
        ExpenseResponseDto saved = expenseService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponseDto> getExpense(@Valid @PathVariable UUID id) {
        // The controller only returns 200 OK if the service does NOT throw an
        // exception.
        // If an exception is thrown, the controller method never completes.
        return ResponseEntity.ok(expenseService.get(id));
    }

    @Operation(summary = "Get expenses with pagination")
    @GetMapping
    public ResponseEntity<PagedResponse<ExpenseResponseDto>> getExpense(Pageable pageable) {
        Page<ExpenseResponseDto> page = expenseService.getAll(pageable);
        PaginationMetaData pData = new PaginationMetaData(page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isLast());
        return ResponseEntity.ok(new PagedResponse<>(page.getContent(), pData));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponseDto> updateExpense(@Valid @PathVariable UUID id,
            @RequestBody ExpenseRequestDto req) {
        return ResponseEntity.ok(expenseService.update(req, id));
    }
}
