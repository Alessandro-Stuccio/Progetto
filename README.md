# FitConnect — Piattaforma SaaS per il Wellness Integrato

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4-6DB33F?logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql)
![Tests](https://img.shields.io/badge/tests-234_passing-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

Backend RESTful per una piattaforma SaaS che connette clienti con Personal Trainer, Nutrizionisti e partner assicurativi in un unico abbonamento. Costruito come monolite modulare su Java 21 e Spring Boot 4 con un'architettura rigorosa a layer.

---

## Indice

1. [Tech Stack](#tech-stack)
2. [Architettura](#architettura)
3. [Pattern di Design](#pattern-di-design)
4. [Domain Model](#domain-model)
5. [Quick Start](#quick-start)
6. [Credenziali di Test](#credenziali-di-test)
7. [API Docs](#api-docs)
8. [Configurazione](#configurazione)
9. [Testing](#testing)
10. [Changelog](#changelog)

---

## Tech Stack

| Categoria | Tecnologia |
|---|---|
| Runtime | Java 21 (LTS) |
| Framework | Spring Boot 4 (Web, Data JPA, Security, WebSocket, Validation) |
| Database | PostgreSQL 16 |
| Sicurezza | Spring Security + JWT (stateless) |
| Messaggistica real-time | STOMP / WebSocket |
| Messaggistica asincrona | RabbitMQ (Dead Letter Queue inclusa) |
| Logging | Log4j2 (via SLF4J) — Console + RollingFile + JDBC async su DB dedicato |
| Build | Maven Wrapper (`mvnw`) |
| Container | Docker Compose (PostgreSQL + pgAdmin + RabbitMQ) |
| Testing | JUnit 5, Mockito, H2 (in-memory) |

---

## Architettura

Il flusso di ogni richiesta segue un percorso unidirezionale senza salti di layer:

```
Controllers → Facades → Services → Builders → Repositories → PostgreSQL
                                 ↕ Mappers (entities ↔ DTOs)
```

- **Controllers** (`controller/`) — REST endpoints; delegano interamente a facade o service, nessuna business logic.
- **Facades** (`facade/` + `facade/impl/`) — Orchestrano più servizi per operazioni complesse. Le interfacce stanno in `facade/` (es. `AdminFacade`, `UserFacade`); le implementazioni in `facade/impl/`. `InsuranceController` è l'unico controller che inietta una facade direttamente senza interfaccia dedicata — riusa `AdminFacade`.
- **Mappers** (`mapper/`) — Convertitori per-entità tra entità JPA e DTO (es. `BookingMapper`, `UserMapper`). `FacadeMapper` in `facade/` è deprecato; usare i mapper dedicati.
- **Services** (`service/` + `service/impl/`) — Business logic. Interfacce in `service/`, implementazioni in `service/impl/`.
- **Builders** (`builder/` + `builder/impl/`) — Costruzione entità tramite Builder pattern; tutte le entità sono assemblate attraverso i builder.
- **Repositories** (`repository/`) — Spring Data JPA; nessun SQL custom eccetto JPQL in `@Query`.

### Struttura dei package principali

```
com.project.tesi/
├── controller/          # REST endpoints
├── facade/              # Interfacce facade
│   └── impl/            # Implementazioni facade
├── service/             # Interfacce service
│   ├── impl/            # Implementazioni service
│   └── strategy/        # Strategy pattern per le prenotazioni
├── builder/             # Interfacce builder
│   └── impl/            # Implementazioni builder
├── mapper/              # Mapper entità ↔ DTO (per-entità, iniettati nei service)
├── dto/
│   ├── request/         # DTO di input
│   ├── response/        # DTO di output
│   └── response/stats/  # DTO statistiche dashboard
├── model/               # Entità JPA
├── repository/          # Spring Data JPA
├── config/              # Async, RabbitMQ, WebSocket, CORS
├── security/            # JWT filter, UserDetailsService, SecurityConfig
├── scheduler/           # SubscriptionScheduler, BookingReminderScheduler
├── messaging/           # RabbitMQ publisher/consumer
├── exception/           # Gerarchia eccezioni per modulo + GlobalExceptionHandler
└── enums/               # Role, SlotStatus, DocumentType, ecc.
```

### Facade: interfacce e responsabilità

| Facade | Controller che la usa | Responsabilità |
|---|---|---|
| `AdminFacade` | `AdminController`, `AdminStatsController`, `InsuranceController` | Gestione utenti, statistiche admin, view assicuratore |
| `UserFacade` | `UserController`, `ProfessionalController`, `ProfessionalStatsController` | Profilo utente, slot professionisti, statistiche professionisti |
| `ModeratorFacade` | `ModeratorController` | Gestione utenti moderabili, abbonamenti, chat moderazione |
| `ChatFacade` | `ChatController` | Conversazioni, messaggi, permessi chat, chiusura chat |
| `DocumentFacade` | `DocumentController` | Upload/download/elimina documenti per ruolo |
| `PlanFacade` | `PlanController` | CRUD piani di abbonamento |
| `ActivityFeedFacade` | `ActivityFeedController` | Feed attività recenti (prenotazioni + documenti) |

> **Nota**: Le interfacce `I<Name>Facade` (es. `IAdminFacade`) sono alias `@Deprecated` mantenuti per retrocompatibilità. Non usarle nel nuovo codice.

---

## Pattern di Design

### GoF implementati

| Pattern | Dove | Descrizione |
|---|---|---|
| **Strategy** | `service/strategy/` | `BookingStrategy` con `PersonalTrainerBookingStrategy` e `NutritionistBookingStrategy`; selezionato a runtime in `SlotServiceImpl` in base al ruolo del professionista |
| **Builder** | `builder/` + `builder/impl/` | Ogni entità del dominio è costruita tramite un builder dedicato (`SlotBuilder`, `UserBuilder`, `SubscriptionBuilder`, ecc.). Il campo `bookedAt` e l'intero ciclo di vita dello slot passano da `SlotBuilderImpl`. |
| **Facade** | `facade/` + `facade/impl/` | Punti di ingresso coarse-grained che orchestrano più servizi. Interfaccia naming: `<Name>Facade`. |

### Mapper dedicati (9 totali)

Ogni entità ha il proprio mapper iniettato nei servizi che ne hanno bisogno. `FacadeMapper` è stato deprecato e svuotato.

| Mapper | Converte |
|---|---|
| `UserMapper` | `User` ↔ `UserResponse`, `toAdminResponse()` |
| `BookingMapper` | `Slot` (stato prenotazione) ↔ `BookingResponse` |
| `SubscriptionMapper` | `Subscription` ↔ `SubscriptionResponse`, `toSubscriptionFromAdmin()` |
| `SlotMapper` | `Slot` ↔ `SlotDTO` |
| `ReviewMapper` | `Review` ↔ `ReviewResponse` |
| `DocumentMapper` | `Document` ↔ `DocumentResponse` |
| `ChatMapper` | `Chat`/`Message` ↔ `ChatMessageResponse`/`ConversationPreviewResponse` |
| `PlanMapper` | `Plan` ↔ `PlanResponseDTO` |
| `ActivityFeedMapper` | `Slot`/`Document` → `ActivityFeedItemResponse` |

### Concorrenza (requisito per voti ≥ 27)

| Meccanismo | Dove | Scopo |
|---|---|---|
| **Optimistic locking** | `@Version` su `Slot`, `Subscription`, `User` | Gestione conflitti senza lock espliciti; `ObjectOptimisticLockingFailureException` → `ConcurrentUpdateException` |
| **Pessimistic locking** | `@Lock(PESSIMISTIC_WRITE)` su `SlotRepository.findByIdWithLock()` e `SubscriptionRepository.findByUserAndActiveTrueWithLock()` | Lock a DB sulle righe calde |
| **Fine-grained in-process locking** | `ConcurrentHashMap<Long, LockReference>` in `SlotServiceImpl` | Lock per-slot via `ReentrantLock` con `synchronized` sull'accesso alla mappa — risorsa condivisa + lock richiesti dal syllabus |

---

## Domain Model

### Ruoli

| Ruolo | Descrizione |
|---|---|
| `CLIENT` | Acquista piani, prenota slot, scarica documenti, lascia recensioni |
| `PERSONAL_TRAINER` | Definisce disponibilità, gestisce fino a 50 clienti, carica schede allenamento |
| `NUTRITIONIST` | Definisce disponibilità, gestisce fino a 50 clienti, carica piani alimentari |
| `INSURANCE_MANAGER` | Gestisce le polizze infortuni legate ai piani (vede clienti e abbonamenti via Admin Facade) |
| `MODERATOR` | Moderazione contenuti, supporto clienti, gestione anagrafiche CLIENT/PT/NUTRITIONIST |
| `ADMIN` | Supervisione globale, creazione piani, gestione MODERATOR e INSURANCE_MANAGER |

### Piani e Crediti

| Piano | Durata | Crediti PT/mese | Crediti Nutri/mese | Prezzo intero | Rata mensile |
|---|---|---|---|---|---|
| Basic Pack | Semestrale | 1 | 1 | € 960 | € 160 |
| Basic Pack | Annuale | 1 | 1 | € 1.800 | € 150 |
| Premium Pack | Semestrale | 2 | 2 | € 1.620 | € 270 |
| Premium Pack | Annuale | 2 | 2 | € 3.000 | € 250 |

I crediti si azzerano mensilmente (non sono cumulabili). Lo `SubscriptionScheduler` gira ogni notte a mezzanotte per il rinnovo crediti e la gestione delle rate.

### Prenotazioni e Slot

Lo stato di prenotazione vive **interamente in `Slot`** (campi: `bookedBy`, `status`, `meetingLink`, `bookedAt`, `reminderSent`). L'entità `Booking` è stata rimossa; il `BookingBuilder`/`BookingDirector` non sono più presenti.

- Slot da 30 minuti generati da `WeeklySchedule` settimanali dei professionisti
- Locking a doppio livello (JVM `ReentrantLock` + DB `PESSIMISTIC_WRITE`) per prevenire overbooking concorrente
- Cancellazione gratuita (credito rimborsato) se richiesta con almeno 24 ore di anticipo
- Link Jitsi generato automaticamente alla prenotazione (`JitsiVideoConferenceServiceImpl`)
- Notifiche email post-commit tramite `@TransactionalEventListener`
- `BookingReminderScheduler` — ogni 5 minuti invia promemoria e imposta `reminderSent` per evitare duplicati

### Recensioni

Un cliente può recensire un professionista solo se:
- esiste almeno una prenotazione confermata tra la coppia **oppure** il cliente è attualmente assegnato al professionista
- non ha ancora lasciato una recensione per quella coppia (unicità garantita a DB)

### Chat

- Real-time via STOMP/WebSocket con autenticazione JWT sul frame STOMP CONNECT (`WebSocketChannelInterceptor`)
- Fallback REST per lo storico (`/api/chat`)
- Messaggistica asincrona via RabbitMQ: `ChatMessagePublisher` → `ChatMessageConsumer` → DLQ per messaggi non recuperabili

#### Permessi di conversazione

| Ruolo | Può avviare chat con | Contatta Amministrazione |
|---|---|---|
| CLIENT | PT assegnato, Nutrizionista assegnato | Moderatore (casuale) |
| PT / NUTRITIONIST | Propri clienti assegnati | Moderatore |
| MODERATOR | CLIENT, PT, NUTRITIONIST, ADMIN, altri MODERATOR | — |
| ADMIN | Tutti (incluso INSURANCE_MANAGER) | — |
| INSURANCE_MANAGER | Solo ADMIN | — |

- Il pulsante "Termina chat" consente al Moderatore di chiudere una conversazione di supporto
- `validateChatPermission()` nel backend impone le regole con ordine di guardie: ADMIN → INSURANCE_MANAGER → MODERATOR → check assegnazione

### Documenti

File system storage (`uploads/`) con metadati in DB. Tipologie per ruolo:
- Personal Trainer → schede allenamento
- Nutrizionista → piani alimentari
- Insurance Manager → polizze

### Activity Feed

`GET /api/activity?days=N` restituisce prenotazioni e documenti degli ultimi N giorni, ordinati cronologicamente.

### Job Applications

`POST /api/job-applications` — riceve il CV in PDF e lo invia via email all'azienda.

### Statistiche Dashboard

| Endpoint | Ruolo | Dati restituiti |
|---|---|---|
| `GET /api/admin/stats` | ADMIN | Utenti per ruolo, crescita mensile, popolarità piani, ricavi, prenotazioni, carico professionisti |
| `GET /api/professional/stats` | PT / NUTRITIONIST | Prenotazioni oggi, clienti da seguire, documenti caricati questa settimana, totale clienti |

### Audit Trail

`AuditLog` + `AuditInterceptor` registrano tutte le azioni utente. Le nuove operazioni auditable vanno aggiunte in `AuditInterceptor`.

---

## Quick Start

### Prerequisiti

- **Java 21** — [Adoptium Temurin](https://adoptium.net/temurin/releases/?version=21)
- **Docker Desktop** — richiesto per il profilo `dev`

> Maven è incluso nel wrapper (`mvnw`), non serve installarlo.

### Profilo dev (database locale)

```bash
git clone <url-repository>
cd Progetto/tesi

# macOS / Linux
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Windows PowerShell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Docker Compose avvia automaticamente:
- PostgreSQL su `localhost:5432` (con healthcheck `pg_isready`)
- pgAdmin su `localhost:5050` (credenziali: `a@a.a` / `root`)
- RabbitMQ su `localhost:5672`; management UI su `localhost:15672` (guest/guest)

Il database viene ricreato e popolato con dati di test ad ogni avvio (`create` DDL + `data.sql`).

### Comandi utili

```bash
# Eseguire i test
./mvnw test

# Singola classe di test
./mvnw test -Dtest=SlotServiceImplTest

# Build JAR (senza test)
./mvnw clean package -DskipTests

# Report coverage JaCoCo (generato in target/site/jacoco/)
./mvnw verify
```

---

## Credenziali di Test

Disponibili solo con il profilo `dev`. Password comune: `password`.

| Email | Ruolo | Note |
|---|---|---|
| `pt1@test.com` | Personal Trainer | Disponibile lun/mer/ven |
| `pt2@test.com` | Personal Trainer | Disponibile mar/gio/sab |
| `nutri1@test.com` | Nutrizionista | Disponibile lun/mer/ven |
| `nutri2@test.com` | Nutrizionista | Disponibile mar/gio/sab |
| `luca@test.com` | Cliente | Assegnato a pt1 + nutri1, Basic Pack Semestrale |
| `sofia@test.com` | Cliente | Assegnato a pt1 + nutri2, Basic Pack Annuale |
| `matteo@test.com` | Cliente | Assegnato a pt2 + nutri1, Premium Pack Semestrale |
| `chiara@test.com` | Cliente | Assegnato a pt2 + nutri2, Premium Pack Annuale |
| `testreview@test.com` | Cliente | Utente dedicato al test delle recensioni |
| `admin@test.com` | Admin | Accesso completo |
| `insurance@test.com` | Insurance Manager | Gestione polizze |
| `moderator1@test.com` | Moderatore | Moderazione contenuti |

> Il seed `data.sql` include anche 15+ clienti aggiuntivi, 40+ slot, 20+ prenotazioni, 12+ recensioni e 14+ documenti per testare le statistiche dashboard.

---

## API Docs

### Endpoint principali

| Gruppo | Base path | Ruoli |
|---|---|---|
| Autenticazione | `/api/auth` | Pubblico |
| Prenotazioni / Slot | `/api/bookings`, `/api/slots` | CLIENT, PT, NUTRITIONIST |
| Professionisti | `/api/professionals` | CLIENT (ricerca), PT/NUTRITIONIST (gestione slot) |
| Recensioni | `/api/reviews` | CLIENT |
| Abbonamenti | `/api/subscriptions` | CLIENT, ADMIN, MODERATOR |
| Profilo utente | `/api/users` | Autenticato |
| Documenti | `/api/documents` | CLIENT (lettura), PT/NUTRITIONIST/INSURANCE (upload) |
| Chat | `/api/chat` (REST) + WebSocket `/ws` | Autenticato |
| Activity Feed | `/api/activity` | Autenticato |
| Statistiche professionista | `/api/professional/stats` | PT, NUTRITIONIST |
| Statistiche admin | `/api/admin/stats` | ADMIN |
| Pannello admin | `/api/admin` | ADMIN |
| Pannello moderatore | `/api/moderator` | MODERATOR |
| Pannello assicuratore | `/api/insurance` | INSURANCE_MANAGER |
| Piani | `/api/plans` | ADMIN (CRUD), Autenticato (lettura) |
| Candidature | `/api/job-applications` | Pubblico |

---

## Configurazione

### Variabili d'ambiente

| Variabile | Default dev | Descrizione |
|---|---|---|
| `JWT_SECRET` | valore di fallback in `TesiApplication` | Chiave segreta JWT (min. 32 caratteri, obbligatoria in prod) |
| `MAIL_FROM` | `koreadministration@gmail.com` | Indirizzo mittente email transazionali |
| `SMTP_HOST` | `smtp.gmail.com` | SMTP host |
| `SMTP_PORT` | `587` | SMTP port |
| `SMTP_USERNAME` | `koreadministration@gmail.com` | Credenziale SMTP — username |
| `SMTP_PASSWORD` | *(app password configurata)* | Credenziale SMTP — password o app password |

In sviluppo, i valori di default sono definiti in `application-dev.yaml`.

### Profili

| Profilo | DB | Docker Compose | DDL |
|---|---|---|---|
| `dev` (default) | PostgreSQL locale (`localhost:5432`) | Auto-avviato | `create` (schema ricreato ad ogni restart) |

### Log4j2

Il logging è gestito da Log4j2 (`src/main/resources/log4j2-spring.xml`) con tre appender:

| Appender | Destinazione | Livelli |
|---|---|---|
| `Console` | stdout | tutti i layer applicativi |
| `File` (RollingFile) | `logs/app.log` — rolling giornaliero, max 10 MB, 30 file | tutti i layer applicativi |
| `AsyncLogDB` (JDBC async) | PostgreSQL `tesi_logs.app_logs` — buffer 512 eventi | controller, service, security, scheduler, exception, audit |

Livelli per layer:
- `com.project.tesi.controller` — INFO
- `com.project.tesi.service` — DEBUG
- `com.project.tesi.security` — INFO
- `com.project.tesi.scheduler` — INFO
- `com.project.tesi.exception` — WARN
- Hibernate / Spring Framework — WARN (solo Console)

#### Database di log (`tesi_logs`)

Il database `tesi_logs` e la tabella `app_logs` vengono creati automaticamente all'avvio del profilo `dev` da `LogsDatabaseInitializer`. Il database usa la stessa istanza PostgreSQL locale del database principale ma in un catalog separato.

Schema tabella:
```sql
CREATE TABLE app_logs (
    id         BIGSERIAL    PRIMARY KEY,
    event_date TIMESTAMPTZ  NOT NULL,
    level      VARCHAR(10)  NOT NULL,
    logger     VARCHAR(200),
    message    TEXT,
    thread     VARCHAR(100),
    throwable  TEXT
);
```

### RabbitMQ

- `default-requeue-rejected: false` e `max-attempts: 3` in entrambi i profili
- I messaggi non recuperabili (`DataIntegrityViolationException`) vengono instradati alla Dead Letter Queue `chat.messages.dlq` tramite `AmqpRejectAndDontRequeueException`
- Thread pool configurati in `AsyncConfig`

### CORS

L'origin permessa è configurabile via `cors.allowed-origins` (default dev: `http://localhost:4200`).

### Note non ovvie

- **Email come username** — `UserDetails.getUsername()` restituisce l'email; non esiste un campo username separato.
- **Doppia durata JWT** — token di autenticazione: 24 h; token reset password: 30 min (entrambi in `JwtUtil`).
- **IPv4 per SMTP** — `TesiApplication` imposta `java.net.preferIPv4Stack=true` all'avvio per prevenire hang SMTP su IPv6.
- **WebSocket JWT** — `WebSocketChannelInterceptor` valida il token JWT sul frame STOMP CONNECT prima di permettere qualsiasi subscription.
- **Audit trail** — `AuditLog` + `AuditInterceptor` registrano tutte le azioni utente; le nuove operazioni auditable vanno aggiunte in `AuditInterceptor`.
- **Dev DDL** — `spring.jpa.hibernate.ddl-auto: create` nel profilo dev significa che lo schema viene ricreato ad ogni avvio; `data.sql` lo ripopola ogni volta.
- **Database di log separato** — `tesi_logs` è un catalog PostgreSQL distinto dal database principale; viene creato automaticamente da `LogsDatabaseInitializer` al primo avvio in profilo `dev`.

---

## Testing

```bash
# Suite completa (234 test, 32 classi)
./mvnw test

# Singola classe
./mvnw test -Dtest=SlotServiceImplTest

# Report coverage JaCoCo (generato in target/site/jacoco/)
./mvnw verify
```

I test usano H2 in-memory con profilo `test` (`create-drop`). Scheduler e Docker Compose sono disabilitati automaticamente durante i test.

### Pattern adottati

- `@ExtendWith(MockitoExtension.class)` + `@Mock` / `@InjectMocks` per unit test puri
- `@WebMvcTest` + `MockMvc` per i controller
- `@DisplayName` su ogni metodo per output leggibile

### Classi di test

| Layer | Classi |
|---|---|
| Controller | `AuthControllerTest`, `BookingControllerTest`, `PlanControllerTest`, `ReviewControllerTest`, `AllControllersTest` |
| Facade | `AdminFacadeTest`, `UserFacadeTest` |
| Service | `SlotServiceImplTest`, `SubscriptionServiceImplTest`, `ReviewServiceImplTest`, `UserServiceImplTest`, `AdminServiceImplTest`, `AdminStatsServiceImplTest`, `ProfessionalStatsServiceImplTest`, `AuthServiceImplTest`, `DocumentServiceImplTest`, `PlanServiceImplTest`, `ActivityFeedServiceImplTest` |
| Strategy | `PersonalTrainerBookingStrategyTest`, `NutritionistBookingStrategyTest` |
| Mapper | `BookingMapperTest`, `SubscriptionMapperTest`, `UserMapperTest` |
| Security | `JwtUtilTest`, `JwtAuthenticationFilterTest`, `CustomUserDetailsServiceTest` |
| Scheduler | `SubscriptionSchedulerTest`, `BookingReminderSchedulerTest` |
| Exception | `CustomExceptionsTest`, `GlobalExceptionHandlerTest` |
| Config | `WebSocketEventListenerTest` |

---

## Changelog

### Refactoring architetturale (Facade/Service/Mapper/DTO)

- **Nuove interfacce Facade** — `AdminFacade`, `UserFacade`, `ModeratorFacade`, `ChatFacade`, `DocumentFacade`, `PlanFacade`, `ActivityFeedFacade` con implementazioni in `facade/impl/`. Le interfacce `I<Name>Facade` sono ora alias `@Deprecated`.
- **Mapper dedicati** — `FacadeMapper` deprecato e svuotato; 9 mapper per-entità iniettati direttamente nei servizi.
- **Rimozione BookingBuilder/BookingDirector** — la prenotazione non è più un'entità separata; lo stato vive interamente in `Slot`. `SlotBuilder` copre l'intero ciclo di vita dello slot incluso `bookedAt`.
- **DTO unificati** — `UserResponse` e `SubscriptionResponse` sostituiscono i DTO precedenti frammentati.
- **`User.getFullName()`** — aggiunto metodo di convenienza; rimosso `profilePictureUrl`.

### Nuovi Controller e Feature

- **Activity Feed** (`GET /api/activity?days=N`) — prenotazioni e documenti recenti, ordinati cronologicamente.
- **Job Applications** (`POST /api/job-applications`) — candidature lavorative con CV allegato, inviate via email.
- **Professional Stats** (`GET /api/professional/stats`) — prenotazioni oggi, clienti da seguire, documenti caricati questa settimana.
- **Admin Stats** (`GET /api/admin/stats`) — utenti per ruolo, crescita mensile, popolarità piani, ricavi (mensili/annuali), prenotazioni, carico per professionista.
- **Moderator Controller** (`/api/moderator`) — CRUD utenti moderabili, abbonamenti, contatti chat, chiusura chat.
- **Professional Controller** (`/api/professionals`) — lista professionisti per ruolo, slot disponibili.

### Chat — Permessi e Bug Fix

- **Ridefinizione permessi** — nuova logica `validateChatPermission()` con ordine di guardie: ADMIN → INSURANCE_MANAGER (blocca se l'altra parte non è ADMIN) → MODERATOR → check assegnazione.
- **Pulsante "Termina chat"** — il Moderatore può chiudere una conversazione di supporto.
- **Fix message overlap** — `chatMessages` e `messagesSubject` vengono svuotati prima di caricare i messaggi di una nuova conversazione; guard su `chatId` nelle subscription per scartare messaggi di chat diverse durante la transizione.
- **Fix double checkmark** — SVG ridisegnato (stile WhatsApp): spunta singola bianca per messaggi inviati, doppia spunta blu per messaggi letti (`status === 'READ'`).

### Infrastruttura & Messaggistica

- **Fix RabbitMQ infinite redelivery loop** — `default-requeue-rejected: false`, `max-attempts: 3`, `AmqpRejectAndDontRequeueException` per errori permanenti, Dead Letter Queue `chat.messages.dlq`.
- **Fix deprecation RabbitMQConfig** — `JacksonJsonMessageConverter` sostituito con `Jackson2JsonMessageConverter`.
- **Fix Docker Compose auto-discovery** — rimosso path esplicito `file: docker-compose.yml`; auto-discovery attivo. Healthcheck su `pg_isready` (PostgreSQL) e `rabbitmq-diagnostics check_port_connectivity` (RabbitMQ).
- **Dipendenza `spring-boot-docker-compose`** — aggiunta in `pom.xml` (scope `runtime`, `optional: true`).

### Endpoint Insurance Manager

Nuovi endpoint `GET /api/insurance/subscriptions` e `GET /api/insurance/users` riservati al ruolo `INSURANCE_MANAGER`. Le KPI e la lista clienti nella dashboard dell'assicuratore ora si popolano correttamente (prima chiamavano `/api/admin/*` restituendo 403).

### Abbonamento alla creazione utente

- **`UserCreateRequestDTO`** — aggiunti campi `planId` e `paymentFrequency`.
- **`AdminServiceImpl.createUserInternal()`** — se il nuovo utente è CLIENT e `planId` è presente, viene creata e salvata la `Subscription` tramite `SubscriptionMapper.toSubscriptionFromAdmin()`.
- **Frontend** — form creazione utente (step 2) include selezione frequenza di pagamento (Unica soluzione / Rate mensili).

### Restrizione modifica profili

Solo il **MODERATORE** può modificare CLIENT, PERSONAL_TRAINER e NUTRITIONIST. L'ADMIN gestisce solo MODERATOR e INSURANCE_MANAGER.

- **`validateUpdatePermissions()`** — se l'actor è `null` (ADMIN) e il target non è in `ADMIN_MANAGEABLE_ROLES` viene sollevata `UnauthorizedAccessException`.
- **Frontend `admin-users-tab`** — pulsante "Modifica" visibile solo per i ruoli che il ruolo corrente può gestire.

### Contatta Amministrazione

Il pulsante "Contatta Amministrazione" (CLIENT, PT, NUTRITIONIST) apre una chat con il **MODERATORE** (non più con l'Admin). Nuovo endpoint `GET /api/users/moderator` restituisce un moderatore casuale.

### Seed dati di test espanso

`data.sql` aggiornato con: 15+ clienti, 40+ slot, 20+ prenotazioni, 12+ recensioni, 14+ documenti — necessario per popolare le statistiche della dashboard.
