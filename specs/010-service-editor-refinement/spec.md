# Feature Specification: Service Definition Editor Refinement

**Feature Branch**: `010-service-editor-refinement`
**Created**: 2026-01-09
**Status**: Draft
**Input**: User description: "Using the wireframes document in specs/005-service-definition-editor/mockup/service-definition-ui-wireframes.md, create a specification to refine Libre311's Service Definition Configuration editor with a split-pane interface, tree navigation, and explicit save behavior"

## Overview

This specification refines the Service Definition Configuration editor in Libre311 based on detailed UX wireframes. The refinement introduces a split-pane interface with tree navigation, Google Forms-style card editing, and explicit save behavior per card. This replaces the previous auto-save approach to give administrators more control over when changes are persisted.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Navigate Service Groups and Services (Priority: P1)

An administrator wants to navigate the hierarchical structure of service groups and their contained services using a tree panel, allowing them to quickly find and select the service they need to edit.

**Why this priority**: Navigation is the entry point to all editor functionality. Without efficient tree navigation, administrators cannot access services to edit them.

**Independent Test**: Can be fully tested by navigating to the Service Definition Configuration page, expanding/collapsing groups, and selecting services in the tree panel.

**Acceptance Scenarios**:

1. **Given** the administrator is on the Service Definition Configuration page, **When** the page loads, **Then** they see a split-pane interface with the Tree Panel on the left showing all service groups
2. **Given** a service group is displayed in the tree, **When** the administrator clicks the group name, **Then** the group expands to reveal its contained services
3. **Given** a service group is expanded, **When** the administrator clicks the group name again, **Then** the group collapses to show only the group name and service count badge
4. **Given** a collapsed group displays a service count badge, **When** the administrator views the badge, **Then** it shows the total number of services in that group

---

### User Story 2 - Edit Service Header Information (Priority: P1)

An administrator wants to modify a service's name and description through a dedicated header card in the Editor Panel.

**Why this priority**: Service name and description are the most basic and frequently updated properties. This is core editing functionality.

**Independent Test**: Can be fully tested by selecting a service, modifying the name/description in the header card, and clicking Save to persist changes.

**Acceptance Scenarios**:

1. **Given** the administrator clicks on a service in the Tree Panel, **When** the Editor Panel loads, **Then** it displays a header card with a purple top border containing the service name and description fields
2. **Given** the administrator modifies the service name or description, **When** changes are made, **Then** the Save button in the header card becomes enabled
3. **Given** the administrator clicks the Save button on the header card, **When** the save completes, **Then** a confirmation toast appears and the Tree Panel updates to reflect the new name
4. **Given** the administrator clicks the Cancel button on the header card, **When** changes exist, **Then** all modifications are reverted to the last saved state

---

### User Story 3 - Edit Attribute Cards (Priority: P1)

An administrator wants to edit individual attributes (questions/fields) using expandable cards that show full editing controls when active.

**Why this priority**: Attribute editing is the primary function of the service definition editor. Without this, administrators cannot customize the data collected from citizens.

**Independent Test**: Can be fully tested by selecting a service, clicking on an attribute card to expand it, modifying fields, and saving changes.

**Acceptance Scenarios**:

1. **Given** the administrator is viewing a service in the Editor Panel, **When** they click on a collapsed attribute card, **Then** the card expands to show full editing controls and other cards collapse
2. **Given** an attribute card is expanded, **When** the administrator modifies the question text, **Then** the Save button becomes enabled
3. **Given** an attribute card is expanded, **When** the administrator changes the answer type using the dropdown, **Then** the card UI updates to show controls appropriate for that type
4. **Given** an attribute card is expanded, **When** the administrator toggles the Required switch and saves, **Then** an asterisk appears in the collapsed view

---

### User Story 4 - Manage List Options (Priority: P1)

An administrator wants to add, edit, and remove options for dropdown and multiple choice attributes.

**Why this priority**: List-type attributes (dropdown, multiple choice) are common and require option management to be functional.

**Independent Test**: Can be fully tested by creating or editing a Multiple Choice attribute, adding options, editing option text, and deleting options.

**Acceptance Scenarios**:

1. **Given** an attribute is set to "Multiple choice" or "Dropdown" type, **When** the card is expanded, **Then** the options are displayed with checkbox indicators (for multiple choice) or circle indicators (for dropdown)
2. **Given** a list-type attribute is being edited, **When** the administrator clicks "Add option", **Then** a new empty option row appears and receives focus
3. **Given** a list-type attribute has multiple options, **When** the administrator clicks the X button on an option, **Then** that option is immediately removed from the list
4. **Given** a list-type attribute has only one option, **When** the administrator attempts to delete it, **Then** deletion is prevented and the delete button is disabled

---

### User Story 5 - Reorder Attributes via Drag and Drop (Priority: P2)

An administrator wants to change the order of attribute cards by dragging them to new positions.

**Why this priority**: Question order affects citizen experience but is secondary to basic editing functionality.

**Independent Test**: Can be fully tested by expanding an attribute card, grabbing its drag handle, dragging to a new position, and verifying the order persists after save.

**Acceptance Scenarios**:

1. **Given** an attribute card is expanded, **When** the administrator grabs the drag handle (::) at the top center, **Then** the card becomes draggable
2. **Given** an attribute is being dragged, **When** it is positioned over another card, **Then** a purple border appears on the target card indicating the drop location
3. **Given** an attribute has been dropped in a new position, **When** the administrator saves changes, **Then** the new order is persisted

---

### User Story 6 - Copy and Delete Attributes (Priority: P2)

An administrator wants to duplicate an attribute to quickly create similar questions, or delete attributes that are no longer needed.

**Why this priority**: Productivity features that speed up form configuration but not essential for basic functionality.

**Independent Test**: Can be fully tested by copying an attribute and verifying a duplicate appears, then deleting an attribute and confirming removal after dialog confirmation.

**Acceptance Scenarios**:

1. **Given** an attribute card is expanded, **When** the administrator clicks the Copy button, **Then** a copy of the attribute appears below with "(copy)" appended to the question text
2. **Given** an attribute card is expanded, **When** the administrator clicks the Delete button, **Then** a confirmation dialog appears asking to confirm deletion
3. **Given** the delete confirmation dialog is shown, **When** the administrator confirms, **Then** the attribute is removed with an animation

---

### User Story 7 - Reorder Services Within Groups (Priority: P2)

An administrator wants to change the order of services within a group or move services between groups using drag and drop in the Tree Panel.

**Why this priority**: Organizational flexibility that improves administrator efficiency but secondary to editing.

**Independent Test**: Can be fully tested by dragging a service to a new position within a group or to a different group, and verifying the order persists.

**Acceptance Scenarios**:

1. **Given** services are displayed in an expanded group, **When** the administrator grabs the drag handle (::) on a service, **Then** the service becomes draggable
2. **Given** a service is being dragged, **When** positioned between other services, **Then** a purple insertion line with dot indicator shows the drop position
3. **Given** a service has been dropped in a new position or group, **When** the drop completes, **Then** the new order/grouping is saved

---

### User Story 8 - Create and Edit Groups (Priority: P3)

An administrator wants to create new service groups and edit existing group names to organize services effectively.

**Why this priority**: Group management is less frequent than service editing and most jurisdictions work with existing groups.

**Independent Test**: Can be fully tested by clicking "+ Group" to create a new group, entering a name, and saving.

**Acceptance Scenarios**:

1. **Given** the administrator is viewing the Tree Panel, **When** they click the "+ Group" button, **Then** a new empty group is created and the Group Editor appears in the Editor Panel
2. **Given** a group is selected in the Tree Panel, **When** the Editor Panel loads, **Then** it displays a Group Editor with the group name field
3. **Given** the administrator modifies the group name and clicks Save, **When** the save completes, **Then** the Tree Panel updates to show the new group name

---

### User Story 9 - Add New Service to Group (Priority: P3)

An administrator wants to add a new service request type to a group.

**Why this priority**: Creating new services expands system coverage but is less frequent than editing existing ones.

**Independent Test**: Can be fully tested by clicking "+ Add Svc" within a group, filling in service details, and verifying the new service appears.

**Acceptance Scenarios**:

1. **Given** a service group is expanded in the Tree Panel, **When** the administrator clicks "+ Add Svc", **Then** the Service Editor appears with empty header card and add question button
2. **Given** the administrator fills in the service name and saves, **When** the save completes, **Then** the new service appears in the Tree Panel under the selected group

---

### User Story 10 - Unsaved Changes Protection (Priority: P2)

An administrator wants to be warned when navigating away from a card with unsaved changes to prevent accidental data loss.

**Why this priority**: Data loss prevention is critical for user trust and reduces frustration.

**Independent Test**: Can be fully tested by making changes to a card, then clicking on a different item in the tree and verifying a confirmation dialog appears.

**Acceptance Scenarios**:

1. **Given** the administrator has unsaved changes in a card, **When** they attempt to select a different item in the Tree Panel, **Then** an "Unsaved Changes" modal appears
2. **Given** the Unsaved Changes modal is displayed, **When** the administrator clicks "Save", **Then** changes are saved and navigation proceeds
3. **Given** the Unsaved Changes modal is displayed, **When** the administrator clicks "Discard", **Then** changes are reverted and navigation proceeds

---

### Edge Cases

- What happens when the administrator attempts to delete the last option in a list-type attribute? The delete button is disabled for the last remaining option.
- How does the system handle very long question text? Text wraps within the card without breaking the layout.
- What happens if a save operation fails due to network error? An error toast appears with a retry option and the changes remain unsaved.
- How does the system behave when two administrators edit the same service? Last-write-wins with no conflict detection or notification.
- What happens when attempting to delete a group that contains services? The system prevents deletion and displays an error message.
- How does the collapsed attribute card display list-type options? Checkbox indicators ([ ]) for multiple choice, circle indicators (O) for dropdown, with option names truncated if too long.

## Requirements *(mandatory)*

### Functional Requirements

**Split-Pane Layout**
- **FR-001**: System MUST display a split-pane interface with Tree Panel on the left and Editor Panel on the right
- **FR-002**: System MUST show a header "Service Definition Configuration" with back navigation to Admin section

**Tree Panel**
- **FR-003**: System MUST display all service groups in a collapsible/expandable hierarchical list
- **FR-004**: System MUST show a service count badge next to each group when collapsed
- **FR-005**: System MUST display a expand/collapse chevron for each group
- **FR-006**: System MUST show a drag handle (::) for each service to enable reordering
- **FR-007**: System MUST provide a "+ Group" button at the top of the Tree Panel
- **FR-008**: System MUST provide a "+ Add Svc" button at the bottom of each expanded group
- **FR-009**: System MUST highlight the selected item with a purple left border and blue-tinted background

**Editor Panel States**
- **FR-010**: System MUST display a Group Editor when a group is selected
- **FR-011**: System MUST display a Service Editor when a service is selected
- **FR-012**: System MUST display an empty state when nothing is selected

**Group Editor**
- **FR-013**: System MUST display a card with purple top border containing the group name field
- **FR-014**: System MUST provide Save and Delete buttons for the group

**Service Editor - Header Card**
- **FR-015**: System MUST display a header card with purple top border containing service name and description
- **FR-016**: System MUST provide Save and Cancel buttons on the header card
- **FR-017**: System MUST enable the Save button only when changes are made

**Service Editor - Attribute Cards**
- **FR-018**: System MUST display each attribute as a collapsible card
- **FR-019**: System MUST show only one attribute card expanded at a time
- **FR-020**: System MUST display in collapsed view: question text, field type indicator, and required asterisk
- **FR-021**: System MUST display checkbox indicators ([ ]) for multiple choice types in collapsed view
- **FR-022**: System MUST display circle indicators (O) for dropdown types in collapsed view
- **FR-023**: System MUST display in expanded view: drag handle, question input, type selector, options (if list type), and footer controls

**Attribute Type Selector**
- **FR-024**: System MUST provide a dropdown with options: Short answer, Paragraph, Multiple choice, Dropdown, Number, Date
- **FR-025**: System MUST update the card UI when attribute type changes

**Options Management (for list types)**
- **FR-026**: System MUST display options with delete buttons (x) for list-type attributes
- **FR-027**: System MUST provide "Add option" functionality
- **FR-028**: System MUST prevent deletion of the last remaining option

**Attribute Card Footer**
- **FR-029**: System MUST provide Cancel and Save buttons for each attribute card
- **FR-030**: System MUST provide Copy and Delete action buttons
- **FR-031**: System MUST provide a Required toggle switch
- **FR-032**: System MUST provide a "More options" menu (: icon) containing a help text field (datatypeDescription) for providing hints to users

**Drag and Drop**
- **FR-033**: System MUST allow dragging services within and between groups in the Tree Panel
- **FR-034**: System MUST allow dragging attribute cards to reorder within the Service Editor
- **FR-035**: System MUST show a purple insertion line with dot indicator for drop position
- **FR-036**: System MUST apply 50% opacity to the item being dragged

**Save Behavior**
- **FR-037**: System MUST use explicit save (click Save button) rather than auto-save
- **FR-038**: System MUST track unsaved changes at the card level
- **FR-039**: System MUST display confirmation toast on successful save
- **FR-040**: System MUST update the Tree Panel to reflect saved changes
- **FR-048**: System MUST disable the Save button and display an inline spinner while save is in progress

**Unsaved Changes Modal**
- **FR-041**: System MUST display an "Unsaved Changes" modal when navigating with pending changes
- **FR-042**: System MUST provide "Discard" and "Save" options in the modal

**New Question Flow**
- **FR-043**: System MUST display an "+ Add question" card at the bottom of attributes
- **FR-044**: System MUST create new questions with "Short answer" type by default
- **FR-045**: System MUST auto-expand and focus the new question card

**Delete Confirmations**
- **FR-046**: System MUST display a confirmation dialog before deleting an attribute
- **FR-047**: System MUST display a confirmation dialog before deleting a service

### Non-Functional Requirements

- **NFR-001**: System MUST meet WCAG 2.2 Level AA accessibility standards including keyboard navigation, focus indicators, color contrast ratios, and screen reader compatibility

### Key Entities

- **Service Group**: A category for organizing related service request types. Contains a name and ordered list of services. Displayed as expandable nodes in the Tree Panel.
- **Service**: A type of issue that citizens can report. Has a name, description, and ordered list of attributes. Displayed within groups in the Tree Panel.
- **Service Definition Attribute**: A field/question that collects information for a service request. Has question text (description), data type, required flag, and display order. Displayed as collapsible cards in the Service Editor.
- **Attribute Value**: A selectable option for list-type attributes (Multiple choice, Dropdown). Has a value name displayed as a choice to users.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Administrators can navigate to any service within 3 clicks from page load
- **SC-002**: Administrators can identify unsaved changes through visual indicators (enabled Save button)
- **SC-003**: All save operations provide visual confirmation within 2 seconds of completion
- **SC-004**: Administrators can complete editing of a service's attributes within 5 minutes for a typical 5-question form
- **SC-005**: Drag and drop operations provide clear visual feedback throughout the entire drag sequence
- **SC-006**: 90% of administrators can successfully add a new attribute on their first attempt without documentation
- **SC-007**: The interface remains usable on screens 1024 pixels wide or larger
- **SC-008**: Unsaved changes modal prevents accidental data loss when navigating away
- **SC-009**: Split-pane layout allows simultaneous view of navigation (tree) and editing context

## Clarifications

### Session 2026-01-09

- Q: What level of accessibility compliance is required? → A: WCAG 2.2 Level AA
- Q: What loading state should display during save operations? → A: Disable Save button with inline spinner
- Q: What settings are available in the "More options" menu? → A: Help text field only (datatypeDescription)

## Assumptions

- The existing API endpoints from `JurisdictionAdminController` are available and support all required CRUD operations
- Service definition changes take effect immediately upon save; there is no draft/publish workflow
- Concurrent edits use simple last-write-wins with no conflict detection
- No artificial limits on number of attributes per service or options per list-type attribute
- The existing frontend component library (STWUI) provides sufficient UI primitives
- Administrators accessing this feature have appropriate permissions already validated
- Service definitions are jurisdiction-specific and isolated from other jurisdictions
- The existing Open311-compliant data model (Service, ServiceDefinitionAttribute, AttributeValue) remains unchanged
