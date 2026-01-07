package com.example.expense_tracker.Repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.expense_tracker.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    
}