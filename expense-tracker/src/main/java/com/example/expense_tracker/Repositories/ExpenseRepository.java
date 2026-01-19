package com.example.expense_tracker.Repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.expense_tracker.entities.Expense;
import com.example.expense_tracker.entities.User;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    // Spring Data navigates from Expense -> User -> email
    public Page<Expense> findByUserEmailAndDeletedFalse(String email, Pageable pageable);

    public Page<Expense> findByUserIdAndDeletedFalse(UUID userId, Pageable pageable);

    // returns a Expense that is not deleted
    public Optional<Expense> findByIdAndUserIdAndDeletedFalse(UUID ExpenseId, UUID userId);

    // returns all Expense that are not deleted, used by only admin
    public Page<Expense> findByDeletedFalse(Pageable pageable);

    // expense entity has User object nested but does it work?
    public Page<Expense> findByUserAndDeletedFalse(User user, Pageable pageable);
}
