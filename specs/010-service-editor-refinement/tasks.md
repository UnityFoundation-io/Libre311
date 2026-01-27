# Tasks: Service Definition Editor Refinement

**Input**: Design documents from `/specs/010-service-editor-refinement/`
**Prerequisites**: plan.md ✓, spec.md ✓, research.md ✓, data-model.md ✓, contracts/ ✓

**Tests**: Unit tests co-located per Constitution Principle IV. No separate test tasks - tests built with each component.

**Organization**: Tasks organized for incremental approach: Service Editor → TreePanel → Group Editing

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Frontend**: `frontend/src/lib/components/ServiceDefinitionEditor/`
- **Route**: `frontend/src/routes/groups/config/`
- **Tests**: Co-located with components (`.test.ts` files)

---

## Phase 1: Setup (Shared Infrastructure) ✅ COMPLETE

**Purpose**: Establish directory structure and shared type definitions

- [x] T001 Create component directory structure per plan in `frontend/src/lib/components/ServiceDefinitionEditor/`
- [x] T002 [P] Update type definitions with EditorState, CardId, SelectionType in `frontend/src/lib/components/ServiceDefinitionEditor/stores/types.ts`
- [x] T003 [P] Create route directory and placeholder page at `frontend/src/routes/groups/config/+page.svelte`

---

## Phase 2: Foundational (Blocking Prerequisites) ✅ COMPLETE

**Purpose**: Core state management and shared components that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T004 Update editorStore with dirty tracking, saving states, and selection management in `frontend/src/lib/components/ServiceDefinitionEditor/stores/editorStore.ts`
- [x] T005 [P] Create UnsavedChangesModal component with Discard/Save options in `frontend/src/lib/components/ServiceDefinitionEditor/Shared/UnsavedChangesModal.svelte`
- [x] T006 [P] Create ConfirmDeleteModal component in `frontend/src/lib/components/ServiceDefinitionEditor/Shared/ConfirmDeleteModal.svelte`
- [x] T007 [P] Create SaveButton component with spinner state in `frontend/src/lib/components/ServiceDefinitionEditor/Shared/SaveButton.svelte`
- [x] T008 [P] Create DragHandle component (:: icon) in `frontend/src/lib/components/ServiceDefinitionEditor/Shared/DragHandle.svelte`

**Checkpoint**: Foundation ready - Service Editor implementation can now begin ✅

---

## Phase 3: User Story 2 - Edit Service Header Information (Priority: P1) ✅ COMPLETE

**Goal**: Administrators can modify service name and description through a header card with explicit save

**Independent Test**: Select a service, modify name/description, click Save, verify toast and tree update

### Implementation for User Story 2

- [x] T009 [US2] Create ServiceHeaderCard component with purple top border, name/description inputs in `frontend/src/lib/components/ServiceDefinitionEditor/ServiceEditor/ServiceHeaderCard.svelte`
- [x] T010 [US2] Implement dirty tracking for header card (enable Save button on changes) in ServiceHeaderCard.svelte
- [x] T011 [US2] Implement Save handler calling updateService API with spinner feedback in ServiceHeaderCard.svelte
- [x] T012 [US2] Implement Cancel handler reverting to original values in ServiceHeaderCard.svelte
- [x] T013 [US2] Create unit test for ServiceHeaderCard in `frontend/src/lib/components/ServiceDefinitionEditor/ServiceEditor/ServiceHeaderCard.test.ts`

**Checkpoint**: Service header editing functional - can edit name/description with explicit save ✅

---

## Phase 4: User Story 3 - Edit Attribute Cards (Priority: P1) ✅ COMPLETE

**Goal**: Administrators can edit attributes using expandable cards with accordion behavior

**Independent Test**: Click attribute card to expand, modify question text, change type, save changes

### Implementation for User Story 3

- [x] T014 [P] [US3] Create AttributeCard container component managing collapsed/expanded states in `frontend/src/lib/components/ServiceDefinitionEditor/AttributeCard/AttributeCard.svelte`
- [x] T015 [P] [US3] Create AttributeCardCollapsed showing question text, type indicator, required asterisk in `frontend/src/lib/components/ServiceDefinitionEditor/AttributeCard/AttributeCardCollapsed.svelte`
- [x] T016 [US3] Create AttributeCardExpanded with full editing controls in `frontend/src/lib/components/ServiceDefinitionEditor/AttributeCard/AttributeCardExpanded.svelte`
- [x] T017 [US3] Create AttributeTypeSelector dropdown (Short answer, Paragraph, Multiple choice, Dropdown, Number, Date) in `frontend/src/lib/components/ServiceDefinitionEditor/AttributeCard/AttributeTypeSelector.svelte`
- [x] T018 [US3] Create AttributeCardFooter with Cancel, Save, Copy, Delete, Required toggle in `frontend/src/lib/components/ServiceDefinitionEditor/AttributeCard/AttributeCardFooter.svelte`
- [x] T019 [US3] Implement accordion behavior (only one card expanded at a time) in AttributeCard.svelte
- [x] T020 [US3] Implement More Options menu with datatypeDescription help text field in AttributeCardExpanded.svelte
- [x] T021 [US3] Create unit test for AttributeCard in `frontend/src/lib/components/ServiceDefinitionEditor/AttributeCard/AttributeCard.test.ts`

**Checkpoint**: Attribute card editing functional with accordion behavior ✅

---

## Phase 5: User Story 4 - Manage List Options (Priority: P1) ✅ COMPLETE

**Goal**: Administrators can add, edit, and remove options for dropdown/multiple choice attributes

**Independent Test**: Create Multiple Choice attribute, add options, edit text, delete options (except last)

### Implementation for User Story 4

- [x] T022 [US4] Create OptionsList component with checkbox ([ ]) or circle (O) indicators in `frontend/src/lib/components/ServiceDefinitionEditor/AttributeCard/OptionsList.svelte`
- [x] T023 [US4] Implement Add option button with auto-focus on new row in OptionsList.svelte
- [x] T024 [US4] Implement option deletion with disabled state for last remaining option in OptionsList.svelte
- [x] T025 [US4] Create unit test for OptionsList in `frontend/src/lib/components/ServiceDefinitionEditor/AttributeCard/OptionsList.test.ts`

**Checkpoint**: List-type attribute options fully manageable ✅

---

## Phase 6: User Story 5 - Reorder Attributes via Drag and Drop (Priority: P2) ✅ COMPLETE

**Goal**: Administrators can change attribute card order by dragging

**Independent Test**: Grab drag handle, drag card to new position, save, verify order persists

### Implementation for User Story 5

- [x] T026 [US5] Create AttributeCardList container managing drag-drop reordering in `frontend/src/lib/components/ServiceDefinitionEditor/ServiceEditor/AttributeCardList.svelte`
- [x] T027 [US5] Implement drag start/end handlers with 50% opacity styling in AttributeCardList.svelte
- [x] T028 [US5] Implement drop target indicators (purple border) in AttributeCardList.svelte
- [x] T029 [US5] Implement reorder API call (PATCH attributes-order) on drop in AttributeCardList.svelte
- [x] T030 [US5] Create unit test for AttributeCardList in `frontend/src/lib/components/ServiceDefinitionEditor/ServiceEditor/AttributeCardList.test.ts`

**Checkpoint**: Attribute reordering via drag-drop functional ✅

---

## Phase 7: User Story 6 - Copy and Delete Attributes (Priority: P2) ✅ COMPLETE

**Goal**: Administrators can duplicate or delete attributes

**Independent Test**: Click Copy on attribute, verify duplicate appears. Click Delete, confirm dialog, verify removal

### Implementation for User Story 6

- [x] T031 [US6] Implement Copy handler creating duplicate with "(copy)" suffix in AttributeCardFooter.svelte
- [x] T032 [US6] Implement Delete handler showing ConfirmDeleteModal in AttributeCardFooter.svelte
- [x] T033 [US6] Implement delete API call and removal animation on confirm in AttributeCard.svelte

**Checkpoint**: Attribute copy/delete functional with confirmation ✅

---

## Phase 8: User Story 10 - Unsaved Changes Protection (Priority: P2) ✅ COMPLETE

**Goal**: Warn administrators when navigating away from unsaved changes

**Independent Test**: Make changes, select different item in tree, verify modal appears with Save/Discard options

### Implementation for User Story 10

- [x] T034 [US10] Add unsaved changes check to selection change handlers in editorStore.ts
- [x] T035 [US10] Integrate UnsavedChangesModal trigger before navigation in ServiceHeaderCard.svelte
- [x] T036 [US10] Integrate UnsavedChangesModal trigger before navigation in AttributeCard.svelte
- [x] T037 [US10] Implement Save then navigate flow in UnsavedChangesModal.svelte
- [x] T038 [US10] Implement Discard and navigate flow in UnsavedChangesModal.svelte
- [x] T039 [US10] Create unit test for UnsavedChangesModal in `frontend/src/lib/components/ServiceDefinitionEditor/Shared/UnsavedChangesModal.test.ts`

**Checkpoint**: Unsaved changes protection prevents accidental data loss ✅

---

## Phase 9: User Story 1 - Navigate Service Groups and Services (Priority: P1) ✅ COMPLETE

**Goal**: Administrators can navigate hierarchical structure using tree panel

**Independent Test**: View tree on page load, expand/collapse groups, select service to load editor

### Implementation for User Story 1

- [x] T040 [P] [US1] Create SplitPaneLayout container component with left/right panels in `frontend/src/lib/components/ServiceDefinitionEditor/SplitPaneEditor/SplitPaneLayout.svelte`
- [x] T041 [P] [US1] Create TreePanel component with ARIA tree role in `frontend/src/lib/components/ServiceDefinitionEditor/SplitPaneEditor/TreePanel.svelte`
- [x] T042 [US1] Create TreeGroup component with expand/collapse, service count badge in `frontend/src/lib/components/ServiceDefinitionEditor/TreeView/TreeGroup.svelte`
- [x] T043 [US1] Create TreeService component with selection highlight (purple border, blue tint) in `frontend/src/lib/components/ServiceDefinitionEditor/TreeView/TreeService.svelte`
- [x] T044 [US1] Implement keyboard navigation (Arrow keys, Enter/Space) in TreePanel.svelte
- [x] T045 [US1] Create EditorPanel component handling empty/group/service states in `frontend/src/lib/components/ServiceDefinitionEditor/SplitPaneEditor/EditorPanel.svelte`
- [x] T046 [US1] Integrate TreePanel selection with EditorPanel content in SplitPaneLayout.svelte
- [x] T047 [US1] Create unit test for TreePanel in `frontend/src/lib/components/ServiceDefinitionEditor/SplitPaneEditor/TreePanel.test.ts`
- [x] T048 [US1] Create unit test for TreeGroup in `frontend/src/lib/components/ServiceDefinitionEditor/TreeView/TreeGroup.test.ts`
- [x] T049 [US1] Create unit test for TreeService in `frontend/src/lib/components/ServiceDefinitionEditor/TreeView/TreeService.test.ts`

**Checkpoint**: Tree navigation functional with keyboard support ✅

---

## Phase 10: User Story 7 - Reorder Services Within Groups (Priority: P2) ✅ COMPLETE

**Goal**: Administrators can reorder services via drag-drop in tree panel

**Independent Test**: Drag service to new position within group or to different group, verify order persists

### Implementation for User Story 7

- [x] T050 [US7] Add drag handle (::) to TreeService component in TreeService.svelte
- [x] T051 [US7] Implement drag-drop handlers in TreePanel for service reordering in TreePanel.svelte
- [x] T052 [US7] Implement drop indicators (purple insertion line with dot) in TreePanel.svelte
- [x] T053 [US7] Implement reorder API call (PATCH services-order) on drop in TreePanel.svelte
- [x] T054 [US7] Implement move service between groups on cross-group drop in TreePanel.svelte

**Checkpoint**: Service reordering in tree functional ✅

---

## Phase 11: User Story 8 - Create and Edit Groups (Priority: P3)

**Goal**: Administrators can create new groups and edit group names

**Independent Test**: Click "+ Group", enter name, save, verify tree updates

### Implementation for User Story 8

- [x] T055 [US8] Add "+ Group" button to TreePanel header in TreePanel.svelte
- [x] T056 [US8] Create GroupEditor component with name field, Save/Delete buttons in `frontend/src/lib/components/ServiceDefinitionEditor/GroupEditor/GroupEditor.svelte`
- [x] T057 [US8] Implement create group handler (POST /groups) in TreePanel.svelte
- [x] T058 [US8] Implement update group handler (PATCH /groups/{id}) in GroupEditor.svelte
- [x] T059 [US8] Implement delete group handler with empty check (DELETE /groups/{id}) in GroupEditor.svelte
- [ ] T060 [US8] Create unit test for GroupEditor in `frontend/src/lib/components/ServiceDefinitionEditor/GroupEditor/GroupEditor.test.ts`

**Checkpoint**: Group creation and editing functional (T060 pending)

---

## Phase 12: User Story 9 - Add New Service to Group (Priority: P3) ✅ COMPLETE

**Goal**: Administrators can add new services to groups

**Independent Test**: Click "+ Add Svc" in group, fill in name, save, verify service appears in tree

### Implementation for User Story 9

- [x] T061 [US9] Add "+ Add Svc" button to each expanded TreeGroup in TreeGroup.svelte
- [x] T062 [US9] Implement create service handler (POST /services) in TreeGroup.svelte
- [x] T063 [US9] Add "+ Add question" card at bottom of AttributeCardList in AttributeCardList.svelte
- [x] T064 [US9] Implement create attribute handler with Short answer default in AttributeCardList.svelte
- [x] T065 [US9] Implement auto-expand and focus for newly created attribute in AttributeCard.svelte

**Checkpoint**: Service and attribute creation functional ✅

---

## Phase 13: Polish & Cross-Cutting Concerns ✅ COMPLETE

**Purpose**: Final integration, accessibility audit, and cleanup

- [x] T066 [P] Integrate SplitPaneLayout into route page at `frontend/src/routes/groups/config/+page.svelte`
- [x] T067 [P] Add page header "Service Definition Configuration" with back navigation in +page.svelte
- [x] T068 Implement focus management when cards expand/collapse for accessibility
- [x] T069 Add ARIA labels for all interactive elements per WCAG 2.2 AA
- [x] T070 Add `prefers-reduced-motion` checks for all animations
- [x] T071 [P] Verify 4.5:1 contrast ratios for all text elements
- [x] T072 Run `npm run format && npm run lint && npm run check` and fix any issues
- [x] T073 Run `npm run test:unit` and verify all tests pass
- [ ] T074 Manual testing following quickstart.md scenarios

**Checkpoint**: Core accessibility and integration complete

---

## Phase 14: Usability Testing & Validation ✅ COMPLETE

**Purpose**: Manual accessibility validation, usability verification for Success Criteria, and edge case testing

### Accessibility Validation (Manual)

- [x] T075 Manual screen reader testing with VoiceOver (macOS) for tree navigation flow
- [x] T076 Manual screen reader testing with NVDA/VoiceOver for attribute card editing flow
- [x] T077 Document accessibility test results in `specs/010-service-editor-refinement/accessibility-audit.md`

### Usability Validation (Success Criteria)

- [x] T078 [P] Verify responsive layout at 1024px minimum viewport width per SC-007
- [x] T079 [P] Test long question text wrapping (100+ characters) does not break layout per edge case spec
- [x] T080 Usability validation: Time new administrator adding attribute (target: first-attempt success per SC-006)
- [x] T081 Document usability test results and any discovered issues in `specs/010-service-editor-refinement/usability-audit.md`

**Checkpoint**: Accessibility validated, usability metrics confirmed ✅

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - start immediately
- **Foundational (Phase 2)**: Depends on Setup - BLOCKS all user stories
- **Service Editor (Phases 3-8)**: Depends on Foundational - Core editing functionality
- **Tree Navigation (Phases 9-10)**: Depends on Foundational - Can parallel with Service Editor
- **Group Management (Phases 11-12)**: Depends on Tree Navigation components
- **Polish (Phase 13)**: Depends on all features complete
- **Usability & Accessibility Testing (Phase 14)**: Depends on Polish - Final validation gate

### User Story Dependencies (within incremental approach)

| User Story | Depends On | Notes |
|------------|------------|-------|
| US2 (Header) | Foundational | First editor component |
| US3 (Attributes) | US2 | Uses same card patterns |
| US4 (Options) | US3 | Part of attribute editing |
| US5 (Reorder Attrs) | US3 | Requires attribute cards |
| US6 (Copy/Delete) | US3 | Requires attribute cards |
| US10 (Unsaved) | US2, US3 | Integrates with both |
| US1 (Navigation) | Foundational | Can start after Foundational |
| US7 (Reorder Svcs) | US1 | Requires tree components |
| US8 (Groups) | US1 | Requires tree + editor panel |
| US9 (Add Service) | US1, US3 | Requires tree + attribute cards |

### Parallel Opportunities

**Within Service Editor (after Foundational)**:
```bash
# These can run in parallel:
Task T014: AttributeCard container
Task T015: AttributeCardCollapsed
```

**Tree Components (after Foundational)**:
```bash
# These can run in parallel:
Task T040: SplitPaneLayout
Task T041: TreePanel
```

**Usability Testing (Phase 14)**:
```bash
# These can run in parallel:
Task T078: Responsive layout testing (1024px)
Task T079: Long text wrapping testing
```

---

## Implementation Strategy

### MVP First (Service Editor Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all)
3. Complete Phases 3-5: US2, US3, US4 (Header + Attributes + Options)
4. **STOP and VALIDATE**: Test Service Editor in isolation
5. Can demo core editing functionality

### Incremental Delivery (User's Requested Approach)

1. **Setup + Foundational** → Infrastructure ready
2. **Service Editor (US2, US3, US4, US5, US6, US10)** → Full attribute editing with save protection
3. **Tree Navigation (US1, US7)** → Complete navigation with drag-drop
4. **Group Management (US8, US9)** → Full CRUD for groups and services
5. **Polish** → Accessibility, final integration
6. **Usability & Accessibility Testing** → Constitution IX compliance, success criteria validation

### Integration Points

- Service Editor integrates via EditorPanel conditional rendering
- Tree selection drives EditorPanel content via editorStore
- Save operations update tree display via shared state
- Unsaved changes modal gates all navigation

---

## Notes

- Existing components to leverage: SaveToast, EditMultiValueList patterns, DragAndDrop patterns
- STWUI components: Card, Button, Input, Toggle, Modal, Dropdown
- All API endpoints exist - no backend changes needed
- Co-located tests required per Constitution Principle IV
- Run `npm run format` before commits per Constitution Principle VIII
