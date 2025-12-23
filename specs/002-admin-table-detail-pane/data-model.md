# Data Model: Admin Table View with Contextual Detail Pane

**Feature**: 002-admin-table-detail-pane
**Date**: 2025-12-23

## Overview

This feature is a frontend UI refactoring. No new backend entities or database changes are required. The data model section documents the frontend state and existing entities used by this feature.

---

## Existing Entities (No Changes)

### ServiceRequest

The core entity displayed in the table and detail pane. Already defined in the codebase.

**Source**: `frontend/src/lib/services/Libre311/Libre311.ts`

| Field | Type | Description |
|-------|------|-------------|
| service_request_id | number | Unique identifier |
| status | ServiceRequestStatus | open, closed, etc. |
| priority | ServiceRequestPriority | low, medium, high |
| service_name | string | Type of service request |
| service_code | number | Service type code |
| address | string | Location address |
| description | string | Request description |
| requested_datetime | string (ISO) | When request was created |
| expected_datetime | string (ISO) | Expected completion |
| media_url | string | Attached image URL |
| first_name | string | Citizen first name |
| last_name | string | Citizen last name |
| email | string | Citizen email |
| phone | string | Citizen phone |
| agency_responsible | string | Assigned agency |
| agency_email | string | Agency contact |
| service_notice | string | Notice text |
| status_notes | string | Status notes |
| selected_values | object | Custom field values |

### ServiceRequestStatus (Enum)

**Values**: `open`, `closed`, `in_progress`, etc.

### ServiceRequestPriority (Enum)

**Values**: `low`, `medium`, `high`

---

## Frontend State (Existing)

### selectedServiceRequestStore

**Source**: `frontend/src/lib/context/ServiceRequestsContext.ts`

Svelte store containing the currently selected `ServiceRequest` object. Populated when navigating to `/issues/table/[issue_id]`.

**Type**: `Writable<Maybe<ServiceRequest>>`

**Usage**:
- Detail pane reads from this store to display request info
- Set by context when route changes to include `issue_id`
- Cleared when navigating to `/issues/table` (no id)

### serviceRequestsResponse

**Source**: `frontend/src/lib/context/ServiceRequestsContext.ts`

Async result containing the list of service requests for the table.

**Type**: `Writable<AsyncResult<ServiceRequestsResponse>>`

**Usage**:
- Table iterates over `value.serviceRequests` to render rows
- Updated when filters change via `applyServiceRequestParams()`

---

## New Component Props (Phase 1)

### TableWithDetailPane Component

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| detailPaneOpen | boolean | false | Whether detail pane is visible |
| detailPaneWidth | string | '400px' | Fixed width of detail pane |

**Slots**:
- `table`: Main table content area
- `detail-pane`: Detail view content (ServiceRequest component)

---

## State Transitions

### Detail Pane State Machine

```
[Table Only]
    │
    │ click table row
    │ → goto('/issues/table/{id}')
    ▼
[Split View: Table + Detail Pane]
    │
    ├─ click different row
    │  → update selectedServiceRequestStore
    │  → detail pane re-renders
    │
    ├─ click "Close" button
    │  → goto('/issues/table')
    │  → returns to [Table Only]
    │
    ├─ click "Delete"
    │  → confirm → delete → goto('/issues/table')
    │  → returns to [Table Only]
    │
    └─ click "Update"
        → inline edit mode
        → save → refresh data
        → stay in detail view
```

### Mobile State (< 769px)

```
[Table Full Screen]
    │
    │ tap table row
    ▼
[Detail Full Screen]
    │
    │ tap "Back"/"Close"
    ▼
[Table Full Screen]
```

---

## No API Changes Required

This feature reuses all existing API endpoints:
- `GET /api/requests` - Fetch service request list (with filters)
- `GET /api/requests/{id}` - Fetch single request (via context)
- `PATCH /api/requests/{id}` - Update request
- `DELETE /api/requests/{id}` - Delete request
- `GET /api/requests/download` - CSV export

No new endpoints, no schema changes, no data migrations.