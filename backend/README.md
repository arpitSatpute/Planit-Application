# Event Rental & Vendor Platform — Backend

A **production-grade Spring Boot monolithic backend** for an event rental and vendor marketplace.

## Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 17 LTS |
| Framework | Spring Boot | 3.2.x |
| Database | MongoDB | 6.0+ |
| Cache | Redis | 7.0+ |
| Auth | JWT (JJWT) | 0.11.5 |
| Build | Maven | 3.8+ |
| Containerization | Docker + Compose | — |

## Project Structure

```
backend/
├── src/main/java/com/eventrental/
│   ├── EventRentalApplication.java        # Main entry point
│   ├── config/                            # Security, Redis, WebSocket, Async
│   ├── controller/                        # REST controllers
│   ├── dto/request/ & dto/response/       # DTOs
│   ├── exception/                         # Custom exceptions + GlobalExceptionHandler
│   ├── model/                             # MongoDB documents
│   ├── repository/                        # MongoRepository interfaces
│   ├── scheduled/                         # Cron jobs
│   ├── security/                          # JWT + Spring Security
│   └── service/                           # Business logic
├── src/main/resources/
│   └── application.yml                    # Unified configuration
├── docker-compose.yml                     # MongoDB + Redis + App
├── Dockerfile                             # Multi-stage build
└── pom.xml                                # Maven dependencies
```

## API Endpoints

### Auth — `/api/v1/auth/`
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/register` | Public |
| POST | `/login` | Public |
| POST | `/refresh-token` | Public |
| POST | `/send-otp` | Public |
| POST | `/verify-otp` | Public |
| POST | `/logout` | Authenticated |

### Products — `/api/v1/products/`
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/` (with filters) | Public |
| GET | `/{productId}` | Public |
| GET | `/slug/{slug}` | Public |
| POST | `/` | VENDOR / ADMIN |
| PUT | `/{productId}` | VENDOR / ADMIN |
| DELETE | `/{productId}` | VENDOR / ADMIN |

### Bookings — `/api/v1/bookings/`
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/` | Authenticated |
| GET | `/` | Authenticated (own bookings) |
| GET | `/vendor` | VENDOR |
| GET | `/{bookingId}` | Authenticated |
| PATCH | `/{bookingId}/cancel` | Authenticated |

### Payments — `/api/v1/payments/`
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/create-order` | Authenticated |
| POST | `/verify` | Public (webhook) |

### Reviews — `/api/v1/reviews/`
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/` | Authenticated |
| GET | `/product/{productId}` | Public |
| POST | `/{reviewId}/vendor-response` | VENDOR |

### Vendors — `/api/v1/vendors/`
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/register` | Authenticated |
| GET | `/{vendorId}` | Public |
| GET | `/me` | VENDOR |
| PUT | `/me` | VENDOR |

### Users — `/api/v1/users/`
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/me` | Authenticated |
| PUT | `/me/profile` | Authenticated |
| PUT | `/me/address` | Authenticated |
| PUT | `/me/preferences` | Authenticated |

### Admin — `/api/v1/admin/`
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/users` | ADMIN |
| PATCH | `/users/{userId}/suspend` | ADMIN |
| PATCH | `/users/{userId}/activate` | ADMIN |
| GET | `/vendors` | ADMIN |
| PATCH | `/vendors/{vendorId}/verify` | ADMIN |

### Chat — `/api/v1/chat/`
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/send` | Authenticated |
| GET | `/conversations/{id}/messages` | Authenticated |
| POST | `/conversations/{id}/read` | Authenticated |

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### Run with Docker (recommended)
```bash
cd backend
docker-compose up -d
```
App will be available at `http://localhost:8080/api/v1`

### Run locally
1. Start MongoDB and Redis:
   ```bash
   docker-compose up -d mongo redis
   ```
2. Set environment variables or configure `application.yml`
3. Run:
   ```bash
   mvn spring-boot:run
   ```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `MONGODB_URI` | `mongodb://localhost:27017/event_rental` | MongoDB connection string |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `JWT_SECRET` | *(set in production!)* | 256-bit JWT signing secret |
| `RAZORPAY_KEY_ID` | — | Razorpay API key |
| `RAZORPAY_KEY_SECRET` | — | Razorpay secret |
| `AWS_ACCESS_KEY` | — | AWS access key (for S3) |
| `AWS_SECRET_KEY` | — | AWS secret key (for S3) |
| `S3_BUCKET_NAME` | `event-rental-uploads` | S3 bucket name |

## WebSocket

Connect to `ws://localhost:8080/api/v1/ws` (SockJS + STOMP).

- Send messages: `/app/chat.send`
- Receive messages: `/user/queue/messages`
- Booking notifications: `/user/queue/notifications`

## Security

- JWT access tokens (24h expiry) + refresh tokens (7d expiry)
- Bcrypt password hashing (strength 12)
- Role-based access control (USER, VENDOR, PLANNER, ADMIN)
- CORS configured for cross-origin mobile app requests
- Stateless session management
