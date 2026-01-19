package com.example.expense_tracker.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.example.expense_tracker.entities.Category;
import com.example.expense_tracker.entities.Expense;
import com.example.expense_tracker.entities.User;

public class TestDataFactory {

    private final TestEntityManager em;

    public TestDataFactory(TestEntityManager em) {
        this.em = em;
    }

    public User user(String email) {
        return em.persist(User.builder()
                .email(email)
                .password("pw")
                .fullName("Test User")
                .build());
    }

    public Category category(User user, String name) {
        return em.persist(Category.builder()
                .name(name)
                .user(user)
                .build());
    }

    public Expense expense(User user, Category category) {
        return Expense.builder()
                .amount(new BigDecimal("10.00"))
                .currency("EUR")
                .description("Test expense")
                .expenseDate(LocalDate.of(2026, 1, 1))
                .user(user)
                .category(category)
                .build();
    }

    public void flushAndClear() {
        em.flush();
        em.clear();
    }
}
