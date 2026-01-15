package com.example.expense_tracker.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.expense_tracker.dto.category.CategoryRequestDto;
import com.example.expense_tracker.dto.category.CategoryResponseDto;
import com.example.expense_tracker.entities.Category;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {
    @Mapping(source = "user.email", target = "createdBy")
    public CategoryResponseDto toDto(Category category);

    public Category toEntity(CategoryRequestDto categoryRequestDto);

    @Mapping(source = "category.name", target = "name")
    @Mapping(source = "email", target = "createdBy")
    CategoryResponseDto toDtoWithEmail(Category category, String email);
}
