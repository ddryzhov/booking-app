# Booking Application

A comprehensive Spring Boot application for managing accommodation bookings with Stripe payment integration and Telegram notifications.

## Features

- 🏠 **Accommodation Management** - CRUD operations for managing rental properties
- 👥 **User Management** - Registration, authentication with JWT tokens, role-based access
- 📅 **Booking System** - Create, update, and manage accommodation bookings
- 💳 **Stripe Integration** - Secure payment processing through Stripe
- 📱 **Telegram Notifications** - Real-time notifications for bookings, payments, and system events
- 🔒 **Security** - JWT authentication, role-based authorization (Customer, Manager, Admin)
- 📊 **Database** - PostgreSQL with Liquibase migrations
- 📝 **API Documentation** - Swagger/OpenAPI integration

## Technologies

- Java 17
- Spring Boot 3.5.7
- Spring Security
- Spring Data JPA
- PostgreSQL
- Liquibase
- Stripe API
- Telegram Bots API
- JWT (JJWT)
- MapStruct
- Lombok
- Maven

## Prerequisites

- JDK 17+
- PostgreSQL 12+
- Maven 3.8+
- Stripe Account (test mode)
- Telegram Bot Token

## Setup

### 1. Clone the repository

```bash
git clone <repository-url>
cd booking-app
```

### 2. Create PostgreSQL Database

```sql
CREATE DATABASE booking_app_db;
```

### 3. Setup Telegram Bot

1. Open Telegram and search for @BotFather
2. Send `/newbot` command and follow instructions
3. Save the bot token
4. Send `/start` to your bot to get your Chat ID
5. Use the Chat ID in your `.env` file

### 4. Setup Stripe

1. Create account at [stripe.com](https://stripe.com)
2. Get your test API key from Dashboard > Developers > API keys
3. Use the test secret key (starts with `sk_test_`)

### 5. Configure Environment Variables

Copy `.env.sample` to `.env` and fill in your values:

```bash
cp .env.sample .env
```

Edit `.env`:

```properties
# Database
DB_URL=jdbc:postgresql://localhost:5432/booking_app_db
DB_USERNAME=your_username
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_very_long_secret_key_at_least_256_bits
JWT_EXPIRATION=86400000

# Stripe
STRIPE_API_KEY=sk_test_your_stripe_key

# Telegram
TELEGRAM_BOT_TOKEN=your_bot_token
TELEGRAM_BOT_USERNAME=your_bot_username
TELEGRAM_CHAT_ID=your_chat_id

# Application
APP_BASE_URL=http://localhost:8080
```

### 6. Run the Application

```bash
./mvnw clean install
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## API Documentation

Once the application is running, access Swagger UI at:

```
http://localhost:8080/swagger-ui/index.html
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token

### Users
- `GET /api/users/me` - Get current user profile
- `PUT /api/users/me` - Update user profile
- `PUT /api/users/{id}/role` - Update user role (Admin only)

### Accommodations
- `POST /api/accommodations` - Create accommodation (Admin only)
- `GET /api/accommodations` - List all accommodations (Public)
- `GET /api/accommodations/{id}` - Get accommodation details (Public)
- `PUT /api/accommodations/{id}` - Update accommodation (Admin only)
- `DELETE /api/accommodations/{id}` - Delete accommodation (Admin only)

### Bookings
- `POST /api/bookings` - Create new booking
- `GET /api/bookings/my` - Get my bookings
- `GET /api/bookings` - Get all bookings with filters (Manager/Admin)
- `GET /api/bookings/{id}` - Get booking details
- `PUT /api/bookings/{id}` - Update booking
- `DELETE /api/bookings/{id}` - Cancel booking

### Payments
- `POST /api/payments` - Create Stripe payment session
- `GET /api/payments` - Get payments list
- `GET /api/payments/success` - Handle successful payment (Stripe callback)
- `GET /api/payments/cancel` - Handle cancelled payment (Stripe callback)
- `POST /api/payments/renew` - Renew expired payment session

### Health Check
- `GET /api/health` - Check application health

## User Roles

1. **CUSTOMER** (default)
    - Create and manage own bookings
    - View own payments
    - Update profile

2. **MANAGER**
    - All customer permissions
    - View all bookings and payments
    - Filter by user

3. **ADMIN**
    - All manager permissions
    - Manage accommodations (CRUD)
    - Update user roles

## Scheduled Tasks

### Daily Expired Bookings Check
- **Schedule**: Every day at 9:00 AM
- **Action**: Marks expired bookings as EXPIRED and releases accommodation availability
- **Notification**: Sends daily report to Telegram

### Payment Session Expiration Check
- **Schedule**: Every minute
- **Action**: Marks expired payment sessions as EXPIRED
- **Note**: Stripe sessions expire after 24 hours

## Notification Events

The application sends Telegram notifications for:
- New booking created
- Booking cancelled
- New accommodation added
- Accommodation released (after booking expiration)
- Payment session created
- Successful payment
- Daily expired bookings report

## Business Rules

1. **Bookings**
    - Cannot book in the past
    - Check-out must be after check-in
    - No overlapping bookings for same accommodation
    - User cannot have pending bookings without payment
    - Can only cancel future bookings

2. **Payments**
    - One payment per booking
    - Session expires after 24 hours
    - Can renew expired sessions
    - Booking confirmed only after successful payment

3. **Accommodations**
    - Cannot delete with active bookings
    - Availability automatically managed
    - Only admins can modify

## Testing

Run tests:

```bash
./mvnw test
```

## Code Quality

The project uses Checkstyle for code quality:

```bash
./mvnw checkstyle:check
```

## CI/CD

GitHub Actions workflow runs on every push and PR:
- Builds the project
- Runs tests
- Checks code style

## Docker Support

Build and run with Docker:

```bash
docker-compose up -d
```

## Environment Variables for Production

For production deployment, ensure these variables are set:
- Use strong JWT secret (256+ bits)
- Use production Stripe keys
- Set proper database credentials
- Configure correct base URL
- Secure Telegram bot token

## Security Considerations

- JWT tokens stored client-side
- Passwords hashed with BCrypt
- Role-based access control
- Soft delete for sensitive data
- API rate limiting recommended for production

## Troubleshooting

### Liquibase Issues
```bash
./mvnw liquibase:clearCheckSums
```

### Database Connection
- Verify PostgreSQL is running
- Check database credentials in `.env`
- Ensure database exists

### Telegram Notifications Not Working
- Verify bot token is correct
- Check chat ID matches your conversation
- Ensure bot has permission to send messages

### Stripe Payment Issues
- Use test mode credentials
- Check Stripe dashboard for session details
- Verify webhook URLs if configured

## License

This project is licensed under the MIT License.
