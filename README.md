# Small Business Accounting System (SBAS)

A comprehensive web-based application for small businesses to manage accounts, transactions, invoices, budgets, and financial reports.

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Development Setup](#development-setup)
  - [Production Setup](#production-setup)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Security](#security)
- [Sample API Requests](#sample-api-requests)

## Features

- **User Authentication**: Register, login, and password reset functionality with JWT-based authentication
- **Account Management**: Create, view, update, and delete financial accounts
- **Transaction Management**: Record income/expense transactions with categorization
- **Invoicing**: Create, manage, and track invoices with automatic transaction creation when paid
- **Financial Reports**: Generate balance sheets, profit and loss statements, and cash flow reports
- **Budget Management**: Set budgets by category and period, with alerts for overspending
- **Data Export/Import**: Export transactions as CSV/PDF and import from CSV

## Technology Stack

- **Backend**: Spring Boot 3.x with Maven
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Spring Security with JWT
- **Documentation**: Swagger/OpenAPI
- **Export/Import**: Apache POI (CSV), PDFBox (PDF)
- **Containerization**: Docker with docker-compose

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.8 or higher
- Docker and Docker Compose (for production deployment)

### Development Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/sbas.git
   cd sbas
   ```

2. Run the application in development mode:
   ```bash
   ./mvnw spring-boot:run
   ```

   The application will start with the PostgreSQL database and will be accessible at http://localhost:8080.

3. Make sure you have PostgreSQL running locally with the following configuration:
   - Database: `accountingdb`
   - Username: `postgres`
   - Password: `postgres`

### Production Setup

1. Build and run the application using Docker Compose:
   ```bash
   docker-compose up -d
   ```

   This will start both the application and a PostgreSQL database. The application will be accessible at http://localhost:8080.

2. Database Schema Initialization:
   - By default, the application is configured to automatically create and update the database schema when it starts (`spring.jpa.hibernate.ddl-auto=update`).
   - For production environments, once the schema is stable, you may want to change this setting to `validate` in `application-prod.properties` and manage schema changes manually.
   - If you switch to `validate` mode, ensure that all required database tables exist before starting the application.

3. To stop the application:
   ```bash
   docker-compose down
   ```

## API Documentation

The API documentation is available through Swagger UI at http://localhost:8080/swagger-ui when the application is running.

You can also download the OpenAPI specification from http://localhost:8080/api-docs.

## Project Structure

The project follows a layered architecture:

- **Entities**: Domain models representing the business objects
- **Repositories**: Data access layer using Spring Data JPA
- **Services**: Business logic layer
- **Controllers**: REST API endpoints
- **DTOs**: Data Transfer Objects for request/response
- **Security**: JWT authentication and authorization

## Security

- All endpoints except `/api/auth/*` and Swagger UI are secured
- JWT tokens are required in the `Authorization` header for secured endpoints
- Passwords are hashed using BCrypt
- Two roles are supported: OWNER and ACCOUNTANT
  - OWNER: Full access to all features
  - ACCOUNTANT: Read-only access to reports and accounts

## Sample API Requests

### Authentication

#### Register a new user

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "owner",
    "email": "owner@example.com",
    "password": "password123",
    "role": "OWNER"
  }'
```

#### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "owner",
    "password": "password123"
  }'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "owner",
  "email": "owner@example.com",
  "role": "OWNER"
}
```

### Account Management

#### Create an account

```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "Business Checking",
    "type": "ASSET",
    "initialBalance": 1000.00
  }'
```

#### Get all accounts

```bash
curl -X GET http://localhost:8080/api/accounts \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Transaction Management

#### Create a transaction

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "accountId": 1,
    "amount": 500.00,
    "type": "INCOME",
    "category": "Sales",
    "date": "2023-01-15T10:30:00",
    "description": "Product sale"
  }'
```

#### Get transactions with filtering

```bash
curl -X GET "http://localhost:8080/api/transactions?accountId=1&startDate=2023-01-01T00:00:00&endDate=2023-01-31T23:59:59&category=Sales&page=0&size=10" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Invoicing

#### Create an invoice

```bash
curl -X POST http://localhost:8080/api/invoices \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "clientName": "ACME Corp",
    "clientEmail": "billing@acme.com",
    "dueDate": "2023-02-15",
    "accountId": 1,
    "items": [
      {
        "description": "Consulting Services",
        "quantity": 10,
        "unitPrice": 150.00
      },
      {
        "description": "Software License",
        "quantity": 1,
        "unitPrice": 500.00
      }
    ]
  }'
```

#### Update invoice status

```bash
curl -X PUT http://localhost:8080/api/invoices/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "status": "PAID"
  }'
```

### Financial Reports

#### Generate a balance sheet

```bash
curl -X GET http://localhost:8080/api/reports/balance-sheet \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

#### Generate a profit and loss statement

```bash
curl -X GET "http://localhost:8080/api/reports/profit-and-loss?startDate=2023-01-01T00:00:00&endDate=2023-01-31T23:59:59" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Data Export/Import

#### Export transactions

```bash
curl -X GET "http://localhost:8080/api/export/transactions?format=CSV&startDate=2023-01-01T00:00:00&endDate=2023-01-31T23:59:59" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  --output transactions.csv
```

#### Import transactions

```bash
curl -X POST http://localhost:8080/api/import/transactions \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -F "accountId=1" \
  -F "file=@transactions.csv"
```# SBAS
