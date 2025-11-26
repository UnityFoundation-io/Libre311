# GEMINI.md

This file provides guidance to Gemini when working with code in this repository.

## Project Overview

Libre311 is a web application for service requests based on the Open311 standard. It consists of:
- **Backend API**: Micronaut-based Java application implementing Open311 GeoReport API v2
- **Frontend UI**: Svelte/SvelteKit web application
- **Database**: MySQL or PostgreSQL for persistent storage
- **External Auth**: UnityAuth service for authentication and authorization

## Development Setup

### Prerequisites
- JDK 17 or later for the backend
- Node.js and npm for the frontend
- UnityAuth service must be running (https://github.com/UnityFoundation-io/UnityAuth)

### Local Development

**Backend (API)**:
```bash
# From project root
source setenv.sh
./gradlew app:run

# With auto-restart on file changes
./gradlew app:run -t
```

**Frontend (UI)**:
```bash
# From project root
source setenv.sh
cd frontend
npm install
npm run dev
```

### Docker Development

Use Makefile targets for Docker-based development:
```bash
make compose_api    # API + database
make compose_ui     # UI + database
make compose_all    # All services
```

Or use Docker Compose directly:
```bash
docker compose -f docker-compose.local.yml up
```

**Important**: Docker environment uses `.env.docker` (not `.env`) for environment variables.

## Common Commands

### Backend

**Build**:
```bash
./gradlew app:assemble
```

**Run tests**:
```bash
./gradlew app:test
```

**Run single test**:
```bash
./gradlew app:test --tests "ClassName.testMethodName"
```

### Frontend

**Development server**:
```bash
cd frontend
npm run dev
```

**Build for production**:
```bash
cd frontend
npm run build
```

**Preview production build**:
```bash
cd frontend
npm run preview
```

**Run all tests**:
```bash
cd frontend
npm run test
```

**Run unit tests only**:
```bash
cd frontend
npm run test:unit
```

**Run integration tests only**:
```bash
cd frontend
npm run test:integration
```

**Linting and formatting**:
```bash
cd frontend
npm run lint     # Check for issues
npm run format   # Auto-format code
npm run check    # Type checking with svelte-check
```

## Architecture

### Backend Structure

The backend follows a layered architecture:

1. **API Layer** (`app/src/main/java/app/*Controller.java`): HTTP endpoints
   - `RootController.java`: Open311 GeoReport API v2 endpoints
   - `ImageStorageController.java`: Image upload handling
   - `JurisdictionAdminController.java`: Jurisdiction management
   - `TenantAdminController.java`: Multi-tenancy administration

2. **Service Layer** (`app/src/main/java/app/service/`): Business logic
   - Service classes inject repositories and other services as needed
   - Follow CRUD patterns per resource with cross-cutting concerns

3. **Data Layer** (`app/src/main/java/app/model/`): JPA entities and repositories
   - Core models: User, Service, ServiceDefinition, ServiceRequest
   - Micronaut Data repositories for database access
   - Spatial data support via Hibernate Spatial

4. **DTO Layer** (`app/src/main/java/app/dto/`): Data Transfer Objects
   - All HTTP request/response bodies use DTOs
   - Field-level validation annotations
   - Automatic parsing and validation by Micronaut

5. **Security** (`app/src/main/java/app/security/`): Auth and authorization
   - OAuth via UnityAuth service
   - JWT-based authentication with cookies
   - Permission-based authorization with `@RequiresPermissions` annotation
   - Users must exist in `app_users` table for full authentication

6. **External Services**:
   - **Storage**: Google Cloud Storage for user-uploaded images (`app/src/main/java/app/service/storage/`)
   - **SafeSearch**: Google Vision API for content moderation (`app/src/main/java/app/imagedetection/`)
   - **ReCaptcha**: Bot prevention (`app/src/main/java/app/recaptcha/`)

### Frontend Structure

SvelteKit application with file-based routing:

1. **Routes** (`frontend/src/routes/`):
   - `/issues/*`: Service request viewing (list, map, table views)
   - `/issue/create`: Create new service request
   - `/groups/*`: Service group and definition management
   - `/login`: Authentication

2. **Components** (`frontend/src/lib/components/`):
   - `CreateServiceRequest/`: Multi-step form for creating requests
   - `ServiceDefinitionEditor/`: Admin interface for service configuration
   - `MapComponent.svelte`: Leaflet-based mapping
   - Various UI components for service request display and interaction

3. **Services** (`frontend/src/lib/services/`):
   - `Libre311/`: API client for backend
   - `UnityAuth/`: Authentication service client
   - `EventBus/`: Application-wide event handling

4. **Context** (`frontend/src/lib/context/`):
   - Svelte context providers for global state management
   - `Libre311ContextProvider`: Application configuration
   - `ServiceRequestsContextProvider`: Service request state

## Configuration

### Backend Environment Variables

Key variables (see `setenv.sh.example` for full list):
- `GCP_PROJECT_ID`: Google Cloud project ID
- `STORAGE_BUCKET_ID`: GCS bucket for images
- `RECAPTCHA_SECRET`: ReCaptcha verification secret
- `AUTH_BASE_URL`: UnityAuth service URL
- `AUTH_JWKS`: UnityAuth JWKS endpoint
- `LIBRE311_JDBC_URL`: Database JDBC URL
- `LIBRE311_JDBC_DRIVER`: Database driver class
- `LIBRE311_JDBC_USER`: Database username
- `LIBRE311_JDBC_PASSWORD`: Database password
- `LIBRE311_AUTO_SCHEMA_GEN`: Schema generation strategy (default: `update`)
- `LIBRE311_DATABASE_DEPENDENCY`: Database driver dependency for build

### Frontend Environment Variables

- `VITE_BACKEND_URL`: API URL (use `/api` if served by backend, otherwise full URL)
- `VITE_GOOGLE_RECAPTCHA_KEY`: ReCaptcha site key

### Environment-Specific Configuration

Backend uses Micronaut's profile system:
- `application.yml`: Base configuration
- `application-local.yml`: Local development overrides
- `application-docker.yml`: Docker environment overrides
- `application-prod.yml`: Production overrides

Set active profile with `MICRONAUT_ENVIRONMENTS` environment variable.

## Database

### Supported Databases
- MySQL Server 8.0, 5.7
- PostgreSQL 8.2+

### Schema Management
- Managed by Hibernate JPA with auto-schema-gen
- Flyway migrations in `app/src/main/resources/db/`
- Options: `none`, `create-only`, `drop`, `create`, `create-drop`, `validate`, `update`

### Authorizing CSV Download Users
```sql
USE libre311db;
INSERT INTO app_users (email) VALUES ('user@example.com');
```

## Google Cloud Authentication

Backend uses Application Default Credentials (ADC) for GCS and SafeSearch:
- Setup: https://cloud.google.com/docs/authentication/application-default-credentials
- For Docker: Set `ADC_PATH` environment variable to credentials file path

## Testing

### Backend Tests
- Located in `app/src/test/java/`
- Run all: `./gradlew app:test`
- Run specific test: `./gradlew app:test --tests "ClassName.testMethodName"`

### Frontend Tests
- Unit tests with Vitest
- Integration tests with Playwright
- Run all: `cd frontend && npm run test`
- Unit only: `cd frontend && npm run test:unit`
- Integration only: `cd frontend && npm run test:integration`

## Build Artifacts

### Backend
- Executable JAR: `./gradlew app:assemble`
- Output: `app/build/libs/app-[version]-all.jar`
- Run: `java -jar app/build/libs/app-[version]-all.jar`

### Frontend
- Production build: `cd frontend && npm run build`
- Output: `frontend/build/`

## Docker

### Image Rebuilding
After changes to `app/src/main` or `frontend/src`, rebuild images:
```bash
docker rmi <libre311-api-image-id>
docker rmi <libre311-ui-image-id>
docker compose -f docker-compose.local.yml up
```

### Host Entries
Add to `/etc/hosts` for service name resolution:
```
127.0.0.1 libre311-api
127.0.0.1 libre311-db
127.0.0.1 libre311-ui
127.0.0.1 libre311-ui-dev
```

## Key Design Patterns

### Backend
- Controller → Service → Repository layering
- DTO validation via annotations
- Dependency injection via Micronaut
- Permission-based security with custom annotations
- Singleton pattern for storage services

### Frontend
- SvelteKit file-based routing
- Context providers for global state
- Component composition for complex forms
- Service classes for API communication
- Reactive stores for state management
