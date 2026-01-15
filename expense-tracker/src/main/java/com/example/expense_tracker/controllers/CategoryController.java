package com.example.expense_tracker.controllers;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.expense_tracker.Services.CategoryService;
import com.example.expense_tracker.dto.category.CategoryRequestDto;
import com.example.expense_tracker.dto.category.CategoryResponseDto;
import com.example.expense_tracker.dto.pagination.PagedResponse;
import com.example.expense_tracker.dto.pagination.PaginationMetaData;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/category")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<CategoryResponseDto>> getCategory(Pageable pageable) {
        Page<CategoryResponseDto> p = categoryService.getCategoriesForCurrentUser(pageable);
        PaginationMetaData pData = new PaginationMetaData(p.getNumber(), p.getSize(), p.getTotalElements(),
                p.getTotalPages(), p.isLast());
        return ResponseEntity.ok(new PagedResponse<>(p.getContent(), pData));
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(@RequestBody CategoryRequestDto requestDto) {
        System.out.println("Received Name: " + requestDto.getName());
        System.out.println("Received Description: " + requestDto.getDescription());
        return ResponseEntity.ok(categoryService.createCategoryForCurrentUser(requestDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory(@Valid @RequestBody CategoryRequestDto requestDto,
            UUID id) {
        return ResponseEntity.ok(categoryService.updateCategoryForCurrentUser(requestDto, id));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteCategory(UUID id) {
        return ResponseEntity.ok(categoryService.deleteCategoryForCurrentUser(id));
    }
}
