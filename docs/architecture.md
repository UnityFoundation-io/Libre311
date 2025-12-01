# Libre311 Architecture Guide

This document provides a comprehensive overview of the Libre311 architecture for developers and contributors. It covers the system design, component relationships, data flow, and key design patterns used throughout the application.

## Table of Contents

- [Overview](#overview)
- [System Architecture](#system-architecture)
- [Backend Architecture](#backend-architecture)
  - [Technology Stack](#backend-technology-stack)
  - [Project Structure](#backend-project-structure)
  - [Layered Architecture](#layered-architecture)
  - [Key Components](#key-backend-components)
- [Frontend Architecture](#frontend-architecture)
  - [Technology Stack](#frontend-technology-stack)
  - [Project Structure](#frontend-project-structure)
  - [Key Components](#key-frontend-components)
  - [State Management](#state-management)
- [Data Model](#data-model)
  - [Entity Relationships](#entity-relationships)
  - [Core Entities](#core-entities)
- [Multi-Tenancy](#multi-tenancy)
- [Authentication & Authorization](#authentication--authorization)
  - [Authentication Flow](#authentication-flow)
  - [Permission Model](#permission-model)
- [External Integrations](#external-integrations)
- [API Design](#api-design)
- [Request Flow](#request-flow)
- [Configuration](#configuration)
- [Deployment Architecture](#deployment-architecture)
- [Design Patterns](#design-patterns)
- [Key Files Reference](#key-files-reference)

---

## Overview

Libre311 is an open-source web application implementing the [Open311 GeoReport v2](https://wiki.open311.org/GeoReport_v2/) standard for civic service request management. It enables municipalities to receive, track, and manage non-emergency service requests from citizens.

### High-Level Components

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Client Layer                                    │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                    SvelteKit Frontend (UI)                           │    │
│  │    - Public views (map, list, create request)                        │    │
│  │    - Admin views (table, service configuration)                      │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ HTTP/REST
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              API Layer                                       │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                 Micronaut Backend (Java 17)                          │    │
│  │    - Open311 GeoReport v2 API                                        │    │
│  │    - Admin APIs                                                      │    │
│  │    - Image upload handling                                           │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
                    │                   │                   │
                    ▼                   ▼                   ▼
┌──────────────────────┐  ┌──────────────────┐  ┌──────────────────────────┐
│   MySQL/PostgreSQL   │  │    UnityAuth     │  │   Google Cloud Platform  │
│   (Persistent Data)  │  │  (Authentication)│  │   - Cloud Storage        │
│                      │  │                  │  │   - Vision API           │
└──────────────────────┘  └──────────────────┘  └──────────────────────────┘
```

---

## System Architecture

### Key Architectural Decisions

1. **Separation of Concerns**: Clean separation between frontend (SvelteKit) and backend (Micronaut) allows independent scaling and deployment.

2. **External Authentication**: UnityAuth handles user management and authentication, keeping Libre311 focused on service request functionality.

3. **Multi-Tenant Design**: Built from the ground up to support multiple jurisdictions within a single deployment.

4. **Open311 Compliance**: API design follows the Open311 GeoReport v2 specification for interoperability.

5. **Cloud-Native Storage**: Image storage delegated to cloud providers (GCS, AWS S3, Azure Blob) rather than local filesystem.

---

## Backend Architecture

### Backend Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Framework | Micronaut | 3.7.10 |
| Language | Java | 17 |
| ORM | Hibernate with JPA | 5.6.x |
| Spatial Data | Hibernate Spatial | 5.6.15 |
| Database | MySQL / PostgreSQL | 8.0+ / 8.2+ |
| Migrations | Flyway | - |
| HTTP Server | Netty | - |
| Security | Micronaut Security JWT | - |
| API Docs | OpenAPI/Swagger | - |
| Build Tool | Gradle | - |

### Backend Project Structure

```
app/src/main/java/app/
├── Application.java                    # Entry point, OpenAPI configuration
├── RootController.java                 # Open311 GeoReport API endpoints
├── ImageStorageController.java         # Image upload endpoint
├── JurisdictionAdminController.java    # Jurisdiction management
├── PermissionsController.java          # Permission queries
├── TenantAdminController.java          # Multi-tenancy management
│
├── controller/                         # Additional admin controllers
│   ├── ServiceGroupController.java
│   └── ...
│
├── dto/                                # Data Transfer Objects
│   ├── servicerequest/                 # Service request DTOs
│   │   ├── PostServiceRequestDTO.java
│   │   ├── GetServiceRequestDTO.java
│   │   ├── PatchServiceRequestDTO.java
│   │   └── ...
│   ├── service/                        # Service DTOs
│   ├── servicedefinition/              # Service definition DTOs
│   ├── jurisdiction/                   # Jurisdiction DTOs
│   ├── group/                          # Service group DTOs
│   └── download/                       # CSV export DTOs
│
├── model/                              # JPA Entities & Repositories
│   ├── servicerequest/
│   │   ├── ServiceRequest.java         # Core entity
│   │   ├── ServiceRequestStatus.java   # Status enum
│   │   ├── ServiceRequestPriority.java # Priority enum
│   │   └── ServiceRequestRepository.java
│   ├── service/
│   │   ├── Service.java
│   │   ├── ServiceGroup.java
│   │   └── ServiceRepository.java
│   ├── servicedefinition/
│   │   ├── ServiceDefinitionAttribute.java
│   │   └── AttributeValue.java
│   ├── jurisdiction/
│   │   ├── Jurisdiction.java
│   │   ├── JurisdictionBoundary.java
│   │   └── RemoteHost.java
│   └── user/
│       ├── User.java
│       └── JurisdictionUser.java
│
├── service/                            # Business Logic Layer
│   ├── servicerequest/
│   │   └── ServiceRequestService.java  # Core business logic
│   ├── service/
│   │   └── ServiceService.java
│   ├── jurisdiction/
│   │   ├── JurisdictionService.java
│   │   └── JurisdictionBoundaryService.java
│   ├── discovery/
│   │   └── DiscoveryEndpointService.java
│   ├── geometry/
│   │   └── LibreGeometryFactory.java
│   └── storage/
│       ├── StorageService.java
│       ├── CloudStorageUploader.java
│       └── GoogleCloudStorageUploader.java
│
├── security/                           # Authentication & Authorization
│   ├── UnityAuthService.java           # UnityAuth integration
│   ├── UnityAuthClient.java
│   ├── RequiresPermissions.java        # Authorization annotation
│   ├── RequiresPermissionsAnnotationRule.java
│   └── Permission.java                 # Permission enum
│
├── imagedetection/                     # Content Moderation
│   └── GoogleImageSafeSearchService.java
│
├── recaptcha/                          # Bot Prevention
│   ├── ReCaptchaService.java
│   └── ReCaptchaClient.java
│
└── exception/                          # Error Handling
    ├── Libre311BaseException.java
    └── Libre311ExceptionHandler.java
```

### Layered Architecture

The backend follows a traditional layered architecture:

```
┌─────────────────────────────────────────────────────────┐
│                   Controller Layer                       │
│  - HTTP endpoint handling                                │
│  - Request/Response serialization (JSON/XML)            │
│  - Input validation                                      │
│  - Security annotations                                  │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                    Service Layer                         │
│  - Business logic implementation                         │
│  - DTO ↔ Entity conversion                              │
│  - Transaction management                                │
│  - Cross-cutting concerns (validation, moderation)      │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                  Repository Layer                        │
│  - Data access via Micronaut Data                       │
│  - Custom queries with @Query annotation                │
│  - Pagination support                                    │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                   Database Layer                         │
│  - MySQL 8.0+ or PostgreSQL 8.2+                        │
│  - Spatial data support (boundaries, coordinates)       │
│  - Flyway migrations                                     │
└─────────────────────────────────────────────────────────┘
```

### Key Backend Components

#### Controllers

| Controller | Path | Responsibility |
|------------|------|----------------|
| `RootController` | `/api/*` | Open311 GeoReport v2 API (services, requests, definitions) |
| `ImageStorageController` | `/api/images/*` | Image upload handling |
| `JurisdictionAdminController` | `/api/jurisdiction-admin/*` | Jurisdiction CRUD for admins |
| `TenantAdminController` | `/api/tenant-admin/*` | Tenant-level administration |
| `ServiceGroupController` | `/api/groups/*` | Service group management |

#### Services

| Service | Responsibility |
|---------|----------------|
| `ServiceRequestService` | CRUD operations, search, filtering, CSV export, boundary validation |
| `ServiceService` | Service and definition management, attribute configuration |
| `JurisdictionService` | Jurisdiction CRUD, hostname-based lookup |
| `JurisdictionBoundaryService` | Spatial queries for boundary validation |
| `StorageService` | Image upload orchestration, content moderation |
| `UnityAuthService` | Authentication integration, permission validation |

#### Key Design Patterns in Backend

1. **Repository Pattern**: Repositories extend Micronaut Data interfaces for type-safe database access.

2. **DTO Pattern**: All API inputs/outputs use DTOs, never exposing JPA entities directly.

3. **Singleton Services**: Business logic services are `@Singleton` scoped for dependency injection.

4. **Annotation-based Security**: `@RequiresPermissions` annotation declaratively protects endpoints.

---

## Frontend Architecture

### Frontend Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Framework | SvelteKit | 2.4.3 |
| UI Library | Svelte | 4.2.19 |
| Language | TypeScript | 5.0 |
| HTTP Client | Axios | 1.8.2 |
| Validation | Zod | 3.22.4 |
| Mapping | Leaflet | 1.9.4 |
| Styling | TailwindCSS + stwui | 3.3.6 |
| Testing | Playwright + Vitest | - |
| Build Tool | Vite | 5.4.20 |

### Frontend Project Structure

```
frontend/src/
├── routes/                             # File-based routing (SvelteKit)
│   ├── +layout.svelte                  # Root layout (header, nav, context)
│   ├── +page.svelte                    # Homepage
│   ├── issues/
│   │   ├── +layout.svelte              # Issues layout wrapper
│   │   ├── +page.svelte                # Issues index
│   │   ├── list/+page.svelte           # List view
│   │   ├── map/+page.svelte            # Map view (Leaflet)
│   │   └── table/+page.svelte          # Table view (admin)
│   ├── issue/
│   │   └── create/                     # Multi-step creation wizard
│   │       ├── +layout.svelte
│   │       └── [step]/+page.svelte
│   ├── groups/                         # Service group management
│   │   ├── +page.svelte
│   │   └── [groupId]/+page.svelte
│   └── login/+page.svelte              # Authentication UI
│
├── lib/
│   ├── context/                        # Svelte Context Providers
│   │   ├── Libre311Context.ts          # Type definitions
│   │   ├── Libre311ContextProvider.svelte
│   │   └── Libre311AlertStore.ts       # Alert/notification store
│   │
│   ├── services/                       # API & External Services
│   │   ├── Libre311/
│   │   │   ├── Libre311.ts             # Main API client
│   │   │   └── types/                  # TypeScript type definitions
│   │   │       ├── Service.ts
│   │   │       ├── ServiceRequest.ts
│   │   │       └── ServiceDefinition.ts
│   │   ├── UnityAuth/
│   │   │   └── UnityAuth.ts            # Auth service client
│   │   ├── LinkResolver.ts             # URL helpers
│   │   └── RecaptchaService.ts
│   │
│   ├── components/                     # Reusable UI Components
│   │   ├── CreateServiceRequest/       # Multi-step form components
│   │   │   ├── CreateServiceRequestLayout.svelte
│   │   │   ├── SelectARequestCategory.svelte
│   │   │   ├── ContactInformation.svelte
│   │   │   ├── ServiceRequestDetailsForm.svelte
│   │   │   └── ServiceDefinitionAttributes/
│   │   │       ├── StringServiceDefinitionAttribute.svelte
│   │   │       ├── NumberServiceDefinitionAttribute.svelte
│   │   │       ├── DateTimeServiceDefinitionAttribute.svelte
│   │   │       ├── SingleValueListServiceDefinitionAttribute.svelte
│   │   │       └── MultiSelectServiceDefinitionAttribute.svelte
│   │   ├── MapComponent.svelte         # Leaflet map wrapper
│   │   ├── ServiceRequest.svelte       # Request display card
│   │   ├── MenuDrawer.svelte           # Navigation drawer
│   │   └── User.svelte                 # User profile/logout
│   │
│   └── utils/                          # Utility Functions
│       ├── types.ts                    # Shared types
│       └── validation.ts               # Form validation helpers
│
├── app.html                            # HTML template
└── app.pcss                            # Global styles (Tailwind)
```

### Key Frontend Components

#### Route Structure

| Route | Description | Access |
|-------|-------------|--------|
| `/` | Homepage/redirect | Public |
| `/issues/map` | Map view of requests | Public |
| `/issues/list` | List view of requests | Public |
| `/issues/table` | Admin table view | Authenticated |
| `/issue/create/*` | Multi-step creation wizard | Public |
| `/groups/*` | Service configuration | Authenticated |
| `/login` | Authentication | Public |

#### Component Hierarchy

```
+layout.svelte (Root)
├── Libre311ContextProvider
│   ├── Header/Navigation
│   └── <slot/> (Page content)
│
├── /issues/map
│   └── MapComponent
│       └── Leaflet Map + Markers
│
├── /issue/create
│   └── CreateServiceRequestLayout
│       ├── Step 1: SelectARequestCategory
│       ├── Step 2: Location Selection (MapComponent)
│       ├── Step 3: Image Upload
│       ├── Step 4: ServiceRequestDetailsForm
│       │   └── ServiceDefinitionAttributes/*
│       └── Step 5: ContactInformation
│
└── /groups
    └── ServiceDefinitionEditor
        └── AttributeEditor
```

### State Management

The frontend uses multiple state management patterns:

#### 1. Svelte Context API (Global State)

```typescript
// Libre311Context.ts - Type definitions
export type Libre311Context = {
  libre311: Libre311Service;
  unityAuth: UnityAuthService;
  user: Readable<UnityAuthUser | undefined>;
  alertError: (error: unknown) => void;
  alertSuccess: (message: string) => void;
};

// Usage in components
const ctx = useLibre311Context();
const requests = await ctx.libre311.getServiceRequests();
```

#### 2. Svelte Stores (Reactive State)

```typescript
// Alert store for notifications
export const alertStore = writable<Alert[]>([]);

// User state as readable store
export const userStore: Readable<User | undefined>;
```

#### 3. Async Result Pattern

```typescript
// Consistent async state handling
type AsyncResult<T> =
  | { type: 'success'; value: T }
  | { type: 'inProgress' }
  | { type: 'failure'; error: unknown };
```

---

## Data Model

### Entity Relationships

```
┌─────────────────┐
│   Jurisdiction  │
└────────┬────────┘
         │ 1:N
         ▼
┌─────────────────┐     ┌─────────────────┐
│  RemoteHost     │     │JurisdictionUser │
│ (hostname map)  │     │ (user access)   │
└─────────────────┘     └────────┬────────┘
                                 │ N:1
         ┌───────────────────────┘
         │
         ▼
┌─────────────────┐
│      User       │
└─────────────────┘

┌─────────────────┐
│   Jurisdiction  │
└────────┬────────┘
         │ 1:N
         ├──────────────────────────┬───────────────────────────┐
         ▼                          ▼                           ▼
┌─────────────────┐     ┌─────────────────────┐     ┌───────────────────┐
│  ServiceGroup   │     │ JurisdictionBoundary│     │  ServiceRequest   │
└────────┬────────┘     │   (GIS polygons)    │     └───────────────────┘
         │ 1:N          └─────────────────────┘               ▲
         ▼                                                    │
┌─────────────────┐                                           │
│    Service      │───────────────────────────────────────────┘
└────────┬────────┘              N:1
         │ 1:N
         ▼
┌─────────────────────────┐
│ServiceDefinitionAttribute│
└────────┬────────────────┘
         │ 1:N
         ▼
┌─────────────────┐
│ AttributeValue  │
└─────────────────┘
```

### Core Entities

#### ServiceRequest

The central entity representing a citizen's service request.

```java
@Entity
public class ServiceRequest {
    @Id Long id;
    String description;
    String address;
    Point location;              // Spatial coordinate
    String mediaUrl;             // Image URL
    ServiceRequestStatus status; // OPEN, ASSIGNED, IN_PROGRESS, CLOSED
    ServiceRequestPriority priority;

    @ManyToOne Service service;
    @ManyToOne Jurisdiction jurisdiction;

    // Contact info (optional)
    String firstName, lastName, email, phone;

    // Admin fields
    String agencyName, agencyEmail, statusNotes;
    LocalDateTime expectedDate;

    // Timestamps
    LocalDateTime requestedDatetime, updatedDatetime;
}
```

#### Service

Defines a type of service request that can be created.

```java
@Entity
public class Service {
    @Id Long id;
    String serviceName;
    String description;
    ServiceType type;           // REALTIME, BATCH, BLACKBOX
    Boolean isActive;
    Integer orderPosition;

    @ManyToOne ServiceGroup serviceGroup;
    @ManyToOne Jurisdiction jurisdiction;

    @OneToMany List<ServiceDefinitionAttribute> attributes;
}
```

#### ServiceDefinitionAttribute

Custom fields for a service type.

```java
@Entity
public class ServiceDefinitionAttribute {
    @Id Long id;
    String attributeCode;
    String description;
    AttributeDataType datatype;  // STRING, NUMBER, DATETIME, SINGLEVALUELIST, MULTIVALUELIST
    Boolean required;
    Integer attributeOrder;

    @ManyToOne Service service;
    @OneToMany List<AttributeValue> values;  // For dropdown types
}
```

#### Jurisdiction

Represents a municipality or administrative area.

```java
@Entity
public class Jurisdiction {
    @Id Long id;
    String name;
    String tenantId;            // External tenant reference

    @OneToMany List<Service> services;
    @OneToMany List<JurisdictionBoundary> bounds;
    @OneToMany List<RemoteHost> remoteHosts;
}
```

---

## Multi-Tenancy

Libre311 supports a three-level multi-tenancy model:

```
┌─────────────────────────────────────────────────────────────┐
│                     SYSTEM Level                             │
│  - Global administrators                                     │
│  - Can manage all tenants and jurisdictions                  │
│  - Permissions: LIBRE311_*_SYSTEM                           │
└─────────────────────────────────────────────────────────────┘
                          │
          ┌───────────────┴───────────────┐
          ▼                               ▼
┌─────────────────────┐       ┌─────────────────────┐
│   TENANT Level      │       │   TENANT Level      │
│   (Organization)    │       │   (Organization)    │
│   - tenantId: "A"   │       │   - tenantId: "B"   │
│   - Permissions:    │       │   - Permissions:    │
│     LIBRE311_*_TENANT       │     LIBRE311_*_TENANT
└─────────┬───────────┘       └─────────────────────┘
          │
    ┌─────┴─────┐
    ▼           ▼
┌────────┐  ┌────────┐
│ SUBTENANT │ SUBTENANT │
│(Jurisdiction)│(Jurisdiction)│
│ City A    │ City B    │
│ Permissions:│ Permissions:│
│ LIBRE311_*  │ LIBRE311_*  │
│ _SUBTENANT  │ _SUBTENANT  │
└────────┘  └────────┘
```

### How Multi-Tenancy Works

1. **Hostname Detection**: `RemoteHost` maps request hostnames to jurisdictions
2. **Jurisdiction Context**: `jurisdiction_id` query parameter identifies the context
3. **User Assignment**: `JurisdictionUser` junction table links users to jurisdictions
4. **Permission Scoping**: Permissions are scoped to system, tenant, or subtenant level

---

## Authentication & Authorization

### Authentication Flow

```
┌──────────┐     ┌──────────────┐     ┌───────────┐     ┌──────────────┐
│  Browser │────▶│   Frontend   │────▶│ UnityAuth │────▶│   Backend    │
└──────────┘     └──────────────┘     └───────────┘     └──────────────┘
     │                  │                   │                   │
     │  1. Click Login  │                   │                   │
     │─────────────────▶│                   │                   │
     │                  │  2. Redirect      │                   │
     │                  │─────────────────▶│                   │
     │                  │                   │                   │
     │  3. Login Form   │◀──────────────────│                   │
     │◀─────────────────│                   │                   │
     │                  │                   │                   │
     │  4. Credentials  │                   │                   │
     │─────────────────▶│─────────────────▶│                   │
     │                  │                   │                   │
     │                  │  5. JWT Token     │                   │
     │                  │◀──────────────────│                   │
     │                  │                   │                   │
     │  6. Store in     │                   │                   │
     │     Cookie       │                   │                   │
     │◀─────────────────│                   │                   │
     │                  │                   │                   │
     │  7. API Request  │                   │                   │
     │     + JWT        │                   │                   │
     │─────────────────▶│──────────────────────────────────────▶│
     │                  │                   │                   │
     │                  │                   │  8. Validate JWT  │
     │                  │                   │◀──────────────────│
     │                  │                   │                   │
     │                  │                   │  9. Check         │
     │                  │                   │     Permissions   │
     │                  │                   │──────────────────▶│
```

### Permission Model

Libre311 uses 12 distinct permissions organized by scope and action:

| Permission | Description |
|------------|-------------|
| `LIBRE311_ADMIN_EDIT_SYSTEM` | System-wide admin edit access |
| `LIBRE311_ADMIN_VIEW_SYSTEM` | System-wide admin view access |
| `LIBRE311_ADMIN_EDIT_TENANT` | Tenant-level admin edit access |
| `LIBRE311_ADMIN_VIEW_TENANT` | Tenant-level admin view access |
| `LIBRE311_ADMIN_EDIT_SUBTENANT` | Jurisdiction-level admin edit |
| `LIBRE311_ADMIN_VIEW_SUBTENANT` | Jurisdiction-level admin view |
| `LIBRE311_REQUEST_EDIT_SYSTEM` | System-wide request management |
| `LIBRE311_REQUEST_VIEW_SYSTEM` | System-wide request viewing |
| `LIBRE311_REQUEST_EDIT_TENANT` | Tenant-level request management |
| `LIBRE311_REQUEST_VIEW_TENANT` | Tenant-level request viewing |
| `LIBRE311_REQUEST_EDIT_SUBTENANT` | Jurisdiction request management |
| `LIBRE311_REQUEST_VIEW_SUBTENANT` | Jurisdiction request viewing |

### Authorization Implementation

```java
// Controller method protection
@RequiresPermissions(
    tenant = Permission.LIBRE311_REQUEST_VIEW_TENANT,
    subtenant = Permission.LIBRE311_REQUEST_VIEW_SUBTENANT
)
@Get("/admin/requests")
public Page<ServiceRequestDTO> getAdminRequests(...) { ... }
```

The `RequiresPermissionsAnnotationRule` intercepts requests and:
1. Extracts JWT from Authorization header
2. Calls UnityAuth to validate permissions
3. Verifies user exists in local database
4. Checks jurisdiction assignment for subtenant actions

---

## External Integrations

### UnityAuth

| Purpose | Authentication & Authorization |
|---------|-------------------------------|
| URL | Configured via `AUTH_BASE_URL` |
| Protocol | OAuth 2.0 with JWT |
| Key Files | `UnityAuthService.java`, `UnityAuthClient.java` |

### Google Cloud Storage

| Purpose | Image upload storage |
|---------|---------------------|
| Service | Cloud Storage |
| Auth | Application Default Credentials |
| Key Files | `GoogleCloudStorageUploader.java`, `StorageService.java` |

### Google Vision API (SafeSearch)

| Purpose | Content moderation for uploaded images |
|---------|---------------------------------------|
| Service | Cloud Vision |
| Function | Detects explicit/inappropriate content |
| Key Files | `GoogleImageSafeSearchService.java` |

### ReCaptcha v3

| Purpose | Bot prevention on public forms |
|---------|-------------------------------|
| Integration | Server-side token validation |
| Key Files | `ReCaptchaService.java`, `ReCaptchaClient.java` |

---

## API Design

### Open311 GeoReport v2 Endpoints

The primary API follows the Open311 standard:

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/services` | List available service types |
| GET | `/api/services/{code}` | Get service definition |
| GET | `/api/requests` | List service requests (paginated) |
| GET | `/api/requests/{id}` | Get single request |
| POST | `/api/requests` | Create new request |
| PATCH | `/api/requests/{id}` | Update request (admin) |

### Response Formats

Both JSON and XML are supported via content negotiation:
- `Accept: application/json`
- `Accept: application/xml`

### Pagination

List endpoints support pagination via query parameters:
- `page`: Page number (0-indexed)
- `page_size`: Items per page

Response includes pagination headers:
- `X-Total-Count`: Total items
- `X-Total-Pages`: Total pages

---

## Request Flow

### Creating a Service Request

```
┌──────────┐     ┌──────────────┐     ┌──────────────────────────┐
│ Frontend │────▶│   Backend    │────▶│       Services           │
└──────────┘     └──────────────┘     └──────────────────────────┘
     │                  │                          │
     │  POST /requests  │                          │
     │  + form data     │                          │
     │─────────────────▶│                          │
     │                  │                          │
     │                  │  1. Validate ReCaptcha   │
     │                  │─────────────────────────▶│ ReCaptchaService
     │                  │                          │
     │                  │  2. Validate DTO         │
     │                  │  (annotations)           │
     │                  │                          │
     │                  │  3. Check jurisdiction   │
     │                  │     boundaries           │
     │                  │─────────────────────────▶│ JurisdictionBoundaryService
     │                  │                          │
     │                  │  4. If image provided:   │
     │                  │     a. SafeSearch check  │
     │                  │─────────────────────────▶│ GoogleImageSafeSearchService
     │                  │     b. Upload to GCS     │
     │                  │─────────────────────────▶│ StorageService
     │                  │                          │
     │                  │  5. Save to database     │
     │                  │─────────────────────────▶│ ServiceRequestRepository
     │                  │                          │
     │  201 Created     │                          │
     │  + request ID    │                          │
     │◀─────────────────│                          │
```

---

## Configuration

### Backend Configuration Files

```
app/src/main/resources/
├── application.yml          # Base configuration
├── application-local.yml    # Local development
├── application-docker.yml   # Docker environment
└── application-prod.yml     # Production
```

### Key Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `LIBRE311_JDBC_URL` | Database JDBC URL | Yes |
| `LIBRE311_JDBC_USER` | Database username | Yes |
| `LIBRE311_JDBC_PASSWORD` | Database password | Yes |
| `AUTH_BASE_URL` | UnityAuth service URL | Yes |
| `AUTH_JWKS` | UnityAuth JWKS endpoint | Yes |
| `GCP_PROJECT_ID` | Google Cloud project | For GCS |
| `STORAGE_BUCKET_ID` | GCS bucket name | For GCS |
| `RECAPTCHA_SECRET` | ReCaptcha secret key | For captcha |

### Frontend Environment Variables

| Variable | Description |
|----------|-------------|
| `VITE_BACKEND_URL` | API URL (`/api` or full URL) |
| `VITE_GOOGLE_RECAPTCHA_KEY` | ReCaptcha site key |

---

## Deployment Architecture

### Docker Compose (Local/Development)

```yaml
services:
  libre311-api:
    build: ./app
    ports: ["8080:8080"]
    environment:
      - MICRONAUT_ENVIRONMENTS=docker
    depends_on: [libre311-db]

  libre311-ui:
    build: ./frontend
    ports: ["5173:5173"]

  libre311-db:
    image: mysql:8.0
    volumes: [db-data:/var/lib/mysql]

networks:
  unity-network:
    external: true  # Shared with UnityAuth
```

### Production Considerations

1. **Database**: Use managed MySQL/PostgreSQL service
2. **Storage**: Configure appropriate cloud storage (GCS, S3, Azure)
3. **Auth**: Deploy UnityAuth separately or use managed OAuth provider
4. **SSL**: Terminate SSL at load balancer
5. **Scaling**: Backend is stateless, scale horizontally as needed

---

## Design Patterns

### Backend Patterns

| Pattern | Usage |
|---------|-------|
| **Repository** | `*Repository` interfaces for data access |
| **Service Layer** | `*Service` classes for business logic |
| **DTO** | All API I/O uses DTOs, not entities |
| **Dependency Injection** | Micronaut's compile-time DI |
| **Annotation-based Security** | `@RequiresPermissions` for authorization |
| **Strategy** | `CloudStorageUploader` for storage backends |

### Frontend Patterns

| Pattern | Usage |
|---------|-------|
| **Context Provider** | `Libre311ContextProvider` for global state |
| **Factory** | `libre311Factory()` for service instantiation |
| **Async Result** | Consistent async state handling |
| **Component Composition** | Multi-step form wizard |
| **Reactive Stores** | Svelte stores for state changes |

---

## Key Files Reference

### Backend - Must-Know Files

| File | Purpose |
|------|---------|
| `app/src/main/java/app/RootController.java` | Main Open311 API endpoints |
| `app/src/main/java/app/service/servicerequest/ServiceRequestService.java` | Core business logic |
| `app/src/main/java/app/security/UnityAuthService.java` | Authentication integration |
| `app/src/main/java/app/security/Permission.java` | Permission definitions |
| `app/src/main/java/app/model/servicerequest/ServiceRequest.java` | Core entity |
| `app/src/main/resources/application.yml` | Base configuration |

### Frontend - Must-Know Files

| File | Purpose |
|------|---------|
| `frontend/src/routes/+layout.svelte` | Root layout, context setup |
| `frontend/src/lib/context/Libre311Context.ts` | Global context types |
| `frontend/src/lib/services/Libre311/Libre311.ts` | API client |
| `frontend/src/lib/services/UnityAuth/UnityAuth.ts` | Auth client |
| `frontend/src/routes/issue/create/` | Service request creation flow |

### Configuration Files

| File | Purpose |
|------|---------|
| `docker-compose.local.yml` | Local Docker setup |
| `app/build.gradle` | Backend dependencies |
| `frontend/package.json` | Frontend dependencies |
| `setenv.sh.example` | Environment variable template |

---

## Further Reading

- [Open311 GeoReport v2 Specification](https://wiki.open311.org/GeoReport_v2/)
- [Micronaut Documentation](https://docs.micronaut.io/)
- [SvelteKit Documentation](https://kit.svelte.dev/docs)
- [Libre311 User Guide](./users-guide.md)
- [CLAUDE.md](../CLAUDE.md) - Development commands and setup
