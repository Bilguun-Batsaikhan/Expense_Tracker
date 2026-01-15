package com.example.expense_tracker.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.expense_tracker.Services.CategoryService;
import com.example.expense_tracker.dto.category.CategoryRequestDto;
import com.example.expense_tracker.dto.category.CategoryResponseDto;
import com.example.expense_tracker.dto.pagination.PagedResponse;
import com.example.expense_tracker.dto.pagination.PaginationMetaData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/category")
@Tag(name = "Category Management", description = "Endpoints for managing expense categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Get all categories for current user", description = "Fetches a paged list of custom and default categories.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND")
    })

    @GetMapping
    public ResponseEntity<PagedResponse<CategoryResponseDto>> getCategories(Pageable pageable) {
        Page<CategoryResponseDto> p = categoryService.getCategoriesForCurrentUser(pageable);
        PaginationMetaData pData = new PaginationMetaData(p.getNumber(), p.getSize(), p.getTotalElements(),
                p.getTotalPages(), p.isLast());
        return ResponseEntity.ok(new PagedResponse<>(p.getContent(), pData));
    }

    @Operation(summary = "ADMIN: Get category by id", description = "Fetch any category details by its UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND")
    })

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(summary = "ADMIN: Get categories by user email", description = "Fetch all categories belonging to a specific user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories found"),
            @ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND")
    })
    // /category/admin/user?email=test@example.com
    @GetMapping("/admin/user") // Changed path to avoid conflict with the default GET
    public ResponseEntity<List<CategoryResponseDto>> getCategoryByEmail(@RequestParam String email) {
        return ResponseEntity.ok(categoryService.getCategoriesForAdmin(email));
    }

    @Operation(summary = "Create new category", description = "Creates a custom category for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category created"),
            @ApiResponse(responseCode = "409", description = "DUPLICATE_ENTRY")
    })
    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(@RequestBody CategoryRequestDto requestDto) {
        return ResponseEntity.ok(categoryService.createCategoryForCurrentUser(requestDto));
    }

    @Operation(summary = "Update category", description = "Update name or description. Cannot update default categories.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated"),
            @ApiResponse(description = "FORBIDDEN", responseCode = "403"),
            @ApiResponse(description = "RESOURCE_NOT_FOUND", responseCode = "404"),
            @ApiResponse(description = "INVALID_INPUT (Default Category)", responseCode = "400")
    })

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory(@Valid @RequestBody CategoryRequestDto requestDto,
            @PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.updateCategoryForCurrentUser(requestDto, id));
    }

    @Operation(summary = "Delete category", description = "Deletes a custom category. Cannot delete default categories.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category deleted"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN"),
            @ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND")
    })

    @DeleteMapping("/{id}") // Added /{id} path variable which was missing
    public ResponseEntity<String> deleteCategory(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.deleteCategoryForCurrentUser(id));
    }
}