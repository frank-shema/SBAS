# Small Business Accounting System (SBAS) - Codebase Explanation

## Overview

The Small Business Accounting System (SBAS) is a comprehensive web-based application designed for small businesses to manage their financial operations. It provides functionality for managing accounts, transactions, invoices, budgets, and generating financial reports.

## Technology Stack

- **Backend**: Spring Boot 3.x with Maven
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Spring Security with JWT
- **Documentation**: Swagger/OpenAPI
- **Export/Import**: Apache POI (CSV), PDFBox (PDF)
- **Containerization**: Docker with docker-compose

## Architecture

The application follows a layered architecture:

### 1. Entity Layer

The entity layer contains the domain models that represent the business objects:

- **User**: Represents a user in the system with roles (OWNER or ACCOUNTANT)
- **Account**: Represents a financial account (ASSET or LIABILITY)
- **Transaction**: Represents a financial transaction (INCOME or EXPENSE)
- **Invoice**: Represents an invoice with status (DRAFT, SENT, PAID, OVERDUE)
- **InvoiceItem**: Represents a line item in an invoice
- **Budget**: Represents a budget for a specific category and period
- **PasswordResetToken**: Used for password reset functionality

### 2. Repository Layer

The repository layer provides data access using Spring Data JPA:

- **UserRepository**: CRUD operations for users
- **AccountRepository**: CRUD operations for accounts
- **TransactionRepository**: CRUD operations for transactions with filtering and aggregation
- **InvoiceRepository**: CRUD operations for invoices with filtering
- **BudgetRepository**: CRUD operations for budgets
- **PasswordResetTokenRepository**: CRUD operations for password reset tokens

### 3. Controller Layer

The controller layer exposes REST APIs:

- **AuthController**: User registration, login, and password reset
- **AccountController**: Account management
- **TransactionController**: Transaction management with filtering and pagination
- **InvoiceController**: Invoice management with status updates
- **BudgetController**: Budget management with spending tracking
- **ReportController**: Financial report generation
- **ExportImportController**: Data export and import

### 4. Security Layer

The security layer handles authentication and authorization:

- **SecurityConfig**: Configures Spring Security with JWT
- **JwtTokenProvider**: Generates and validates JWT tokens
- **JwtAuthenticationFilter**: Intercepts requests to validate JWT tokens
- **UserDetailsServiceImpl**: Loads user details for authentication
- **UserDetailsImpl**: Adapts User entity to Spring Security's UserDetails

### 5. DTO Layer

The DTO (Data Transfer Object) layer handles request/response objects:

- **Auth DTOs**: RegisterRequest, LoginRequest, JwtResponse, etc.
- **Account DTOs**: AccountRequest, AccountResponse
- **Transaction DTOs**: TransactionRequest, TransactionResponse
- **Invoice DTOs**: InvoiceRequest, InvoiceResponse, InvoiceItemRequest, etc.
- **Budget DTOs**: BudgetRequest, BudgetResponse, BudgetAlertResponse
- **Report DTOs**: BalanceSheetResponse, ProfitAndLossResponse, CashFlowResponse
- **Export/Import DTOs**: TransactionExportRequest, TransactionImportRequest

## Key Features

### 1. User Authentication

- JWT-based authentication
- User registration with role selection
- Password reset functionality
- Role-based access control (OWNER, ACCOUNTANT)

### 2. Account Management

- Create, view, update, and delete financial accounts
- Support for different account types (ASSET, LIABILITY)
- Balance tracking

### 3. Transaction Management

- Record income and expense transactions
- Categorize transactions
- Filter transactions by account, date range, category, type
- Pagination support

### 4. Invoicing

- Create invoices with multiple line items
- Track invoice status (DRAFT, SENT, PAID, OVERDUE)
- Automatic transaction creation when invoice is marked as paid
- Filter invoices by status, client name, date range

### 5. Budget Management

- Set budgets by category and period (DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY)
- Track spending against budgets
- Get alerts for budgets nearing or exceeding limits

### 6. Financial Reporting

- Generate balance sheet reports
- Generate profit and loss statements
- Generate cash flow reports
- Customize reports by date range

### 7. Data Export/Import

- Export transactions as CSV or PDF
- Import transactions from CSV
- Filter exports by account and date range

## Security Implementation

- All endpoints except `/api/auth/*` and Swagger UI are secured
- JWT tokens are required in the `Authorization` header
- Passwords are hashed using BCrypt
- Role-based access control using Spring Security's @PreAuthorize

## Database Design

The database schema follows the entity relationships:

- User has many Accounts, Budgets, and PasswordResetTokens
- Account has many Transactions and Invoices
- Invoice has many InvoiceItems

## API Documentation

The API is documented using Swagger/OpenAPI, accessible at `/swagger-ui` when the application is running.

## Deployment

The application can be deployed using Docker and docker-compose:

- The `Dockerfile` builds the application
- The `docker-compose.yml` sets up the application and PostgreSQL database
- Environment variables configure the database connection

## Testing

The application includes unit tests for controllers:

- AuthControllerTest: Tests user registration and login
- AccountControllerTest: Tests account management
- TransactionControllerTest: Tests transaction management

## Conclusion

The Small Business Accounting System is a well-structured Spring Boot application that provides comprehensive financial management capabilities for small businesses. It follows best practices for layered architecture, security, and API design.