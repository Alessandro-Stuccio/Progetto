# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

The project lives under `tesi/`. All Maven commands run from that directory.

```bash
cd tesi

# Run the app — the dev profile is active by default and auto-starts Docker Compose
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=BookingServiceImplTest

# Build JAR (skip tests)
./mvnw clean package -DskipTests
```

**Prerequisites for dev profile:** Java 21, Docker Desktop running.

**Test credentials** (from `data.sql` seed, dev only — password `password` for all):
- Admin: `admin@test.com`
- Client: `luca@test.com`
- Personal Trainer: `pt1@test.com`
- Nutritionist: `nutri1@test.com`
- Moderator: `moderator1@test.com`
- Insurance Manager: `insurance@test.com`

## Architecture

Single Spring Boot 4 monolith with a strict layered flow:

```
Controllers → Facades → Services → Builders → Repositories → PostgreSQL
                                 ↕ Mappers (entities ↔ DTOs)
```

- **Controllers** (`controller/`) — REST endpoints; delegate entirely to facades or services, no business logic.
- **Facades** (`facade/` + `facade/impl/`) — Orchestrate multiple services for complex operations. Interfaces live in `facade/` (e.g., `AdminFacade`, `UserFacade`); implementations in `facade/impl/` (e.g., `AdminFacadeImpl`). `InsuranceController` is the only controller that injects a facade directly without a dedicated interface — it reuses `AdminFacade`.
- **Mappers** (`mapper/`) — Per-entity converters between JPA entities and DTOs (e.g., `BookingMapper`, `UserMapper`). `FacadeMapper` in `facade/` is deprecated; use the dedicated mappers instead.
- **Services** (`service/` + `service/impl/`) — Business logic. Interfaces under `service/`, implementations under `service/impl/`.
- **Builders** (`builder/` + `builder/impl/`) — Entity construction via the Builder pattern; all entities are assembled through builders.
- **Repositories** (`repository/`) — Spring Data JPA; no custom SQL except JPQL in `@Query` annotations.

### Key Design Patterns (GoF, framework-independent)

- **Builder** — Every domain entity is assembled through a hand-written Builder: interface in `builder/` (`SlotBuilder`, `UserBuilder`, …), concrete implementation in `builder/impl/`. The `SlotBuilder` covers the full slot lifecycle including the `bookedAt` field.
- **Strategy** — `BookingStrategy` interface (`service/strategy/`) with `PersonalTrainerBookingStrategy` and `NutritionistBookingStrategy`; `SlotServiceImpl` selects the concrete strategy at runtime based on the professional's role (true dynamic dispatch).
- **Facade** — Coarse-grained entry points over multiple services. Interface naming: `<Name>Facade` (e.g., `AdminFacade`). The `I<Name>Facade` files (e.g., `IAdminFacade`) are legacy aliases that simply extend the primary interface and should not be used in new code.

### Concurrency (requirement for grades ≥27)

- **Optimistic locking** — `@Version` on `Slot`, `Subscription`, `User`; `ObjectOptimisticLockingFailureException` is caught and translated into `ConcurrentUpdateException`.
- **Pessimistic locking on hot rows** — `@Lock(LockModeType.PESSIMISTIC_WRITE)` on `SlotRepository.findByIdWithLock` and `SubscriptionRepository.findByUserAndActiveTrueWithLock`.
- **Fine-grained in-process locking** — `SlotServiceImpl` keeps a `ConcurrentHashMap<Long, LockReference>` of per-slot `ReentrantLock`s plus a `synchronized` block on the map for safe acquire/release; this is the shared resource + lock combination required by the syllabus.

### Domain Overview

| Concept | Key rules |
|---|---|
| **Subscription** | Plans are Basic (1+1 credits/month) or Premium (2+2 credits/month); semi-annual or annual, lump-sum or installments |
| **Booking** | Booking state lives entirely in `Slot` (fields: `bookedBy`, `status`, `meetingLink`, `bookedAt`, `reminderSent`). Booking deducts credits, uses per-slot `ReentrantLock` to prevent overbooking, and generates a Jitsi meeting link (`JitsiVideoConferenceServiceImpl`). |
| **Slot** | 30-minute windows generated from a `WeeklySchedule`; max 50 clients per professional |
| **Review** | One review per client–professional pair; only clients who have booked can review |
| **Chat** | Real-time via STOMP/WebSocket; REST fallback for history |
| **Document** | Files stored on filesystem (`uploads/` dir) with metadata in DB; separate types per role |

### Roles

`CLIENT`, `PERSONAL_TRAINER`, `NUTRITIONIST`, `MODERATOR`, `INSURANCE_MANAGER`, `ADMIN`

### Background Jobs

- `SubscriptionScheduler` — runs daily at midnight; resets monthly credits and processes installment charges.
- `BookingReminderScheduler` — runs every 5 minutes; queries upcoming bookings and sets a `reminderSent` flag after sending to prevent duplicate emails.

Both are disabled automatically during tests via Spring test profile configuration.

### Async Messaging

RabbitMQ handles async chat delivery: `ChatMessagePublisher` enqueues messages, `ChatMessageConsumer` processes them. Thread pools are configured in `AsyncConfig`.

## Profiles & Configuration

Configuration is a **single `application.yaml`** (`src/main/resources/`). The old `application-dev.yaml` and the separate prod profile no longer exist. The dev profile is still kept active (`spring.profiles.active: dev`) on purpose — the two `@Profile("dev")` beans (`DevFileSeedInitializer`, `LogsDatabaseInitializer`) need it — not to separate environments. So there is effectively one runtime config: local PostgreSQL on `localhost:5432`, Docker Compose auto-started, `ddl-auto: create` (schema dropped and recreated on every startup). Tests still run under their own `test` profile with H2 in-memory (`create-drop`), Docker and schedulers disabled.

**Dev Docker Compose services:**
- PostgreSQL — `localhost:5432`
- pgAdmin — `localhost:5050` (credentials: `a@a.a` / `root`)
- RabbitMQ — `localhost:5672`; management UI at `localhost:15672` (guest/guest)

Overridable via environment variables (all with dev defaults baked into `application.yaml`): `MAIL_FROM`, `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`. There is no `JWT_SECRET`: the signing key is generated randomly at startup from `jwt.length`, so every restart invalidates issued tokens.

CORS allowed origin is set via `cors.allowed-origins` (dev default: `http://localhost:4200`).

## Testing

Tests use JUnit 5 + Mockito + Spring Test. The test profile uses H2 in-memory with `create-drop` DDL.

Pattern used throughout:
- `@ExtendWith(MockitoExtension.class)` + `@Mock`/`@InjectMocks` for pure unit tests
- `MockMvc` + `@WebMvcTest` for controller-layer tests
- `@DisplayName` on every test method for readable output

Tests mirror the source tree under `src/test/java/com/project/tesi/`.

## Exception Handling

All domain exceptions extend `BaseException` (which carries an HTTP status) and are organized by module under `exception/auth/`, `exception/booking/`, `exception/subscription/`, `exception/document/`. `GlobalExceptionHandler` (`@RestControllerAdvice`) maps them all centrally.

## Non-Obvious Constraints

- **Email as username** — `UserDetails.getUsername()` returns the user's email address; there is no separate username field.
- **Dual JWT lifetimes** — auth tokens expire in 24 h; password-reset tokens expire in 30 min (both in `JwtUtil`).
- **IPv4 for SMTP** — `TesiApplication` sets `java.net.preferIPv4Stack=true` at startup to prevent IPv6-related SMTP hangs.
- **WebSocket JWT validation** — `WebSocketChannelInterceptor` validates the JWT token on the STOMP CONNECT frame before allowing any subscription.
- **Audit trail** — `AuditLog` entity + `AuditInterceptor` records all user actions; add new auditable operations there.
- **Dev DDL** — `spring.jpa.hibernate.ddl-auto: create` in the dev profile means the database schema is dropped and recreated on every application startup; `data.sql` re-seeds it each time.

## API Documentation

Swagger UI is available at `/swagger-ui.html` when the app is running (via springdoc-openapi).
