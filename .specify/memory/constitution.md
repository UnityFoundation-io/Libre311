# Libre311 Constitution

<!--
SYNC IMPACT REPORT
==================
Version change: [NEW FILE] → 1.0.0
Rationale: Initial constitution creation capturing existing project principles from CLAUDE.md and adding WCAG 2.2 AA compliance requirement.

Modified principles: N/A (initial creation)
Added sections:
  - Core Principles (7 principles total)
  - Compliance Standards
  - Development Workflow
  - Governance

Removed sections: N/A (initial creation)

Templates requiring updates:
  ✅ plan-template.md - Constitution Check section already present, aligns with principles
  ✅ spec-template.md - Requirements section supports accessibility and quality principles
  ✅ tasks-template.md - Task structure supports testing and quality requirements
  ⚠ No command files found in .specify/templates/commands/

Follow-up TODOs: None
-->

## Core Principles

### I. Layered Architecture (Backend)

The backend MUST follow a strict layered architecture pattern:
- **API Layer**: Controllers handle HTTP endpoints only, no business logic
- **Service Layer**: Business logic lives here, injected into controllers
- **Data Layer**: JPA entities and Micronaut Data repositories
- **DTO Layer**: All HTTP request/response bodies use validated DTOs
- **Security Layer**: Permission-based authorization with declarative annotations

**Rationale**: Clear separation of concerns enables independent testing, maintainability, and adherence to Open311 GeoReport API v2 standard. This pattern is foundational to the existing codebase and MUST be preserved.

### II. Component-Based Architecture (Frontend)

The frontend MUST follow SvelteKit conventions:
- **File-based routing**: Routes defined by directory structure
- **Context providers**: Global state via Svelte context
- **Service classes**: API communication abstracted from components
- **Reactive stores**: State management using Svelte stores
- **Component composition**: Complex forms built from reusable components

**Rationale**: Enables modular, testable UI development and aligns with SvelteKit best practices. The existing codebase demonstrates this pattern throughout `frontend/src/`.

### III. Test Coverage (Non-Negotiable)

All new features and bug fixes MUST include appropriate test coverage:
- **Backend**: Unit tests required for services, integration tests for controllers
- **Frontend**: Unit tests with Vitest for logic, integration tests with Playwright for user journeys
- **Run commands**: Tests MUST pass before merge:
  - Backend: `./gradlew app:test`
  - Frontend: `cd frontend && npm run test`
- **Single test execution**: Support isolated test runs for debugging

**Rationale**: The project already has established test infrastructure (Vitest, Playwright, Gradle test). This principle ensures quality gates are enforced and regressions are prevented.

### IV. Security-First Development

Security MUST be a primary consideration for all code changes:
- **OWASP Top 10**: All features MUST avoid common vulnerabilities (SQL injection, XSS, CSRF, etc.)
- **Authentication**: OAuth via UnityAuth service, JWT-based with secure cookies
- **Authorization**: Permission-based with `@RequiresPermissions` annotations
- **Input validation**: DTO-level validation with annotations, never trust client input
- **Secret management**: Never commit `.env` files or credentials
- **Content moderation**: User uploads MUST use SafeSearch API
- **Bot prevention**: ReCaptcha required for public-facing forms

**Rationale**: Libre311 handles civic service requests with potentially sensitive data. Security violations are non-negotiable blockers.

### V. WCAG 2.2 AA Compliance

All user-facing features MUST meet WCAG 2.2 Level AA accessibility standards:
- **Perceivable**: Text alternatives, adaptable content, distinguishable presentation
- **Operable**: Keyboard accessible, sufficient time, navigable, input modalities
- **Understandable**: Readable, predictable, input assistance
- **Robust**: Compatible with assistive technologies

Testing requirements:
- Manual keyboard navigation testing for all interactive elements
- Screen reader compatibility verification
- Color contrast ratio validation (minimum 4.5:1 for normal text, 3:1 for large text)
- Form labels and error messages that work with assistive tech

**Rationale**: Civic technology MUST be accessible to all community members regardless of ability. WCAG 2.2 AA is the recognized standard for government and public sector applications.

### VI. Open311 Standard Compliance

API endpoints MUST adhere to Open311 GeoReport API v2 specification:
- **Discovery endpoint**: `/discovery` with service discovery metadata
- **Standard endpoints**: Service list, service definition, service requests
- **Data formats**: JSON responses matching Open311 schema
- **Spatial data**: GeoJSON support via Hibernate Spatial
- **Multi-tenancy**: Jurisdiction-based data isolation

**Rationale**: Libre311 is commited to Open311 compliance as an open source product based on this standard. This is non-negotiable and ensures the open principles are maintained.

### VII. Minimal, Focused Changes

Code changes MUST be minimal and directly address the stated requirement:
- **No over-engineering**: Avoid abstractions for hypothetical future needs
- **No scope creep**: Bug fixes should fix bugs, not refactor surrounding code
- **No unnecessary additions**: Don't add comments, type hints, or documentation to unchanged code
- **Simple first**: Three similar lines beats premature abstraction
- **YAGNI principle**: You Aren't Gonna Need It - build what's needed now

**Rationale**: Complexity is technical debt. Simple, focused changes are easier to review, test, and maintain.

## Compliance Standards

### Technology Stack Requirements

**Backend**:
- JDK 17 or later
- Micronaut framework
- MySQL 8.0/5.7 or PostgreSQL 8.2+
- Hibernate JPA with auto-schema-gen
- Google Cloud Storage for media
- Google Vision API for SafeSearch
- ReCaptcha for bot prevention

**Frontend**:
- Node.js and npm
- SvelteKit framework
- Vite build tool
- Vitest for unit testing
- Playwright for integration testing
- Leaflet for mapping

**External dependencies**:
- UnityAuth service for authentication/authorization
- Google Cloud Platform (Storage, Vision API)

### Environment Management

**Local development**:
- Use `setenv.sh` for environment variables (created from `setenv.sh.example`)
- Environment-specific Micronaut profiles: `local`, `docker`, `prod`
- Frontend uses `VITE_BACKEND_URL` to configure API endpoint

**Docker development**:
- Use `.env.docker` for environment variables (NOT `.env`)
- ADC_PATH for Google Cloud credentials sharing
- Consistent service naming: `libre311-api`, `libre311-ui-dev`, `libre311-db`
- Makefile targets: `compose_api`, `compose_ui`, `compose_all`

**Never commit**:
- `.env` files
- Credentials or secrets
- ADC files
- Database passwords

## Development Workflow

### Code Quality Gates

All pull requests MUST pass:
1. **Tests**: All backend and frontend tests passing
2. **Linting**: `cd frontend && npm run lint` with no errors
3. **Type checking**: `cd frontend && npm run check` with no errors
4. **Format**: `cd frontend && npm run format` applied
5. **Build**: Both `./gradlew app:assemble` and `cd frontend && npm run build` succeed
6. **Security review**: No OWASP Top 10 vulnerabilities introduced
7. **Accessibility review**: WCAG 2.2 AA compliance for UI changes

### Commit Standards

Commits MUST:
- Focus on "why" not just "what" in commit messages
- Be clear and descriptive
- Be atomic (one logical change per commit)
- Pass all quality gates before push

### Branch Strategy

- **Main branch**: `main` (stable, production-ready)
- **Feature branches**: Follow naming convention from project workflow
- **Pull requests**: Required for all changes to main

### Development Commands

**Backend**:
```bash
source setenv.sh
./gradlew app:run          # Standard run
./gradlew app:run -t       # Auto-restart on changes
./gradlew app:test         # All tests
./gradlew app:test --tests "ClassName.testMethodName"  # Single test
./gradlew app:assemble     # Build JAR
```

**Frontend**:
```bash
source setenv.sh
cd frontend
npm install                # Install dependencies
npm run dev                # Development server
npm run build              # Production build
npm run preview            # Preview production build
npm run test               # All tests
npm run test:unit          # Unit tests only
npm run test:integration   # Integration tests only
npm run lint               # Lint check
npm run format             # Auto-format
npm run check              # Type checking
```

**Docker**:
```bash
make compose_api           # API + database
make compose_ui            # UI + database
make compose_all           # All services
# or
docker compose -f docker-compose.local.yml up
```

## Governance

### Amendment Process

Constitution changes MUST follow semantic versioning:
- **MAJOR**: Backward incompatible governance/principle removals or redefinitions
- **MINOR**: New principle/section added or materially expanded guidance
- **PATCH**: Clarifications, wording, typo fixes, non-semantic refinements

All amendments MUST:
1. Document rationale for change
2. Update `LAST_AMENDED_DATE` to current date
3. Update version per semver rules
4. Include Sync Impact Report at file top
5. Propagate changes to dependent templates
6. Receive approval before merge

### Compliance Review

All code reviews MUST verify:
- Constitution principles followed
- No violations introduced
- Security best practices observed
- Accessibility requirements met
- Tests present and passing
- Documentation updated if needed

### Complexity Justification

Any feature that violates the "Minimal, Focused Changes" principle MUST:
- Document the violation in plan.md Complexity Tracking section
- Explain why the complexity is necessary
- Justify why simpler alternatives were rejected
- Receive explicit approval before implementation

### Template Consistency

This constitution governs:
- `.specify/templates/plan-template.md`: Constitution Check section MUST validate against current principles
- `.specify/templates/spec-template.md`: Requirements section MUST support accessibility and security principles
- `.specify/templates/tasks-template.md`: Task structure MUST support testing requirements
- All project documentation and development guidance

**Version**: 1.0.0 | **Ratified**: 2025-11-26 | **Last Amended**: 2025-11-26
