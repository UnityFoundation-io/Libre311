# Service Definition Configuration Model

This document describes the backend implementation of service definition configuration in Libre311, including the data model, supported features, and API endpoints.

## Data Model

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              JURISDICTION                                    │
│  (Top-level tenant - e.g., a city or municipality)                          │
└─────────────────────────────────────────────────────────────────────────────┘
                │
                │ 1:N
                ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            SERVICE GROUP                                     │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │ id: Long (PK)                                                        │    │
│  │ name: String (required, unique per jurisdiction)                     │    │
│  │ jurisdiction_id: FK                                                  │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│  Examples: "Sanitation", "Parks & Recreation", "Roads & Traffic"            │
└─────────────────────────────────────────────────────────────────────────────┘
                │
                │ 1:N
                ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                               SERVICE                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │ id: Long (PK, acts as service_code)                                  │    │
│  │ serviceName: String (required)                                       │    │
│  │ description: String (optional)                                       │    │
│  │ type: ServiceType (REALTIME | BATCH | BLACKBOX)                      │    │
│  │ orderPosition: int (display ordering, default -1)                    │    │
│  │ service_group_id: FK (required)                                      │    │
│  │ jurisdiction_id: FK                                                  │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│  Examples: "Pothole Repair", "Graffiti Removal", "Broken Streetlight"       │
└─────────────────────────────────────────────────────────────────────────────┘
                │
                │ 1:N (cascade delete)
                ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    SERVICE DEFINITION ATTRIBUTE                              │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │ id: Long (PK, acts as attribute_code)                                │    │
│  │ service_id: FK (required)                                            │    │
│  │ variable: boolean (user-provided vs system-set)                      │    │
│  │ datatype: AttributeDataType (required)                               │    │
│  │ required: boolean                                                    │    │
│  │ description: String (field label/prompt)                             │    │
│  │ datatypeDescription: String (help text for the data type)            │    │
│  │ attributeOrder: int (display ordering)                               │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│  Examples: "Size of pothole", "Type of graffiti", "Pole number"             │
└─────────────────────────────────────────────────────────────────────────────┘
                │
                │ 1:N (cascade delete, eager fetch)
                ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          ATTRIBUTE VALUE                                     │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │ id: Long (PK)                                                        │    │
│  │ service_definition_attribute_id: FK (required)                       │    │
│  │ valueName: String (required, not blank)                              │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│  Only used for SINGLEVALUELIST and MULTIVALUELIST datatypes                 │
│  Examples: "Small", "Medium", "Large" for pothole size dropdown             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Supported Attribute Data Types

From `app/src/main/java/app/model/service/AttributeDataType.java`:

| Type | Description | Use Case |
|------|-------------|----------|
| `STRING` | Single-line text input | Short answers (pole #, address) |
| `TEXT` | Multi-line text input | Detailed descriptions |
| `NUMBER` | Numeric input | Quantities, measurements |
| `DATETIME` | Date/time picker | Incident times, scheduling |
| `SINGLEVALUELIST` | Single-select dropdown | Categories, severity levels |
| `MULTIVALUELIST` | Multi-select checkboxes | Multiple applicable options |

## What Can Be Defined

### 1. Service Groups (Organizational Categories)

- Logical grouping of related services
- Each group has a unique name within a jurisdiction
- Cannot be deleted if services are assigned to it

### 2. Services (Request Types)

The actual issue types citizens can report.

Properties:
- **Name**: Display name (e.g., "Pothole Repair")
- **Description**: Detailed explanation
- **Type**: Processing mode (realtime/batch/blackbox per Open311 spec)
- **Order Position**: Controls display sequence in lists

### 3. Service Definition Attributes (Custom Fields)

Dynamic form fields specific to each service type.

Configurable properties:
- **Variable**: `true` = user enters value, `false` = system-generated
- **Required**: Validation enforcement
- **Description**: Field label/prompt shown to user
- **Datatype Description**: Help text explaining expected input
- **Attribute Order**: Display sequence

### 4. Attribute Values (Dropdown Options)

- Predefined choices for list-type attributes
- Required when using `SINGLEVALUELIST` or `MULTIVALUELIST`
- Each value has a name displayed to users

## Example Configuration

```
Jurisdiction: "City of Springfield"
│
├── Service Group: "Roads & Traffic"
│   ├── Service: "Pothole Repair"
│   │   ├── Attribute: "Pothole Size" (SINGLEVALUELIST, required)
│   │   │   ├── Value: "Small (< 6 inches)"
│   │   │   ├── Value: "Medium (6-12 inches)"
│   │   │   └── Value: "Large (> 12 inches)"
│   │   ├── Attribute: "Location Details" (TEXT, optional)
│   │   └── Attribute: "Estimated Depth" (NUMBER, optional)
│   │
│   └── Service: "Broken Streetlight"
│       ├── Attribute: "Pole Number" (STRING, optional)
│       └── Attribute: "Light Status" (SINGLEVALUELIST, required)
│           ├── Value: "Not Working"
│           ├── Value: "Flickering"
│           └── Value: "Damaged"
│
└── Service Group: "Sanitation"
    └── Service: "Graffiti Removal"
        ├── Attribute: "Surface Type" (MULTIVALUELIST, required)
        │   ├── Value: "Brick"
        │   ├── Value: "Concrete"
        │   ├── Value: "Metal"
        │   └── Value: "Wood"
        └── Attribute: "Offensive Content" (SINGLEVALUELIST, required)
            ├── Value: "Yes"
            └── Value: "No"
```

## Key Implementation Files

| Component | Location |
|-----------|----------|
| Entities | `app/src/main/java/app/model/` |
| Service layer | `app/src/main/java/app/service/service/ServiceService.java` |
| Admin API | `app/src/main/java/app/JurisdictionAdminController.java` |
| DTOs | `app/src/main/java/app/dto/servicedefinition/` |
| Migrations | `app/src/main/resources/db/migration/V4__add_service_definitions.sql` |

### Entity Files

- `app/src/main/java/app/model/service/group/ServiceGroup.java`
- `app/src/main/java/app/model/service/Service.java`
- `app/src/main/java/app/model/servicedefinition/ServiceDefinitionAttribute.java`
- `app/src/main/java/app/model/servicedefinition/AttributeValue.java`
- `app/src/main/java/app/model/service/AttributeDataType.java`

### DTO Files

- `app/src/main/java/app/dto/servicedefinition/ServiceDefinitionDTO.java`
- `app/src/main/java/app/dto/servicedefinition/ServiceDefinitionAttributeDTO.java`
- `app/src/main/java/app/dto/servicedefinition/CreateServiceDefinitionAttributeDTO.java`
- `app/src/main/java/app/dto/servicedefinition/UpdateServiceDefinitionAttributeDTO.java`
- `app/src/main/java/app/dto/servicedefinition/AttributeValueDTO.java`
- `app/src/main/java/app/dto/group/GroupDTO.java`
- `app/src/main/java/app/dto/group/CreateUpdateGroupDTO.java`
- `app/src/main/java/app/dto/service/ServiceDTO.java`
- `app/src/main/java/app/dto/service/CreateServiceDTO.java`
- `app/src/main/java/app/dto/service/UpdateServiceDTO.java`

## API Endpoints for Configuration

All endpoints require authentication and appropriate permissions.

### Group Operations

| Operation | Method | Endpoint |
|-----------|--------|----------|
| List groups | GET | `/api/jurisdiction-admin/groups` |
| Create group | POST | `/api/jurisdiction-admin/groups` |
| Update group | PATCH | `/api/jurisdiction-admin/groups/{groupId}` |
| Delete group | DELETE | `/api/jurisdiction-admin/groups/{groupId}` |

### Service Operations

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Create service | POST | `/api/jurisdiction-admin/services` |
| Update service | PATCH | `/api/jurisdiction-admin/services/{serviceCode}` |
| Delete service | DELETE | `/api/jurisdiction-admin/services/{serviceCode}` |
| Reorder services | PATCH | `/api/jurisdiction-admin/groups/{groupId}/services-order` |

### Attribute Operations

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Add attribute | POST | `/api/jurisdiction-admin/services/{serviceCode}/attributes` |
| Update attribute | PATCH | `/api/jurisdiction-admin/services/{serviceCode}/attributes/{attributeCode}` |
| Delete attribute | DELETE | `/api/jurisdiction-admin/services/{serviceCode}/attributes/{attributeCode}` |
| Reorder attributes | PATCH | `/api/jurisdiction-admin/services/{serviceCode}/attributes-order` |

## Implementation Notes

### Cascade Delete Behavior

- Service deletion cascades to ServiceDefinitionAttributes and their AttributeValues
- ServiceDefinitionAttribute deletion cascades to AttributeValues
- All configured with JPA cascade and Hibernate `@OnDelete`

### Ordering

- Services ordered by `orderPosition` field
- Attributes ordered by `attributeOrder` field
- Dedicated PATCH endpoints for reordering operations with transactional updates

### Validation Rules

- Services must belong to an existing group in the same jurisdiction
- Groups must not have duplicate names within a jurisdiction
- Groups cannot be deleted if they have associated services
- `SINGLEVALUELIST` and `MULTIVALUELIST` attributes require at least one AttributeValue
- Service names cannot be blank

### Open311 Compliance

This implementation follows the **Open311 GeoReport v2** specification while adding the group organizational layer on top for better service categorization.
