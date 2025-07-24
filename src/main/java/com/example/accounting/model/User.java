package com.example.accounting.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a user in the system.
 * Users can have one of three roles: OWNER, ACCOUNTANT, or EMPLOYEE.
 * - OWNER: Has full access to all features
 * - ACCOUNTANT: Has read-only access to reports and accounts, with limited write capabilities
 * - EMPLOYEE: Has limited access to basic features needed for day-to-day operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "username"),
           @UniqueConstraint(columnNames = "email")
       })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    private String username;

    @NotBlank
    @Size(max = 100)
    @Email
    private String email;

    @NotBlank
    @Size(max = 120)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role {
        OWNER,
        ACCOUNTANT,
        EMPLOYEE
    }
}
