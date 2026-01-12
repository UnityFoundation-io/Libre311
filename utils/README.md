# Libre311 Utilities

Command-line utilities for managing Libre311 deployments.

## Prerequisites

- `curl` - for API requests
- `jq` - for JSON processing
- Valid UnityAuth credentials with admin permissions

## Configuration

Copy the example configuration file and customize for your environment:

```bash
cp .libre311rc.example .libre311rc
```

Edit `.libre311rc` with your deployment settings:

```bash
# Base URL of the Libre311 API (no trailing slash)
export API_BASE_URL='https://your-deployment.example.com'

# Base URL of the UnityAuth service
export AUTH_BASE_URL='https://auth.example.com'

# Tenant ID (usually 1 for single-tenant deployments)
export TENANT_ID='1'

# Optional: Set AUTH_TOKEN for non-interactive use
# export AUTH_TOKEN='your-jwt-token-here'
```

Alternatively, set `LIBRE311_CONFIG` to use a config file in a different location:

```bash
export LIBRE311_CONFIG=/path/to/your/config
```

## Scripts

### update-jurisdiction.sh

Update an existing jurisdiction's configuration including name, colors, logo, policies, and geographic bounds.

**Usage:**

```bash
source .libre311rc
./update-jurisdiction.sh <jurisdiction_id> [options]
```

**Options:**

| Option | Description |
|--------|-------------|
| `-n, --name <name>` | Update display name (required for all updates) |
| `-c, --primary-color <color>` | Update primary color (HSL format: 'H S% L%') |
| `-C, --primary-hover-color <color>` | Update hover color (HSL format: 'H S% L%') |
| `-l, --logo-url <url>` | Update logo URL |
| `-p, --privacy-policy <file>` | Update privacy policy from markdown file |
| `-t, --terms-of-use <file>` | Update terms of use from markdown file |
| `-b, --bounds-file <file>` | Update bounds from JSON file |
| `-d, --dry-run` | Preview changes without making them |
| `-y, --yes` | Skip confirmation prompt |
| `-v, --verbose` | Enable verbose debug output |
| `-h, --help` | Show help message |

**Examples:**

```bash
# Update jurisdiction name
./update-jurisdiction.sh demo -n 'Demo Jurisdiction'

# Update name and colors
./update-jurisdiction.sh demo -n 'Demo' \
  --primary-color '221 83% 53%' \
  --primary-hover-color '221 83% 45%'

# Update geographic bounds
./update-jurisdiction.sh demo -n 'Demo' --bounds-file data/bounds-us.json

# Preview changes without applying them
./update-jurisdiction.sh demo -n 'Test Name' --dry-run

# Update with policies from markdown files
./update-jurisdiction.sh demo -n 'Demo' \
  --privacy-policy privacy.md \
  --terms-of-use terms.md
```

**Required Permissions:**

Your UnityAuth account must have one of these roles:
- `LIBRE311_ADMIN_EDIT-SYSTEM`
- `LIBRE311_ADMIN_EDIT-TENANT`

## Example Data

The `data/` directory contains example jurisdiction boundary files:

### data/bounds-us.json

Continental United States bounding box. Useful for demo or national deployments.

```json
[
  [49.384358, -125.001651],
  [24.396308, -125.001651],
  [24.396308, -66.934570],
  [49.384358, -66.934570],
  [49.384358, -125.001651]
]
```

### data/bounds-stlma.json

St. Louis Metropolitan Area bounding box. Example of a regional jurisdiction.

```json
[
  [39.020416723317176, -92.39711460337287],
  [38.867419966739476, -92.39711460337287],
  [38.867419966739476, -92.2803785225467],
  [39.020416723317176, -92.2803785225467],
  [39.020416723317176, -92.39711460337287]
]
```

### Bounds File Format

Bounds files define a polygon as an array of `[latitude, longitude]` coordinate pairs. The polygon must be closed (first and last points must match).

```json
[
  [lat1, lon1],
  [lat2, lon2],
  [lat3, lon3],
  [lat4, lon4],
  [lat1, lon1]
]
```

You can generate bounds coordinates using tools like:
- [bboxfinder.com](http://bboxfinder.com/) - Draw a bounding box and get coordinates
- [geojson.io](https://geojson.io/) - Draw polygons and export as GeoJSON

## Exit Codes

| Code | Description |
|------|-------------|
| 0 | Success |
| 1 | Usage error |
| 2 | Authentication failed |
| 3 | API error |
| 4 | File not found |

## Troubleshooting

### "Jurisdiction not found" error

- Verify the jurisdiction ID exists by querying the API:
  ```bash
  curl "$API_BASE_URL/api/services?jurisdiction_id=<your-id>"
  ```
- Ensure `API_BASE_URL` has no trailing slash

### Authentication failures

- Check that `AUTH_BASE_URL` points to the correct UnityAuth instance
- Verify your credentials have the required permissions
- Try getting a fresh token by unsetting `AUTH_TOKEN`

### "HTTP 401 Unauthorized"

- Your JWT token may have expired
- Unset `AUTH_TOKEN` to prompt for fresh credentials:
  ```bash
  unset AUTH_TOKEN
  ```
