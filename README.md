# Wallet Service

## Overview

This project is a Wallet Service application built with Spring Boot. It provides APIs for managing wallets,
transactions, and ledger entries.

### Tech Stack

- **Java**: 19
- **Spring Boot**: 3.1.3
- **Database**: PostgreSQL (Docker image: `postgres:13-alpine`)

## Quick Start (How to Run)

Follow these steps to get the application running locally.

### Prerequisites

- Java 19+ installed
- Docker and Docker Compose installed and running

### Step 1: Start the Database

The application requires a PostgreSQL database. We use Docker Compose to spin up Postgres and pgAdmin.

**Windows:**
Run the interactive menu script:

```powershell
.\docker\docker-menu.ps1
```

Select option **1** to start containers.

**Linux / macOS:**
Run the start script:

```bash
  ./docker/start-containers.sh
```

*Alternatively, run `docker-compose -f docker/docker-posgress-pgadmin.yml up -d`*

### Step 2: Run the Application

Once the database is up and running, start the Spring Boot application:

```bash
  ./gradlew :modules:app:bootRun
```

The application will start on port **8080**.

### Step 3: Access the API

You can access the Swagger UI to explore and test the API endpoints:
http://localhost:8080/swagger-ui.html

You can also access pgAdmin to inspect the database at http://localhost:8000.

- **pgAdmin Login**: Email: `admin@admin.com`, Password: `admin`
- **Database Connection**: When accessing the `wallet-postgres-db` server in pgAdmin for the first time, you may be
  prompted for the **database password**.
    - Password: **`wallet_db_password`**
    - You can check "Save Password" to avoid entering it again.

---

## Modules

The project is organized into the following modules:

- **modules:api**: Contains the REST controllers and API definitions.
    - **WalletController**: Handles core wallet operations (create, transfer, withdraw, deposit). Supports API
      versioning ( `/api/v1/wallets`).
    - **ManagementController**: Provides administrative endpoints, such as adding and removing wallets from the
      blacklist (`/api/v1/management/blacklist`).
- **modules:core**: Contains the domain entities, repositories, and core business logic.
- **modules:services**: Contains the service layer that implements business logic, transaction management, and
  orchestrates operations on domain entities.
- **modules:shared:hibernate**: Shared Hibernate configuration and base classes.
- **modules:shared:spring**: Shared Spring configuration and utilities.
- **modules:shared:testing**: Shared testing utilities and base classes.
- **modules:app**: The main application module that packages everything together.

## Configuration

The application runs on port **8080** by default.

### Database

- **Production/Dev**: Uses PostgreSQL. Configuration can be found in `modules/app/src/main/resources/application.yml`.
- **Integration Tests**: Uses PostgreSQL (requires a running instance or Docker).
- **Unit Tests**: Uses H2 in-memory database.

## Development

### Building the Project

To build the project without running tests:

```bash
  ./gradlew assemble
```

### Code Style (Spotless)

This project uses **Spotless** to enforce code formatting.
If your build fails due to formatting issues, run the following command to automatically fix them:

```bash
  ./gradlew spotlessApply
```

## Testing

### Unit Tests

Unit tests ( repository tests) use an in-memory H2 database. No external infrastructure is required.
These tests use the configuration from **`application-test.yml`** (located in
`modules/shared/testing/src/main/resources`).

To run unit tests:

```bash
  ./gradlew test
```

### Integration Tests

Integration tests ( `ManagementControllerTest`, `ConcurrencyIntegrationTest`) require a running PostgreSQL database.
These tests use the configuration from **`application-itest.yml`** (located in
`modules/shared/testing/src/main/resources`).

**Ensure Docker is running** (Step 1 of Quick Start) before running integration tests.

To run integration tests:

```bash
  ./gradlew itest
```

## Docker & Environment

The `docker` directory contains configuration files and scripts to manage the development environment (PostgreSQL and
pgAdmin).

### Directory Structure

- **docker-posgress-pgadmin.yml**: The Docker Compose file defining the services (PostgreSQL database and pgAdmin
  interface).
- **postgres/**: Contains initialization scripts ( `init.sql`) for the PostgreSQL database.
- **pgadmin/**: Contains configuration for pgAdmin ( `servers.json`).

### Managing Containers

#### Windows

For Windows users, a user-friendly PowerShell script is provided:

- **docker-menu.ps1**: An interactive menu to manage Docker containers. It allows you to:
    - Start containers
    - Stop containers
    - Remove containers
    - View container status
    - View logs

To run it, right-click the file and select "Run with PowerShell" or execute it from a terminal:

```powershell
.\docker\docker-menu.ps1
```

#### Linux / macOS / Manual Execution

For Linux and macOS users, shell scripts are provided. Alternatively, you can run the `docker-compose` commands directly
from the `docker` directory.

**Start Containers:**

```bash
# Using script
./docker/start-containers.sh

# Manual command
docker-compose -f docker/docker-posgress-pgadmin.yml up -d
```

**Stop Containers:**

```bash
# Using script
./docker/stop-containers.sh

# Manual command
docker-compose -f docker/docker-posgress-pgadmin.yml down -v
```

**Remove Containers (Clean up):**

```bash
# Using script
./docker/remove-containers.sh

# Manual command
docker-compose -f docker/docker-posgress-pgadmin.yml rm -vf
```

### Services & Credentials

#### PostgreSQL

- **Port**: `5432`
- **Database**: `wallet_db`
- **User**: `wallet_db_user`
- **Password**: `wallet_db_password`

#### pgAdmin

- **URL**: http://localhost:8000
- **Email**: `admin@admin.com`
- **Password**: `admin`

## Troubleshooting

### Port Conflicts

If you see an error like `Bind for 0.0.0.0:5432 failed: port is already allocated`, it means another service (likely a
local PostgreSQL instance) is using port 5432.

- Stop the local PostgreSQL service.
- Or modify `docker-posgress-pgadmin.yml` to map to a different port ( `"5433:5432"`), but remember to update
  `application.yml` and `application-itest.yml` accordingly.

## Future Improvements & Production Readiness

To make this application fully production-ready, the following improvements are recommended:

### 1. Security

- Implement a **Security Module** using Spring Security.
- Secure API endpoints using OAuth2 or JWT authentication.
- Ensure only authorized users can perform sensitive operations ( blocking wallets).

### 2. Auditing

- Enhance the current JPA Auditing.
- Implement **Entity Auditing** (using Hibernate Envers) to track not just *when* data changed, but *who* changed it and
  *what* the previous values were. This is critical for financial applications.

### 3. Distributed Scheduling

- If the application runs on multiple instances (horizontal scaling), ensure scheduled tasks (schedulers) run correctly.
- Use a distributed lock mechanism (**ShedLock**) to ensure a job runs on only one instance at a time.

### 4. Data Cleanup (Idempotency Keys)

- Implement a scheduled job to clean up old records from the `idempotency_keys` table.
- Define a retention policy ( delete keys older than 72 hours or 30 days) to prevent the table from growing
  indefinitely.

### 5. Caching

- Implement caching for frequently accessed, read-heavy data (wallet balances, blacklist status) to improve performance.
- For a single instance, local caching (Caffeine) is sufficient.
- For multiple instances, use a distributed cache like **Redis** to ensure cache consistency across nodes.

### 6. Monitoring & Observability

- Enable **Spring Boot Actuator** to expose health checks and metrics.
- Integrate with **Prometheus** and **Grafana** for real-time monitoring and alerting.
- Implement centralized logging (ELK Stack or Splunk) to aggregate logs from all instances.

## API Documentation

Once the application is running, Swagger UI is available at:
http://localhost:8080/swagger-ui.html
