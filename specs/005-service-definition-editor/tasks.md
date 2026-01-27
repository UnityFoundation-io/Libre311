# Tasks: Service Definition Editor

**Input**: Design documents from `/specs/005-service-definition-editor/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Unit tests (Vitest, co-located) + Integration tests (Playwright) included per user story.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Frontend**: `frontend/src/` (SvelteKit application)
- **Components**: `frontend/src/lib/components/ServiceDefinitionEditor/`
- **Routes**: `frontend/src/routes/groups/`
- **Unit Tests**: Co-located `*.test.ts` files
- **Integration Tests**: `frontend/tests/service-definition-editor/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create project structure and shared types/stores for the Service Definition Editor

- [X] T001 Create EditorView directory at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/
- [X] T002 Create ListView directory at frontend/src/lib/components/ServiceDefinitionEditor/ListView/
- [X] T003 [P] Create TypeScript types file with AttributeTypeLabel, DATATYPE_MAP, CardState, AttributeCardState, EditorState, SaveStatus at frontend/src/lib/components/ServiceDefinitionEditor/types.ts
- [X] T004 [P] Create editor store with editorState, expandedAttribute, hasUnsavedChanges, saveStatus at frontend/src/lib/components/ServiceDefinitionEditor/stores/editorStore.ts
- [X] T005 [P] Create list view store with ListViewState, GroupListState at frontend/src/lib/components/ServiceDefinitionEditor/stores/listViewStore.ts
- [X] T006 Create integration tests directory at frontend/tests/service-definition-editor/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure components that MUST be complete before ANY user story can be implemented

**CRITICAL**: No user story work can begin until this phase is complete

- [X] T007 Create SaveToast component with success/error states and auto-dismiss at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/SaveToast.svelte
- [X] T008 [P] Create SaveToast unit tests for success, error, and auto-dismiss behavior at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/SaveToast.test.ts
- [X] T009 Create auto-save utility function with 500ms debounce and optimistic updates at frontend/src/lib/components/ServiceDefinitionEditor/utils/autoSave.ts
- [X] T010 [P] Create auto-save utility unit tests for debounce timing and error handling at frontend/src/lib/components/ServiceDefinitionEditor/utils/autoSave.test.ts
- [X] T011 Create EditorContainer layout component with navigation header and scrollable content area at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/EditorContainer.svelte
- [X] T012 [P] Create EditorContainer unit tests for layout and navigation rendering at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/EditorContainer.test.ts

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Browse Service Groups and Services (Priority: P1) MVP

**Goal**: Enable administrators to view all service groups in a collapsible/expandable list with service counts and nested services

**Independent Test**: Navigate to /groups/ and verify groups display with expand/collapse functionality and service counts

### Tests for User Story 1

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T013 [P] [US1] Create integration test for groups list loading and display at frontend/tests/service-definition-editor/browse-groups.spec.ts
- [ ] T014 [P] [US1] Create integration test for group expand/collapse behavior at frontend/tests/service-definition-editor/browse-groups.spec.ts

### Implementation for User Story 1

- [ ] T015 [P] [US1] Create ServiceGroupCard component with expand/collapse toggle, service count badge, and animation at frontend/src/lib/components/ServiceDefinitionEditor/ListView/ServiceGroupCard.svelte
- [ ] T016 [P] [US1] Create ServiceGroupCard unit tests for expand/collapse state and count display at frontend/src/lib/components/ServiceDefinitionEditor/ListView/ServiceGroupCard.test.ts
- [ ] T017 [P] [US1] Create ServiceListRow component with service name, Edit button, and More actions menu at frontend/src/lib/components/ServiceDefinitionEditor/ListView/ServiceListRow.svelte
- [ ] T018 [P] [US1] Create ServiceListRow unit tests for button clicks and navigation at frontend/src/lib/components/ServiceDefinitionEditor/ListView/ServiceListRow.test.ts
- [ ] T019 [US1] Enhance GroupListItem component to use new expand/collapse behavior with Svelte transitions at frontend/src/lib/components/ServiceDefinitionEditor/GroupListItem.svelte
- [ ] T020 [US1] Update groups list page to use ServiceGroupCard and ServiceListRow components at frontend/src/routes/groups/+page.svelte
- [ ] T021 [US1] Add keyboard navigation (Enter to toggle expand/collapse) to ServiceGroupCard at frontend/src/lib/components/ServiceDefinitionEditor/ListView/ServiceGroupCard.svelte
- [ ] T022 [US1] Add ARIA attributes (aria-expanded, aria-controls) for accessibility to ServiceGroupCard at frontend/src/lib/components/ServiceDefinitionEditor/ListView/ServiceGroupCard.svelte

**Checkpoint**: User Story 1 complete - groups browsing with expand/collapse is functional

---

## Phase 4: User Story 2 - Edit Service Request Definition (Priority: P1)

**Goal**: Enable administrators to open a full-page Google Forms-style editor with header card for service name and description

**Independent Test**: Click Edit on any service, verify editor opens with header card showing service name/description, edit and verify auto-save toast appears

### Tests for User Story 2

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T023 [P] [US2] Create integration test for editor page load and header display at frontend/tests/service-definition-editor/edit-service.spec.ts
- [ ] T024 [P] [US2] Create integration test for service name auto-save at frontend/tests/service-definition-editor/edit-service.spec.ts

### Implementation for User Story 2

- [ ] T025 [US2] Create edit route page with route params loading at frontend/src/routes/groups/[group_id]/services/[service_id]/edit/+page.svelte
- [ ] T026 [US2] Create HeaderCard component with colored banner (border-t-8), inline editable service name and description at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/HeaderCard.svelte
- [ ] T027 [P] [US2] Create HeaderCard unit tests for inline editing and auto-save triggers at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/HeaderCard.test.ts
- [ ] T028 [US2] Implement service data loading in edit page using getServiceDefinition API call at frontend/src/routes/groups/[group_id]/services/[service_id]/edit/+page.svelte
- [ ] T029 [US2] Add auto-save functionality for service name changes with debounce and toast feedback at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/HeaderCard.svelte
- [ ] T030 [US2] Add auto-save functionality for service description changes with debounce and toast feedback at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/HeaderCard.svelte
- [ ] T031 [US2] Add back navigation button to return to services list at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/EditorContainer.svelte
- [ ] T032 [US2] Update ServiceListRow Edit button to navigate to /groups/[group_id]/services/[service_id]/edit at frontend/src/lib/components/ServiceDefinitionEditor/ListView/ServiceListRow.svelte

**Checkpoint**: User Story 2 complete - editor view opens with editable header card

---

## Phase 5: User Story 3 - Manage Attribute Cards (Priority: P1)

**Goal**: Enable administrators to view attributes as accordion cards, expand to edit, change type, toggle required status

**Independent Test**: In editor view, click an attribute card to expand, edit question text, change type dropdown, toggle required switch, verify all changes auto-save

### Tests for User Story 3

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T033 [P] [US3] Create integration test for attribute card expand/collapse accordion at frontend/tests/service-definition-editor/manage-attributes.spec.ts
- [ ] T034 [P] [US3] Create integration test for attribute type change at frontend/tests/service-definition-editor/manage-attributes.spec.ts
- [ ] T035 [P] [US3] Create integration test for add new attribute via FAB at frontend/tests/service-definition-editor/manage-attributes.spec.ts

### Implementation for User Story 3

- [ ] T036 [P] [US3] Create AttributeCardCollapsed component showing question preview, type icon, required indicator (*) at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeCardCollapsed.svelte
- [ ] T037 [P] [US3] Create AttributeCardCollapsed unit tests for preview rendering and required indicator at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeCardCollapsed.test.ts
- [ ] T038 [P] [US3] Create AttributeTypeSelector dropdown with Short answer, Paragraph, Multiple choice, Dropdown, Number, Date options at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeTypeSelector.svelte
- [ ] T039 [P] [US3] Create AttributeTypeSelector unit tests for option display and selection events at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeTypeSelector.test.ts
- [ ] T040 [P] [US3] Create AttributeFooter component with Required toggle switch and action buttons area at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeFooter.svelte
- [ ] T041 [P] [US3] Create AttributeFooter unit tests for toggle state and event emission at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeFooter.test.ts
- [ ] T042 [US3] Create AttributeCardExpanded component with question text input, type selector, footer at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeCardExpanded.svelte
- [ ] T043 [P] [US3] Create AttributeCardExpanded unit tests for form controls and auto-save at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeCardExpanded.test.ts
- [ ] T044 [US3] Create AttributeCard accordion wrapper managing collapsed/expanded state transitions at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeCard.svelte
- [ ] T045 [P] [US3] Create AttributeCard unit tests for accordion state transitions at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeCard.test.ts
- [ ] T046 [US3] Implement single-card-expanded-at-a-time logic in editorStore (expandCard, collapseCard actions) at frontend/src/lib/components/ServiceDefinitionEditor/stores/editorStore.ts
- [ ] T047 [P] [US3] Create editorStore unit tests for expand/collapse state management at frontend/src/lib/components/ServiceDefinitionEditor/stores/editorStore.test.ts
- [ ] T048 [US3] Create AddFieldButton floating action button to add new attributes at bottom of editor at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AddFieldButton.svelte
- [ ] T049 [P] [US3] Create AddFieldButton unit tests for click handling and type selection at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AddFieldButton.test.ts
- [ ] T050 [US3] Integrate attribute cards list in edit page with expand/collapse accordion behavior at frontend/src/routes/groups/[group_id]/services/[service_id]/edit/+page.svelte
- [ ] T051 [US3] Add auto-save for attribute question text (description) changes at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeCardExpanded.svelte
- [ ] T052 [US3] Add auto-save for attribute type (datatype) changes at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeCardExpanded.svelte
- [ ] T053 [US3] Add auto-save for attribute required toggle changes at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeFooter.svelte
- [ ] T054 [US3] Add keyboard navigation (Escape to collapse expanded card) to AttributeCard at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeCard.svelte
- [ ] T055 [US3] Add ARIA attributes (aria-expanded, role="button") for accessibility to AttributeCard at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeCard.svelte

**Checkpoint**: User Story 3 complete - full attribute card editing with accordion behavior

---

## Phase 6: User Story 4 - Manage List Options for Multiple Choice Fields (Priority: P2)

**Goal**: Enable administrators to add, edit, and delete options for dropdown and multiple choice attribute types

**Independent Test**: Create/edit a Multiple choice attribute, add option, edit option text, delete option, add "Other" option for free-text

### Tests for User Story 4

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T056 [P] [US4] Create integration test for add/edit/delete options flow at frontend/tests/service-definition-editor/manage-attributes.spec.ts
- [ ] T057 [P] [US4] Create integration test for "Other" option toggle at frontend/tests/service-definition-editor/manage-attributes.spec.ts

### Implementation for User Story 4

- [ ] T058 [US4] Create OptionsList component with option rows, each having text input and delete (X) button at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/OptionsList.svelte
- [ ] T059 [P] [US4] Create OptionsList unit tests for rendering options and delete button at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/OptionsList.test.ts
- [ ] T060 [US4] Add "Add option" button at end of options list that creates new empty option row at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/OptionsList.svelte
- [ ] T061 [US4] Implement option add functionality generating unique key and auto-focusing new row at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/OptionsList.svelte
- [ ] T062 [US4] Implement option edit with inline text editing and auto-save on blur at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/OptionsList.svelte
- [ ] T063 [US4] Implement option delete with immediate removal (prevent if last option) at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/OptionsList.svelte
- [ ] T064 [US4] Add "Add Other option" checkbox/toggle for free-text entry support at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/OptionsList.svelte
- [ ] T065 [US4] Conditionally show OptionsList in AttributeCardExpanded only for multivaluelist/singlevaluelist types at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeCardExpanded.svelte
- [ ] T066 [US4] Add validation preventing deletion of last option with warning message at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/OptionsList.svelte
- [ ] T067 [P] [US4] Create unit test for last option deletion prevention at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/OptionsList.test.ts

**Checkpoint**: User Story 4 complete - list options management fully functional

---

## Phase 7: User Story 5 - Reorder Attributes via Drag and Drop (Priority: P2)

**Goal**: Enable administrators to drag attribute cards to new positions with visual feedback

**Independent Test**: Drag an attribute card by its handle, drop in new position, verify order persists after page reload

### Tests for User Story 5

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T068 [P] [US5] Create integration test for drag and drop reorder with persistence at frontend/tests/service-definition-editor/manage-attributes.spec.ts

### Implementation for User Story 5

- [ ] T069 [US5] Add drag handle element (grip icon) to AttributeCardCollapsed and AttributeCardExpanded at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeCardCollapsed.svelte
- [ ] T070 [US5] Add drag handle element to AttributeCardExpanded at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeCardExpanded.svelte
- [ ] T071 [US5] Integrate existing DragAndDrop component wrapping attribute cards list at frontend/src/routes/groups/[group_id]/services/[service_id]/edit/+page.svelte
- [ ] T072 [US5] Implement handleReorder function calling updateAttributesOrder API on drop at frontend/src/routes/groups/[group_id]/services/[service_id]/edit/+page.svelte
- [ ] T073 [US5] Add visual drop indicator styling (placeholder line/highlight) during drag at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeCard.svelte
- [ ] T074 [US5] Add keyboard-accessible reorder (move up/down with Ctrl+Arrow) to AttributeCard at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeCard.svelte
- [ ] T075 [P] [US5] Create unit test for keyboard reorder key handling at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeCard.test.ts

**Checkpoint**: User Story 5 complete - drag and drop reordering functional

---

## Phase 8: User Story 6 - Duplicate and Delete Attributes (Priority: P2)

**Goal**: Enable administrators to duplicate existing attributes or delete them with confirmation

**Independent Test**: Click duplicate on attribute, verify copy appears with "(copy)" suffix; click delete, confirm, verify attribute removed with animation

### Tests for User Story 6

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T076 [P] [US6] Create integration test for duplicate attribute flow at frontend/tests/service-definition-editor/manage-attributes.spec.ts
- [ ] T077 [P] [US6] Create integration test for delete attribute with confirmation at frontend/tests/service-definition-editor/manage-attributes.spec.ts

### Implementation for User Story 6

- [ ] T078 [US6] Add duplicate icon button to AttributeFooter at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeFooter.svelte
- [ ] T079 [US6] Implement duplicateAttribute function creating copy with "(copy)" appended to description at frontend/src/lib/components/ServiceDefinitionEditor/stores/editorStore.ts
- [ ] T080 [P] [US6] Create unit test for duplicateAttribute store action at frontend/src/lib/components/ServiceDefinitionEditor/stores/editorStore.test.ts
- [ ] T081 [US6] Add delete icon button to AttributeFooter at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeFooter.svelte
- [ ] T082 [US6] Create DeleteConfirmationModal component with cancel/confirm buttons at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/DeleteConfirmationModal.svelte
- [ ] T083 [P] [US6] Create DeleteConfirmationModal unit tests for cancel/confirm actions at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/DeleteConfirmationModal.test.ts
- [ ] T084 [US6] Implement deleteAttribute function with API call and removal animation at frontend/src/lib/components/ServiceDefinitionEditor/stores/editorStore.ts
- [ ] T085 [US6] Add slide-out animation on attribute card deletion using Svelte transitions at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeCard.svelte

**Checkpoint**: User Story 6 complete - duplicate and delete with confirmation working

---

## Phase 9: User Story 7 - Create New Service Group (Priority: P3)

**Goal**: Enable administrators to create new service groups from the groups list

**Independent Test**: Click "+ New Group", enter name, save, verify new empty group appears in list

### Tests for User Story 7

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T086 [P] [US7] Create integration test for new group creation flow at frontend/tests/service-definition-editor/browse-groups.spec.ts

### Implementation for User Story 7

- [ ] T087 [US7] Add "+ New Group" button at top of groups list page at frontend/src/routes/groups/+page.svelte
- [ ] T088 [US7] Create NewGroupModal component with name input and save/cancel buttons at frontend/src/lib/components/ServiceDefinitionEditor/ListView/NewGroupModal.svelte
- [ ] T089 [P] [US7] Create NewGroupModal unit tests for form validation and API call at frontend/src/lib/components/ServiceDefinitionEditor/ListView/NewGroupModal.test.ts
- [ ] T090 [US7] Implement createGroup API call with success toast and list refresh at frontend/src/lib/components/ServiceDefinitionEditor/ListView/NewGroupModal.svelte
- [ ] T091 [US7] Add validation for required group name with error message at frontend/src/lib/components/ServiceDefinitionEditor/ListView/NewGroupModal.svelte
- [ ] T092 [US7] Add keyboard support (Enter to submit, Escape to cancel) to NewGroupModal at frontend/src/lib/components/ServiceDefinitionEditor/ListView/NewGroupModal.svelte

**Checkpoint**: User Story 7 complete - new group creation working

---

## Phase 10: User Story 8 - Add New Service Request to Group (Priority: P3)

**Goal**: Enable administrators to add new service request types within existing groups

**Independent Test**: Expand a group, click "+ Add Service Request", enter name, verify new service appears and editor opens

### Tests for User Story 8

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T093 [P] [US8] Create integration test for new service creation and editor navigation at frontend/tests/service-definition-editor/browse-groups.spec.ts

### Implementation for User Story 8

- [ ] T094 [US8] Add "+ Add Service Request" button at bottom of expanded group's service list at frontend/src/lib/components/ServiceDefinitionEditor/ListView/ServiceGroupCard.svelte
- [ ] T095 [US8] Create NewServiceModal component with service name input at frontend/src/lib/components/ServiceDefinitionEditor/ListView/NewServiceModal.svelte
- [ ] T096 [P] [US8] Create NewServiceModal unit tests for form validation and API call at frontend/src/lib/components/ServiceDefinitionEditor/ListView/NewServiceModal.test.ts
- [ ] T097 [US8] Implement createService API call with group_id parameter at frontend/src/lib/components/ServiceDefinitionEditor/ListView/NewServiceModal.svelte
- [ ] T098 [US8] Navigate to editor view after successful service creation at frontend/src/lib/components/ServiceDefinitionEditor/ListView/NewServiceModal.svelte
- [ ] T099 [US8] Add keyboard support (Enter to submit, Escape to cancel) to NewServiceModal at frontend/src/lib/components/ServiceDefinitionEditor/ListView/NewServiceModal.svelte

**Checkpoint**: User Story 8 complete - new service creation with editor navigation working

---

## Phase 11: Polish & Cross-Cutting Concerns

**Purpose**: Accessibility compliance, edge cases, and final validation

- [ ] T100 [P] Add ARIA live region for save status announcements to screen readers at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/SaveToast.svelte
- [ ] T101 [P] Add focus management - auto-focus first input when card expands at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeCardExpanded.svelte
- [ ] T102 [P] Add visible focus indicators (2px outline) to all interactive elements at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/
- [ ] T103 Implement error toast with retry button for failed auto-save operations at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/SaveToast.svelte
- [ ] T104 Handle long question text wrapping without breaking card layout at frontend/src/lib/components/ServiceDefinitionEditor/EditorView/AttributeCardCollapsed.svelte
- [ ] T105 Add delete group prevention - show error modal if group contains services at frontend/src/lib/components/ServiceDefinitionEditor/ListView/ServiceGroupCard.svelte
- [ ] T106 [P] Create integration test for delete group prevention at frontend/tests/service-definition-editor/browse-groups.spec.ts
- [ ] T107 Verify color contrast meets WCAG 4.5:1 ratio across all new components
- [ ] T108 Run full keyboard navigation test through all interactive flows
- [ ] T109 Validate all scenarios from quickstart.md manually
- [ ] T110 Run npm run test:unit to verify all unit tests pass
- [ ] T111 Run npm run test:integration to verify all integration tests pass

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-10)**: All depend on Foundational phase completion
- **Polish (Phase 11)**: Depends on all user stories being complete

### User Story Dependencies

| Story | Priority | Dependencies | Can Start After |
|-------|----------|--------------|-----------------|
| US1 (Browse) | P1 | None | Phase 2 complete |
| US2 (Edit Service) | P1 | US1 (for navigation) | T032 complete |
| US3 (Attribute Cards) | P1 | US2 (needs editor) | T031 complete |
| US4 (List Options) | P2 | US3 (needs cards) | T044 complete |
| US5 (Reorder) | P2 | US3 (needs cards) | T044 complete |
| US6 (Duplicate/Delete) | P2 | US3 (needs cards) | T044 complete |
| US7 (Create Group) | P3 | US1 (uses list) | T022 complete |
| US8 (Add Service) | P3 | US1 (uses list) | T022 complete |

### Within Each User Story

- Integration tests MUST be written FIRST and FAIL before implementation
- Unit tests written alongside component implementation
- Models/types before components
- Parent containers before child components
- Core UI before auto-save integration
- Implementation before accessibility enhancements

### Parallel Opportunities

- T003, T004, T005 (all Setup types/stores) - different files
- T008, T010, T012 (foundation unit tests) - different files
- T013, T014 (US1 integration tests) - same file but independent tests
- T015, T017 (ListView components) - different files
- T036, T038, T040 (independent card subcomponents) - different files
- T056, T057 (US4 integration tests) - different tests in same file
- T100, T101, T102 (accessibility tasks) - different components

---

## Parallel Execution Examples

### Phase 1 Setup - All Parallel
```
T003: Create TypeScript types at types.ts
T004: Create editor store at stores/editorStore.ts
T005: Create list view store at stores/listViewStore.ts
```

### Phase 2 Foundation - Tests Parallel
```
T008: SaveToast unit tests
T010: autoSave utility unit tests
T012: EditorContainer unit tests
```

### User Story 1 - Tests First, Then Components Parallel
```
# First (parallel):
T013: Integration test for groups list loading
T014: Integration test for expand/collapse

# Then (parallel):
T015: Create ServiceGroupCard
T016: ServiceGroupCard unit tests
T017: Create ServiceListRow
T018: ServiceListRow unit tests
```

### User Story 3 - Subcomponents Parallel
```
T036 + T037: AttributeCardCollapsed + tests
T038 + T039: AttributeTypeSelector + tests
T040 + T041: AttributeFooter + tests
```

### User Stories 4, 5, 6 - Can Run in Parallel After US3
```
Developer A: T056-T067 (US4 - List Options)
Developer B: T068-T075 (US5 - Reorder)
Developer C: T076-T085 (US6 - Duplicate/Delete)
```

---

## Implementation Strategy

### MVP First (User Stories 1-3 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1 (Browse) - with tests
4. Complete Phase 4: User Story 2 (Edit Header) - with tests
5. Complete Phase 5: User Story 3 (Attribute Cards) - with tests
6. **RUN ALL TESTS**: `npm run test:unit && npm run test:integration`
7. **VALIDATE**: Test browsing, editing, and attribute management manually
8. Deploy/demo core editor functionality

### Incremental Delivery

1. **MVP Release**: US1 + US2 + US3 = Core editor (browse, edit service, manage attributes)
   - Unit tests: ~20 test files
   - Integration tests: browse-groups.spec.ts, edit-service.spec.ts, manage-attributes.spec.ts (partial)
2. **Enhancement Release 1**: US4 + US5 + US6 = Power user features (list options, reorder, duplicate)
   - Additional unit tests: ~8 test files
   - Additional integration tests: manage-attributes.spec.ts (complete)
3. **Enhancement Release 2**: US7 + US8 = Group/service creation
   - Additional unit tests: ~4 test files
   - Additional integration tests: browse-groups.spec.ts (complete)
4. **Final Release**: Phase 11 polish + accessibility compliance + full test suite verification

### Parallel Team Strategy

With 2-3 developers:

1. All: Complete Setup + Foundational together
2. Dev A: US1 (Browse) → US7 (Create Group)
3. Dev B: US2 (Edit) → US4 (List Options)
4. Dev C: US3 (Cards) → US5 (Reorder) → US6 (Duplicate/Delete)
5. All: Phase 11 Polish

---

## Test Coverage Summary

| Phase/Story | Unit Tests | Integration Tests |
|-------------|------------|-------------------|
| Phase 2 (Foundation) | 3 | 0 |
| US1 (Browse) | 2 | 2 |
| US2 (Edit Service) | 1 | 2 |
| US3 (Attribute Cards) | 8 | 3 |
| US4 (List Options) | 2 | 2 |
| US5 (Reorder) | 1 | 1 |
| US6 (Duplicate/Delete) | 2 | 2 |
| US7 (Create Group) | 1 | 1 |
| US8 (Add Service) | 1 | 1 |
| Phase 11 (Polish) | 0 | 1 |
| **TOTAL** | **21** | **15** |

---

## Summary

| Metric | Count |
|--------|-------|
| **Total Tasks** | 111 |
| **Setup Phase** | 6 tasks |
| **Foundational Phase** | 6 tasks |
| **User Story 1 (P1)** | 10 tasks |
| **User Story 2 (P1)** | 10 tasks |
| **User Story 3 (P1)** | 23 tasks |
| **User Story 4 (P2)** | 12 tasks |
| **User Story 5 (P2)** | 8 tasks |
| **User Story 6 (P2)** | 10 tasks |
| **User Story 7 (P3)** | 7 tasks |
| **User Story 8 (P3)** | 7 tasks |
| **Polish Phase** | 12 tasks |
| **Unit Test Tasks** | 21 tasks |
| **Integration Test Tasks** | 15 tasks |
| **Parallel Opportunities** | 41 tasks marked [P] |
| **MVP Scope** | 55 tasks (Phases 1-5) |

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story is independently completable and testable
- Integration tests written FIRST (TDD approach for user flows)
- Unit tests co-located with components (*.test.ts)
- This is a frontend-only feature - all API endpoints exist
- Run `npm run test:unit` for unit tests, `npm run test:integration` for Playwright
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
