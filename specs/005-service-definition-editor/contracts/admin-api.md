# Service Definition Admin API Contract

**Feature**: 005-service-definition-editor
**Date**: 2026-01-05
**Status**: EXISTING - No changes required

All endpoints documented below already exist in `JurisdictionAdminController.java`. This document serves as a reference for frontend implementation.

## Base URL

```
/api/jurisdiction-admin
```

All endpoints require:
- `jurisdiction_id` query parameter
- JWT Bearer token in Authorization header

---

## Service Groups

### List Groups

```
GET /groups/?jurisdiction_id={jurisdictionId}
```

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "name": "Roads & Traffic"
  },
  {
    "id": 2,
    "name": "Sanitation"
  }
]
```

### Create Group

```
POST /groups/?jurisdiction_id={jurisdictionId}
Content-Type: application/json

{
  "name": "New Group Name"
}
```

**Response**: `201 Created`
```json
{
  "id": 3,
  "name": "New Group Name"
}
```

### Update Group

```
PATCH /groups/{groupId}?jurisdiction_id={jurisdictionId}
Content-Type: application/json

{
  "name": "Updated Group Name"
}
```

**Response**: `200 OK`
```json
{
  "id": 1,
  "name": "Updated Group Name"
}
```

### Delete Group

```
DELETE /groups/{groupId}?jurisdiction_id={jurisdictionId}
```

**Response**: `204 No Content`

**Note**: Per spec clarification, deletion should fail if group contains services. Frontend must check and display appropriate error message.

---

## Services

### Create Service

```
POST /services?jurisdiction_id={jurisdictionId}
Content-Type: application/json

{
  "service_name": "Pothole Repair",
  "group_id": 1
}
```

**Response**: `201 Created`
```json
{
  "service_code": 42,
  "service_name": "Pothole Repair",
  "description": null,
  "metadata": true,
  "type": "realtime",
  "group_id": 1,
  "jurisdiction_id": "stlma"
}
```

### Update Service

```
PATCH /services/{serviceCode}?jurisdiction_id={jurisdictionId}
Content-Type: application/json

{
  "service_name": "Updated Service Name"
}
```

**Response**: `200 OK`
```json
{
  "service_code": 42,
  "service_name": "Updated Service Name",
  "description": null,
  "metadata": true,
  "type": "realtime",
  "group_id": 1
}
```

### Delete Service

```
DELETE /services/{serviceCode}?jurisdiction_id={jurisdictionId}
```

**Response**: `204 No Content`

### Reorder Services in Group

```
PATCH /groups/{groupId}/services-order?jurisdiction_id={jurisdictionId}
Content-Type: application/json

[
  { "service_code": 42, "order_position": 0 },
  { "service_code": 43, "order_position": 1 },
  { "service_code": 44, "order_position": 2 }
]
```

**Response**: `200 OK` - Returns updated service list

---

## Service Definition (Attributes)

### Get Service Definition

```
GET /api/services/{serviceCode}?jurisdiction_id={jurisdictionId}
```

**Note**: This is a public Open311 endpoint (not under /jurisdiction-admin)

**Response**: `200 OK`
```json
{
  "service_code": 42,
  "attributes": [
    {
      "code": 101,
      "variable": true,
      "datatype": "string",
      "required": true,
      "datatype_description": "Enter your answer",
      "order": 1,
      "description": "What is the issue?"
    },
    {
      "code": 102,
      "variable": true,
      "datatype": "multivaluelist",
      "required": false,
      "datatype_description": "Select all that apply",
      "order": 2,
      "description": "Type of damage",
      "values": [
        { "key": "1", "name": "Surface crack" },
        { "key": "2", "name": "Deep hole" },
        { "key": "3", "name": "Other" }
      ]
    }
  ]
}
```

### Create Attribute

```
POST /services/{serviceCode}/attributes?jurisdiction_id={jurisdictionId}
Content-Type: application/json

{
  "description": "What is the issue?",
  "datatype": "string",
  "datatype_description": "Enter your answer",
  "variable": true,
  "required": true,
  "order": 1,
  "values": []
}
```

**Response**: `201 Created`
```json
{
  "service_code": 42,
  "attributes": [/* full updated attribute list */]
}
```

### Update Attribute

```
PATCH /services/{serviceCode}/attributes/{attributeCode}?jurisdiction_id={jurisdictionId}
Content-Type: application/json

{
  "description": "Updated question text",
  "datatype_description": "Updated help text",
  "required": false,
  "values": [
    { "key": "1", "name": "Option A" },
    { "key": "2", "name": "Option B" }
  ]
}
```

**Response**: `200 OK`
```json
{
  "service_code": 42,
  "attributes": [/* full updated attribute list */]
}
```

### Delete Attribute

```
DELETE /services/{serviceCode}/attributes/{attributeCode}?jurisdiction_id={jurisdictionId}
```

**Response**: `204 No Content`

### Reorder Attributes

```
PATCH /services/{serviceCode}/attributes-order?jurisdiction_id={jurisdictionId}
Content-Type: application/json

[
  { "code": 102, "order": 1 },
  { "code": 101, "order": 2 },
  { "code": 103, "order": 3 }
]
```

**Response**: `200 OK`
```json
{
  "service_code": 42,
  "attributes": [/* full updated attribute list with new order */]
}
```

---

## Error Responses

### 400 Bad Request
```json
{
  "message": "Validation failed",
  "errors": [
    { "field": "name", "message": "Name is required" }
  ]
}
```

### 401 Unauthorized
```json
{
  "message": "Authentication required"
}
```

### 403 Forbidden
```json
{
  "message": "Insufficient permissions"
}
```

### 404 Not Found
```json
{
  "message": "Resource not found"
}
```

---

## Frontend Client Methods

All endpoints are already wrapped in `Libre311.ts`:

| Endpoint | Client Method |
|----------|---------------|
| GET /groups/ | `getGroupList()` |
| POST /groups/ | `createGroup(params)` |
| PATCH /groups/{id} | `editGroup(params)` |
| POST /services | `createService(params)` |
| PATCH /services/{code} | `editService(params)` |
| DELETE /services/{code} | `deleteService(params)` |
| PATCH /groups/{id}/services-order | `updateServicesOrder(params)` |
| GET /api/services/{code} | `getServiceDefinition(params)` |
| POST /services/{code}/attributes | `createAttribute(params)` |
| PATCH /services/{code}/attributes/{code} | `editAttribute(params)` |
| DELETE /services/{code}/attributes/{code} | `deleteAttribute(params)` |
| PATCH /services/{code}/attributes-order | `updateAttributesOrder(params)` |
