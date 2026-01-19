package com.example.expense_tracker.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.example.expense_tracker.Repositories.ExpenseRepository;
import com.example.expense_tracker.entities.Category;
import com.example.expense_tracker.entities.Expense;
import com.example.expense_tracker.entities.User;

// Repository tests exist to prove mapping + constraints + queries, not business logic.
@DataJpaTest
// @AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) this
// is optional now
class ExpenseRepositoryTest {

        @Autowired
        TestEntityManager em;

        @Autowired
        ExpenseRepository expenseRepository;

        TestDataFactory data;

        record Fixture(User user, Category category, Expense expense) {
        }

        @BeforeEach
        void setup() {
                data = new TestDataFactory(em);
        }

        private Fixture givenUserWithCategoryAndExpense() {
                User user = data.user("test@test.com");
                Category category = data.category(user, "my food category");
                Expense expense = data.expense(user, category);
                return new Fixture(user, category, expense);
        }

        private Fixture givenSavedUserWithCategoryAndExpense() {
                Fixture fx = givenUserWithCategoryAndExpense();
                Expense saved = expenseRepository.save(fx.expense());
                data.flushAndClear(); // avoid false positives from persistence context
                return new Fixture(fx.user(), fx.category(), saved);
        }

        @Test
        void save_ReturnSavedExpense() {
                // Arrange
                Fixture fx = givenUserWithCategoryAndExpense();

                // Act
                Expense result = expenseRepository.save(fx.expense());

                // Assert
                Assertions.assertNotNull(result);
                Assertions.assertNotNull(result.getId());
                Assertions.assertEquals(fx.user().getId(), result.getUser().getId());
                Assertions.assertEquals(fx.category().getId(), result.getCategory().getId());
        }

        @Test
        void findByIdAndUserIdAndDeletedFalse_ReturnsExpense() {
                // Arrange
                Fixture fx = givenSavedUserWithCategoryAndExpense();

                // Act
                Expense result = expenseRepository
                                .findByIdAndUserIdAndDeletedFalse(fx.expense().getId(), fx.user().getId())
                                .orElseThrow();

                // Assert
                Assertions.assertEquals(fx.expense().getId(), result.getId());
                Assertions.assertFalse(result.isDeleted());
        }

        @Test
        void findByUserIdAndDeletedFalse_ReturnsPagedExpense() {
                // Arrange
                Fixture fx = givenSavedUserWithCategoryAndExpense();
                Pageable pageable = PageRequest.of(0, 1);

                // Act
                Page<Expense> result = expenseRepository.findByUserIdAndDeletedFalse(fx.user().getId(), pageable);

                // Assert
                Assertions.assertNotNull(result);
                Assertions.assertEquals(1, result.getNumberOfElements());
        }
}
