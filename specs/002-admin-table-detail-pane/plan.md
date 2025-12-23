# Implementation Plan: Admin Table View with Contextual Detail Pane

**Branch**: `002-admin-table-detail-pane` | **Date**: 2025-12-23 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/002-admin-table-detail-pane/spec.md`

## Summary

Refactor the admin service request table view to remove the redundant left sidebar card list and display the table at full width. When a row is selected, a detail pane (reusing the existing `ServiceRequest` component) slides in from the left at a fixed 400px width, creating a split view with the table. This is a frontend-only change requiring layout restructuring in SvelteKit with no backend modifications.

## Technical Context

**Language/Version**: TypeScript/JavaScript (SvelteKit)
**Primary Dependencies**: SvelteKit, STWUI, Tailwind CSS, Svelte transitions
**Storage**: N/A (frontend-only, data fetched via existing API)
**Testing**: Vitest (unit), Playwright (integration)
**Target Platform**: Web (desktop and mobile browsers)
**Project Type**: Web application (frontend changes only)
**Performance Goals**: Smooth animations (<300ms transitions), instant row selection feedback
**Constraints**: Maintain existing filter, CSV download, and update/delete functionality
**Scale/Scope**: ~5 frontend files modified, 1 new component created

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Open311 Compliance | N/A | Frontend UI change only, no API modifications |
| II. Layered Architecture | PASS | No backend changes required |
| III. Security First | PASS | Existing permission guards (`AuthGuard`) preserved |
| IV. Test Coverage | PASS | Will add co-located unit tests, Playwright integration tests |
| V. Dual Environment Support | PASS | No environment-specific changes |
| VI. Database Flexibility | N/A | No database changes |
| VII. Minimal Dependencies | PASS | Using existing STWUI, Svelte transitions, Tailwind |
| VIII. Consistent Formatting | PASS | Will follow Prettier configuration |

**Gate Result**: PASS - No constitution violations

## Project Structure

### Documentation (this feature)

```text
specs/002-admin-table-detail-pane/
├── spec.md              # Feature specification
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output (minimal - UI state only)
├── quickstart.md        # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
frontend/
├── src/
│   ├── routes/
│   │   └── issues/
│   │       └── table/
│   │           ├── +layout.svelte           # MODIFY: Remove SideBarMainContentLayout, add conditional split view
│   │           ├── +page.svelte             # MODIFY: Remove card list, keep slot for detail pane
│   │           ├── [issue_id]/
│   │           │   └── +page.svelte         # MODIFY: Render in detail pane slot instead of sidebar
│   │           └── table.ts                 # NO CHANGE: Table column configuration
│   └── lib/
│       ├── components/
│       │   ├── SideBarMainContentLayout.svelte  # NO CHANGE: Keep for other views using sidebar
│       │   ├── TableWithDetailPane.svelte       # NEW: Conditional split layout component
│       │   │   └── TableWithDetailPane.test.ts  # NEW: Co-located unit tests
│       │   ├── ServiceRequest.svelte            # NO CHANGE: Reuse in detail pane
│       │   └── ServiceRequestPreview.svelte     # NO CHANGE: Keep for list view (not used in table)
│       └── context/
│           └── ServiceRequestsContext.ts        # MODIFY: Add detail pane open/close state if needed
└── tests/
    └── integration/
        └── admin-table-detail-pane.spec.ts      # NEW: Playwright integration tests
```

**Structure Decision**: Web application frontend-only modification. Create a new reusable layout component (`TableWithDetailPane`) to encapsulate the split view behavior, modifying existing route files to use it.

## Complexity Tracking

No constitution violations requiring justification.

## Implementation Approach

### Phase 1: Layout Refactoring (P1 User Stories)

1. **Create `TableWithDetailPane.svelte` component**
   - Props: `detailPaneOpen: boolean`, `detailPaneWidth: string = '400px'`
   - Slots: `table`, `detail-pane`
   - CSS Grid: `grid-template-columns: {open ? '400px auto' : '100%'}`
   - Svelte `slide` transition for pane animation
   - Media query: At <769px, detail replaces table entirely

2. **Modify `+layout.svelte`**
   - Replace `SideBarMainContentLayout` with `TableWithDetailPane`
   - Pass detail pane open state based on route (`$page.params.issue_id`)
   - Move table content to `table` slot
   - Move detail content to `detail-pane` slot via `<slot>`

3. **Modify `+page.svelte` (table route)**
   - Remove card list rendering entirely
   - Return empty or null (table is in layout)

4. **Modify `[issue_id]/+page.svelte`**
   - Add "Close" button that navigates to `/issues/table` (clears selection)
   - Keep existing `ServiceRequest` component with all functionality

### Phase 2: Preserve Existing Functionality (P2-P3 User Stories)

1. **Update/Delete functionality**
   - Already implemented in `ServiceRequest.svelte`
   - Ensure navigation after delete goes to `/issues/table` (already does)
   - After update, refresh table data via context

2. **Filter and CSV functionality**
   - Already implemented in `+layout.svelte`
   - Ensure filters remain visible when detail pane is open
   - CSV download button position unchanged (bottom right of table)

### Phase 3: Mobile Responsive (P4 User Story)

1. **Media query breakpoint at 768px**
   - Below breakpoint: Detail pane replaces table entirely
   - Show "Back" button prominently in mobile detail view
   - Hide filter controls, search, CSV download on mobile

### Key Technical Decisions

1. **Component reuse**: Existing `ServiceRequest` component is feature-complete and will be reused without modification in the detail pane.

2. **State management**: Use SvelteKit's URL-based routing to determine open/closed state:
   - `/issues/table` = table only (full width)
   - `/issues/table/[issue_id]` = split view with detail pane

3. **Animation**: Use Svelte's built-in `slide` transition for smooth pane animation (already used in filter panel).

4. **Row highlight**: Existing `#selected` CSS already handles row highlighting via `resolveStyleId()` function.
