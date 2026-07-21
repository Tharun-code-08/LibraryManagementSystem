# Library Management System

An enterprise-grade, multi-branch Library Management System desktop application built with
Java 21, JavaFX, Hibernate/JPA, HikariCP, and Flyway, following Clean Architecture.

## Documentation
Full design documentation (SRS, architecture, ER/class/sequence diagrams, database schema, UI
wireframes, color palette, and the phased implementation roadmap) lives in [`docs/`](docs/README.md).

## Getting Started

### Prerequisites
- Java 21 (JDK)
- Maven 3.9+
- MySQL 8.x (running locally or reachable per `src/main/resources/database.properties`)

### Configure
Edit `src/main/resources/database.properties` (or supply an override on the classpath) with
your MySQL connection details. On first run, Flyway automatically creates and seeds the schema
(see `src/main/resources/db/migration/`), including a default administrator account:

- Username: `admin`
- Password: `Admin@123` (rotate immediately in any non-development environment)

### Build & Run
```bash
mvn clean compile
mvn javafx:run
```

### Test
```bash
mvn test
```

## Project Status
**Phase 0 — Project Bootstrap** is complete: Maven project skeleton, layered package structure,
Flyway-managed database schema (security/catalog/people/circulation/finance/inventory/
notifications), Hibernate + HikariCP wiring, the DI composition root (`AppContext`), global
exception handling, base theming (light/dark), and a bootstrap shell screen proving the stack
boots end-to-end.

See [`docs/13-ImplementationRoadmap.md`](docs/13-ImplementationRoadmap.md) for what's next.
