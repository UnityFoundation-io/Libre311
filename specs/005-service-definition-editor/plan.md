# Implementation Plan: Service Definition Editor

**Branch**: `005-service-definition-editor` | **Date**: 2026-01-05 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/005-service-definition-editor/spec.md`

## Summary

Implement a Google Forms-style UI for administrators to manage service definitions (service request types and their attributes/questions). This is a **frontend-only feature** that builds new UI components on top of existing API endpoints in `JurisdictionAdminController`. The editor will allow browsing service groups, editing service definitions, managing attribute cards with drag-and-drop reordering, and configuring list options for multiple choice fields.

## Technical Context

**Language/Version**: TypeScript 5.x (SvelteKit frontend)
**Primary Dependencies**: SvelteKit, STWUI (UI components), Tailwind CSS, Svelte transitions, axios
**Storage**: N/A (frontend-only, data fetched via existing API)
**Testing**: Vitest (unit), Playwright (integration)
**Target Platform**: Web browsers (desktop-first, 1024px+ viewport)
**Project Type**: Web application (frontend-only feature)
**Performance Goals**: Page load <3s, auto-save <1s (per SC-001, SC-003)
**Constraints**: WCAG 2.1 Level AA accessibility compliance
**Scale/Scope**: Admin interface for managing ~10-50 service groups, ~5-20 services per group, ~5-30 attributes per service

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Open311 Compliance | ✅ PASS | No API changes; uses existing Open311-compliant endpoints |
| II. Layered Architecture | ✅ N/A | Frontend-only feature; backend architecture unchanged |
| III. Security First | ✅ PASS | Uses existing JWT auth via UnityAuth; no new security surface |
| IV. Test Coverage | ⚠️ PENDING | Unit tests required for new components; integration tests for critical flows |
| V. Dual Environment Support | ✅ PASS | Standard SvelteKit dev/build; no special config |
| VI. Database Flexibility | ✅ N/A | No schema changes |
| VII. Minimal External Dependencies | ✅ PASS | No new dependencies; uses existing STWUI, Svelte |
| VIII. Consistent Code Formatting | ✅ PASS | Prettier enforced via npm run lint |
| IX. Accessibility (WCAG 2.2 AA) | ⚠️ PENDING | Requires keyboard navigation, focus management, ARIA labels, color contrast |

**Gate Result**: PASS with pending test and accessibility requirements to fulfill during implementation.

## Project Structure

### Documentation (this feature)

```text
specs/005-service-definition-editor/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (API documentation)
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
frontend/
├── src/
│   ├── routes/
│   │   └── groups/
│   │       ├── +page.svelte                    # Groups list (EXISTING - enhance)
│   │       └── [group_id]/
│   │           ├── +page.svelte                # Services in group (EXISTING)
│   │           └── services/
│   │               └── [service_id]/
│   │                   ├── +page.svelte        # Service attribute list (EXISTING)
│   │                   └── edit/
│   │                       └── +page.svelte    # NEW: Google Forms-style editor
│   ├── lib/
│   │   └── components/
│   │       └── ServiceDefinitionEditor/
│   │           ├── GroupListItem.svelte        # EXISTING
│   │           ├── ServiceListItem.svelte      # EXISTING
│   │           ├── SdaListItem.svelte          # EXISTING
│   │           ├── DragAndDrop.svelte          # EXISTING (at lib/components/)
│   │           ├── EditorView/                 # NEW: Google Forms-style components
│   │           │   ├── EditorContainer.svelte
│   │           │   ├── HeaderCard.svelte
│   │           │   ├── AttributeCard.svelte
│   │           │   ├── AttributeCardCollapsed.svelte
│   │           │   ├── AttributeCardExpanded.svelte
│   │           │   ├── AttributeTypeSelector.svelte
│   │           │   ├── OptionsList.svelte
│   │           │   ├── AttributeFooter.svelte
│   │           │   ├── AddFieldButton.svelte
│   │           │   └── SaveToast.svelte
│   │           └── ListView/                   # NEW: Enhanced list view
│   │               ├── ServiceGroupCard.svelte
│   │               └── ServiceListRow.svelte
│   └── tests/                                  # Playwright integration tests
│       └── service-definition-editor/
│           ├── browse-groups.spec.ts
│           ├── edit-service.spec.ts
│           └── manage-attributes.spec.ts
```

**Structure Decision**: Web application frontend-only. New components organized under `ServiceDefinitionEditor/EditorView/` for the Google Forms-style editor, with the existing list view components enhanced in-place. New route at `/groups/[group_id]/services/[service_id]/edit` for the full-page editor.

## Complexity Tracking

No constitution violations requiring justification. The feature uses existing patterns and dependencies.

## Existing Assets Analysis

### Backend API (Already Complete)

All required endpoints exist in `JurisdictionAdminController.java`:

| Operation | Endpoint | Method | Status |
|-----------|----------|--------|--------|
| List groups | `/jurisdiction-admin/groups/` | GET | ✅ Exists |
| Create group | `/jurisdiction-admin/groups/` | POST | ✅ Exists |
| Edit group | `/jurisdiction-admin/groups/{groupId}` | PATCH | ✅ Exists |
| Delete group | `/jurisdiction-admin/groups/{groupId}` | DELETE | ✅ Exists |
| Create service | `/jurisdiction-admin/services` | POST | ✅ Exists |
| Edit service | `/jurisdiction-admin/services/{serviceCode}` | PATCH | ✅ Exists |
| Delete service | `/jurisdiction-admin/services/{serviceCode}` | DELETE | ✅ Exists |
| Reorder services | `/jurisdiction-admin/groups/{groupId}/services-order` | PATCH | ✅ Exists |
| Create attribute | `/jurisdiction-admin/services/{serviceCode}/attributes` | POST | ✅ Exists |
| Edit attribute | `/jurisdiction-admin/services/{serviceCode}/attributes/{code}` | PATCH | ✅ Exists |
| Delete attribute | `/jurisdiction-admin/services/{serviceCode}/attributes/{code}` | DELETE | ✅ Exists |
| Reorder attributes | `/jurisdiction-admin/services/{serviceCode}/attributes-order` | PATCH | ✅ Exists |

### Frontend API Client (Already Complete)

All API methods exist in `Libre311.ts`:

- `getGroupList()`, `createGroup()`, `editGroup()`
- `createService()`, `editService()`, `deleteService()`, `updateServicesOrder()`
- `createAttribute()`, `editAttribute()`, `deleteAttribute()`, `updateAttributesOrder()`
- `getServiceDefinition()` (for loading existing attributes)

### Existing Components (Reusable)

| Component | Location | Reuse Strategy |
|-----------|----------|----------------|
| DragAndDrop.svelte | `lib/components/` | Direct reuse for attribute reordering |
| GroupListItem.svelte | `ServiceDefinitionEditor/` | Enhance for expand/collapse UX |
| ServiceListItem.svelte | `ServiceDefinitionEditor/` | Enhance with Edit button navigation |
| DisplayListItem.svelte | `ServiceDefinitionEditor/` | Reuse for collapsed views |
| EditListItem.svelte | `ServiceDefinitionEditor/` | Reuse for inline editing |

## Design Decisions

### 1. Route Structure

**Decision**: Add new `/groups/[group_id]/services/[service_id]/edit` route for the Google Forms-style editor rather than replacing existing routes.

**Rationale**: Preserves existing functionality, allows gradual migration, and provides clear separation between list view and editor view.

### 2. State Management

**Decision**: Use Svelte stores with context for editor state (current service, attributes list, expanded card index, pending saves).

**Rationale**: Matches existing patterns in the codebase, avoids new dependencies, and provides reactive updates.

### 3. Auto-Save Implementation

**Decision**: Debounced save on blur/change with optimistic UI updates and error recovery.

**Pattern**:
1. User edits field → immediate UI update
2. Debounce 500ms → API call
3. Success → show toast
4. Failure → show error toast with retry, revert UI if needed

### 4. Accordion Behavior

**Decision**: Only one attribute card expanded at a time; clicking a collapsed card expands it and collapses the previously expanded card.

**Rationale**: Matches Google Forms UX and reduces visual clutter.

### 5. Accessibility Strategy

**Decision**: Full keyboard navigation with focus trapping in expanded cards, ARIA live regions for save confirmations, visible focus indicators.

**Implementation**:
- Tab order: groups → services → editor cards → card controls
- Escape key: collapse expanded card
- Arrow keys: navigate between options in lists
- Screen reader announcements for saves/errors

---

## Constitution Check (Post-Design)

*Re-evaluation after Phase 1 design completion.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Open311 Compliance | ✅ PASS | No API changes; uses existing Open311-compliant endpoints |
| II. Layered Architecture | ✅ N/A | Frontend-only feature; backend architecture unchanged |
| III. Security First | ✅ PASS | Uses existing JWT auth via UnityAuth; no new security surface |
| IV. Test Coverage | ✅ PLANNED | Unit tests specified for new components (co-located .test.ts); integration tests in tests/service-definition-editor/ |
| V. Dual Environment Support | ✅ PASS | Standard SvelteKit dev/build; no special config |
| VI. Database Flexibility | ✅ N/A | No schema changes |
| VII. Minimal External Dependencies | ✅ PASS | No new dependencies added; uses existing STWUI, Svelte, Tailwind |
| VIII. Consistent Code Formatting | ✅ PASS | Prettier enforced via npm run lint; all new files follow existing patterns |
| IX. Accessibility (WCAG 2.2 AA) | ✅ PLANNED | Detailed accessibility strategy defined: keyboard nav, ARIA labels, focus management, live regions |

**Post-Design Gate Result**: ✅ PASS - All principles addressed. Testing and accessibility requirements have implementation plans.

---

## Generated Artifacts

| Artifact | Path | Description |
|----------|------|-------------|
| Implementation Plan | [plan.md](plan.md) | This file |
| Research | [research.md](research.md) | Codebase exploration, existing assets analysis |
| Data Model | [data-model.md](data-model.md) | TypeScript types, Svelte stores, state management |
| API Contract | [contracts/admin-api.md](contracts/admin-api.md) | Existing API endpoint documentation |
| Quickstart | [quickstart.md](quickstart.md) | Development setup, key files, coding patterns |

---

## Next Steps

Run `/speckit.tasks` to generate the task breakdown for implementation.
