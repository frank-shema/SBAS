package com.example.accounting.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a password reset token in the system.
 * When a user requests a password reset, a token is generated and stored in this entity.
 * The token is sent to the user's email and can be used to reset the password.
 * 
 * Each token:
 * - Is associated with a specific user
 * - Has an expiry date (typically 24 hours from creation)
 * - Can only be used once
 * 
 * The isExpired() method checks if the token has expired based on the current time.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
