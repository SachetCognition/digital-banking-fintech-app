# Authentication & Authorization System

This document describes the comprehensive authentication and authorization system implemented for the Digital Banking Fintech application.

## Features Implemented

### 1. User Registration Flow
- **Email Verification**: New users must verify their email address before accessing the system
- **Password Requirements**: Strong password policy with complexity requirements
- **Data Validation**: Comprehensive validation for all registration fields
- **Terms Acceptance**: Users must accept terms and conditions

### 2. Password Reset Functionality
- **Email-based Reset**: Secure password reset via email tokens
- **Token Expiration**: Reset tokens expire after 1 hour
- **One-time Use**: Tokens are invalidated after successful use
- **Secure Generation**: Cryptographically secure token generation

### 3. Multi-Factor Authentication (MFA)
- **TOTP Support**: Time-based One-Time Password using RFC 6238
- **Backup Codes**: 10 single-use backup codes for account recovery
- **QR Code Generation**: Easy setup with QR codes for authenticator apps
- **Email Fallback**: MFA codes sent via email as backup

### 4. Role-Based Access Control (RBAC)
- **Granular Permissions**: Fine-grained permission system
- **Role Hierarchy**: Predefined roles with specific permissions
- **Resource-based Access**: Permissions tied to specific resources and actions
- **Dynamic Assignment**: Roles can be assigned/removed dynamically

### 5. Session Management
- **Session Timeout**: Configurable session expiration (default: 30 minutes)
- **Concurrent Login Control**: Option to invalidate other sessions
- **Device Tracking**: Track login devices and locations
- **Activity Monitoring**: Last activity tracking and automatic cleanup

## Database Schema

### Core Tables

#### customers
```sql
CREATE TABLE customers (
    id UUID PRIMARY KEY,
    user_no VARCHAR(32) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(32) UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    dob DATE,
    country CHAR(2) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    password_hash VARCHAR(255),
    email_verified BOOLEAN NOT NULL DEFAULT false,
    phone_verified BOOLEAN NOT NULL DEFAULT false,
    mfa_enabled BOOLEAN NOT NULL DEFAULT false,
    last_login_at TIMESTAMPTZ,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

#### email_verification_tokens
```sql
CREATE TABLE email_verification_tokens (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    type VARCHAR(32) NOT NULL, -- EMAIL_VERIFICATION, PASSWORD_RESET, MFA_VERIFICATION
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

#### mfa_secrets
```sql
CREATE TABLE mfa_secrets (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    secret_key VARCHAR(255) NOT NULL,
    backup_codes TEXT[], -- Array of backup codes
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

#### user_sessions
```sql
CREATE TABLE user_sessions (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    session_token VARCHAR(255) UNIQUE NOT NULL,
    device_fingerprint VARCHAR(255),
    ip_address INET,
    user_agent TEXT,
    expires_at TIMESTAMPTZ NOT NULL,
    last_activity_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

#### roles & permissions
```sql
CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(64) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    name VARCHAR(64) UNIQUE NOT NULL,
    resource VARCHAR(64) NOT NULL,
    action VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE customer_roles (
    customer_id UUID NOT NULL,
    role_id UUID NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    assigned_by UUID,
    PRIMARY KEY (customer_id, role_id)
);
```

#### auth_audit_log
```sql
CREATE TABLE auth_audit_log (
    id UUID PRIMARY KEY,
    customer_id UUID,
    event_type VARCHAR(64) NOT NULL,
    event_data JSONB,
    ip_address INET,
    user_agent TEXT,
    success BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

## API Endpoints

### Authentication Endpoints

#### POST /api/v1/auth/register
Register a new customer account.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "fullName": "John Doe",
  "phone": "+1234567890",
  "dateOfBirth": "1990-01-01",
  "country": "US",
  "acceptTerms": true
}
```

**Response:**
```json
{
  "customerId": "uuid",
  "email": "user@example.com",
  "fullName": "John Doe",
  "accessToken": null,
  "refreshToken": null,
  "sessionToken": null,
  "expiresAt": null,
  "mfaRequired": false,
  "roles": ["CUSTOMER"],
  "permissions": ["ACCOUNT_READ", "ACCOUNT_CREATE", ...]
}
```

#### POST /api/v1/auth/verify-email?token=...
Verify email address with token.

#### POST /api/v1/auth/login
Login with email and password.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "mfaCode": "123456",
  "deviceFingerprint": "device_fingerprint"
}
```

#### POST /api/v1/auth/password-reset
Request password reset.

**Request:**
```json
{
  "email": "user@example.com"
}
```

#### POST /api/v1/auth/password-reset/confirm
Confirm password reset with token.

**Request:**
```json
{
  "token": "reset_token",
  "newPassword": "NewSecurePass123!"
}
```

#### POST /api/v1/auth/mfa/setup
Setup MFA for customer.

**Response:**
```json
{
  "secret": "base64_secret_key",
  "backupCodes": ["12345678", "87654321", ...],
  "qrCodeUrl": "otpauth://totp/..."
}
```

#### POST /api/v1/auth/mfa/disable
Disable MFA for customer.

#### POST /api/v1/auth/logout
Logout current session.

#### POST /api/v1/auth/logout-all
Logout all sessions for customer.

#### GET /api/v1/auth/sessions
Get active sessions for customer.

#### GET /api/v1/auth/permissions
Get customer roles and permissions.

## Security Features

### Password Security
- **BCrypt Hashing**: Passwords hashed with BCrypt (strength 12)
- **Complexity Requirements**: Minimum 8 characters with mixed case, digits, and special characters
- **Secure Generation**: Cryptographically secure random password generation

### Token Security
- **JWT Tokens**: Signed JWT tokens for API access
- **Session Tokens**: Separate session tokens for session management
- **Secure Generation**: Cryptographically secure token generation
- **Expiration**: Configurable token expiration times

### Account Security
- **Account Lockout**: Automatic lockout after failed login attempts
- **Rate Limiting**: Protection against brute force attacks
- **Audit Logging**: Comprehensive audit trail for all authentication events
- **Device Tracking**: Track and manage login devices

### MFA Security
- **TOTP Standard**: RFC 6238 compliant TOTP implementation
- **Backup Codes**: Single-use backup codes for account recovery
- **Secure Storage**: MFA secrets encrypted and securely stored

## Configuration

### Application Properties
```yaml
app:
  auth:
    max-failed-attempts: 5
    lockout-duration-minutes: 30
  session:
    timeout-minutes: 30
  jwt:
    secret: mySecretKeyForJWTTokenGeneration123456789
    access-token-expiration-minutes: 60
    refresh-token-expiration-days: 7
  email:
    enabled: false
    from: noreply@digitalbank.com
```

## Frontend Integration

### React Components
- **Login**: Login form with MFA support
- **Register**: Registration form with validation
- **ForgotPassword**: Password reset request
- **ResetPassword**: Password reset confirmation
- **VerifyEmail**: Email verification handler

### Authentication Flow
1. User registers with email and password
2. System sends verification email
3. User clicks verification link
4. User can now login
5. Optional MFA setup for enhanced security
6. Session management with automatic timeout

### Security Headers
- **Authorization**: Bearer token for API requests
- **X-Session-Token**: Session token for session management
- **Device Fingerprinting**: Automatic device identification

## Testing

### Unit Tests
- Password validation and hashing
- Token generation and validation
- MFA code generation and verification
- Session management

### Integration Tests
- Complete authentication flows
- Email verification process
- Password reset workflow
- MFA setup and verification

### Security Tests
- Brute force protection
- Token security
- Session hijacking prevention
- MFA bypass attempts

## Monitoring and Auditing

### Audit Events
- Registration attempts
- Login attempts (success/failure)
- Password reset requests
- MFA setup/disable
- Session creation/destruction
- Permission changes

### Metrics
- Login success/failure rates
- MFA adoption rates
- Session duration statistics
- Failed attempt patterns

## Deployment Considerations

### Environment Variables
- JWT secret key
- Database connection strings
- Email service configuration
- Session timeout settings

### Security Hardening
- HTTPS enforcement
- Secure cookie settings
- CORS configuration
- Rate limiting implementation

### Monitoring
- Authentication event monitoring
- Failed attempt alerting
- Session anomaly detection
- Security incident response

## Future Enhancements

### Planned Features
- **SMS MFA**: SMS-based MFA as alternative to TOTP
- **Biometric Authentication**: Fingerprint/face recognition support
- **Risk-based Authentication**: Adaptive authentication based on risk factors
- **Social Login**: OAuth integration with social providers
- **Advanced Session Management**: Device trust and location-based controls

### Security Improvements
- **Hardware Security Keys**: FIDO2/WebAuthn support
- **Advanced Threat Detection**: Machine learning-based anomaly detection
- **Compliance Features**: SOX, PCI DSS, GDPR compliance tools
- **Advanced Audit**: Real-time security monitoring and alerting

