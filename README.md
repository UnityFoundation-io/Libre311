# Libre311

Libre311 is an open-source web application for managing municipal service requests, built on the [Open311 GeoReport v2](https://wiki.open311.org/GeoReport_v2/) specification.

## What is Open311?

[Open311](https://www.open311.org) is a standardized protocol for civic issue tracking, enabling residents to report non-emergency problems (potholes, broken streetlights, graffiti, etc.) to their local government. Libre311 provides a complete implementation of this standard, including:

- **Public Interface** - Citizens can submit service requests with location, photos, and details
- **Multiple Views** - Browse issues via interactive map, searchable list, or data table
- **Admin Dashboard** - Configure service types, manage jurisdictions, and track request status
- **REST API** - Open311-compliant endpoints for integration with other civic tech tools

## Tech Stack

| Component | Technology |
|-----------|------------|
| Backend API | Java 25 / Micronaut 4.x |
| Frontend | SvelteKit / TypeScript / Tailwind CSS |
| Database | MySQL or PostgreSQL |
| Authentication | OAuth via UnityAuth service |
| Maps | Leaflet with Nominatim geocoding |

## Prerequisites

### Required Software

| Software | Version | Notes |
|----------|---------|-------|
| Java JDK | 25 | [Eclipse Temurin](https://adoptium.net/) recommended. Use [SDKMAN](https://sdkman.io/) for easy installation. |
| Node.js | 18+ | Required for frontend development |
| npm | 9+ | Comes with Node.js |
| Docker | 20+ | Optional, for containerized development |
| Docker Compose | 2.0+ | Optional, for containerized development |

### Required Services

**UnityAuth** - Libre311 uses [UnityAuth](https://github.com/UnityFoundation-io/UnityAuth) for authentication and authorization. You must have UnityAuth running before starting Libre311.

1. Clone the UnityAuth repository
2. Follow its setup instructions to run locally or via Docker
3. Note the UnityAuth URL for configuring Libre311

### Optional Services

- **Google Cloud Platform** - For image storage (Cloud Storage) and content moderation (SafeSearch API)
- **MySQL or PostgreSQL** - Can use Docker-provided database for development

## Quick Start

### Local Development

1. **Configure environment variables**
   ```shell
   cp setenv.sh.example setenv.sh
   # Edit setenv.sh with your configuration (database, auth URLs, etc.)
   ```

2. **Start the API server** (from project root)
   ```shell
   source setenv.sh
   ./gradlew app:run
   ```
   API will be available at http://localhost:8080

3. **Start the frontend** (in a new terminal)
   ```shell
   source setenv.sh
   cd frontend
   npm install
   npm run dev
   ```
   UI will be available at http://localhost:3000

See [app/README.md](app/README.md) and [frontend/README.md](frontend/README.md) for detailed configuration options.

## Architecture

### System Overview

```
              +------------+     +---------+     +----------+
              | Web App UI |<--->| Web API |<-+->| Database |
              +------------+     +---------+  |  +----------+
                                              |  +---------------+
                                              +->| Auth Provider |
                                              |  +---------------+
                                              |  +------------------+
                                              +->| Geocoding Service|
                                              |  +------------------+
                                              |  +---------------+
                                              +->| Object Storage |
                                                 +---------------+
```

| Component | Description |
|-----------|-------------|
| **Web App UI** | SvelteKit application served independently or by the API |
| **Web API** | Micronaut REST API (horizontally scalable) |
| **Database** | MySQL or PostgreSQL with spatial extensions |
| **Auth Provider** | UnityAuth service for OAuth/JWT authentication |
| **Geocoding Service** | Nominatim (default) for reverse geocoding |
| **Object Storage** | GCP, AWS, or Azure for user-uploaded images |

### Code Structure

```
Libre311/
├── app/                          # Backend API (Java/Micronaut)
│   └── src/main/java/app/
│       ├── RootController.java       # Open311 GeoReport API endpoints
│       ├── JurisdictionAdminController.java
│       ├── ImageStorageController.java
│       ├── dto/                      # Request/response objects
│       ├── model/                    # JPA entities (Service, ServiceRequest, etc.)
│       ├── service/                  # Business logic layer
│       ├── security/                 # OAuth/JWT authentication
│       └── geocode/                  # Geocoding provider abstraction
│
├── frontend/                     # Frontend UI (SvelteKit)
│   └── src/
│       ├── routes/
│       │   ├── issues/               # Browse service requests (map, list, table)
│       │   ├── issue/create/         # Submit new service request
│       │   ├── groups/               # Admin: manage service types
│       │   ├── policies/             # Terms of use, privacy policy
│       │   └── login/                # Authentication
│       └── lib/
│           ├── components/           # Reusable UI components
│           └── context/              # Svelte context providers
│
├── docker-compose.local.yml      # Local Docker environment
└── setenv.sh.example             # Environment variable template
```

### Key Data Models

| Model | Description |
|-------|-------------|
| `Service` | Type of issue (e.g., "Pothole", "Streetlight Out") |
| `ServiceDefinition` | Attributes/subtypes for a service |
| `ServiceRequest` | A reported issue with location, description, status |
| `Jurisdiction` | Geographic/administrative boundary for services |
| `User` | Authorized admin user |

## Development

### Running Tests

**Backend (Java)**
```shell
# Run all tests
./gradlew app:test

# Run specific test class
./gradlew app:test --tests app.RootControllerTest

# Run with continuous rebuild
./gradlew app:test -t
```

**Frontend (SvelteKit)**
```shell
cd frontend

# Run all tests (unit + integration)
npm run test

# Run unit tests only
npm run test:unit

# Run integration tests (Playwright)
npm run test:integration
```

### Code Quality

**Frontend**
```shell
cd frontend
npm run lint      # Check for linting errors
npm run format    # Auto-format code with Prettier
npm run check     # TypeScript type checking
```

### Building for Production

**Backend**
```shell
./gradlew app:assemble
# Output: app/build/libs/app-*-all.jar
```

**Frontend**
```shell
cd frontend
npm run build     # Production build
npm run preview   # Preview production build locally
```

## Docker Environment

### Setup

1. **Configure environment files**
   ```shell
   cp .env.example .env.docker
   cp frontend/.env.example frontend/.env.docker
   # Edit both files with your configuration
   ```

2. **Configure GCP credentials** (if using image uploads)
   ```shell
   # Linux/macOS
   export ADC_PATH=$HOME/.config/gcloud/application_default_credentials.json

   # Windows
   set ADC_PATH=%APPDATA%\gcloud\application_default_credentials.json
   ```
   See [app/README.md](app/README.md#object-storage-and-safesearch) for details on Application Default Credentials.

3. **Add hosts entries** (recommended)
   ```
   # Add to /etc/hosts
   127.0.0.1 libre311-api
   127.0.0.1 libre311-db
   127.0.0.1 libre311-ui
   127.0.0.1 libre311-ui-dev
   ```

### Running Containers

```shell
# Start all services
docker compose -f docker-compose.local.yml up

# Or use Makefile shortcuts:
make compose_api    # API + database only
make compose_ui     # UI + database only
make compose_all    # All services
```

**Service URLs:**

| Service | Host URL | Docker Internal |
|---------|----------|-----------------|
| API | http://localhost:8080 | http://libre311-api:8080 |
| UI | http://localhost:3000 | http://libre311-ui-dev:3000 |
| MySQL | localhost:23306 | libre311-db:3306 |

### Rebuilding Images

After code changes, rebuild the affected images:
```shell
docker compose -f docker-compose.local.yml build libre311-api
docker compose -f docker-compose.local.yml build libre311-ui-dev
docker compose -f docker-compose.local.yml up
```

## Configuration

### Service Discovery

As outlined in Open311's Service Discovery [page](https://wiki.open311.org/Service_Discovery), an endpoint is offered
at `/discovery` which describes organization contact and the base URLs of endpoints.
As a convenience, Libre311 provides a default set of endpoint configurations for a set
of `production` and `test` environments as well as the ability to set
configuration values via the following environment variables:

- `LIBRE311_DISCOVERY_CHANGESET_DATETIME`
- `LIBRE311_DISCOVERY_CONTACT_MESSAGE`
- `LIBRE311_DISCOVERY_PRODUCTION_URL`
- `LIBRE311_DISCOVERY_TEST_URL`

Please feel free to modify `app/src/main/resources/application.yml`'s `app.discovery`
content to your use case.

### Configuring the Web API

The following environment variables should be set to configure the application:

**Database:**
- `LIBRE311_JDBC_URL` - JDBC connection URL
- `LIBRE311_JDBC_DRIVER` - JDBC driver class
- `LIBRE311_JDBC_USER` - Database username
- `LIBRE311_JDBC_PASSWORD` - Database password
- `LIBRE311_AUTO_SCHEMA_GEN` - Schema generation mode (`update` for development)

**Object Storage:**
- `GCP_PROJECT_ID` - The GCP project ID
- `STORAGE_BUCKET_ID` - The ID of the bucket where user-uploaded images are hosted

**Security:**
- `RECAPTCHA_SECRET` - Site abuse prevention
- `MICRONAUT_SECURITY_TOKEN_JWT_SIGNATURES_SECRET_GENERATOR_SECRET` - Secret used to sign JWTs
- `MICRONAUT_SECURITY_TOKEN_JWT_GENERATOR_REFRESH_TOKEN_SECRET` - Secret for JWT renewal tokens
- `MICRONAUT_SECURITY_REDIRECT_LOGIN_SUCCESS`
- `MICRONAUT_SECURITY_REDIRECT_LOGIN_FAILURE`
- `MICRONAUT_SECURITY_REDIRECT_LOGOUT`

**Authentication (UnityAuth):**
- `AUTH_BASE_URL` - UnityAuth service base URL
- `AUTH_JWKS` - UnityAuth JWKS endpoint for JWT validation

**Geocoding (optional, has sensible defaults):**
- `NOMINATIM_URL` - Geocoding service URL (default: `https://nominatim.openstreetmap.org`)
- `GEOCODING_PROVIDER` - Provider selection (default: `nominatim`)

### Configuring the Web Application UI

The Web Application UI requires the URL of the API when built.
This is set using the `VITE_BACKEND_URL` environment variable.
If the Web API will serve the UI, then set `VITE_BACKEND_URL` to `/api`.

### Configuring Google as an Auth Provider

Set the following environment variables to enable Google as an auth provider:

- `GOOGLE_CLIENT_ID` - The id of OAuth client.
- `GOOGLE_CLIENT_SECRET` - The secret of the OAuth client.
- `MICRONAUT_SECURITY_OAUTH2_CLIENTS_GOOGLE_OPENID_ISSUER` - Set to "https://accounts.google.com"

See `app/src/main/resources/application.yml` for a complete list of configuration options.

## License

Copyright 2023-2025 Libre311 Authors. Licensed under the [Apache License 2.0](LICENSE).
