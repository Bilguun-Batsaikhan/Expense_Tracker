package com.example.expense_tracker.Repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.expense_tracker.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findAllByUserEmail(String email);

    List<Category> findAllByUserEmailAndUserEnabledTrue(String email);

    boolean existsByNameAndUserEmail(String name, String email);
}