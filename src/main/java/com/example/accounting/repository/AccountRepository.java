package com.example.accounting.repository;

import com.example.accounting.model.Account;
import com.example.accounting.model.Account.AccountType;
import com.example.accounting.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser(User user);
    
    List<Account> findByUserAndType(User user, AccountType type);
    
    Optional<Account> findByIdAndUser(Long id, User user);
    
    boolean existsByIdAndUser(Long id, User user);
    
    boolean existsByNameAndUser(String name, User user);
}