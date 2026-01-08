# Jurisdiction API Guide

This document describes how to create and modify jurisdictions in Libre311 using the Tenant Admin API.

## Prerequisites

- A running Libre311 API server
- Authentication via UnityAuth with one of these permissions:
  - `LIBRE311_ADMIN_EDIT-SYSTEM` (System Admin)
  - `LIBRE311_ADMIN_EDIT-TENANT` (Tenant Admin)
- A valid JWT bearer token

> **Note:** Jurisdiction Admins (`LIBRE311_ADMIN_EDIT-SUBTENANT`) cannot create or modify jurisdictions.

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/tenant-admin/jurisdictions?tenant_id={tenant_id}` | Create a new jurisdiction |
| PATCH | `/api/tenant-admin/jurisdictions/{jurisdiction_id}?tenant_id={tenant_id}` | Update an existing jurisdiction |

## Create a Jurisdiction

### Endpoint

```
POST /api/tenant-admin/jurisdictions?tenant_id={tenant_id}
```

### Request Body

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | Yes | Display name for the jurisdiction |
| `jurisdiction_id` | string | Yes | Unique identifier (used in API queries) |
| `bounds` | array | Yes | Geographic boundary as coordinate pairs (minimum 4 points, must be closed) |
| `primary_color` | string | No | HSL color value (format: `"H, S%, L%"` e.g., `"221, 83%, 53%"`) |
| `primary_hover_color` | string | No | HSL color value for hover state |
| `logo_media_url` | string | No | URL to the jurisdiction's logo |
| `terms_of_use_content` | string | No | Terms of use content (markdown, max 50000 chars) |
| `privacy_policy_content` | string | No | Privacy policy content (markdown, max 50000 chars) |

### Bounds Format

The `bounds` field is an array of `[latitude, longitude]` coordinate pairs that define a closed polygon:

- Minimum 4 coordinate pairs required
- First and last coordinates must be identical (to close the polygon)
- Uses WGS84 coordinate system (SRID 4326)
- Format: `[lat, lon]` where lat is -90 to 90 and lon is -180 to 180

### Example Request

This example creates a new jurisdiction for "City of Springfield" with custom branding colors, a logo, and a geographic boundary defined by 5 coordinate pairs (a closed rectangle):

```bash
curl -X POST "http://localhost:8080/api/tenant-admin/jurisdictions?tenant_id=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "City of Springfield",
    "jurisdiction_id": "springfield.city",
    "primary_color": "221, 83%, 53%",
    "primary_hover_color": "221, 83%, 45%",
    "logo_media_url": "https://example.com/logos/springfield.png",
    "bounds": [
      [38.68777201455936, -90.30025693587594],
      [38.61729515893717, -90.34433315946103],
      [38.61061640035771, -90.2360192439154],
      [38.68231721278784, -90.20658055119375],
      [38.68777201455936, -90.30025693587594]
    ]
  }'
```

### Example Response

```json
{
  "name": "City of Springfield",
  "jurisdiction_id": "springfield.city",
  "tenant_id": 1,
  "primary_color": "221, 83%, 53%",
  "primary_hover_color": "221, 83%, 45%",
  "logo_media_url": "https://example.com/logos/springfield.png",
  "auth_base_url": "https://auth.example.com",
  "terms_of_use_content": null,
  "privacy_policy_content": null,
  "bounds": [
    [38.68777201455936, -90.30025693587594],
    [38.61729515893717, -90.34433315946103],
    [38.61061640035771, -90.2360192439154],
    [38.68231721278784, -90.20658055119375],
    [38.68777201455936, -90.30025693587594]
  ]
}
```

## Update a Jurisdiction

### Endpoint

```
PATCH /api/tenant-admin/jurisdictions/{jurisdiction_id}?tenant_id={tenant_id}
```

### Request Body

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | **Yes** | Display name (required for all updates) |
| `bounds` | array | No | Updated geographic boundary |
| `primary_color` | string | No | Updated HSL color value |
| `primary_hover_color` | string | No | Updated HSL hover color value |
| `logo_media_url` | string | No | Updated logo URL |
| `terms_of_use_content` | string | No | Updated terms of use (markdown, max 50000 chars) |
| `privacy_policy_content` | string | No | Updated privacy policy (markdown, max 50000 chars) |

> **Note:** The `jurisdiction_id` cannot be changed after creation.

> **Note:** The `name` field is required for all PATCH requests due to backend validation.

### Example Request

```bash
curl -X PATCH "http://localhost:8080/api/tenant-admin/jurisdictions/springfield.city?tenant_id=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "City of Springfield - Americas Heartland",
    "primary_color": "180, 70%, 45%"
  }'
```

### Example: Update with Policy Content

```bash
curl -X PATCH "http://localhost:8080/api/tenant-admin/jurisdictions/springfield.city?tenant_id=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "City of Springfield",
    "terms_of_use_content": "# Terms of Use\n\nBy using this service...",
    "privacy_policy_content": "# Privacy Policy\n\nWe collect..."
  }'
```

## CLI Utility

A command-line utility is available for updating jurisdictions. See [utils/README.md](../utils/README.md) for details.

```bash
# Update jurisdiction name and bounds
source utils/.libre311rc
utils/update-jurisdiction.sh demo -n 'Demo Jurisdiction' --bounds-file utils/data/bounds-us.json
```

## Error Responses

| HTTP Status | Description |
|-------------|-------------|
| 400 Bad Request | Invalid input (e.g., invalid color format, bounds not tuples) |
| 401 Unauthorized | Missing or invalid authentication token |
| 403 Forbidden | Insufficient permissions |
| 404 Not Found | Jurisdiction not found |
| 500 Internal Server Error | Server error (e.g., bounds polygon not closed) |

### Common Validation Errors

1. **Color format invalid**: Colors must match the pattern `H, S%, L%` (e.g., `"221, 83%, 53%"`)
2. **Bounds not closed**: First and last coordinate pairs must be identical
3. **Insufficient bounds**: Minimum 4 coordinate pairs required
4. **Non-tuple coordinates**: Each coordinate must be exactly `[latitude, longitude]`
5. **Name required**: The `name` field is required for PATCH requests

## Color Format

The `primary_color` and `primary_hover_color` fields use HSL (Hue, Saturation, Lightness) format with comma separators:

- **Hue**: 0-360 (degrees on the color wheel)
- **Saturation**: 0%-100%
- **Lightness**: 0%-100%

Format: `"H, S%, L%"` (comma-separated with optional spaces)

Examples:
- Blue: `"221, 83%, 53%"`
- Green: `"120, 60%, 50%"`
- Red: `"0, 80%, 50%"`

## Bounds Coordinate Format

Bounds use `[latitude, longitude]` format (NOT GeoJSON's `[lon, lat]`):

| Component | Range | Example |
|-----------|-------|---------|
| Latitude | -90 to 90 | 38.627 (St. Louis) |
| Longitude | -180 to 180 | -90.199 (St. Louis) |

Example bounds for St. Louis area:
```json
[
  [38.68777, -90.30025],
  [38.61729, -90.34433],
  [38.61061, -90.23601],
  [38.68231, -90.20658],
  [38.68777, -90.30025]
]
```

## Related Files

- Controller: `app/src/main/java/app/TenantAdminController.java`
- Service: `app/src/main/java/app/service/jurisdiction/JurisdictionService.java`
- DTOs: `app/src/main/java/app/dto/jurisdiction/`
- Tests: `app/src/test/java/app/TenantAdminControllerTest.java`
- CLI Utility: `utils/update-jurisdiction.sh`
- Example Bounds: `utils/data/bounds-us.json`, `utils/data/bounds-stlma.json`
