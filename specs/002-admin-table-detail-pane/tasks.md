# Tasks: Admin Table View with Contextual Detail Pane

**Input**: Design documents from `/specs/002-admin-table-detail-pane/`
**Prerequisites**: plan.md ✓, spec.md ✓, research.md ✓, data-model.md ✓, quickstart.md ✓

**Tests**: Included (unit tests co-located, integration tests for key stories)

**Organization**: Tasks grouped by user story for independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2)
- Include exact file paths in descriptions

---

## Phase 1: Setup

**Purpose**: Verify existing project structure and dependencies

- [X] T001 Verify frontend dependencies are installed in frontend/package.json
- [X] T002 Verify Svelte transitions and STWUI are available in frontend/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Create core layout component that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T003 Create TableWithDetailPane.svelte component in frontend/src/lib/components/TableWithDetailPane.svelte
- [X] T004 [P] Create co-located unit test file in frontend/src/lib/components/TableWithDetailPane.test.ts
- [X] T005 Implement CSS Grid layout with conditional columns in frontend/src/lib/components/TableWithDetailPane.svelte
- [X] T006 Add Svelte slide transition for detail pane animation in frontend/src/lib/components/TableWithDetailPane.svelte
- [X] T007 Define component props (detailPaneOpen, detailPaneWidth) and slots (table, detail-pane) in frontend/src/lib/components/TableWithDetailPane.svelte

**Checkpoint**: TableWithDetailPane component ready - user story implementation can now begin ✅

---

## Phase 3: User Story 1 - View Service Requests in Full-Width Table (Priority: P1) 🎯 MVP

**Goal**: Remove redundant card list and display table at full width when no request is selected

**Independent Test**: Navigate to /issues/table and verify card list is removed, table spans full available width

### Implementation for User Story 1

- [X] T008 [US1] Modify +layout.svelte to replace SideBarMainContentLayout with TableWithDetailPane in frontend/src/routes/issues/table/+layout.svelte
- [X] T009 [US1] Remove card list rendering from +page.svelte in frontend/src/routes/issues/table/+page.svelte
- [X] T010 [US1] Move table content to table slot in layout in frontend/src/routes/issues/table/+layout.svelte
- [X] T011 [US1] Ensure table displays full-width when $page.params.issue_id is undefined in frontend/src/routes/issues/table/+layout.svelte
- [X] T012 [US1] Verify column alignment and scroll behavior in full-width table

**Checkpoint**: User Story 1 complete - table displays full-width without card list ✅

---

## Phase 4: User Story 2 - Select Row to Open Detail Pane (Priority: P1)

**Goal**: Enable row selection to open sliding detail pane with split view

**Independent Test**: Click any table row and verify detail pane slides in from left, row is highlighted, split view displays

### Implementation for User Story 2

- [X] T013 [US2] Configure detail pane slot to receive child route content in frontend/src/routes/issues/table/+layout.svelte
- [X] T014 [US2] Pass detailPaneOpen prop based on $page.params.issue_id presence in frontend/src/routes/issues/table/+layout.svelte
- [X] T015 [US2] Verify ServiceRequest component renders in detail-pane slot in frontend/src/routes/issues/table/[issue_id]/+page.svelte
- [X] T016 [US2] Verify row highlighting via existing #selected CSS styling
- [X] T017 [US2] Test split view layout with 400px detail pane and remaining table width

**Checkpoint**: User Story 2 complete - clicking row opens detail pane in split view ✅

---

## Phase 5: User Story 3 - Close Detail Pane to Return to Full-Width Table (Priority: P2)

**Goal**: Allow admin to close detail pane and return to full-width table view

**Independent Test**: Open detail pane, click Close button, verify table returns to full-width and row is deselected

### Implementation for User Story 3

- [X] T018 [US3] Add Close button to detail pane header in frontend/src/routes/issues/table/[issue_id]/+page.svelte
- [X] T019 [US3] Implement Close button navigation to /issues/table in frontend/src/routes/issues/table/[issue_id]/+page.svelte
- [X] T020 [US3] Verify row deselection styling when navigating away from [issue_id] route
- [X] T021 [US3] Verify detail pane closes with slide-out animation

**Checkpoint**: User Story 3 complete - Close button returns to full-width table ✅

---

## Phase 6: User Story 4 - Update Service Request from Detail Pane (Priority: P2)

**Goal**: Enable editing and updating service request fields from detail pane

**Independent Test**: Select request, click Update, modify fields, save, verify changes persist in detail pane and table

### Implementation for User Story 4

- [X] T022 [US4] Verify existing Update functionality works in new detail pane context in frontend/src/lib/components/ServiceRequest.svelte
- [X] T023 [US4] Ensure table data refreshes after update via context in frontend/src/lib/context/ServiceRequestsContext.ts
- [X] T024 [US4] Verify detail pane stays open after successful update (optional UX enhancement per research.md)

**Checkpoint**: User Story 4 complete - Update functionality works in detail pane

---

## Phase 7: User Story 5 - Delete Service Request from Detail Pane (Priority: P2)

**Goal**: Enable deletion of service request with confirmation from detail pane

**Independent Test**: Select request, click Delete, confirm, verify request removed from table and detail pane closes

### Implementation for User Story 5

- [ ] T025 [US5] Verify existing Delete functionality works in new detail pane context in frontend/src/lib/components/ServiceRequest.svelte
- [ ] T026 [US5] Verify delete navigates to /issues/table and closes detail pane
- [ ] T027 [US5] Verify table refreshes to remove deleted request

**Checkpoint**: User Story 5 complete - Delete functionality works in detail pane

---

## Phase 8: User Story 6 - Filter Service Requests (Priority: P3)

**Goal**: Preserve existing filter functionality with new layout

**Independent Test**: Apply various filter combinations, verify table displays only matching requests, filters persist when detail pane is open

### Implementation for User Story 6

- [ ] T028 [US6] Ensure filter panel remains visible in table area when detail pane is open in frontend/src/routes/issues/table/+layout.svelte
- [ ] T029 [US6] Verify filter icon and panel toggle work with new layout
- [ ] T030 [US6] Verify filters persist when opening/closing detail pane
- [ ] T031 [US6] Handle edge case: close detail pane when selected request is filtered out

**Checkpoint**: User Story 6 complete - Filter functionality preserved

---

## Phase 9: User Story 7 - Download Filtered Results as CSV (Priority: P3)

**Goal**: Preserve existing CSV download functionality with new layout

**Independent Test**: Apply filters, click Download CSV, verify downloaded file contains filtered data

### Implementation for User Story 7

- [ ] T032 [US7] Verify CSV download button position (bottom right of table) works with new layout in frontend/src/routes/issues/table/+layout.svelte
- [ ] T033 [US7] Verify CSV download includes currently filtered results
- [ ] T034 [US7] Verify CSV download works when detail pane is open

**Checkpoint**: User Story 7 complete - CSV download functionality preserved

---

## Phase 10: User Story 8 - Mobile/Small Screen Table View (Priority: P4)

**Goal**: Provide simplified mobile experience with full-screen detail view

**Independent Test**: On mobile-sized screen, tap row, verify detail replaces table entirely, Back button returns to table

### Implementation for User Story 8

- [ ] T035 [US8] Add media query breakpoint at 768px in frontend/src/lib/components/TableWithDetailPane.svelte
- [ ] T036 [US8] Implement full-screen detail mode on mobile (replace table instead of split view) in frontend/src/lib/components/TableWithDetailPane.svelte
- [ ] T037 [US8] Ensure Back/Close button is prominently displayed on mobile in frontend/src/routes/issues/table/[issue_id]/+page.svelte
- [ ] T038 [US8] Hide filter controls, search, and CSV download on mobile per FR-015 in frontend/src/routes/issues/table/+layout.svelte
- [ ] T039 [US8] Handle viewport resize transitions between mobile and desktop breakpoints

**Checkpoint**: User Story 8 complete - Mobile view functional

---

## Phase 11: Polish & Cross-Cutting Concerns

**Purpose**: Integration testing, validation, and final polish

- [ ] T040 [P] Create Playwright integration test file in frontend/tests/integration/admin-table-detail-pane.spec.ts
- [ ] T041 [P] Add integration test: full-width table when no selection
- [ ] T042 [P] Add integration test: detail pane opens on row click
- [ ] T043 [P] Add integration test: close button returns to full-width
- [ ] T044 [P] Add integration test: update and delete in detail pane
- [ ] T045 [P] Add integration test: filter persistence with detail pane
- [ ] T046 Run npm run check for TypeScript validation in frontend/
- [ ] T047 Run npm run lint and npm run format in frontend/
- [ ] T048 Run quickstart.md verification checklist
- [ ] T049 Test edge cases from spec.md (zero results, concurrent updates, viewport resize)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup - BLOCKS all user stories
- **User Stories (Phase 3-10)**: All depend on Foundational phase completion
  - US1 + US2 (both P1): Form the core MVP - implement sequentially
  - US3, US4, US5 (all P2): Depend on US2 being complete
  - US6, US7 (both P3): Can proceed after US1 (preserving existing functionality)
  - US8 (P4): Depends on US1 + US2 for mobile adaptation
- **Polish (Phase 11)**: Depends on all user stories being complete

### User Story Dependencies

```
Foundational (T003-T007)
    │
    ├─► US1: Full-Width Table (T008-T012)
    │       │
    │       └─► US2: Open Detail Pane (T013-T017) ──► US3: Close Detail Pane (T018-T021)
    │               │                                         │
    │               ├─► US4: Update (T022-T024) ◄─────────────┤
    │               │                                         │
    │               └─► US5: Delete (T025-T027) ◄─────────────┘
    │
    ├─► US6: Filters (T028-T031) - Can start after US1
    │
    ├─► US7: CSV Download (T032-T034) - Can start after US1
    │
    └─► US8: Mobile (T035-T039) - Depends on US1 + US2
```

### Within Each User Story

- Foundational component must exist before any layout modifications
- Layout modifications (T008-T011) before interaction testing (T012)
- Core implementation before edge case handling
- Story complete before moving to dependent stories

### Parallel Opportunities

**Foundational Phase:**
- T003 and T004 can run in parallel (component file + test file)

**User Story Phase:**
- US6 and US7 can run in parallel (both preserve existing functionality)
- US4 and US5 can run in parallel (both are detail pane actions)

**Polish Phase:**
- T040-T045 can all run in parallel (separate test files)
- T046-T047 can run in parallel (different lint/check commands)

---

## Parallel Example: Foundational Phase

```bash
# Launch in parallel:
Task: "Create TableWithDetailPane.svelte component in frontend/src/lib/components/TableWithDetailPane.svelte"
Task: "Create co-located unit test file in frontend/src/lib/components/TableWithDetailPane.test.ts"
```

## Parallel Example: Polish Phase

```bash
# Launch all integration tests in parallel:
Task: "Add integration test: full-width table when no selection"
Task: "Add integration test: detail pane opens on row click"
Task: "Add integration test: close button returns to full-width"
Task: "Add integration test: update and delete in detail pane"
Task: "Add integration test: filter persistence with detail pane"
```

---

## Implementation Strategy

### MVP First (User Stories 1 + 2 Only)

1. Complete Phase 1: Setup (verify dependencies)
2. Complete Phase 2: Foundational (create TableWithDetailPane component)
3. Complete Phase 3: User Story 1 (full-width table)
4. Complete Phase 4: User Story 2 (detail pane opens)
5. **STOP and VALIDATE**: Test core split view functionality independently
6. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Component ready
2. Add US1 + US2 → Core experience works → Deploy/Demo (MVP!)
3. Add US3 → Close button works → Deploy/Demo
4. Add US4 + US5 → Update/Delete work → Deploy/Demo
5. Add US6 + US7 → Existing features preserved → Deploy/Demo
6. Add US8 → Mobile support → Deploy/Demo
7. Each story adds value without breaking previous stories

### Suggested MVP Scope

**MVP = User Stories 1 + 2 (both P1)**
- Full-width table when no selection
- Detail pane slides in on row click
- Split view with 400px detail pane

This provides the core new experience. Remaining stories (Close button, Update, Delete, Filters, CSV, Mobile) can be added incrementally.

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- All existing functionality (filters, CSV, update, delete) already works - stories US4-US7 are about verification in new layout context
