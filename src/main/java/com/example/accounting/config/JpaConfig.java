package com.example.accounting.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration class for JPA.
 * Enables JPA auditing which automatically manages entity timestamps (createdAt, updatedAt)
 * for entities that use @CreatedDate and @LastModifiedDate annotations.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // No additional configuration needed
}