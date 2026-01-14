package com.example.expense_tracker.controllers;

import java.util.List;
import java.util.UUID;

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
    public ResponseEntity<List<CategoryResponseDto>> getCategory() {
        return ResponseEntity.ok(categoryService.getCategoriesForCurrentUser());
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
