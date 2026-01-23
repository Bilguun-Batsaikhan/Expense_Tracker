package com.example.expense_tracker.Repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.expense_tracker.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Page<Category> findAllByIsDefaultTrueOrUserId(UUID userId, Pageable pageable);

    List<Category> findAllByUserEmailAndUserEnabledTrue(String email);

    boolean existsByNameAndUserId(String name, UUID userId);

    // boolean existsByNameAndUserEmail(String name, String email);

    Optional<Category> findByIdAndUserId(UUID id, UUID userId);

    long deleteByIdAndIsDefaultFalseAndUserId(UUID id, UUID userId);
}