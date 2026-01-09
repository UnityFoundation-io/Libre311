# Implementation Plan: Service Definition Editor Refinement

**Branch**: `010-service-editor-refinement` | **Date**: 2026-01-09 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/010-service-editor-refinement/spec.md`

## Summary

Refine the Service Definition Configuration editor with a split-pane interface featuring tree navigation on the left and a Google Forms-style editor on the right. This replaces the current multi-page hierarchical navigation with an integrated single-page experience. Key changes include explicit save per card (replacing auto-save), drag-and-drop reordering for services and attributes, and unsaved changes protection.

## Technical Context

**Language/Version**: TypeScript 5.x (SvelteKit frontend only)
**Primary Dependencies**: SvelteKit, STWUI (UI components), Tailwind CSS, axios (HTTP client), zod (validation)
**Storage**: N/A (frontend only - uses existing backend API)
**Testing**: Vitest (unit tests, co-located), Playwright (integration tests in `frontend/tests/`)
**Target Platform**: Web browsers (Chrome, Firefox, Safari, Edge - latest 2 versions)
**Project Type**: Web application (frontend modification only)
**Performance Goals**: Page load <3s, save operations <2s with visual feedback
**Constraints**: Must maintain WCAG 2.2 AA accessibility, minimum 1024px viewport width
**Scale/Scope**: Admin interface for ~10-50 service groups with ~5-20 services each

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Open311 Compliance | N/A | UI only - uses existing compliant API endpoints |
| II. Layered Architecture | N/A | Frontend only - no backend changes |
| III. Security First | PASS | Uses existing JWT auth via UnityAuth |
| IV. Test Coverage | APPLIES | Unit tests required for new components (co-located) |
| V. Dual Environment | PASS | SvelteKit works in both local and Docker |
| VI. Database Flexibility | N/A | No database changes |
| VII. Minimal Dependencies | PASS | Uses existing STWUI, no new dependencies needed |
| VIII. Prettier | APPLIES | Must run `npm run format` before commits |
| IX. Accessibility | APPLIES | WCAG 2.2 AA required per clarification |

**Gate Status**: PASS - No violations. Proceed to Phase 0.

## Project Structure

### Documentation (this feature)

```text
specs/010-service-editor-refinement/
├── spec.md              # Feature specification (complete)
├── plan.md              # This file
├── research.md          # Phase 0 output - component patterns
├── data-model.md        # Phase 1 output - frontend types/state
├── quickstart.md        # Phase 1 output - development guide
├── contracts/           # Phase 1 output - API contracts (existing)
│   └── admin-api.md     # Documents existing JurisdictionAdminController endpoints
└── tasks.md             # Phase 2 output (created by /speckit.tasks)
```

### Source Code (repository root)

```text
frontend/
├── src/
│   ├── routes/
│   │   └── groups/
│   │       └── config/           # NEW: Split-pane editor route
│   │           └── +page.svelte  # Main editor page
│   │
│   └── lib/
│       └── components/
│           └── ServiceDefinitionEditor/
│               ├── SplitPaneEditor/           # NEW: Main layout components
│               │   ├── SplitPaneLayout.svelte
│               │   ├── SplitPaneLayout.test.ts
│               │   ├── TreePanel.svelte
│               │   ├── TreePanel.test.ts
│               │   ├── EditorPanel.svelte
│               │   └── EditorPanel.test.ts
│               │
│               ├── TreeView/                  # NEW: Tree navigation
│               │   ├── TreeGroup.svelte
│               │   ├── TreeGroup.test.ts
│               │   ├── TreeService.svelte
│               │   ├── TreeService.test.ts
│               │   └── TreeDragDrop.svelte
│               │
│               ├── GroupEditor/               # NEW: Group editing
│               │   ├── GroupEditor.svelte
│               │   └── GroupEditor.test.ts
│               │
│               ├── ServiceEditor/             # NEW: Service editing
│               │   ├── ServiceHeaderCard.svelte
│               │   ├── ServiceHeaderCard.test.ts
│               │   ├── AttributeCardList.svelte
│               │   └── AttributeCardList.test.ts
│               │
│               ├── AttributeCard/             # NEW: Attribute editing
│               │   ├── AttributeCard.svelte
│               │   ├── AttributeCard.test.ts
│               │   ├── AttributeCardCollapsed.svelte
│               │   ├── AttributeCardExpanded.svelte
│               │   ├── AttributeTypeSelector.svelte
│               │   ├── OptionsList.svelte
│               │   ├── OptionsList.test.ts
│               │   └── AttributeCardFooter.svelte
│               │
│               ├── Shared/                    # NEW: Shared components
│               │   ├── UnsavedChangesModal.svelte
│               │   ├── UnsavedChangesModal.test.ts
│               │   ├── ConfirmDeleteModal.svelte
│               │   ├── SaveButton.svelte      # With spinner state
│               │   └── DragHandle.svelte
│               │
│               └── stores/                    # NEW: State management
│                   ├── editorStore.ts         # Selection, dirty state
│                   └── editorStore.test.ts
│
└── tests/
    └── integration/
        └── service-definition-editor.spec.ts  # Playwright tests
```

**Structure Decision**: Web application with frontend-only changes. New components organized under `ServiceDefinitionEditor/` following existing patterns. Co-located unit tests per Constitution Principle IV.

## Complexity Tracking

> No violations - table not needed.

## Key Technical Decisions

### 1. State Management Approach

Use Svelte stores for:
- **Selection state**: Currently selected group/service in tree
- **Dirty state**: Per-card unsaved changes tracking
- **Expansion state**: Which attribute card is currently expanded

### 2. Existing API Endpoints (No Backend Changes)

All required endpoints already exist in `JurisdictionAdminController`:

| Operation | Endpoint | Used For |
|-----------|----------|----------|
| List groups | `GET /api/jurisdiction-admin/groups` | Tree panel data |
| Create group | `POST /api/jurisdiction-admin/groups` | "+ Group" button |
| Update group | `PATCH /api/jurisdiction-admin/groups/{id}` | Group name save |
| Delete group | `DELETE /api/jurisdiction-admin/groups/{id}` | Group delete |
| Create service | `POST /api/jurisdiction-admin/services` | "+ Add Svc" button |
| Update service | `PATCH /api/jurisdiction-admin/services/{code}` | Header card save |
| Delete service | `DELETE /api/jurisdiction-admin/services/{code}` | Service delete |
| Reorder services | `PATCH /api/jurisdiction-admin/groups/{id}/services-order` | Tree drag-drop |
| Create attribute | `POST /api/jurisdiction-admin/services/{code}/attributes` | Add question |
| Update attribute | `PATCH /api/jurisdiction-admin/services/{code}/attributes/{id}` | Attribute save |
| Delete attribute | `DELETE /api/jurisdiction-admin/services/{code}/attributes/{id}` | Attribute delete |
| Reorder attributes | `PATCH /api/jurisdiction-admin/services/{code}/attributes-order` | Card drag-drop |

### 3. Component Reuse

Existing components to leverage:
- `SaveToast.svelte` - Already exists for save confirmations
- `DragAndDrop.svelte` - Existing drag-drop implementation (adapt for new use)
- STWUI components: `Card`, `Button`, `Input`, `Toggle`, `Modal`, `Breadcrumbs`

### 4. Accessibility Implementation

Per WCAG 2.2 AA and Constitution Principle IX:
- Keyboard navigation for all interactive elements
- Focus management when cards expand/collapse
- ARIA labels for tree structure (`role="tree"`, `role="treeitem"`)
- Skip links for navigation
- Minimum 4.5:1 contrast ratios
- Respect `prefers-reduced-motion` for animations
