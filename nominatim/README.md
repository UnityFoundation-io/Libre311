# Local Nominatim Setup for Libre311

This directory contains everything needed to run a local Nominatim geocoding service for development and testing.

## Overview

- **Image**: [mediagis/nominatim](https://hub.docker.com/r/mediagis/nominatim/) (all-in-one Docker image)
- **Data**: Missouri state OSM extract from [Geofabrik](https://download.geofabrik.de/north-america/us/missouri.html)
- **Coverage**: Greater St. Louis Metropolitan Area (St. Louis City, St. Louis County, and surrounding areas)
- **Port**: 8088 (to avoid conflict with backend on 8080)

## TIGER Address Data

By default, the setup imports [TIGER address data](https://nominatim.org/release-docs/latest/customize/Tiger/) from the US Census Bureau. This enables:

- **House number interpolation** - Resolves specific street addresses (e.g., "1234 Oak Street") even when not explicitly mapped in OSM
- **Better residential coverage** - Most US residential addresses work out of the box

| Setting | Impact |
|---------|--------|
| Downloads | ~600 MB additional data |
| Import time | +30-60 minutes |
| Disk space | +1-2 GB |

To disable TIGER import (faster setup, less coverage):
```bash
# In .env
IMPORT_TIGER_ADDRESSES=false
```

## Quick Start

### 1. Start Nominatim

```bash
cd nominatim
docker compose up -d
```

### 2. Monitor Import Progress

The first startup downloads OSM data and imports it into PostgreSQL. This takes **45-90 minutes** with TIGER data (default), or **15-30 minutes** without TIGER.

```bash
# Watch import logs
docker compose logs -f nominatim

# Check if import is complete (look for "Starting Apache" message)
docker compose logs nominatim | grep -i apache
```

### 3. Verify Service is Running

```bash
# Check status endpoint
curl http://localhost:8088/status

# Or run the test script
./test-nominatim.sh
```

### 4. Test Geocoding

```bash
# Forward geocoding
curl "http://localhost:8088/search?q=City+Hall,+St+Louis,+MO&format=json"

# Reverse geocoding
curl "http://localhost:8088/reverse?lat=38.6270&lon=-90.1994&format=json"
```

## Resource Requirements

| Resource | Requirement | Notes |
|----------|-------------|-------|
| Disk Space | ~5-8 GB | After import with TIGER (~3-5 GB without) |
| Memory | 4-6 GB | During import; 1-2 GB at runtime |
| CPU | 4+ cores | For import; fewer at runtime |
| Import Time | 45-90 min | With TIGER (default); 15-30 min without |

## File Structure

```
nominatim/
├── docker-compose.yml      # Docker Compose configuration
├── .env                    # Environment variables
├── test-nominatim.sh       # Test script for validation
├── integration-example.ts  # leaflet-geosearch integration code
└── README.md              # This file
```

## API Endpoints

All endpoints are compatible with the [Nominatim API documentation](https://nominatim.org/release-docs/latest/api/Overview/).

| Endpoint | Description | Example |
|----------|-------------|---------|
| `/search` | Forward geocoding | `?q=City+Hall,+St+Louis&format=json` |
| `/reverse` | Reverse geocoding | `?lat=38.627&lon=-90.199&format=json` |
| `/lookup` | Lookup by OSM ID | `?osm_ids=R12345&format=json` |
| `/status` | Service health | Returns import status |

### Search Parameters

| Parameter | Description | Example |
|-----------|-------------|---------|
| `q` | Search query | `City Hall, St Louis, MO` |
| `format` | Output format | `json`, `jsonv2`, `xml` |
| `limit` | Max results | `5` |
| `addressdetails` | Include address breakdown | `1` |
| `viewbox` | Bounding box preference | `-90.4,38.5,-90.0,38.8` |
| `bounded` | Strict bounding box | `1` |

### Reverse Parameters

| Parameter | Description | Example |
|-----------|-------------|---------|
| `lat` | Latitude | `38.6270` |
| `lon` | Longitude | `-90.1994` |
| `format` | Output format | `json` |
| `addressdetails` | Include address breakdown | `1` |
| `zoom` | Detail level (0-18) | `18` |

## Development Workflow

### Starting/Stopping

```bash
# Start in background
docker compose up -d

# Stop (preserves data)
docker compose stop

# Stop and remove containers (preserves data volume)
docker compose down

# Stop and remove everything including data
docker compose down -v
```

### Viewing Logs

```bash
# Follow logs
docker compose logs -f nominatim

# Last 100 lines
docker compose logs --tail=100 nominatim

# Search for errors
docker compose logs nominatim 2>&1 | grep -i error
```

### Checking Resource Usage

```bash
# Container stats
docker stats nominatim

# Disk usage
docker system df -v | grep nominatim
```

### Rebuilding from Scratch

If you need to reimport data:

```bash
# Remove everything and start fresh
docker compose down -v
docker compose up -d
```

## Libre311 Backend Integration

To configure the Libre311 backend to use your local Nominatim instance:

### Local Development (without Docker)

Add to your `setenv.sh`:

```bash
export NOMINATIM_URL=http://localhost:8088
```

### Docker Development

Add to your `.env.docker`:

```bash
# Use the local Nominatim instance for geocoding
NOMINATIM_URL=http://host.docker.internal:8088
```

Note: Use `host.docker.internal` to reach services running on your host machine from within Docker containers. The port is `8088` (the host-mapped port), not the internal container port.

### Verification

After configuring, you can verify the backend is using your local Nominatim by calling the geocode endpoint:

```bash
curl "http://localhost:8080/api/geocode/reverse?lat=38.6270&lon=-90.1994"
```

## Troubleshooting

### Service Not Ready After Startup

**Symptom**: `/status` returns error or "Database not ready"

**Cause**: Import is still in progress

**Solution**: Wait for import to complete. Check logs:
```bash
docker compose logs -f nominatim | grep -E "(import|apache|ready)"
```

Look for: "Starting Apache" or "Nominatim is ready"

### Out of Memory During Import

**Symptom**: Container crashes or system becomes unresponsive

**Solution**: Reduce memory settings in `.env`:
```bash
POSTGRES_SHARED_BUFFERS=1GB
POSTGRES_MAINTENANCE_WORK_MEM=512MB
POSTGRES_EFFECTIVE_CACHE_SIZE=2GB
MEMORY_LIMIT=4G
```

### Slow Query Performance

**Symptom**: Queries take >2 seconds

**Solutions**:
1. Ensure import is complete (check `/status`)
2. Increase `POSTGRES_SHARED_BUFFERS` in `.env`
3. Restart container after config changes

### No Results for Valid Address

**Symptom**: Known addresses return empty results

**Causes**:
- Address not in OSM data
- Different address format
- Bounded search too restrictive

**Solutions**:
1. Try without bounding box parameters
2. Try different query formats (with/without city, state)
3. Check if address exists: https://www.openstreetmap.org

### Port Conflict

**Symptom**: "Port 8088 already in use"

**Solution**: Change port in `.env`:
```bash
NOMINATIM_PORT=8089
```

### Container Won't Start

**Solution**: Check Docker Desktop memory allocation:
1. Open Docker Desktop > Settings > Resources
2. Ensure at least 6 GB memory allocated
3. Restart Docker Desktop

## Updating OSM Data

To update with latest OSM data:

```bash
# Option 1: Manual update (faster)
docker compose exec nominatim nominatim replication --update

# Option 2: Full reimport (complete refresh)
docker compose down -v
docker compose up -d
```

## Alternative Data Sources

### Missouri (Current)
- **File**: missouri-latest.osm.pbf
- **Size**: ~156 MB
- **URL**: https://download.geofabrik.de/north-america/us/missouri-latest.osm.pbf
- **Coverage**: Full state including St. Louis metro

### Illinois (to include East St. Louis)
If you need Illinois-side coverage:
```bash
# In .env, change:
PBF_URL=https://download.geofabrik.de/north-america/us/illinois-latest.osm.pbf
```
Note: Illinois is larger (~500 MB), import takes longer.

### Custom Extract
Use [BBBike Extract](https://extract.bbbike.org/) to create a custom bounding box for just the metro area (~50 MB estimate).

## Performance Tuning

### For Faster Import

```bash
# In .env:
THREADS=8                        # Use more CPU cores
IMPORT_STYLE=address             # Skip POI data
POSTGRES_SYNCHRONOUS_COMMIT=off  # Already default
```

### For Better Query Performance

```bash
# In .env:
POSTGRES_SHARED_BUFFERS=4GB
POSTGRES_EFFECTIVE_CACHE_SIZE=8GB
```

### For Lower Memory Usage

```bash
# In .env:
POSTGRES_SHARED_BUFFERS=512MB
POSTGRES_MAINTENANCE_WORK_MEM=256MB
MEMORY_LIMIT=3G
FLATNODE_FILE=/nominatim/flatnode.file  # Trade disk for RAM
```

## Success Criteria Checklist

After setup, verify these work:

- [ ] Service running: `curl http://localhost:8088/status` returns 200
- [ ] Forward geocoding: "City Hall, St Louis, MO" returns coordinates
- [ ] Reverse geocoding: (38.6270, -90.1994) returns address
- [ ] Response time: Queries complete in <2 seconds
- [ ] Persistence: Data survives `docker compose stop/start`

Run the test script to check all criteria:
```bash
./test-nominatim.sh
```

## References

- [Nominatim Documentation](https://nominatim.org/release-docs/latest/)
- [mediagis/nominatim Docker Image](https://github.com/mediagis/nominatim-docker)
- [Geofabrik Download Server](https://download.geofabrik.de/)
- [leaflet-geosearch Documentation](https://smeijer.github.io/leaflet-geosearch/)
- [OpenStreetMap Nominatim API](https://nominatim.org/release-docs/latest/api/Overview/)