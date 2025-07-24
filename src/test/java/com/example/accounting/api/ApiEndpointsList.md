# Small Business Accounting System API Endpoints

This document provides a comprehensive list of all API endpoints available in the Small Business Accounting System.

## Authentication API

### Register User
- **URL**: `/api/auth/register`
- **Method**: POST
- **Description**: Register a new user with username, email, password, and role
- **Request Body**:
  ```json
  {
    "username": "string",
    "email": "string",
    "password": "string",
    "role": "OWNER | ACCOUNTANT | EMPLOYEE"
  }
  ```
- **Response**: 201 Created

### Login
- **URL**: `/api/auth/login`
- **Method**: POST
- **Description**: Login with username and password to get JWT token
- **Request Body**:
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```
- **Response**: 200 OK with JWT token

### Request Password Reset
- **URL**: `/api/auth/password/reset-request`
- **Method**: POST
- **Description**: Request password reset via email
- **Request Body**:
  ```json
  {
    "email": "string"
  }
  ```
- **Response**: 200 OK

### Reset Password
- **URL**: `/api/auth/password/reset`
- **Method**: POST
- **Description**: Reset password using token
- **Request Body**:
  ```json
  {
    "token": "string",
    "password": "string"
  }
  ```
- **Response**: 200 OK

## Account Management API

### Create Account
- **URL**: `/api/accounts`
- **Method**: POST
- **Description**: Create a financial account with name, type, and initial balance
- **Authentication**: Required (Bearer Token)
- **Request Body**:
  ```json
  {
    "name": "string",
    "type": "ASSET | LIABILITY",
    "initialBalance": "number"
  }
  ```
- **Response**: 201 Created

### Get Account
- **URL**: `/api/accounts/{accountId}`
- **Method**: GET
- **Description**: Get details of a specific account by ID
- **Authentication**: Required (Bearer Token)
- **Response**: 200 OK

### List Accounts
- **URL**: `/api/accounts`
- **Method**: GET
- **Description**: List all accounts or filter by type
- **Authentication**: Required (Bearer Token)
- **Query Parameters**:
  - `type` (optional): Account type (ASSET, LIABILITY)
- **Response**: 200 OK

### Update Account
- **URL**: `/api/accounts/{accountId}`
- **Method**: PUT
- **Description**: Update the name of an existing account
- **Authentication**: Required (Bearer Token)
- **Request Body**:
  ```json
  {
    "name": "string"
  }
  ```
- **Response**: 200 OK

### Delete Account
- **URL**: `/api/accounts/{accountId}`
- **Method**: DELETE
- **Description**: Delete an account if it has no transactions
- **Authentication**: Required (Bearer Token)
- **Authorization**: OWNER role required
- **Response**: 204 No Content

## Transaction Management API

### Create Transaction
- **URL**: `/api/transactions`
- **Method**: POST
- **Description**: Record income/expense transaction with account, amount, type, category, date, and description
- **Authentication**: Required (Bearer Token)
- **Request Body**:
  ```json
  {
    "accountId": "number",
    "amount": "number",
    "type": "INCOME | EXPENSE",
    "category": "string",
    "date": "datetime",
    "description": "string"
  }
  ```
- **Response**: 201 Created

### Get Transaction
- **URL**: `/api/transactions/{transactionId}`
- **Method**: GET
- **Description**: Get details of a specific transaction by ID
- **Authentication**: Required (Bearer Token)
- **Response**: 200 OK

### List Transactions
- **URL**: `/api/transactions`
- **Method**: GET
- **Description**: List transactions with filters: accountId, startDate, endDate, category, type; support pagination
- **Authentication**: Required (Bearer Token)
- **Query Parameters**:
  - `accountId` (optional): Account ID
  - `startDate` (optional): Start date (ISO format)
  - `endDate` (optional): End date (ISO format)
  - `category` (optional): Transaction category
  - `type` (optional): Transaction type (INCOME, EXPENSE)
  - `page` (optional, default: 0): Page number
  - `size` (optional, default: 10): Page size
- **Response**: 200 OK

### Update Transaction
- **URL**: `/api/transactions/{transactionId}`
- **Method**: PUT
- **Description**: Update transaction category/description
- **Authentication**: Required (Bearer Token)
- **Request Body**:
  ```json
  {
    "category": "string",
    "description": "string"
  }
  ```
- **Response**: 200 OK

### Delete Transaction
- **URL**: `/api/transactions/{transactionId}`
- **Method**: DELETE
- **Description**: Delete transaction and update balance
- **Authentication**: Required (Bearer Token)
- **Response**: 204 No Content

## Budget Management API

### Create Budget
- **URL**: `/api/budgets`
- **Method**: POST
- **Description**: Create budget with category, amount, and period
- **Authentication**: Required (Bearer Token)
- **Request Body**:
  ```json
  {
    "category": "string",
    "amount": "number",
    "period": "DAILY | WEEKLY | MONTHLY | QUARTERLY | YEARLY"
  }
  ```
- **Response**: 201 Created

### List Budgets
- **URL**: `/api/budgets`
- **Method**: GET
- **Description**: List all budgets with current spending
- **Authentication**: Required (Bearer Token)
- **Query Parameters**:
  - `period` (optional): Budget period (DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY)
- **Response**: 200 OK

### Get Budget Alerts
- **URL**: `/api/budgets/alerts`
- **Method**: GET
- **Description**: Get alerts for budgets nearing/exceeding limits
- **Authentication**: Required (Bearer Token)
- **Response**: 200 OK

## Invoice Management API

### Create Invoice
- **URL**: `/api/invoices`
- **Method**: POST
- **Description**: Create invoice with client details, due date, items, and account
- **Authentication**: Required (Bearer Token)
- **Request Body**:
  ```json
  {
    "clientName": "string",
    "clientEmail": "string",
    "dueDate": "date",
    "accountId": "number",
    "items": [
      {
        "description": "string",
        "quantity": "number",
        "unitPrice": "number"
      }
    ]
  }
  ```
- **Response**: 201 Created

### Get Invoice
- **URL**: `/api/invoices/{invoiceId}`
- **Method**: GET
- **Description**: Get details of a specific invoice by ID
- **Authentication**: Required (Bearer Token)
- **Response**: 200 OK

### List Invoices
- **URL**: `/api/invoices`
- **Method**: GET
- **Description**: List invoices with filters: status, clientName, date; support pagination
- **Authentication**: Required (Bearer Token)
- **Query Parameters**:
  - `status` (optional): Invoice status (DRAFT, SENT, PAID, CANCELLED)
  - `clientName` (optional): Client name
  - `startDate` (optional): Start date (ISO format)
  - `endDate` (optional): End date (ISO format)
  - `page` (optional, default: 0): Page number
  - `size` (optional, default: 10): Page size
- **Response**: 200 OK

### Update Invoice Status
- **URL**: `/api/invoices/{invoiceId}/status`
- **Method**: PUT
- **Description**: Update invoice status (creates transaction if PAID)
- **Authentication**: Required (Bearer Token)
- **Request Body**:
  ```json
  {
    "status": "DRAFT | SENT | PAID | CANCELLED"
  }
  ```
- **Response**: 200 OK

### Delete Invoice
- **URL**: `/api/invoices/{invoiceId}`
- **Method**: DELETE
- **Description**: Delete draft invoice
- **Authentication**: Required (Bearer Token)
- **Response**: 204 No Content

## Financial Reports API

### Generate Balance Sheet
- **URL**: `/api/reports/balance-sheet`
- **Method**: GET
- **Description**: Generate balance sheet (assets, liabilities, equity; optional date)
- **Authentication**: Required (Bearer Token)
- **Query Parameters**:
  - `asOfDate` (optional): As of date (ISO format)
- **Response**: 200 OK

### Generate Profit and Loss Statement
- **URL**: `/api/reports/profit-and-loss`
- **Method**: GET
- **Description**: Generate profit and loss statement (revenue, expenses, net profit; requires startDate, endDate)
- **Authentication**: Required (Bearer Token)
- **Query Parameters**:
  - `startDate` (required): Start date (ISO format)
  - `endDate` (required): End date (ISO format)
- **Response**: 200 OK

### Generate Cash Flow Report
- **URL**: `/api/reports/cash-flow`
- **Method**: GET
- **Description**: Generate cash flow report (inflows, outflows; requires startDate, endDate)
- **Authentication**: Required (Bearer Token)
- **Query Parameters**:
  - `startDate` (required): Start date (ISO format)
  - `endDate` (required): End date (ISO format)
- **Response**: 200 OK

## Data Export/Import API

### Export Transactions
- **URL**: `/api/export/transactions`
- **Method**: GET
- **Description**: Export transactions as CSV or PDF with optional filters
- **Authentication**: Required (Bearer Token)
- **Query Parameters**:
  - `format` (required): Export format (CSV, PDF)
  - `accountId` (optional): Account ID
  - `startDate` (optional): Start date (ISO format)
  - `endDate` (optional): End date (ISO format)
- **Response**: 200 OK with file download

### Import Transactions
- **URL**: `/api/import/transactions`
- **Method**: POST
- **Description**: Import transactions from CSV file
- **Authentication**: Required (Bearer Token)
- **Request Body**: Multipart form data
  - `accountId` (required): Account ID
  - `file` (required): CSV file
- **Response**: 201 Created