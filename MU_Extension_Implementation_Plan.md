# MU Extension Features - Implementation Plan

**Created**: 2025-11-26
**Source**: UM_Extension.csv Jira stories
**Total Stories**: 11 (40 story points)

## Executive Summary

This plan organizes 11 Jira stories into 5 logical implementation phases based on technical dependencies, complexity, and the Libre311 Constitution principles. The plan prioritizes foundational work (Open311 compliance, data model extensions) before building dependent features.

## Story Overview

| Story | Title | Points | Parent | Category |
|-------|-------|--------|--------|----------|
| LIB-26 | Fix Noncompliant Service Codes | 3 | LIB-22 (Enhancements) | API Compliance |
| LIB-5 | Invalidate/Remove Service Requests | 2 | LIB-1 (MU Features) | Admin Feature |
| LIB-25 | Service Request Removal by Admins | 3 | LIB-1 (MU Features) | Admin Feature |
| LIB-9 | Setup Projects | 5 | LIB-22 (Enhancements) | Core Entity |
| LIB-11 | Project Close Out | 3 | LIB-1 (MU Features) | Project Management |
| LIB-12 | Data Export (filter on project) | 1 | LIB-1 (MU Features) | Analytics |
| LIB-6 | Privacy Warning and Consent | 1 | LIB-1 (MU Features) | Privacy/Security |
| LIB-15 | Service Request Definition Editor | 5 | LIB-1 (MU Features) | Admin UI |
| LIB-10 | Submitter Removal Request | 3 | LIB-1 (MU Features) | Citizen Feature |
| LIB-7 | Walk Audit and Photo Voice Modes | 5 | LIB-1 (MU Features) | Citizen UI |
| LIB-8 | Unreliable Connectivity Support | 5 | LIB-1 (MU Features) | Offline Support |
| LIB-24 | Accessibility and Branding | 5 | LIB-1 (MU Features) | Cross-Cutting |

## Key Observations

### Duplicate/Overlapping Stories
**LIB-5** and **LIB-25** appear to describe the same functionality (admin soft-delete of service requests). Recommend consolidating into a single implementation under LIB-25.

### Cross-Cutting Concerns
**LIB-24** (Accessibility and Branding Compliance) is not a standalone feature but a quality requirement that applies to ALL new features. Per Constitution Principle V (WCAG 2.2 AA Compliance), this should be enforced during each feature's implementation rather than as a separate story.

### Critical Dependencies
- **LIB-9** (Setup Projects) must be implemented before **LIB-11** and **LIB-12**
- **LIB-26** (Fix Service Codes) affects Open311 API compliance and should be prioritized
- **LIB-6** (Privacy Consent) should be implemented before citizen-facing features that collect data

---

## Phase 1: Foundation & Compliance (7 points)

**Goal**: Fix Open311 compliance issues and establish privacy foundations

**Duration Estimate**: 1-2 sprints

### Stories

#### LIB-26: Fix Noncompliant Service Codes (3 points)
**Priority**: CRITICAL - Constitution Principle VI (Open311 Compliance)

**Why First**:
- Breaks Open311 GeoReport API v2 specification
- Affects core API contract that other features depend on
- Database schema change (service codes: numeric → string)

**Technical Scope**:
- Update `Service` entity (`app/src/main/java/app/model/Service.java`)
- Modify `RootController.java` GET /services and GET /services/{code} endpoints
- Create database migration to convert numeric IDs → string codes
- Update frontend API client (`frontend/src/lib/services/Libre311/`)
- Update all existing service references in frontend components

**Acceptance Criteria**:
- [ ] Service codes use strings (e.g., "POTHOLE") instead of numeric IDs
- [ ] GET /services returns string codes per Open311 spec
- [ ] GET /services/{code} accepts string codes
- [ ] Migration preserves existing data with reasonable string mappings
- [ ] Frontend components updated to use string codes
- [ ] All tests pass (backend: `./gradlew app:test`, frontend: `npm run test`)

**Constitution Compliance**:
- ✅ Principle VI: Open311 Standard Compliance (primary driver)
- ✅ Principle III: Test Coverage required
- ✅ Principle I: Layered Architecture maintained

---

#### LIB-6: Privacy Warning and Consent Acceptance (1 point)
**Priority**: HIGH - Constitution Principle IV (Security-First)

**Why Second**:
- Required before citizen-facing data collection features
- Simple implementation (frontend + consent tracking)
- Establishes privacy baseline for future features

**Technical Scope**:
- Create privacy consent component (`frontend/src/lib/components/PrivacyConsent.svelte`)
- Create backend endpoint for consent tracking
- Add `consent_records` table (user identifier, timestamp, consent text hash)
- Implement persistent cookie for consent status
- Display on first service request submission

**Acceptance Criteria**:
- [ ] Privacy warning displays before first submission
- [ ] User consent captured and stored in database
- [ ] Persistent cookie prevents repeated prompts (same browser)
- [ ] Consent text versioning supported
- [ ] WCAG 2.2 AA compliant (keyboard navigation, screen reader support)
- [ ] Backend endpoint secured (CSRF protection)

**Constitution Compliance**:
- ✅ Principle IV: Security-First Development (privacy, consent)
- ✅ Principle V: WCAG 2.2 AA Compliance
- ✅ Principle III: Test Coverage required

---

#### LIB-5/LIB-25: Service Request Removal by Administrators (3 points)
**CONSOLIDATED** - Implement as single feature

**Priority**: HIGH - Data hygiene and privacy

**Why Third**:
- Core admin capability needed before advanced features
- Soft delete pattern reusable for other entities
- Privacy/GDPR compliance consideration

**Technical Scope**:
- Add `deleted` boolean and `deleted_at` timestamp to `ServiceRequest` entity
- Add `deleted_by` foreign key to `app_users` table
- Create DELETE endpoint (soft delete) in `RootController.java`
- Update service layer to filter deleted requests from public queries
- Create admin UI for marking requests as deleted
- Add restoration capability (admin only)
- Update analytics queries to exclude deleted requests

**Acceptance Criteria**:
- [ ] Admins can soft-delete service requests (admin UI)
- [ ] Deleted requests excluded from GET /requests public endpoint
- [ ] Deleted requests excluded from analytics/reporting
- [ ] Deleted requests retained in database with audit trail
- [ ] Admins can restore deleted requests if needed
- [ ] Permission check: `@RequiresPermissions("service_requests:delete")`
- [ ] WCAG 2.2 AA compliant admin UI

**Constitution Compliance**:
- ✅ Principle IV: Security-First (data privacy, permission-based auth)
- ✅ Principle I: Layered Architecture (controller → service → repository)
- ✅ Principle V: WCAG 2.2 AA Compliance
- ✅ Principle VII: Minimal, Focused Changes (soft delete, not complex purging)

---

## Phase 2: Project Management Foundation (9 points)

**Goal**: Implement audit project entity and core management features

**Duration Estimate**: 2-3 sprints

**Dependencies**: Phase 1 complete (especially LIB-26 for stable API)

### Stories

#### LIB-9: Setup Projects (5 points)
**Priority**: CRITICAL - Blocks LIB-11 and LIB-12

**Why First in Phase**:
- Creates foundational `Project` entity
- Required dependency for project-related features
- Database schema addition
- Open311 extension (requires careful API design)

**Technical Scope**:
- Create `Project` entity (`app/src/main/java/app/model/Project.java`)
  - Fields: id, name, description, start_date, end_date, status, jurisdiction_id
- Create `ProjectRepository` (Micronaut Data)
- Create `ProjectService` (business logic)
- Add `project_id` foreign key to `ServiceRequest` entity
- Create `ProjectController` (admin endpoints for CRUD)
- Create admin UI for project management (`frontend/src/routes/projects/`)
- Handle Open311 compatibility (project_id as optional metadata)

**Acceptance Criteria**:
- [ ] Admins can create time-bound projects with name, dates, description
- [ ] Service requests can be associated with a project (optional)
- [ ] Projects filterable by jurisdiction (multi-tenancy)
- [ ] Project CRUD operations secured with permissions
- [ ] Open311 endpoints remain compliant (project_id in metadata, not breaking change)
- [ ] Database migration creates `projects` table and adds FK to `service_requests`
- [ ] WCAG 2.2 AA compliant admin UI
- [ ] Unit tests for `ProjectService`, integration tests for `ProjectController`

**Constitution Compliance**:
- ✅ Principle VI: Open311 Compliance (non-breaking extension)
- ✅ Principle I: Layered Architecture (entity → repository → service → controller)
- ✅ Principle IV: Security-First (permission-based access)
- ✅ Principle III: Test Coverage

---

#### LIB-11: Project Close Out (3 points)
**Priority**: MEDIUM - Depends on LIB-9

**Why Second in Phase**:
- Natural extension of LIB-9
- Adds state management to projects
- Enables data immutability for completed studies

**Technical Scope**:
- Add `status` enum to `Project` entity (ACTIVE, CLOSED)
- Create endpoint to update project status (PUT /projects/{id}/status)
- Add business logic to prevent modifications when status=CLOSED
- Update `ServiceRequestService` to reject new requests for closed projects
- Create admin UI for closing projects (with confirmation dialog)
- Generate immutability warnings in UI for closed projects

**Acceptance Criteria**:
- [ ] Admins can change project status to CLOSED
- [ ] Closed projects prevent new service request associations
- [ ] Existing service requests in closed projects remain viewable
- [ ] UI displays closed status with visual indicator
- [ ] Confirmation dialog warns about immutability
- [ ] Permission check: `@RequiresPermissions("projects:close")`
- [ ] WCAG 2.2 AA compliant UI (status indicators, dialogs)

**Constitution Compliance**:
- ✅ Principle I: Layered Architecture
- ✅ Principle IV: Security-First (permission-based)
- ✅ Principle V: WCAG 2.2 AA Compliance
- ✅ Principle VII: Minimal, Focused Changes (status field, simple state machine)

---

#### LIB-12: Data Export (filter on project) (1 point)
**Priority**: LOW - Nice-to-have after LIB-9

**Why Third in Phase**:
- Simple enhancement to existing export feature
- Depends on LIB-9 for project_id FK
- Low complexity

**Technical Scope**:
- Update existing CSV export endpoint to accept `project_id` query parameter
- Modify export query to filter by project_id when provided
- Add project filter dropdown to export UI
- Update authorization (user must have access to project's jurisdiction)

**Acceptance Criteria**:
- [ ] Export endpoint accepts optional `?project_id=X` parameter
- [ ] CSV export filters service requests by project when parameter provided
- [ ] UI includes project dropdown in export dialog
- [ ] Authorization check: user can only export from accessible jurisdictions
- [ ] WCAG 2.2 AA compliant filter UI

**Constitution Compliance**:
- ✅ Principle I: Layered Architecture
- ✅ Principle IV: Security-First (authorization check)
- ✅ Principle V: WCAG 2.2 AA Compliance
- ✅ Principle VII: Minimal, Focused Changes (add filter, don't refactor export)

---

## Phase 3: Admin Tooling (5 points)

**Goal**: Improve admin experience with visual editor

**Duration Estimate**: 1-2 sprints

**Dependencies**: Phase 1 and 2 complete

### Stories

#### LIB-15: Service Request Definition Editor (5 points)
**Priority**: MEDIUM - Usability improvement

**Why This Phase**:
- Enhances existing functionality (basic editor already exists)
- Not a blocker for other features
- Requires UI polish and testing

**Technical Scope**:
- Audit existing service definition editor (`frontend/src/lib/components/ServiceDefinitionEditor/`)
- Add drag-and-drop field reordering (if missing)
- Improve validation and error handling
- Add field type previews
- Backend: Ensure `ServiceDefinitionService` supports all CRUD operations robustly
- Add automated tests for editor component (Playwright)

**Acceptance Criteria**:
- [ ] Admins can create/edit service definitions without code
- [ ] Drag-and-drop field reordering supported
- [ ] Real-time validation with clear error messages
- [ ] Field type previews (text, dropdown, etc.)
- [ ] Changes persist correctly to backend
- [ ] Backend handles edge cases (duplicate fields, invalid schemas)
- [ ] WCAG 2.2 AA compliant (keyboard-only drag/drop alternative, screen reader support)
- [ ] Integration tests with Playwright

**Constitution Compliance**:
- ✅ Principle II: Component-Based Architecture (frontend)
- ✅ Principle V: WCAG 2.2 AA Compliance (keyboard navigation critical)
- ✅ Principle III: Test Coverage (Playwright integration tests)
- ✅ Principle VII: Minimal Changes (improve existing, don't rebuild)

---

## Phase 4: Citizen-Facing Features (9 points)

**Goal**: Enhance citizen experience with new submission modes and data control

**Duration Estimate**: 2-3 sprints

**Dependencies**: Phase 1 complete (especially LIB-6 for privacy consent)

### Stories

#### LIB-10: Submitter Removal Request (3 points)
**Priority**: MEDIUM - Citizen data control

**Why First in Phase**:
- Privacy/GDPR-related feature
- Depends on LIB-6 (consent tracking)
- Simpler than LIB-7

**Technical Scope**:
- Design approach: Issue unique "removal token" on service request creation
- Add `removal_token` (UUID) to `ServiceRequest` entity
- Create endpoint: POST /requests/{service_request_id}/removal-request
  - Requires removal token in request body
  - Sends email/SMS to submitter for confirmation
- Create confirmation endpoint: POST /requests/{service_request_id}/confirm-removal
- Integrate with soft-delete logic from LIB-5/LIB-25
- Create frontend UI for removal request flow
- Optional: OAuth guest client for email-based identification

**Acceptance Criteria**:
- [ ] Citizens receive unique removal token on request creation
- [ ] Removal request endpoint accepts token and sends confirmation
- [ ] Email/SMS notification includes confirmation link with token
- [ ] Confirmation link triggers soft-delete of request
- [ ] Invalid tokens rejected with clear error message
- [ ] WCAG 2.2 AA compliant UI (form accessibility)
- [ ] Security: Rate limiting on removal request endpoint

**Constitution Compliance**:
- ✅ Principle IV: Security-First (token-based auth, rate limiting)
- ✅ Principle V: WCAG 2.2 AA Compliance
- ✅ Principle III: Test Coverage
- ✅ Principle VII: Minimal Changes (leverage existing soft-delete)

---

#### LIB-7: Walk Audit and Photo Voice Modes (5 points)
**Priority**: MEDIUM - New submission workflows

**Why Second in Phase**:
- Frontend-only feature (no backend changes expected)
- Complex UI workflows
- Depends on LIB-6 (privacy consent)

**Technical Scope**:
- Create "Photo Voice Mode" workflow (`frontend/src/routes/issue/create/photo-voice/`)
  - Simplified form: topic prompt → photo upload → narrative description
  - Voice-to-text integration (browser Speech Recognition API)
- Create "Walk Audit Mode" workflow (`frontend/src/routes/issue/create/walk-audit/`)
  - Multi-entry form for single session
  - Session grouping UI (display related entries together)
  - Optional summary entry for walk session
- Add mode selector to service request creation flow
- Update service request display to show mode/session grouping

**Acceptance Criteria**:
- [ ] Users can select "Photo Voice" or "Walk Audit" mode before creating request
- [ ] Photo Voice mode uses simplified UI with topic prompts
- [ ] Voice-to-text supported for narrative descriptions
- [ ] Walk Audit mode allows multiple entries per session
- [ ] Walk Audit entries visually grouped by session
- [ ] Mode selection works on mobile devices
- [ ] WCAG 2.2 AA compliant (alternative to voice-to-text, keyboard nav)
- [ ] Integration tests with Playwright

**Constitution Compliance**:
- ✅ Principle II: Component-Based Architecture (new UI workflows)
- ✅ Principle V: WCAG 2.2 AA Compliance (voice-to-text alternatives critical)
- ✅ Principle III: Test Coverage (Playwright)
- ✅ Principle VII: Minimal Changes (no backend changes expected)

---

## Phase 5: Offline Support (5 points)

**Goal**: Enable offline service request creation with background sync

**Duration Estimate**: 2 sprints

**Dependencies**: All previous phases recommended (most complex feature)

### Stories

#### LIB-8: Unreliable Connectivity Support (5 points)
**Priority**: LOW - Advanced feature, high complexity

**Why Last**:
- Most complex frontend feature (service workers, background sync)
- Requires stable foundation from all previous features
- Edge cases with offline maps and duplicate submissions
- Can be deferred if timeline pressured

**Technical Scope**:
- Implement service worker for offline detection (`frontend/src/service-worker.js`)
- Create IndexedDB schema for queued service requests
- Implement background sync for queued submissions
- Add client-generated `submission_id` (UUID) to prevent duplicates
- Update backend to handle idempotent submissions via `submission_id`
- Cache OpenStreetMap tiles for offline map view (limited zoom levels)
- Disable address lookup when offline (show warning)
- Add offline status indicator in UI
- Add "queued requests" view for pending submissions

**Acceptance Criteria**:
- [ ] Service requests can be created while offline
- [ ] Offline requests queued in IndexedDB with photos
- [ ] Background sync submits queued requests when connection restored
- [ ] Duplicate submissions prevented via client-generated `submission_id`
- [ ] Backend transaction ensures duplicate `submission_id` rejected
- [ ] Map tiles cached for reasonable offline area
- [ ] Address lookup disabled with clear message when offline
- [ ] Offline status indicator visible in UI
- [ ] Queued requests viewable in UI before submission
- [ ] WCAG 2.2 AA compliant (status indicators, error messages)
- [ ] Integration tests for online/offline transitions

**Constitution Compliance**:
- ✅ Principle II: Component-Based Architecture (service worker integration)
- ✅ Principle IV: Security-First (duplicate prevention, transaction safety)
- ✅ Principle V: WCAG 2.2 AA Compliance
- ✅ Principle III: Test Coverage (complex testing required)

---

## Cross-Cutting: Accessibility & Branding

### LIB-24: Accessibility and Branding Compliance (5 points)
**Treatment**: NOT a separate implementation phase

**Recommendation**: Apply as quality gate for EVERY story above

**Constitution Alignment**:
Per **Principle V (WCAG 2.2 AA Compliance)**, accessibility is mandatory for all user-facing features. This story should be:
1. **Decomposed** into acceptance criteria for each feature story
2. **Enforced** during PR reviews for every phase
3. **Tracked** as part of "Definition of Done" for all UI changes

**Quality Checklist (apply to each story)**:
- [ ] Keyboard navigation tested for all interactive elements
- [ ] Screen reader compatibility verified (VoiceOver, NVDA)
- [ ] Color contrast ratios meet WCAG 2.2 AA (4.5:1 normal, 3:1 large text)
- [ ] Form labels and ARIA attributes present
- [ ] Error messages accessible to assistive tech
- [ ] MU Extension brand guidelines applied (colors, typography, logos)
- [ ] Responsive design tested (mobile, tablet, desktop)

**Testing Requirements**:
- Manual accessibility audit before each feature's PR merge
- Automated tools: axe DevTools, Lighthouse accessibility score
- Brand review: Visual QA against MU Extension style guide

---

## Implementation Order Summary

### Recommended Sequence

```
Phase 1: Foundation (Sprints 1-2)
  └─ LIB-26 → LIB-6 → LIB-5/LIB-25

Phase 2: Project Management (Sprints 3-5)
  └─ LIB-9 → LIB-11 → LIB-12

Phase 3: Admin Tooling (Sprints 6-7)
  └─ LIB-15

Phase 4: Citizen Features (Sprints 8-10)
  └─ LIB-10 → LIB-7

Phase 5: Offline Support (Sprints 11-12)
  └─ LIB-8

Cross-Cutting: LIB-24 applied throughout all phases
```

### Critical Path

The minimum viable feature set (MVP) would be:
1. **Phase 1** - Fix compliance issues and establish privacy baseline
2. **Phase 2** - Enable project-based reporting (core MU Extension need)
3. **LIB-10** - Citizen data control (privacy requirement)

Everything else can be prioritized based on user feedback after MVP.

---

## Risk Assessment

### High Risk

**LIB-8 (Offline Support)**:
- Complexity: Service workers, background sync, duplicate prevention
- Testing: Difficult to test offline scenarios reliably
- Browser compatibility: Service worker support varies
- Recommendation: Defer to Phase 5, consider MVP without offline support

**LIB-26 (Service Code Migration)**:
- Risk: Database migration on production data
- Mitigation: Thorough testing, rollback plan, data backup

### Medium Risk

**LIB-9 (Setup Projects)**:
- Risk: Open311 compatibility concerns with non-standard extension
- Mitigation: Metadata approach, ensure GET /requests remains compliant

**LIB-7 (Walk Audit/Photo Voice)**:
- Risk: Voice-to-text accessibility challenges
- Mitigation: Ensure text input alternative, WCAG compliance review

### Low Risk

All other stories have low technical risk with established patterns.

---

## Testing Strategy

### Per-Story Testing (Constitution Principle III)

Each story MUST include:
- **Backend**: Unit tests (services), integration tests (controllers)
- **Frontend**: Unit tests (Vitest), integration tests (Playwright)
- **Run before merge**: `./gradlew app:test` AND `cd frontend && npm run test`

### Accessibility Testing (Constitution Principle V)

Each UI story MUST include:
- Manual keyboard navigation testing
- Screen reader testing (VoiceOver or NVDA)
- Automated tools (axe DevTools)
- Color contrast validation

### Regression Testing

- Full test suite on main branch after each merge
- Smoke tests for Open311 endpoints (LIB-26 critical)

---

## Resource Estimates

### Development Effort

| Phase | Story Points | Estimated Sprints | Notes |
|-------|--------------|-------------------|-------|
| Phase 1 | 7 | 1-2 | Includes complex migration (LIB-26) |
| Phase 2 | 9 | 2-3 | New entity + endpoints |
| Phase 3 | 5 | 1-2 | UI polish + testing |
| Phase 4 | 8 | 2-3 | Complex UI workflows |
| Phase 5 | 5 | 2 | High complexity (service workers) |
| **Total** | **34** | **8-12 sprints** | Assuming 5-7 points/sprint |

*Note: LIB-5 and LIB-25 consolidated (removed duplicate 3 points from total)*

### Team Composition (Recommended)

- **1 Backend Developer**: Java/Micronaut expertise (Phases 1-2 critical)
- **1 Frontend Developer**: Svelte/SvelteKit expertise (all phases)
- **1 Full-Stack Developer**: Support both tracks (Phases 3-5)
- **1 QA/Accessibility Specialist**: WCAG testing (continuous)

---

## Success Metrics

### Technical Metrics
- [ ] All tests passing (100% of stories)
- [ ] Zero Open311 compliance violations (validator tool)
- [ ] WCAG 2.2 AA score (Lighthouse) ≥ 90 for all new pages

### User Metrics
- [ ] Admin task time reduced by 50% (service definition editing, project management)
- [ ] Citizen submission completion rate ≥ 80% (Photo Voice/Walk Audit modes)
- [ ] Privacy consent acceptance rate tracked (baseline for compliance)

### Code Quality
- [ ] No OWASP Top 10 vulnerabilities (security scan)
- [ ] Code review approval required (100% of PRs)
- [ ] Constitution compliance verified (code review checklist)

---

## Open Questions

1. **LIB-5 vs LIB-25**: Confirm these should be consolidated into single implementation
2. **LIB-10 Email/SMS**: Which notification service will be used? (Twilio, SendGrid, etc.)
3. **LIB-8 Offline Maps**: What geographic area should be cached? (jurisdiction boundaries?)
4. **LIB-24 Brand Guidelines**: Where are MU Extension brand assets documented?
5. **LIB-9 Open311 Extension**: Should project metadata follow Open311 extended attributes pattern?

---

## Appendix: Constitution Alignment Matrix

| Story | Principle I | Principle II | Principle III | Principle IV | Principle V | Principle VI | Principle VII |
|-------|-------------|--------------|---------------|--------------|-------------|--------------|---------------|
|       | Layered Arch | Component Arch | Test Coverage | Security-First | WCAG 2.2 AA | Open311 | Minimal Changes |
| LIB-26 | ✅ | - | ✅ | - | - | ✅ | ✅ |
| LIB-5/25 | ✅ | - | ✅ | ✅ | ✅ | - | ✅ |
| LIB-9 | ✅ | - | ✅ | ✅ | ✅ | ✅ | - |
| LIB-11 | ✅ | - | ✅ | ✅ | ✅ | - | ✅ |
| LIB-12 | ✅ | - | ✅ | ✅ | ✅ | - | ✅ |
| LIB-6 | ✅ | ✅ | ✅ | ✅ | ✅ | - | ✅ |
| LIB-15 | - | ✅ | ✅ | ✅ | ✅ | - | ✅ |
| LIB-10 | ✅ | ✅ | ✅ | ✅ | ✅ | - | ✅ |
| LIB-7 | - | ✅ | ✅ | - | ✅ | - | ✅ |
| LIB-8 | - | ✅ | ✅ | ✅ | ✅ | - | - |

**Key Takeaway**: All stories align with Constitution principles, with particular emphasis on Test Coverage (III), WCAG 2.2 AA (V), and Minimal Changes (VII).
