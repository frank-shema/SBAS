package com.example.accounting.repository;

import com.example.accounting.model.Account;
import com.example.accounting.model.Invoice;
import com.example.accounting.model.Invoice.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Invoice entity.
 * Provides methods for CRUD operations on invoices and custom query methods.
 * Includes methods for:
 * - Finding invoices with various filters (account, status, client name, date range)
 * - Finding overdue invoices
 * - Counting invoices by status
 * - Pagination support for listing invoices
 * 
 * The repository supports both single account filtering and filtering by a collection
 * of accounts to support different access patterns.
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Page<Invoice> findByAccount(Account account, Pageable pageable);

    Page<Invoice> findByAccountAndStatus(
            Account account, 
            InvoiceStatus status, 
            Pageable pageable);

    Page<Invoice> findByAccountAndClientNameContainingIgnoreCase(
            Account account, 
            String clientName, 
            Pageable pageable);

    Page<Invoice> findByAccountAndDueDateBetween(
            Account account, 
            LocalDate startDate, 
            LocalDate endDate, 
            Pageable pageable);

    Page<Invoice> findByAccountAndStatusAndDueDateBetween(
            Account account, 
            InvoiceStatus status, 
            LocalDate startDate, 
            LocalDate endDate, 
            Pageable pageable);

    Optional<Invoice> findByIdAndAccount(Long id, Account account);

    @Query("SELECT i FROM Invoice i WHERE i.account = :account AND i.status = 'SENT' AND i.dueDate < CURRENT_DATE")
    List<Invoice> findOverdueInvoices(@Param("account") Account account);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.account = :account AND i.status = :status")
    long countByAccountAndStatus(
            @Param("account") Account account, 
            @Param("status") InvoiceStatus status);

    // Methods for filtering by account collection
    Page<Invoice> findByAccountIn(
            Collection<Account> accounts, 
            Pageable pageable);

    Page<Invoice> findByAccountInAndStatus(
            Collection<Account> accounts, 
            InvoiceStatus status, 
            Pageable pageable);

    Page<Invoice> findByAccountInAndClientNameContainingIgnoreCase(
            Collection<Account> accounts, 
            String clientName, 
            Pageable pageable);

    Page<Invoice> findByAccountInAndDueDateBetween(
            Collection<Account> accounts, 
            LocalDate startDate, 
            LocalDate endDate, 
            Pageable pageable);

    Page<Invoice> findByAccountInAndStatusAndDueDateBetween(
            Collection<Account> accounts, 
            InvoiceStatus status, 
            LocalDate startDate, 
            LocalDate endDate, 
            Pageable pageable);

    Page<Invoice> findByAccountInAndClientNameContainingIgnoreCaseAndDueDateBetween(
            Collection<Account> accounts, 
            String clientName, 
            LocalDate startDate, 
            LocalDate endDate, 
            Pageable pageable);

    Page<Invoice> findByAccountInAndStatusAndClientNameContainingIgnoreCase(
            Collection<Account> accounts, 
            InvoiceStatus status, 
            String clientName, 
            Pageable pageable);

    Page<Invoice> findByAccountInAndStatusAndClientNameContainingIgnoreCaseAndDueDateBetween(
            Collection<Account> accounts, 
            InvoiceStatus status, 
            String clientName, 
            LocalDate startDate, 
            LocalDate endDate, 
            Pageable pageable);
}
