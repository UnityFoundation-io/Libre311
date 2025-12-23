# Feature Specification: Admin Table View with Contextual Detail Pane

**Feature Branch**: `002-admin-table-detail-pane`
**Created**: 2025-12-23
**Status**: Draft
**Input**: User description: "Admin service request table view with contextual detail pane - remove redundant card list, table as primary view with slide-in detail pane on row selection"

## Clarifications

### Session 2025-12-23

- Q: What should the detail pane width be in split view? → A: Fixed-width detail pane (~400px) with table taking remaining space

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Service Requests in Full-Width Table (Priority: P1)

As an administrator, I want to view service requests in a full-width table so that I can see more data at once without visual redundancy from the card list.

**Why this priority**: This is the foundational change that removes the redundant UI and establishes the table as the primary view. All other stories depend on this layout change.

**Independent Test**: Can be fully tested by navigating to the admin table view and verifying the card list is removed and the table spans the full available width.

**Acceptance Scenarios**:

1. **Given** an admin user is on the service requests table view, **When** no request is selected, **Then** the table occupies the full available width of the main content area
2. **Given** the table view is loaded, **When** the admin views the page, **Then** the persistent card list on the left side is not displayed
3. **Given** the full-width table view, **When** the admin scrolls the table, **Then** all columns remain visible and properly aligned

---

### User Story 2 - Select Row to Open Detail Pane (Priority: P1)

As an administrator, I want to click on a table row to open a detail pane that slides in from the left so that I can view complete request information while maintaining context of the table.

**Why this priority**: The detail pane is essential for viewing and managing individual requests. Without this, admins cannot access request details at all.

**Independent Test**: Can be fully tested by clicking any table row and verifying the detail pane appears with the selected request's information.

**Acceptance Scenarios**:

1. **Given** an admin is viewing the full-width table, **When** they click on a table row, **Then** a detail pane slides in from the left side
2. **Given** a row has been clicked, **When** the detail pane opens, **Then** the selected row is visually highlighted in the table
3. **Given** the detail pane is open, **When** viewing the layout, **Then** the table and detail pane display side-by-side (split view)
4. **Given** the detail pane is open, **When** viewing the detail pane, **Then** all request information currently shown in cards is displayed (Request ID, status, timestamp, image, service name, address, description, selected values, citizen contact, expected completion, priority, agency contact, service notice, status notes)

---

### User Story 3 - Close Detail Pane to Return to Full-Width Table (Priority: P2)

As an administrator, I want to close the detail pane to return to the full-width table view so that I can maximize the table space when I'm done reviewing a request.

**Why this priority**: Important for usability, allowing admins to easily toggle between focused detail view and expanded table view.

**Independent Test**: Can be fully tested by opening the detail pane, clicking the close button, and verifying the table returns to full-width.

**Acceptance Scenarios**:

1. **Given** the detail pane is open, **When** the admin clicks the "Close" button, **Then** the detail pane closes and the table returns to full-width view
2. **Given** the detail pane closes, **When** viewing the table, **Then** the previously selected row is no longer highlighted
3. **Given** the detail pane is open, **When** the admin selects a different row, **Then** the detail pane updates to show the newly selected request

---

### User Story 4 - Update Service Request from Detail Pane (Priority: P2)

As an administrator, I want to edit and update service request fields from the detail pane so that I can manage requests without navigating away from the table context.

**Why this priority**: Core administrative functionality that enables request management. Depends on the detail pane being implemented.

**Independent Test**: Can be fully tested by selecting a request, clicking "Update" in the detail pane, modifying fields, saving, and verifying changes persist.

**Acceptance Scenarios**:

1. **Given** the detail pane is open for a request, **When** the admin clicks "Update", **Then** they can edit request fields including status, priority, expected completion date, and status notes
2. **Given** the admin has made changes, **When** they save the update, **Then** the changes are persisted and reflected in both the detail pane and the table row
3. **Given** the admin is editing, **When** they cancel the edit, **Then** changes are discarded and the original values are restored

---

### User Story 5 - Delete Service Request from Detail Pane (Priority: P2)

As an administrator, I want to delete a service request from the detail pane so that I can remove invalid or duplicate requests.

**Why this priority**: Important administrative function for data management. Lower priority than update since deletion is less frequent.

**Independent Test**: Can be fully tested by selecting a request, clicking "Delete", confirming, and verifying the request is removed from the table.

**Acceptance Scenarios**:

1. **Given** the detail pane is open for a request, **When** the admin clicks "Delete", **Then** a confirmation dialog is displayed
2. **Given** the confirmation dialog is shown, **When** the admin confirms deletion, **Then** the request is deleted, the detail pane closes, and the request is removed from the table
3. **Given** the confirmation dialog is shown, **When** the admin cancels, **Then** the request is not deleted and the detail pane remains open

---

### User Story 6 - Filter Service Requests (Priority: P3)

As an administrator, I want to filter service requests by priority, status, request type, and date range so that I can find specific requests efficiently.

**Why this priority**: Enhances usability but the existing filter functionality is already present; this story ensures it continues to work with the new layout.

**Independent Test**: Can be fully tested by applying various filter combinations and verifying the table displays only matching requests.

**Acceptance Scenarios**:

1. **Given** the admin is viewing the table, **When** they click the filter icon, **Then** the filter panel is displayed
2. **Given** the filter panel is open, **When** the admin selects priority values (e.g., High, Medium), **Then** the table displays only requests matching those priorities
3. **Given** the filter panel is open, **When** the admin selects status values (e.g., Open, In Progress), **Then** the table displays only requests matching those statuses
4. **Given** the filter panel is open, **When** the admin selects a date range (from/to), **Then** the table displays only requests reported within that range
5. **Given** the filter panel is open, **When** the admin selects request types, **Then** the table displays only requests of those types
6. **Given** filters are applied, **When** the admin opens the detail pane for a filtered result, **Then** the filters remain active

---

### User Story 7 - Download Filtered Results as CSV (Priority: P3)

As an administrator, I want to download the current filtered service requests as a CSV file so that I can analyze or share the data externally.

**Why this priority**: Export functionality is valuable but not critical for core table/detail pane functionality. Existing feature that must continue working.

**Independent Test**: Can be fully tested by applying filters, clicking "Download CSV", and verifying the downloaded file contains the filtered data.

**Acceptance Scenarios**:

1. **Given** the admin is viewing the table (with or without filters), **When** they click "Download CSV" at the bottom right of the table, **Then** a CSV file containing the currently displayed/filtered requests is downloaded
2. **Given** filters are applied, **When** the admin downloads CSV, **Then** the file only contains requests matching the current filters
3. **Given** the download completes, **When** the admin opens the file, **Then** all relevant request fields are included in the CSV columns

---

### User Story 8 - Mobile/Small Screen Table View (Priority: P4)

As an administrator using a mobile device, I want a simplified table experience where selecting a row shows the detail view full-screen so that I can still manage requests on smaller screens.

**Why this priority**: Lower priority as per user input questioning necessity. Provides basic mobile support with limited functionality.

**Independent Test**: Can be fully tested on a mobile-sized screen by tapping a row and verifying the detail view replaces the table entirely.

**Acceptance Scenarios**:

1. **Given** an admin is viewing the table on a mobile device, **When** no request is selected, **Then** the table displays full-width
2. **Given** the admin taps a table row on mobile, **When** the tap is registered, **Then** the detail view replaces the table entirely (not split view)
3. **Given** the detail view is open on mobile, **When** the admin taps "Back" or "Close", **Then** they return to the table view
4. **Given** mobile view, **When** viewing the interface, **Then** filter controls, search, and CSV download are not displayed (limited functionality)

---

### Edge Cases

- What happens when the admin clicks a row while the detail pane is already open for a different request? (The detail pane should update to show the newly selected request)
- What happens when a request is deleted while being viewed? (The detail pane should close and the table should refresh)
- What happens when the admin applies filters that result in zero requests? (The table should display an empty state message; the detail pane should not be available)
- What happens when the detail pane is open and the admin applies a filter that excludes the currently selected request? (The detail pane should close since the selected request is no longer in the result set)
- What happens when the admin resizes the browser window between desktop and mobile breakpoints? (The layout should smoothly transition between split view and full-screen detail modes)
- What happens if multiple admins try to update the same request simultaneously? (Standard optimistic concurrency - last write wins, or show conflict notification if implemented)

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST display the service request table at full width when no request is selected (desktop view)
- **FR-002**: System MUST NOT display the persistent card list on the left side of the table view
- **FR-003**: System MUST display a detail pane that slides in from the left when a table row is selected
- **FR-004**: System MUST display the table and detail pane side-by-side (split view) when a request is selected (desktop view); detail pane width is fixed at ~400px with table taking remaining space
- **FR-005**: System MUST visually highlight the selected row in the table
- **FR-006**: The detail pane MUST display all request information: Request ID, status, timestamp, image (if available), service name, address, description, selected values, citizen contact, expected completion, priority, agency contact, service notice, and status notes
- **FR-007**: Admin MUST be able to close/dismiss the detail pane via a "Close" button
- **FR-008**: Admin MUST be able to edit request fields and save changes via an "Update" button in the detail pane
- **FR-009**: Admin MUST be able to update request status (e.g., open, in progress, closed) from the detail pane
- **FR-010**: Admin MUST be able to delete a request via a "Delete" button with confirmation
- **FR-011**: Admin MUST be able to access the filter panel via a filter icon to filter by priority, status, request type, and date range (from/to)
- **FR-012**: Admin MUST be able to download filtered service requests as CSV via a "Download CSV" button at the bottom right of the table
- **FR-013**: On mobile/small screens, selecting a row MUST replace the table entirely with the detail view (not split view)
- **FR-014**: On mobile, the detail view MUST include a back/close button to return to the table
- **FR-015**: On mobile, filter controls, search, and CSV download MUST NOT be displayed (limited functionality)

### Key Entities

- **ServiceRequest**: Represents a service request with attributes including: request ID, status, priority, service type, address, description, reported date, expected completion date, citizen contact info, agency contact info, service notice, status notes, and associated media/images
- **Admin User**: A user with administrative permissions who can view, update, and delete service requests
- **Filter State**: The current set of active filters (priority, status, service type, date range) applied to the table view

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Admins can locate and select a specific request from the table in under 10 seconds (without using search)
- **SC-002**: Admins can view complete request details without page navigation or full page reload
- **SC-003**: Admins can update a request status in under 5 clicks (select row, click update, change status, save)
- **SC-004**: The table view displays at least 20% more horizontal space when no request is selected compared to the previous card list + table layout
- **SC-005**: 90% of admin workflows (view, update, delete, filter, export) can be completed without leaving the table view
- **SC-006**: Time to complete a "find and update request status" task is reduced compared to current interface (measurable via user testing)
- **SC-007**: Mobile users can view and edit request details with full functionality (except filters and export as scoped)

## Assumptions

- The existing filter functionality (priority, status, service type, date range) and CSV download feature will be preserved and continue to work with the new layout
- The detail pane will reuse the existing `ServiceRequest` component for displaying request information
- Permission checks for update and delete actions will continue to use the existing permission system
- The slide-in animation direction (from left) matches the current detail view behavior
- Browser breakpoints for mobile vs desktop will use the existing responsive design breakpoints (~768px)
- The existing table column configuration and data will remain unchanged; only the layout wrapper changes