package com.example.accounting.repository;

import com.example.accounting.model.Budget;
import com.example.accounting.model.Budget.BudgetPeriod;
import com.example.accounting.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Budget entity.
 * Provides methods for CRUD operations on budgets and custom query methods.
 * Includes methods for:
 * - Finding budgets by user, category, and period
 * - Checking if a budget exists for a specific category and period
 * - Calculating total budget amounts for reporting
 * 
 * All methods include user-based filtering to ensure data isolation between users.
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUser(User user);

    List<Budget> findByUserAndPeriod(User user, BudgetPeriod period);

    List<Budget> findByUserAndCategory(User user, String category);

    Optional<Budget> findByIdAndUser(Long id, User user);

    Optional<Budget> findByUserAndCategoryAndPeriod(User user, String category, BudgetPeriod period);

    boolean existsByUserAndCategoryAndPeriod(User user, String category, BudgetPeriod period);

    @Query("SELECT SUM(b.amount) FROM Budget b WHERE b.user = :user AND b.period = :period")
    BigDecimal sumAmountByUserAndPeriod(
            @Param("user") User user, 
            @Param("period") BudgetPeriod period);
}
