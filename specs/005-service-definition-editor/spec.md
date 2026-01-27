# Feature Specification: Service Definition Editor

**Feature Branch**: `005-service-definition-editor`
**Created**: 2025-12-31
**Status**: Draft
**Input**: User description: "Implement the UI defined in specs/service-definition-ui-wireframes.md and mocked up in specs/service-definition-mockup.html"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Browse Service Groups and Services (Priority: P1)

An administrator wants to see all available service groups and the service request types within each group to understand what citizens can currently report.

**Why this priority**: This is the entry point for the feature. Without the ability to browse existing service groups and services, administrators cannot navigate to edit or create service definitions.

**Independent Test**: Can be fully tested by navigating to the Service Definition Configuration page and verifying groups and services are displayed in an expandable/collapsible list format.

**Acceptance Scenarios**:

1. **Given** the administrator is on the Admin section, **When** they navigate to Service Definition Configuration, **Then** they see a list of all service groups with their contained service requests
2. **Given** a service group is collapsed, **When** the administrator clicks the group header, **Then** the group expands to show its service requests
3. **Given** a service group is expanded, **When** the administrator clicks the group header, **Then** the group collapses showing only a service count

---

### User Story 2 - Edit Service Request Definition (Priority: P1)

An administrator wants to modify an existing service request definition to update the questions/fields that citizens must answer when submitting that type of request.

**Why this priority**: Editing existing service definitions is the core functionality of this feature and enables administrators to maintain and improve the data collection for service requests.

**Independent Test**: Can be fully tested by selecting "Edit" on any service, modifying the service name, description, or attributes, and verifying changes persist.

**Acceptance Scenarios**:

1. **Given** the administrator is viewing the services list, **When** they click "Edit" on a service request, **Then** they see the full-page Google Forms-style editor with the header card and attribute cards
2. **Given** the administrator is in the editor view, **When** they modify the service name in the header card, **Then** the change is auto-saved and a confirmation toast appears
3. **Given** the administrator is in the editor view, **When** they modify the service description, **Then** the change is auto-saved and a confirmation toast appears

---

### User Story 3 - Manage Attribute Cards (Priority: P1)

An administrator wants to add, edit, and remove attribute fields (questions) on a service request to customize what information is collected from citizens.

**Why this priority**: Attributes are the core data collection mechanism for service requests. Without attribute management, the editor has no practical use.

**Independent Test**: Can be fully tested by adding a new attribute field, editing its question text and type, toggling required status, and deleting an attribute.

**Acceptance Scenarios**:

1. **Given** the administrator is editing a service, **When** they click on a collapsed attribute card, **Then** the card expands to show full editing controls and other cards collapse
2. **Given** an attribute card is expanded, **When** the administrator edits the question text, **Then** the change is auto-saved
3. **Given** an attribute card is expanded, **When** the administrator changes the answer type dropdown, **Then** the card UI updates to show appropriate controls for that type
4. **Given** an attribute card is expanded, **When** the administrator toggles the "Required" switch, **Then** an asterisk indicator appears/disappears in the collapsed view

---

### User Story 4 - Manage List Options for Multiple Choice Fields (Priority: P2)

An administrator wants to configure the available options for dropdown and multiple choice attribute fields so citizens can select from predefined values.

**Why this priority**: Many service requests need controlled vocabulary for answers. This enables structured data collection that improves reporting and routing.

**Independent Test**: Can be fully tested by creating a Multiple Choice attribute, adding/editing/deleting option values, and including an "Other" option for free-text entry.

**Acceptance Scenarios**:

1. **Given** an attribute is set to "Multiple choice" or "Dropdown" type, **When** the administrator views the expanded card, **Then** they see the list of option values with delete buttons
2. **Given** a list-type attribute is being edited, **When** the administrator clicks "Add option", **Then** a new empty option row appears and receives focus
3. **Given** a list-type attribute has multiple options, **When** the administrator clicks the X button on an option, **Then** that option is immediately removed
4. **Given** a list-type attribute is being edited, **When** the administrator adds an "Other" option, **Then** citizens will be able to enter free-text when selecting this option

---

### User Story 5 - Reorder Attributes via Drag and Drop (Priority: P2)

An administrator wants to change the order in which attribute fields appear so that the most important questions appear first for citizens.

**Why this priority**: Question order affects user experience and completion rates. Administrators need control over the flow of the form.

**Independent Test**: Can be fully tested by dragging an attribute card from one position to another and verifying the new order persists.

**Acceptance Scenarios**:

1. **Given** an attribute card is expanded, **When** the administrator grabs the drag handle at the top, **Then** they can drag the card to a new position
2. **Given** an attribute is being dragged, **When** the administrator moves it over another card, **Then** a visual indicator shows where it will be dropped
3. **Given** an attribute has been dropped in a new position, **When** the administrator releases the mouse, **Then** the new order is saved and reflected in the attribute order

---

### User Story 6 - Duplicate and Delete Attributes (Priority: P2)

An administrator wants to quickly duplicate an attribute (to create similar questions) or delete attributes that are no longer needed.

**Why this priority**: These productivity features speed up form creation and maintenance.

**Independent Test**: Can be fully tested by duplicating an attribute and verifying a copy appears, then deleting an attribute and confirming it is removed after confirmation.

**Acceptance Scenarios**:

1. **Given** an attribute card is expanded, **When** the administrator clicks the duplicate icon, **Then** a copy of the attribute appears below with "(copy)" appended to the name
2. **Given** an attribute card is expanded, **When** the administrator clicks the delete icon, **Then** a confirmation dialog appears
3. **Given** the delete confirmation is shown, **When** the administrator confirms, **Then** the attribute is removed with an animation

---

### User Story 7 - Create New Service Group (Priority: P3)

An administrator wants to create a new service group to organize related service request types together.

**Why this priority**: While important for organization, most jurisdictions will work with existing groups more often than creating new ones.

**Independent Test**: Can be fully tested by clicking "New Group", entering a name, and verifying the new group appears in the list.

**Acceptance Scenarios**:

1. **Given** the administrator is on the services list view, **When** they click "+ New Group", **Then** a form or modal appears to enter the group name
2. **Given** a new group name has been entered, **When** the administrator saves, **Then** the new empty group appears in the list

---

### User Story 8 - Add New Service Request to Group (Priority: P3)

An administrator wants to create a new service request type within an existing group to allow citizens to report a new type of issue.

**Why this priority**: Adding new service types expands the system's coverage but is less frequent than editing existing ones.

**Independent Test**: Can be fully tested by clicking "+ Add Service Request" within a group, entering details, and verifying the new service appears.

**Acceptance Scenarios**:

1. **Given** a service group is expanded, **When** the administrator clicks "+ Add Service Request", **Then** the editor view opens with an empty service definition
2. **Given** the administrator has filled in the service name and description, **When** they navigate back to the list, **Then** the new service appears in the group

---

### Edge Cases

- What happens when attempting to delete the last option in a list-type attribute? The system should prevent deletion and show a warning that at least one option is required.
- How does the system handle extremely long question text? Text should wrap within the card and not break the layout.
- What happens if auto-save fails due to network error? An error toast should appear with a retry option.
- How does the system behave when two administrators edit the same service simultaneously? Changes are applied in order received with no special handling or notification (last-write-wins).
- What happens when attempting to delete a service group that contains services? The system prevents deletion and displays an error message requiring services to be moved or deleted first.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST display all service groups in a collapsible/expandable list format
- **FR-002**: System MUST show the count of service requests when a group is collapsed
- **FR-003**: System MUST provide Edit, Delete, and More options actions for each service request in the list
- **FR-004**: System MUST provide a full-page editor view with Google Forms-style card layout when editing a service
- **FR-005**: System MUST display a header card with colored banner showing service name and description with inline editing
- **FR-006**: System MUST display attribute fields as cards that can be collapsed (preview) or expanded (editing)
- **FR-007**: System MUST allow only one attribute card to be expanded at a time
- **FR-008**: System MUST provide an answer type dropdown with options: Short answer, Paragraph, Multiple choice, Dropdown, Number, Date
- **FR-009**: System MUST display appropriate field type indicators in collapsed attribute cards
- **FR-010**: System MUST show asterisk (*) indicator for required fields in collapsed view
- **FR-011**: System MUST allow adding, editing, and deleting options for list-type attributes (Multiple choice, Dropdown)
- **FR-012**: System MUST support an "Other" option that allows free-text entry
- **FR-013**: System MUST provide drag-and-drop reordering of attribute cards via a drag handle
- **FR-014**: System MUST provide duplicate and delete actions for attribute cards
- **FR-015**: System MUST display a confirmation dialog before deleting an attribute
- **FR-016**: System MUST provide a Required toggle for each attribute
- **FR-017**: System MUST auto-save changes on field blur/change without requiring an explicit save button
- **FR-018**: System MUST display a toast notification confirming when changes are saved
- **FR-019**: System MUST provide a floating action button to add new attribute fields
- **FR-020**: System MUST provide navigation to return from editor view to services list
- **FR-021**: System MUST allow creating new service groups via "+ New Group" button
- **FR-022**: System MUST allow adding new service requests via "+ Add Service Request" within each group
- **FR-023**: System MUST prevent deletion of service groups that contain services, displaying an error message

### Key Entities

- **Service Group**: A category for organizing related service request types (e.g., "Roads & Traffic", "Sanitation"). Contains a name and ordered list of services.
- **Service (Service Request)**: A type of issue that citizens can report (e.g., "Pothole Repair"). Has a name, description, and ordered list of attributes.
- **Service Definition Attribute**: A field/question that collects information for a service request. Has a question text (description), data type, required flag, and display order. May contain attribute values for list types.
- **Attribute Value**: A selectable option for list-type attributes (Multiple choice, Dropdown). Has a value name that is displayed to users.

### Non-Functional Requirements

- **NFR-001**: System MUST meet WCAG 2.1 Level AA accessibility standards including keyboard navigation, focus indicators, color contrast ratios, and screen reader compatibility

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Administrators can view and navigate the complete service group hierarchy within 3 seconds of page load
- **SC-002**: Administrators can complete editing of an existing service request's attributes in under 5 minutes
- **SC-003**: All changes are auto-saved within 1 second of field modification, with visual confirmation
- **SC-004**: Administrators can reorder attributes via drag-and-drop with clear visual feedback during the operation
- **SC-005**: The editor interface matches the Google Forms-style layout as defined in the wireframes and mockup
- **SC-006**: 90% of administrators can successfully add a new attribute field on their first attempt without documentation
- **SC-007**: The UI remains responsive and usable on screens 1024 pixels wide or larger

## Clarifications

### Session 2026-01-05

- Q: Do service definition changes go live immediately, or should there be a draft/publish workflow? → A: Immediate - all changes are live instantly after auto-save
- Q: How should the system handle concurrent edits when two administrators edit the same service definition? → A: No special handling - accept all writes without notification
- Q: When deleting a service group that contains services, what should happen? → A: Prevent deletion - show error requiring services to be moved or deleted first
- Q: What level of accessibility compliance is required for this admin interface? → A: WCAG 2.1 Level AA
- Q: Should there be limits on the number of attributes per service or options per list-type attribute? → A: No limits - administrators can add unlimited attributes and options

## Assumptions

- Service definition changes take effect immediately upon auto-save; there is no draft/publish workflow
- Concurrent edits use simple last-write-wins with no conflict detection or notification
- No artificial limits on number of attributes per service or options per list-type attribute
- The existing API endpoints from `JurisdictionAdminController` for service and attribute CRUD operations are available and functional
- The existing STWUI component library and Svelte stores are sufficient for building this UI
- The existing `DragAndDrop` component in the frontend can be reused for attribute reordering
- Administrators accessing this feature have appropriate permissions already validated by the application
- Service definitions are jurisdiction-specific and isolated from other jurisdictions