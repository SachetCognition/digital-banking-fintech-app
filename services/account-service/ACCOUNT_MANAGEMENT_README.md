# Account Management System

This document describes the comprehensive account management system implemented for the Digital Banking Fintech application.

## Features Implemented

### 1. Account Opening Workflow with KYC Integration
- **Automated Workflow**: Complete account opening process with status tracking
- **KYC Integration**: Automatic KYC requirement checking based on account type
- **Document Management**: Required document tracking and submission
- **Approval Process**: Multi-level approval workflow for different account types
- **Status Tracking**: Real-time status updates throughout the opening process

### 2. Specialized Account Types
- **Personal Accounts**: Checking, Savings, Money Market, CD, Student, Senior
- **Business Accounts**: Business Checking, Business Savings, Merchant
- **Investment Accounts**: Investment, IRA, Roth IRA
- **Credit Accounts**: Credit Card, Line of Credit, Personal Loan, Business Loan, Mortgage
- **Specialized Accounts**: Joint, Trust, Escrow
- **Dynamic Configuration**: Interest rates, fees, and limits per account type

### 3. Joint Account Support
- **Multi-Customer Accounts**: Support for multiple account holders
- **Role Management**: Primary Owner, Joint Owner, Authorized User, Beneficiary
- **Approval Workflow**: Joint account holder approval process
- **Consent Management**: Track consent from all required parties

### 4. Account Statements with PDF Generation
- **PDF Generation**: Automated PDF statement generation
- **Email Delivery**: Automatic email delivery of statements
- **Transaction History**: Complete transaction history with balances
- **Multiple Formats**: Support for different statement periods
- **Storage Management**: Secure storage and retrieval of statements

### 5. Account Closure Workflow
- **Closure Requests**: Formal account closure request process
- **Balance Transfer**: Automatic transfer of remaining balances
- **Final Statements**: Generation of final account statements
- **Approval Process**: Multi-level approval for account closures
- **Audit Trail**: Complete audit trail of closure process

## Database Schema

### Core Tables

#### accounts (Enhanced)
```sql
CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    account_name VARCHAR(50) NOT NULL,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    currency CHAR(3) NOT NULL,
    balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    available_balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    interest_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0000,
    fee_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0000,
    daily_transfer_limit DECIMAL(19,2) NOT NULL,
    per_transaction_limit DECIMAL(19,2) NOT NULL,
    last_transaction_at TIMESTAMPTZ,
    description TEXT,
    paperless_statements BOOLEAN NOT NULL DEFAULT true,
    email_notifications BOOLEAN NOT NULL DEFAULT true,
    preferred_language CHAR(2) NOT NULL DEFAULT 'EN',
    kyc_required BOOLEAN NOT NULL DEFAULT false,
    kyc_level VARCHAR(16),
    kyc_status VARCHAR(16) DEFAULT 'NOT_REQUIRED',
    opening_status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    opening_reason TEXT,
    closure_reason TEXT,
    closure_requested_at TIMESTAMPTZ,
    closure_approved_at TIMESTAMPTZ,
    closure_approved_by UUID,
    minimum_balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    maximum_balance DECIMAL(19,2),
    monthly_fee DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    overdraft_limit DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    last_interest_calculation TIMESTAMPTZ,
    next_interest_calculation TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

#### joint_account_holders
```sql
CREATE TABLE joint_account_holders (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    role VARCHAR(32) NOT NULL, -- PRIMARY_OWNER, JOINT_OWNER, AUTHORIZED_USER, BENEFICIARY
    is_primary BOOLEAN NOT NULL DEFAULT false,
    added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    approved_at TIMESTAMPTZ,
    approved_by UUID,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, REMOVED
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

#### account_opening_cases
```sql
CREATE TABLE account_opening_cases (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    case_type VARCHAR(32) NOT NULL, -- NEW_ACCOUNT, JOINT_ACCOUNT, ACCOUNT_UPGRADE
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING', -- PENDING, UNDER_REVIEW, APPROVED, REJECTED, COMPLETED
    priority VARCHAR(16) NOT NULL DEFAULT 'NORMAL', -- LOW, NORMAL, HIGH, URGENT
    assigned_to UUID,
    kyc_case_id UUID,
    required_documents TEXT[],
    submitted_documents TEXT[],
    review_notes TEXT,
    approval_notes TEXT,
    rejection_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ
);
```

#### account_statements
```sql
CREATE TABLE account_statements (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    statement_id VARCHAR(64) UNIQUE NOT NULL,
    statement_date DATE NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    opening_balance DECIMAL(19,2) NOT NULL,
    closing_balance DECIMAL(19,2) NOT NULL,
    total_debits DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    total_credits DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    currency CHAR(3) NOT NULL,
    generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    generated_by UUID,
    file_path VARCHAR(500),
    email_sent BOOLEAN NOT NULL DEFAULT false,
    email_sent_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

#### account_closure_requests
```sql
CREATE TABLE account_closure_requests (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    requested_by UUID NOT NULL,
    closure_reason VARCHAR(255) NOT NULL,
    transfer_account_id UUID,
    comments TEXT,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, COMPLETED
    approved_by UUID,
    approved_at TIMESTAMPTZ,
    rejection_reason TEXT,
    final_balance DECIMAL(19,2),
    transfer_amount DECIMAL(19,2),
    final_statement_generated BOOLEAN NOT NULL DEFAULT false,
    final_statement_sent BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ
);
```

## API Endpoints

### Account Opening

#### POST /api/v1/accounts/open
Open a new account with KYC integration.

**Request:**
```json
{
  "accountName": "My Checking Account",
  "accountType": "CHECKING",
  "currency": "USD",
  "initialDeposit": 1000.00,
  "description": "Primary checking account",
  "jointAccountHolders": ["customer-uuid-1", "customer-uuid-2"],
  "kycRequired": false,
  "kycLevel": "BASIC",
  "paperlessStatements": true,
  "emailNotifications": true,
  "preferredLanguage": "EN"
}
```

**Response:**
```json
{
  "accountId": "uuid",
  "accountNumber": "100123456789",
  "accountName": "My Checking Account",
  "accountType": "CHECKING",
  "currency": "USD",
  "initialDeposit": 1000.00,
  "status": "PENDING",
  "openingStatus": "PENDING",
  "message": "Account opening request submitted successfully",
  "nextSteps": ["Account will be reviewed by our team", "You will receive notification once approved"],
  "requiredDocuments": [],
  "createdAt": "2024-01-01T10:00:00Z",
  "estimatedCompletionAt": "2024-01-02T10:00:00Z"
}
```

#### GET /api/v1/accounts/{id}/opening-status
Get account opening status.

#### POST /api/v1/accounts/{id}/approve
Approve account opening (admin only).

### Account Statements

#### POST /api/v1/accounts/{id}/statements/generate
Generate account statement for a period.

**Request:**
```
GET /api/v1/accounts/{id}/statements/generate?fromDate=2024-01-01&toDate=2024-01-31
```

**Response:**
```json
{
  "accountId": "uuid",
  "accountNumber": "100123456789",
  "accountName": "My Checking Account",
  "statementDate": "2024-01-31",
  "periodStart": "2024-01-01",
  "periodEnd": "2024-01-31",
  "openingBalance": 1000.00,
  "closingBalance": 1200.00,
  "totalDebits": 300.00,
  "totalCredits": 500.00,
  "transactions": [...],
  "currency": "USD",
  "generatedAt": "2024-01-31T10:00:00Z",
  "statementId": "STMT-100123456789-202401"
}
```

#### GET /api/v1/accounts/{id}/statements
Get account statements.

#### POST /api/v1/accounts/{id}/statements/{statementId}/email
Email statement to customer.

#### GET /api/v1/statements/{statementId}/download
Download statement PDF.

### Account Closure

#### POST /api/v1/accounts/{id}/close
Request account closure.

**Request:**
```json
{
  "accountId": "uuid",
  "closureReason": "No longer needed",
  "transferAccountId": "uuid",
  "comments": "Please transfer balance to savings account",
  "generateFinalStatement": true,
  "emailFinalStatement": true,
  "allHoldersConsent": true,
  "consentingHolders": ["customer-uuid-1", "customer-uuid-2"]
}
```

#### GET /api/v1/accounts/closure-requests
Get my closure requests.

#### POST /api/v1/account-closure/{requestId}/approve
Approve account closure (admin only).

#### POST /api/v1/account-closure/{requestId}/reject
Reject account closure (admin only).

## Account Types

### Personal Accounts
- **CHECKING**: Basic checking account for daily transactions
- **SAVINGS**: Interest-bearing savings account (0.01% interest)
- **MONEY_MARKET**: High-yield savings with limited transactions (0.02% interest)
- **CD**: Fixed-term deposit with guaranteed interest (0.03% interest)
- **STUDENT**: Account for students with special benefits
- **SENIOR**: Account for seniors with special benefits (0.01% interest)

### Business Accounts
- **BUSINESS_CHECKING**: Checking account for business operations
- **BUSINESS_SAVINGS**: Savings account for business funds (0.01% interest)
- **MERCHANT**: Account for processing payments

### Investment Accounts
- **INVESTMENT**: Account for securities and investments
- **IRA**: Tax-advantaged retirement savings
- **ROTH_IRA**: Tax-free retirement savings

### Credit Accounts
- **CREDIT_CARD**: Revolving credit line (20% APR)
- **LINE_OF_CREDIT**: Flexible credit facility (15% APR)
- **PERSONAL_LOAN**: Fixed-term personal loan (12% APR)
- **BUSINESS_LOAN**: Fixed-term business loan (10% APR)
- **MORTGAGE**: Home loan secured by property (6% APR)

### Specialized Accounts
- **JOINT**: Account shared by multiple customers
- **TRUST**: Account held in trust
- **ESCROW**: Account for holding funds in escrow

## Business Rules

### Account Opening
1. **KYC Requirements**: Business, investment, and credit accounts require KYC
2. **Minimum Deposits**: Varies by account type
3. **Documentation**: Required documents tracked per account type
4. **Approval Process**: Multi-level approval based on account type and risk

### Joint Accounts
1. **Primary Owner**: One primary owner per account
2. **Consent Required**: All joint owners must consent to account operations
3. **Role Permissions**: Different roles have different permissions
4. **Approval Workflow**: Joint account changes require approval from all parties

### Account Statements
1. **Generation**: Statements generated on demand or scheduled
2. **PDF Format**: All statements generated as PDFs
3. **Email Delivery**: Automatic email delivery based on preferences
4. **Retention**: Statements retained for regulatory compliance period

### Account Closure
1. **Balance Transfer**: Remaining balance must be transferred to another account
2. **Final Statement**: Final statement generated and sent
3. **Approval Required**: Closure requests require approval
4. **Cooling Period**: Optional cooling period before final closure

## Security Features

### Access Control
- **Customer Isolation**: Customers can only access their own accounts
- **Role-Based Access**: Different access levels for different roles
- **Audit Logging**: All account operations logged for audit

### Data Protection
- **Encryption**: Sensitive data encrypted at rest and in transit
- **PII Protection**: Personal information protected according to regulations
- **Secure Storage**: Statements and documents stored securely

### Compliance
- **Regulatory Requirements**: Meets banking regulatory requirements
- **Audit Trail**: Complete audit trail for all operations
- **Data Retention**: Proper data retention and disposal policies

## Integration Points

### Customer Service
- **Customer Validation**: Validates customer information and status
- **KYC Integration**: Integrates with KYC service for verification

### Ledger Service
- **Transaction Data**: Retrieves transaction data for statements
- **Balance Calculations**: Calculates account balances and limits

### Email Service
- **Notifications**: Sends account-related notifications
- **Statement Delivery**: Delivers statements via email

### File Service
- **Document Storage**: Stores account documents and statements
- **PDF Generation**: Generates and stores PDF statements

## Monitoring and Analytics

### Metrics
- **Account Opening**: Success rates, processing times, approval rates
- **Statement Generation**: Generation times, delivery success rates
- **Account Closure**: Closure rates, processing times, reasons

### Alerts
- **Failed Operations**: Alerts for failed account operations
- **Suspicious Activity**: Alerts for suspicious account activity
- **System Issues**: Alerts for system-level issues

### Reporting
- **Operational Reports**: Daily, weekly, monthly operational reports
- **Compliance Reports**: Regulatory compliance reports
- **Performance Reports**: System performance and usage reports

## Future Enhancements

### Planned Features
- **Mobile App Integration**: Mobile-specific account management features
- **Real-time Notifications**: Real-time push notifications for account events
- **Advanced Analytics**: AI-powered account insights and recommendations
- **Multi-Currency Support**: Support for multiple currencies per account

### Technical Improvements
- **Microservices**: Further decomposition into specialized services
- **Event Sourcing**: Event sourcing for complete audit trails
- **CQRS**: Command Query Responsibility Segregation for better performance
- **GraphQL**: GraphQL API for flexible data querying

### Compliance Enhancements
- **AML Integration**: Advanced anti-money laundering features
- **Fraud Detection**: AI-powered fraud detection and prevention
- **Regulatory Reporting**: Automated regulatory reporting
- **Data Privacy**: Enhanced data privacy and protection features

