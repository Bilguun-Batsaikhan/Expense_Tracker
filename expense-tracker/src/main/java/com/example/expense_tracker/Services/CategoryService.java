package com.example.expense_tracker.Services;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper,
            UserService userService) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.userService = userService;
    }

    public CategoryResponseDto getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));

        logger.info("Fetching category with id={}", id);
        return categoryMapper.toDto(category);
    }

    public List<CategoryResponseDto> getCategoriesForAdmin(String userEmail) {
        List<Category> categories = categoryRepository.findAllByUserEmailAndUserEnabledTrue(userEmail);

        if (categories.isEmpty()) {
            // Optional: could log a warning here if needed
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        logger.info("Admin fetching categories for user email: {}", userEmail);
        return categories.stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    public Page<CategoryResponseDto> getCategoriesForCurrentUser(Pageable pageable) {
        String email = userService.getCurrentUser().getEmail();
        Pageable safPageable = PageRequest.of(Math.max(pageable.getPageNumber(), 0),
                Math.min(pageable.getPageSize(), 50), Sort.by("name"));

        Page<Category> categories = categoryRepository.findAllByIsDefaultTrueOrUserEmail(email, safPageable);

        if (categories.isEmpty()) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        logger.info("Fetching paged categories for user: {}", email);
        return categories.map(categoryMapper::toDto);
    }

    public CategoryResponseDto createCategoryForCurrentUser(CategoryRequestDto dto) {
        User currentUser = userService.getCurrentUser();

        if (categoryRepository.existsByNameAndUserEmail(dto.getName(), currentUser.getEmail())) {
            throw new ApiException(ErrorCode.DUPLICATE_ENTRY);
        }

        Category category = categoryMapper.toEntity(dto);
        category.setUser(currentUser);

        Category savedCategory = categoryRepository.save(category);

        logger.info("Category '{}' created for user: {}", savedCategory.getName(), currentUser.getEmail());
        return categoryMapper.toDto(savedCategory);
    }

    public CategoryResponseDto updateCategoryForCurrentUser(CategoryRequestDto dto, UUID id) {
        User currentUser = userService.getCurrentUser();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!currentUser.getEmail().equals(category.getUser().getEmail()))
            throw new ApiException(ErrorCode.FORBIDDEN);

        if (category.isDefault())
            throw new ApiException(ErrorCode.INVALID_INPUT);

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        logger.info("Category with id={} updated by user: {}", id, currentUser.getEmail());
        return categoryMapper.toDto(category);
    }

    public String deleteCategoryForCurrentUser(UUID id) {
        String currentUserEmail = userService.getCurrentUser().getEmail();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!category.getUser().getEmail().equals(currentUserEmail) || category.isDefault()) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        categoryRepository.delete(category);

        logger.info("Category with id={} was deleted by user: {}", id, currentUserEmail);
        return "Category: " + id + " is deleted";
    }
}