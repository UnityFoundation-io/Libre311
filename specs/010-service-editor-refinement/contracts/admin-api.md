# API Contracts: Service Definition Editor

**Feature**: 010-service-editor-refinement
**Date**: 2026-01-09
**Status**: Existing (no changes required)

## Overview

This document describes the existing API endpoints from `JurisdictionAdminController` that the split-pane editor will use. **No new endpoints are needed** - all required operations are already supported.

## Base URL

```
/api/jurisdiction-admin
```

## Authentication

All endpoints require JWT authentication via UnityAuth. The `jurisdiction_id` query parameter is required for multi-tenant isolation.

## Group Operations

### List Groups

Fetches all groups with their services for the tree panel.

```
GET /api/jurisdiction-admin/groups?jurisdiction_id={jurisdictionId}
```

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "name": "Roads & Traffic",
    "services": [
      {
        "service_code": 101,
        "service_name": "Pothole Repair",
        "description": "Report potholes on city roads",
        "group_id": 1,
        "order_position": 0
      },
      {
        "service_code": 102,
        "service_name": "Streetlight Out",
        "description": "Report non-functioning streetlights",
        "group_id": 1,
        "order_position": 1
      }
    ]
  }
]
```

### Create Group

Creates a new service group (+ Group button).

```
POST /api/jurisdiction-admin/groups?jurisdiction_id={jurisdictionId}
Content-Type: application/json

{
  "name": "New Group Name"
}
```

**Response** (201 Created):
```json
{
  "id": 5,
  "name": "New Group Name"
}
```

### Update Group

Updates a group's name (Group Editor save).

```
PATCH /api/jurisdiction-admin/groups/{groupId}?jurisdiction_id={jurisdictionId}
Content-Type: application/json

{
  "name": "Updated Group Name"
}
```

**Response** (200 OK):
```json
{
  "id": 1,
  "name": "Updated Group Name"
}
```

### Delete Group

Deletes a group (must be empty).

```
DELETE /api/jurisdiction-admin/groups/{groupId}?jurisdiction_id={jurisdictionId}
```

**Response** (204 No Content)

**Error** (400 Bad Request - if group has services):
```json
{
  "message": "Cannot delete group with associated services"
}
```

## Service Operations

### Create Service

Creates a new service in a group (+ Add Svc button).

```
POST /api/jurisdiction-admin/services?jurisdiction_id={jurisdictionId}
Content-Type: application/json

{
  "serviceName": "New Service",
  "description": "Description of the service",
  "groupId": 1,
  "type": "realtime"
}
```

**Response** (201 Created):
```json
{
  "service_code": 103,
  "service_name": "New Service",
  "description": "Description of the service",
  "group_id": 1,
  "type": "realtime"
}
```

### Update Service

Updates service name/description (Header Card save).

```
PATCH /api/jurisdiction-admin/services/{serviceCode}?jurisdiction_id={jurisdictionId}
Content-Type: application/json

{
  "serviceName": "Updated Name",
  "description": "Updated description"
}
```

**Response** (200 OK):
```json
{
  "service_code": 101,
  "service_name": "Updated Name",
  "description": "Updated description",
  "group_id": 1
}
```

### Delete Service

Deletes a service and all its attributes.

```
DELETE /api/jurisdiction-admin/services/{serviceCode}?jurisdiction_id={jurisdictionId}
```

**Response** (204 No Content)

### Reorder Services

Updates the order of services within a group (tree drag-drop).

```
PATCH /api/jurisdiction-admin/groups/{groupId}/services-order?jurisdiction_id={jurisdictionId}
Content-Type: application/json

{
  "serviceCodeList": [102, 101, 103]
}
```

**Response** (200 OK):
```json
{
  "message": "Services reordered successfully"
}
```

### Move Service to Different Group

Update service with new group_id.

```
PATCH /api/jurisdiction-admin/services/{serviceCode}?jurisdiction_id={jurisdictionId}
Content-Type: application/json

{
  "groupId": 2
}
```

## Attribute Operations

### Get Service Definition

Fetches a service with all its attributes (when service selected).

```
GET /api/services/{serviceCode}?jurisdiction_id={jurisdictionId}
```

**Response** (200 OK):
```json
{
  "service_code": 101,
  "attributes": [
    {
      "code": 1,
      "variable": true,
      "datatype": "singlevaluelist",
      "required": true,
      "description": "What is the pothole size?",
      "order": 0,
      "datatype_description": "Select the approximate size",
      "values": [
        { "key": "1", "name": "Small (< 6 inches)" },
        { "key": "2", "name": "Medium (6-12 inches)" },
        { "key": "3", "name": "Large (> 12 inches)" }
      ]
    },
    {
      "code": 2,
      "variable": true,
      "datatype": "text",
      "required": false,
      "description": "Additional details",
      "order": 1,
      "datatype_description": "Describe the location or condition"
    }
  ]
}
```

### Create Attribute

Adds a new attribute to a service (Add question).

```
POST /api/jurisdiction-admin/services/{serviceCode}/attributes?jurisdiction_id={jurisdictionId}
Content-Type: application/json

{
  "variable": true,
  "datatype": "string",
  "required": false,
  "description": "Question text here",
  "datatypeDescription": "Help text here"
}
```

**For list types, include values**:
```json
{
  "variable": true,
  "datatype": "singlevaluelist",
  "required": true,
  "description": "Select an option",
  "datatypeDescription": "Choose one",
  "values": [
    { "valueName": "Option 1" },
    { "valueName": "Option 2" }
  ]
}
```

**Response** (201 Created):
```json
{
  "code": 5,
  "variable": true,
  "datatype": "string",
  "required": false,
  "description": "Question text here",
  "order": 2,
  "datatype_description": "Help text here"
}
```

### Update Attribute

Updates an attribute (Attribute Card save).

```
PATCH /api/jurisdiction-admin/services/{serviceCode}/attributes/{attributeCode}?jurisdiction_id={jurisdictionId}
Content-Type: application/json

{
  "description": "Updated question text",
  "required": true,
  "datatypeDescription": "Updated help text",
  "values": [
    { "valueName": "New Option 1" },
    { "valueName": "New Option 2" },
    { "valueName": "New Option 3" }
  ]
}
```

**Response** (200 OK): Updated attribute object

### Delete Attribute

Deletes an attribute.

```
DELETE /api/jurisdiction-admin/services/{serviceCode}/attributes/{attributeCode}?jurisdiction_id={jurisdictionId}
```

**Response** (204 No Content)

### Reorder Attributes

Updates the order of attributes (card drag-drop).

```
PATCH /api/jurisdiction-admin/services/{serviceCode}/attributes-order?jurisdiction_id={jurisdictionId}
Content-Type: application/json

{
  "attributeCodeList": [2, 1, 5]
}
```

**Response** (200 OK):
```json
{
  "message": "Attributes reordered successfully"
}
```

## Error Responses

All endpoints return standard error format:

```json
{
  "message": "Error description",
  "status": 400
}
```

| Status | Meaning |
|--------|---------|
| 400 | Bad Request - validation error |
| 401 | Unauthorized - missing/invalid JWT |
| 403 | Forbidden - insufficient permissions |
| 404 | Not Found - resource doesn't exist |
| 500 | Server Error - unexpected failure |

## Frontend API Client

All these endpoints are already wrapped in the `Libre311` service class at:
`frontend/src/lib/services/Libre311/Libre311.ts`

No new API client code is required for this feature.
