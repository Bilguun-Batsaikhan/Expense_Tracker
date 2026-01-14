package com.example.expense_tracker.Services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.expense_tracker.Repositories.CategoryRepository;
import com.example.expense_tracker.dto.category.CategoryRequestDto;
import com.example.expense_tracker.dto.category.CategoryResponseDto;
import com.example.expense_tracker.entities.Category;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.enums.ErrorCode;
import com.example.expense_tracker.exceptions.ApiException;
import com.example.expense_tracker.mapper.CategoryMapper;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final UserService userService;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper,
            UserService userService) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.userService = userService;
    }

    // Only for ADMIN role usage, will update later when roles are added
    public CategoryResponseDto getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
        return categoryMapper.toDto(category);
    }

    // Only for ADMIN role usage, will update later when roles are added
    public List<CategoryResponseDto> getCategoriesForAdmin(String userEmail) {
        // 1. Fetch only categories for the specific user where that user is not
        // "soft-deleted"
        List<Category> categories = categoryRepository.findAllByUserEmailAndUserEnabledTrue(userEmail);

        if (categories.isEmpty()) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        return categories.stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    public List<CategoryResponseDto> getCategoriesForCurrentUser() {
        String email = userService.getCurrentUser().getEmail();
        List<Category> categories = categoryRepository.findAllByUserEmail(email);
        if (categories.isEmpty()) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return categories
                .stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    public CategoryResponseDto createCategoryForCurrentUser(CategoryRequestDto dto) {
        User currentUser = userService.getCurrentUser();

        if (categoryRepository.existsByNameAndUserEmail(dto.getName(), currentUser.getEmail())) {
            throw new ApiException(ErrorCode.DUPLICATE_ENTRY);
        }
        Category category = categoryMapper.toEntity(dto);
        System.out.println("Mapped Category Name: " + category.getName());
        category.setUser(currentUser);

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }

    public CategoryResponseDto updateCategoryForCurrentUser(CategoryRequestDto dto, UUID id) {
        User currentUser = userService.getCurrentUser();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
        if (!currentUser.getEmail().equals(category.getUser().getEmail()))
            throw new ApiException(ErrorCode.FORBIDDEN);
        if (category.isDefault())
            new ApiException(ErrorCode.INVALID_INPUT);
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return categoryMapper.toDto(category);
    }

    public String deleteCategoryForCurrentUser(UUID id) {
        String currentUserEmail = userService.getCurrentUser().getEmail();

        // 1. Fetch the category first
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
        // 2. Ownership Check: Is this the user's category?
        // (Admins might be bypassed here)
        if (!category.getUser().getEmail().equals(currentUserEmail) || category.isDefault()) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        categoryRepository.delete(category);
        return "Category: " + id + " is deleted";
    }
}
