# Small Business Accounting System - Sample Data

This document provides sample data for testing the Small Business Accounting System. You can use this data to test the various features of the application through the API or Swagger UI.

## User Registration

### Owner
```json
{
  "username": "john_owner",
  "email": "john@example.com",
  "password": "password123",
  "role": "OWNER"
}
```

### Accountant
```json
{
  "username": "sarah_accountant",
  "email": "sarah@example.com",
  "password": "password123",
  "role": "ACCOUNTANT"
}
```

### Employee
```json
{
  "username": "mike_employee",
  "email": "mike@example.com",
  "password": "password123",
  "role": "EMPLOYEE"
}
```

## Account Creation

### Checking Account (Asset)
```json
{
  "name": "Business Checking",
  "type": "ASSET",
  "initialBalance": 10000.00
}
```

### Savings Account (Asset)
```json
{
  "name": "Business Savings",
  "type": "ASSET",
  "initialBalance": 25000.00
}
```

### Credit Card (Liability)
```json
{
  "name": "Business Credit Card",
  "type": "LIABILITY",
  "initialBalance": 2500.00
}
```

### Loan (Liability)
```json
{
  "name": "Business Loan",
  "type": "LIABILITY",
  "initialBalance": 50000.00
}
```

### Equipment (Asset)
```json
{
  "name": "Office Equipment",
  "type": "ASSET",
  "initialBalance": 15000.00
}
```

## Transaction Recording

### Income Transactions

#### Client Payment
```json
{
  "accountId": 1,
  "amount": 5000.00,
  "type": "INCOME",
  "category": "Sales",
  "date": "2023-07-15T10:30:00",
  "description": "Client payment for consulting services"
}
```

#### Product Sale
```json
{
  "accountId": 1,
  "amount": 1250.00,
  "type": "INCOME",
  "category": "Product Sales",
  "date": "2023-07-18T14:45:00",
  "description": "Sale of software licenses"
}
```

#### Interest Income
```json
{
  "accountId": 2,
  "amount": 125.50,
  "type": "INCOME",
  "category": "Interest",
  "date": "2023-07-31T00:00:00",
  "description": "Monthly interest on savings account"
}
```

### Expense Transactions

#### Office Rent
```json
{
  "accountId": 1,
  "amount": 2000.00,
  "type": "EXPENSE",
  "category": "Rent",
  "date": "2023-07-01T09:00:00",
  "description": "Monthly office rent payment"
}
```

#### Utility Bills
```json
{
  "accountId": 1,
  "amount": 350.75,
  "type": "EXPENSE",
  "category": "Utilities",
  "date": "2023-07-05T11:20:00",
  "description": "Electricity and water bills"
}
```

#### Office Supplies
```json
{
  "accountId": 1,
  "amount": 125.45,
  "type": "EXPENSE",
  "category": "Office Supplies",
  "date": "2023-07-10T14:15:00",
  "description": "Printer paper, ink cartridges, and stationery"
}
```

#### Software Subscription
```json
{
  "accountId": 3,
  "amount": 99.99,
  "type": "EXPENSE",
  "category": "Software",
  "date": "2023-07-15T00:00:00",
  "description": "Monthly subscription for project management software"
}
```

#### Employee Salary
```json
{
  "accountId": 1,
  "amount": 4500.00,
  "type": "EXPENSE",
  "category": "Salaries",
  "date": "2023-07-25T16:00:00",
  "description": "Monthly salary payment for employees"
}
```

## Transaction Updating

To update a transaction, use the PUT endpoint with the transaction ID:
```
PUT /api/transactions/{transactionId}
```

Note: Only users with OWNER or ACCOUNTANT roles can update transactions. EMPLOYEE users can only view and create transactions.

When updating a transaction, you must provide all fields of the transaction, not just the ones you want to update. The server will replace the entire transaction with the new values.

Here are some examples of updating different aspects of a transaction:

### Update Transaction Category and Description
```json
{
  "accountId": 1,
  "amount": 5000.00,
  "type": "INCOME",
  "category": "Consulting Services",
  "date": "2023-07-15T10:30:00",
  "description": "Updated description for client payment"
}
```

### Update Transaction Amount
```json
{
  "accountId": 1,
  "amount": 5500.00,
  "type": "INCOME",
  "category": "Sales",
  "date": "2023-07-15T10:30:00",
  "description": "Client payment for consulting services (updated amount)"
}
```

### Change Transaction Type
```json
{
  "accountId": 1,
  "amount": 1250.00,
  "type": "EXPENSE",
  "category": "Refunds",
  "date": "2023-07-18T14:45:00",
  "description": "Refund for software licenses"
}
```

### Move Transaction to Different Account
```json
{
  "accountId": 2,
  "amount": 5000.00,
  "type": "INCOME",
  "category": "Sales",
  "date": "2023-07-15T10:30:00",
  "description": "Client payment moved to savings account"
}
```

## Budget Creation

### Office Supplies Budget
```json
{
  "category": "Office Supplies",
  "amount": 500.00,
  "period": "MONTHLY"
}
```

### Rent Budget
```json
{
  "category": "Rent",
  "amount": 2500.00,
  "period": "MONTHLY"
}
```

### Utilities Budget
```json
{
  "category": "Utilities",
  "amount": 400.00,
  "period": "MONTHLY"
}
```

### Marketing Budget
```json
{
  "category": "Marketing",
  "amount": 1500.00,
  "period": "MONTHLY"
}
```

### Travel Budget
```json
{
  "category": "Travel",
  "amount": 2000.00,
  "period": "QUARTERLY"
}
```

## Invoice Creation

### Consulting Services Invoice
```json
{
  "clientName": "Acme Corporation",
  "clientEmail": "billing@acme.com",
  "dueDate": "2023-08-15",
  "accountId": 1,
  "items": [
    {
      "description": "Consulting Services",
      "quantity": 20,
      "unitPrice": 150.00
    },
    {
      "description": "Software License",
      "quantity": 1,
      "unitPrice": 1000.00
    }
  ]
}
```

### Web Development Invoice
```json
{
  "clientName": "XYZ Ltd",
  "clientEmail": "accounts@xyz.com",
  "dueDate": "2023-08-20",
  "accountId": 1,
  "items": [
    {
      "description": "Website Development",
      "quantity": 1,
      "unitPrice": 3500.00
    },
    {
      "description": "Website Hosting (1 year)",
      "quantity": 1,
      "unitPrice": 240.00
    }
  ]
}
```

### Training Services Invoice
```json
{
  "clientName": "Global Enterprises",
  "clientEmail": "finance@global.com",
  "dueDate": "2023-09-01",
  "accountId": 1,
  "items": [
    {
      "description": "Staff Training Workshop",
      "quantity": 1,
      "unitPrice": 1200.00
    },
    {
      "description": "Training Materials",
      "quantity": 15,
      "unitPrice": 25.00
    }
  ]
}
```

## Financial Reports

### Balance Sheet Parameters
```
GET /api/reports/balance-sheet
```
No required parameters, but you can specify an optional `asOfDate` parameter:
```
asOfDate=2023-07-31T23:59:59
```

### Profit and Loss Statement Parameters
```
GET /api/reports/profit-and-loss
```
Required parameters:
```
startDate=2023-07-01T00:00:00
endDate=2023-07-31T23:59:59
```

### Cash Flow Report Parameters
```
GET /api/reports/cash-flow
```
Required parameters:
```
startDate=2023-07-01T00:00:00
endDate=2023-07-31T23:59:59
```

## Data Export/Import

### Export Transactions Parameters
```
GET /api/export/transactions
```
Parameters:
```
format=CSV
startDate=2023-07-01T00:00:00
endDate=2023-07-31T23:59:59
```

You can also export as PDF:
```
format=PDF
accountId=1
startDate=2023-07-01T00:00:00
endDate=2023-07-31T23:59:59
```

### Import Transactions CSV Format
```csv
ID,Account,Amount,Type,Category,Date,Description
,Business Checking,1500.00,INCOME,Consulting,2023-08-01 09:30:00,Client retainer payment
,Business Checking,350.75,EXPENSE,Utilities,2023-08-05 14:15:00,Monthly internet and phone bill
,Business Checking,1200.00,INCOME,Training,2023-08-10 10:00:00,Corporate training session
,Business Checking,75.50,EXPENSE,Office Supplies,2023-08-15 11:45:00,Printer paper and ink cartridges
```

To import transactions, use a multipart form with:
- `accountId`: The ID of the account to import transactions into (e.g., 1)
- `file`: The CSV file containing the transactions

## Complete Business Scenario

Here's a complete business scenario with related data that you can use to test the system:

1. Register as an owner
2. Create checking and savings accounts
3. Record initial income (capital investment)
4. Set up monthly budgets for various expense categories
5. Record regular expenses (rent, utilities, supplies)
6. Create and send invoices to clients
7. Record invoice payments as income
8. Generate financial reports to analyze business performance

This scenario will help you test the full functionality of the system with realistic business data.
