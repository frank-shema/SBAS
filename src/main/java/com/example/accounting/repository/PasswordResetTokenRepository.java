package com.example.accounting.repository;

import com.example.accounting.model.PasswordResetToken;
import com.example.accounting.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for PasswordResetToken entity.
 * Provides methods for CRUD operations on password reset tokens and custom query methods.
 * Used in the password reset flow to:
 * - Find tokens by their string value (for validation)
 * - Find tokens by user (to check if a user already has a token)
 * - Delete tokens when they're used or when a new one is created
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUser(User user);

    void deleteByUser(User user);
}
