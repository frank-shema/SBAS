package com.example.accounting.repository;

import com.example.accounting.model.Account;
import com.example.accounting.model.Transaction;
import com.example.accounting.model.Transaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Transaction entity.
 * Provides methods for CRUD operations on transactions and custom query methods.
 * Includes methods for:
 * - Finding transactions with various filters (account, date range, category, type)
 * - Calculating sums of transaction amounts for reporting
 * - Pagination support for listing transactions
 * 
 * All methods include account-based filtering to ensure data isolation between accounts.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByAccount(Account account, Pageable pageable);

    Page<Transaction> findByAccountAndDateBetween(
            Account account, 
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            Pageable pageable);

    Page<Transaction> findByAccountAndCategory(
            Account account, 
            String category, 
            Pageable pageable);

    Page<Transaction> findByAccountAndType(
            Account account, 
            TransactionType type, 
            Pageable pageable);

    Page<Transaction> findByAccountAndCategoryAndDateBetween(
            Account account, 
            String category, 
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            Pageable pageable);

    Page<Transaction> findByAccountAndTypeAndDateBetween(
            Account account, 
            TransactionType type, 
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            Pageable pageable);

    Optional<Transaction> findByIdAndAccount(Long id, Account account);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.account = :account AND t.type = :type AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByAccountAndTypeAndDateBetween(
            @Param("account") Account account, 
            @Param("type") TransactionType type, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.account = :account AND t.type = :type AND t.category = :category AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByAccountAndTypeAndCategoryAndDateBetween(
            @Param("account") Account account, 
            @Param("type") TransactionType type, 
            @Param("category") String category, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);

    List<Transaction> findByAccountAndDateBetweenOrderByDateDesc(
            Account account, 
            LocalDateTime startDate, 
            LocalDateTime endDate);

    Page<Transaction> findByAccountIn(
            Collection<Account> accounts, 
            Pageable pageable);

    Page<Transaction> findByAccountAndCategoryAndTypeAndDateBetween(
            Account account, 
            String category, 
            TransactionType type, 
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            Pageable pageable);
}
