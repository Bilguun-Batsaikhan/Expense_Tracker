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
import org.springframework.transaction.annotation.Transactional;

import com.example.expense_tracker.Repositories.CategoryRepository;
import com.example.expense_tracker.dto.CustomUserDetails;
import com.example.expense_tracker.dto.category.CategoryRequestDto;
import com.example.expense_tracker.dto.category.CategoryResponseDto;
import com.example.expense_tracker.entities.Category;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.enums.ErrorCode;
import com.example.expense_tracker.exceptions.ApiException;
import com.example.expense_tracker.mapper.CategoryMapper;

import jakarta.persistence.EntityManager;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final UserService userService;
    private final EntityManager entityManager;

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper,
            UserService userService, EntityManager entityManager) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.userService = userService;
        this.entityManager = entityManager;
    }

    // Admin only
    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));

        logger.info("Admin fetching category with id={}", id);
        return categoryMapper.toDto(category);
    }

    // Admin only
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getCategoriesForAdmin(String userEmail) {
        List<Category> categories = categoryRepository.findAllByUserEmailAndUserEnabledTrue(userEmail);

        // if (categories.isEmpty()) {
        // // Optional: could log a warning here if needed
        // throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
        // }

        logger.info("Admin fetching categories for user email: {}", userEmail);
        return categories.stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponseDto> getCategoriesForCurrentUser(Pageable pageable) {
        if (pageable == null)
            pageable = PageRequest.of(0, 20);
        CustomUserDetails currentUser = userService.getCurrentUserDetails();
        UUID userId = currentUser.getId();

        Sort sort = Sort.by(Sort.Order.desc("isDefault"), Sort.Order.asc("name"));

        // int size = Math.max(1, Math.min(pageable.getPageSize(), 50));

        Pageable safePageable = PageRequest.of(Math.max(pageable.getPageNumber(), 0),
                Math.min(pageable.getPageSize(), 50), sort);

        Page<Category> categories = categoryRepository.findAllByIsDefaultTrueOrUserId(userId, safePageable);

        logger.info("Fetching paged categories for user: {}", currentUser.getEmail());
        return categories.map(categoryMapper::toDto);
    }

    @Transactional
    public CategoryResponseDto createCategoryForCurrentUser(CategoryRequestDto dto) {
        CustomUserDetails currentUser = userService.getCurrentUserDetails();

        if (categoryRepository.existsByNameAndUserId(dto.getName(), currentUser.getId())) {
            throw new ApiException(ErrorCode.DUPLICATE_ENTRY);
        }

        Category category = categoryMapper.toEntity(dto);

        User proxyUser = entityManager.getReference(User.class, currentUser.getId());
        category.setUser(proxyUser);

        Category saved = categoryRepository.save(category);

        logger.info("Category '{}' created for user: {}", saved.getName(), currentUser.getEmail());
        return categoryMapper.toDtoWithEmail(saved, currentUser.getEmail());
    }

    @Transactional
    public CategoryResponseDto updateCategoryForCurrentUser(CategoryRequestDto dto, UUID id) {
        CustomUserDetails currentUser = userService.getCurrentUserDetails();
        Category category = categoryRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));

        if (category.isDefault()) {
            throw new ApiException(ErrorCode.INVALID_INPUT);
        }

        if (!category.getName().equals(dto.getName())
                && categoryRepository.existsByNameAndUserId(dto.getName(), currentUser.getId())) {
            throw new ApiException(ErrorCode.DUPLICATE_ENTRY);
        }

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        logger.info("Category with id={} updated by user: {}", id, currentUser.getEmail());
        return categoryMapper.toDto(category);
    }

    @Transactional
    public String deleteCategoryForCurrentUser(UUID id) {
        CustomUserDetails currentUser = userService.getCurrentUserDetails();
        long deleted = categoryRepository.deleteByIdAndIsDefaultFalseAndUserId(id, currentUser.getId());
        if (deleted == 0)
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
        logger.info("Category with id={} was deleted by user: {}", id, currentUser.getUsername());

        return "Category: " + id + " is deleted";
    }

}