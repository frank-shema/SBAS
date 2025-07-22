package com.example.accounting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;

/**
 * Main entry point for the Small Business Accounting System (SBAS) application.
 * This Spring Boot application provides a comprehensive accounting solution for small businesses,
 * including account management, transaction tracking, invoicing, budgeting, and financial reporting.
 */
@SpringBootApplication
public class AccountingApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountingApplication.class, args);
    }

    /**
     * Prints the application URLs when the application starts up.
     * This makes it easier for users to access the application and its documentation.
     */
    @Bean
    public ApplicationListener<ContextRefreshedEvent> applicationUrlPrinter() {
        return event -> {
            ServletWebServerApplicationContext webServerAppCtxt = (ServletWebServerApplicationContext) event.getApplicationContext();
            Environment env = event.getApplicationContext().getEnvironment();
            int port = webServerAppCtxt.getWebServer().getPort();
            String contextPath = env.getProperty("server.servlet.context-path", "");

            System.out.println("\n--------------------------------------------------------------");
            System.out.println("Application is running! Access URLs:");
            System.out.println("Local: \thttp://localhost:" + port + contextPath);
            System.out.println("Swagger UI: \thttp://localhost:" + port + contextPath + "/swagger-ui/index.html");
            System.out.println("API Docs: \thttp://localhost:" + port + contextPath + "/api-docs");
            System.out.println("--------------------------------------------------------------\n");
        };
    }
}
