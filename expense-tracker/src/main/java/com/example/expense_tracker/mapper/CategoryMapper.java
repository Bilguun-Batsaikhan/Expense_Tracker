package com.example.expense_tracker.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.expense_tracker.dto.category.CategoryRequestDto;
import com.example.expense_tracker.dto.category.CategoryResponseDto;
import com.example.expense_tracker.entities.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(source = "user.email", target = "createdBy")
    public CategoryResponseDto toDto(Category category);

    public Category toEntity(CategoryRequestDto categoryRequestDto);
}
