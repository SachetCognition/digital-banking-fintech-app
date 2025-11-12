# Payments Service

The Payments Service handles P2P (Peer-to-Peer) internal transfers with a comprehensive saga pattern implementation.

## Features

### P2P Transfer Saga
- **Validation**: Account validation, limits checking, balance verification
- **Reservation**: Fund reservation to prevent double-spending
- **Posting**: Actual transfer execution in the ledger
- **Notification**: Customer notifications for both parties
- **Compensation**: Automatic rollback on failures

### Idempotency
- **Idempotency-Key**: Required header for all transfer requests
- **Redis Caching**: Fast lookup of existing transfers
- **Database Fallback**: Persistent idempotency tracking

### Event-Driven Architecture
- **Kafka Events**: Transfer lifecycle events published to `transfer.events` topic
- **Event Types**: TransferCreated, TransferStatusChanged, TransferCompleted, TransferFailed

## API Endpoints

### P2P Transfers

#### POST /api/v1/transfers
Creates a new P2P transfer.

**Request Body:**
```json
{
  "payerAccountId": "uuid",
  "payeeAccountId": "uuid", 
  "amount": 100.00,
  "currency": "USD",
  "description": "Transfer description",
  "idempotencyKey": "unique-key"
}
```

**Response:**
```json
{
  "transferId": "uuid",
  "payerAccountId": "uuid",
  "payeeAccountId": "uuid",
  "amount": 100.00,
  "currency": "USD",
  "status": "COMPLETED",
  "description": "Transfer description",
  "createdAt": "2024-01-01T10:00:00Z",
  "completedAt": "2024-01-01T10:00:05Z",
  "idempotencyKey": "unique-key"
}
```

### GET /api/v1/transfers/{id}
Retrieves transfer details by ID.

### GET /api/v1/transfers/{id}/status
Gets current transfer status.

### Beneficiaries

#### POST /api/v1/beneficiaries
Creates a new beneficiary.

**Request Body:**
```json
{
  "name": "John Doe",
  "accountNumber": "1234567890",
  "bankCode": "BANK001",
  "bankName": "Example Bank",
  "currency": "USD",
  "type": "INTERNAL",
  "description": "Monthly rent payment"
}
```

#### GET /api/v1/beneficiaries
Gets beneficiaries for customer with optional filtering.

**Query Parameters:**
- `type`: Filter by beneficiary type (INTERNAL, EXTERNAL, BILL_PAY, MOBILE_MONEY)
- `search`: Search by name or account number

#### GET /api/v1/beneficiaries/{id}
Gets beneficiary by ID.

#### PUT /api/v1/beneficiaries/{id}
Updates beneficiary.

#### DELETE /api/v1/beneficiaries/{id}
Deletes (deactivates) beneficiary.

#### POST /api/v1/beneficiaries/{id}/activate
Activates beneficiary.

### Bill Pay

#### POST /api/v1/bill-pay
Pays bill to saved beneficiary.

**Request Body:**
```json
{
  "payerAccountId": "uuid",
  "beneficiaryId": "uuid",
  "amount": 100.00,
  "currency": "USD",
  "description": "Electricity bill",
  "idempotencyKey": "unique-key"
}
```

## Transfer Limits

- **Daily Limit**: $10,000 per account per day
- **Per-Transaction Limit**: $5,000 per transfer
- **Minimum Amount**: $0.01

## Saga Steps

1. **VALIDATION**: Validate accounts, limits, and balance
2. **RESERVATION**: Reserve funds in payer account
3. **POSTING**: Execute transfer in ledger service
4. **NOTIFICATION**: Send notifications to both parties
5. **COMPLETED**: Mark transfer as completed

## Compensation

If any step fails, the saga automatically:
1. Releases any active reservations
2. Marks transfer as failed
3. Publishes failure event
4. Logs compensation actions

## Configuration

```yaml
external:
  account-service:
    base-url: http://localhost:8082
  ledger-service:
    base-url: http://localhost:8083
  notification-service:
    base-url: http://localhost:8085

transfer:
  limits:
    daily-limit: 10000.00
    per-transaction-limit: 5000.00
    min-amount: 0.01
```

## Database Schema

### Transfers Table
- Transfer details and saga state
- Idempotency key tracking
- Status and step tracking

### Reservations Table
- Fund reservations per transfer
- Account and amount details
- Release tracking

### Transfer Events Table
- Audit trail of all transfer events
- JSON event data storage
- Timestamp tracking

## Running the Service

```bash
# Start infrastructure
docker compose -f docker/compose/docker-compose.yml up -d

# Run the service
mvn spring-boot:run

# Or via IDE
# Run PaymentsServiceApplication.java
```

## Testing

```bash
# Run tests
mvn test

# Run integration tests
mvn test -Dtest=P2PTransferIntegrationTest
```

## Port

- **Payments Service**: http://localhost:8084
- **API Gateway**: http://localhost:8080/api/v1/transfers
- **Swagger UI**: http://localhost:8084/swagger-ui.html
