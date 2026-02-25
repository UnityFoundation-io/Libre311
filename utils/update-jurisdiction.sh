#!/bin/bash
set -e

# Version
VERSION="1.0.0"

# TODO: Query mode is disabled because TenantAdminController lacks a GET endpoint.
# The backend only has POST (create) and PATCH (update) endpoints for jurisdictions.
# To enable query mode, add a GET endpoint to TenantAdminController:
#   @Get(uris = {"/jurisdictions/{jurisdictionId}{?tenant_id}", ...})
#   @RequiresPermissions({LIBRE311_ADMIN_VIEW_SYSTEM, LIBRE311_ADMIN_VIEW_TENANT})
#   public JurisdictionDTO getJurisdiction(String jurisdictionId, @QueryValue Long tenant_id)
# See: app/src/main/java/app/TenantAdminController.java

# Exit codes
EXIT_SUCCESS=0
EXIT_USAGE=1
EXIT_AUTH_FAILED=2
EXIT_API_ERROR=3
EXIT_FILE_NOT_FOUND=4

# Load config file if it exists
CONFIG_FILE="${LIBRE311_CONFIG:-${HOME}/.libre311rc}"
[ -f "$CONFIG_FILE" ] && source "$CONFIG_FILE"

# Configuration - requires environment variables to be set
API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"
AUTH_BASE_URL="${AUTH_BASE_URL:-http://localhost:9090}"
AUTH_TOKEN="${AUTH_TOKEN}"
TENANT_ID="${TENANT_ID:-1}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

usage() {
  echo "Usage: $0 <jurisdiction_id> [options]"
  echo ""
  echo "Update an existing jurisdiction or create a new one."
  echo ""
  echo "Arguments:"
  echo "  jurisdiction_id              The string identifier of the jurisdiction"
  echo "                               (e.g., 'stlma', 'lomocomo'). This is NOT a numeric ID."
  echo "                               Use the jurisdiction_id from the jurisdictions table."
  echo ""
  echo "Options:"
  echo "  -X, --create                 Create a new jurisdiction instead of updating"
  echo "  -n, --name <name>            Display name (REQUIRED for all requests)"
  echo "  -c, --primary-color <color>    Primary color (HSL format: 'H S% L%')"
  echo "  -C, --primary-hover-color <color>"
  echo "                                 Hover color (HSL format: 'H S% L%')"
  echo "  -l, --logo-url <url>           Logo URL"
  echo "  -p, --privacy-policy <file>    Privacy policy from markdown file"
  echo "  -t, --terms-of-use <file>      Terms of use from markdown file"
  echo "  -b, --bounds-file <file>       Bounds from JSON file (REQUIRED for create)"
  echo "  -f, --project-feature <mode>   Project feature mode (DISABLED, OPTIONAL, REQUIRED)"
  echo "  -r, --remote-hosts <hosts>     Remote hosts (comma-separated list)"
  echo "  -u, --visibility-user <days>   Days closed requests are visible to regular users"
  echo "  -a, --visibility-admin <days>  Days closed requests are visible to admins"
  echo ""
  echo "  -o, --output <format>          Output format: json (default), quiet, table"
  echo "  -d, --dry-run                  Preview changes without making them"
  echo "  -y, --yes                      Skip confirmation prompt"
  echo "  -v, --verbose                  Enable verbose debug output"
  echo "  -h, --help                     Show this help message"
  echo "  -V, --version                  Show version information"
  echo ""
  echo "Environment variables:"
  echo "  API_BASE_URL     Base URL of the API (default: http://localhost:8080)"
  echo "  AUTH_BASE_URL    Base URL of UnityAuth (default: http://localhost:9090)"
  echo "  AUTH_TOKEN       JWT bearer token (optional; if not set, will prompt)"
  echo "  TENANT_ID        Tenant ID (default: 1)"
  echo "  LIBRE311_CONFIG  Path to config file (default: ~/.libre311rc)"
  echo ""
  echo "Required permissions:"
  echo "  Your JWT token must include one of these roles:"
  echo "    - LIBRE311_ADMIN_EDIT-SYSTEM"
  echo "    - LIBRE311_ADMIN_EDIT-TENANT"
  echo ""
  echo "Setup:"
  echo "  The script automatically loads ~/.libre311rc if it exists."
  echo "  Set LIBRE311_CONFIG to use a different config file location."
  echo "  See utils/.libre311rc.example for a template."
  echo ""
  echo "Examples:"
  echo "  $0 stlma -X -n 'St Louis' -b bounds.json  # Create new jurisdiction"
  echo "  $0 stlma -n 'St Louis Metropolitan Area'  # Update name"
  echo "  $0 stlma -n 'STLMA' --primary-color '221 83% 53%' --primary-hover-color '221 83% 45%'"
  echo "  $0 stlma --name 'New Name' --logo-url 'https://example.com/logo.png'"
  echo "  $0 stlma -n 'STLMA' --privacy-policy privacy.md --terms-of-use terms.md"
  echo "  $0 stlma -n 'STLMA' --bounds-file bounds.json"
  echo "  $0 stlma --name 'Test' --dry-run          # Preview without updating"
  echo "  $0 stlma --name 'Test' -y                 # Update without confirmation"
  echo ""
  echo "Exit codes:"
  echo "  0  Success"
  echo "  1  Usage error"
  echo "  2  Authentication failed"
  echo "  3  API error"
  echo "  4  File not found"
  exit $EXIT_USAGE
}

error() {
  local exit_code="${2:-$EXIT_API_ERROR}"
  echo -e "${RED}Error: $1${NC}" >&2
  exit "$exit_code"
}

success() {
  echo -e "${GREEN}$1${NC}"
}

info() {
  echo -e "${YELLOW}$1${NC}"
}

debug() {
  if [ "$VERBOSE" = true ]; then
    echo -e "${YELLOW}DEBUG: $1${NC}" >&2
  fi
}

get_jwt_token() {
  local username password response token

  info "UnityAuth credentials required" >&2
  read -p "Username: " username
  read -sp "Password: " password
  echo "" >&2

  # Call UnityAuth to get JWT
  response=$(curl -s -X POST \
    "${AUTH_BASE_URL}/api/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$username\",\"password\":\"$password\"}")

  token=$(echo "$response" | jq -r '.access_token' 2>/dev/null)

  if [ -z "$token" ] || [ "$token" = "null" ]; then
    error "Failed to authenticate with UnityAuth. Response: $(echo "$response" | jq . 2>/dev/null || echo "$response")" $EXIT_AUTH_FAILED
  fi

  echo "$token"
}

# Check for required tools
command -v curl >/dev/null 2>&1 || error "curl is required but not installed" $EXIT_USAGE
command -v jq >/dev/null 2>&1 || error "jq is required but not installed" $EXIT_USAGE

# Helper to check that an option has a value
require_arg() {
  if [ -z "$2" ] || [[ "$2" == -* ]]; then
    error "$1 requires a value" $EXIT_USAGE
  fi
}

# Check if jurisdiction_id is provided (handle --version and --help first)
if [ $# -lt 1 ]; then
  usage
fi

# Handle --version and --help before requiring jurisdiction_id
if [ "$1" = "--version" ] || [ "$1" = "-V" ]; then
  echo "update-jurisdiction.sh v$VERSION"
  exit $EXIT_SUCCESS
fi

if [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
  usage
fi

jurisdiction_id="$1"
shift

# Parse command line arguments using indexed arrays (bash 3.x compatible)
update_keys=()
update_values=()
bounds_file=""
privacy_policy_file=""
terms_of_use_file=""
remote_hosts=""
visibility_user=""
visibility_admin=""
VERBOSE=false
DRY_RUN=false
SKIP_CONFIRM=false
IS_CREATE=false
OUTPUT_FORMAT="json"

while [ $# -gt 0 ]; do
  case "$1" in
    -X|--create)
      IS_CREATE=true
      shift
      ;;
    -n|--name)
      require_arg "$1" "$2"
      update_keys+=("name")
      update_values+=("$2")
      shift 2
      ;;
    -c|--primary-color)
      require_arg "$1" "$2"
      update_keys+=("primary_color")
      update_values+=("$2")
      shift 2
      ;;
    -C|--primary-hover-color)
      require_arg "$1" "$2"
      update_keys+=("primary_hover_color")
      update_values+=("$2")
      shift 2
      ;;
    -l|--logo-url)
      require_arg "$1" "$2"
      update_keys+=("logo_media_url")
      update_values+=("$2")
      shift 2
      ;;
    -p|--privacy-policy)
      require_arg "$1" "$2"
      privacy_policy_file="$2"
      shift 2
      ;;
    -t|--terms-of-use)
      require_arg "$1" "$2"
      terms_of_use_file="$2"
      shift 2
      ;;
    -b|--bounds-file)
      require_arg "$1" "$2"
      bounds_file="$2"
      shift 2
      ;;
    -f|--project-feature)
      require_arg "$1" "$2"
      update_keys+=("project_feature")
      update_values+=("$2")
      shift 2
      ;;
    -r|--remote-hosts)
      require_arg "$1" "$2"
      remote_hosts="$2"
      shift 2
      ;;
    -u|--visibility-user)
      require_arg "$1" "$2"
      visibility_user="$2"
      shift 2
      ;;
    -a|--visibility-admin)
      require_arg "$1" "$2"
      visibility_admin="$2"
      shift 2
      ;;
    -o|--output)
      require_arg "$1" "$2"
      case "$2" in
        json|quiet|table)
          OUTPUT_FORMAT="$2"
          ;;
        *)
          error "Invalid output format: $2 (valid: json, quiet, table)" $EXIT_USAGE
          ;;
      esac
      shift 2
      ;;
    -d|--dry-run)
      DRY_RUN=true
      shift
      ;;
    -y|--yes)
      SKIP_CONFIRM=true
      shift
      ;;
    -v|--verbose)
      VERBOSE=true
      shift
      ;;
    -h|--help)
      usage
      ;;
    -V|--version)
      echo "update-jurisdiction.sh v$VERSION"
      exit $EXIT_SUCCESS
      ;;
    --)
      shift
      break
      ;;
    -*)
      error "Unknown option: $1" $EXIT_USAGE
      ;;
    *)
      error "Unexpected argument: $1" $EXIT_USAGE
      ;;
  esac
done

# Require at least one update option
# NOTE: Query mode (GET) is disabled - see TODO at top of file
if [ ${#update_keys[@]} -eq 0 ] && [ -z "$bounds_file" ] && \
   [ -z "$privacy_policy_file" ] && [ -z "$terms_of_use_file" ] && \
   [ -z "$remote_hosts" ] && [ -z "$visibility_user" ] && [ -z "$visibility_admin" ]; then
  error "At least one update option is required.\nQuery mode is not yet supported (backend GET endpoint missing).\nRun '$0 --help' for usage information." $EXIT_USAGE
fi

# Check if name is provided (required by backend for all requests)
name_provided=false
for key in "${update_keys[@]}"; do
  if [ "$key" = "name" ]; then
    name_provided=true
    break
  fi
done

if [ "$name_provided" = false ]; then
  error "The --name option is required for all requests (backend validation requirement).\nExample: $0 $jurisdiction_id --name 'Jurisdiction Name' --bounds-file bounds.json" $EXIT_USAGE
fi

# Check if bounds are provided for creation
if [ "$IS_CREATE" = true ] && [ -z "$bounds_file" ]; then
  error "The --bounds-file option is required when creating a new jurisdiction." $EXIT_USAGE
fi

# Get AUTH_TOKEN - either from environment or interactively (skip for dry-run without token)
if [ -z "$AUTH_TOKEN" ] && [ "$DRY_RUN" = false ]; then
  AUTH_TOKEN=$(get_jwt_token)
fi

# Build JSON payload
payload="{}"

# Add jurisdiction_id if creating
if [ "$IS_CREATE" = true ]; then
  payload=$(echo "$payload" | jq --arg id "$jurisdiction_id" '. + {jurisdiction_id: $id}')
fi

for i in "${!update_keys[@]}"; do
  key="${update_keys[$i]}"
  value="${update_values[$i]}"
  payload=$(echo "$payload" | jq --arg k "$key" --arg v "$value" '. + {($k): $v}')
done

# Add numeric fields
if [ -n "$visibility_user" ]; then
  payload=$(echo "$payload" | jq --argjson v "$visibility_user" '. + {closed_request_days_visible_user: $v}')
fi

if [ -n "$visibility_admin" ]; then
  payload=$(echo "$payload" | jq --argjson v "$visibility_admin" '. + {closed_request_days_visible_admin: $v}')
fi

# Add privacy policy content if provided
if [ -n "$privacy_policy_file" ]; then
  if [ ! -f "$privacy_policy_file" ]; then
    error "Privacy policy file not found: $privacy_policy_file" $EXIT_FILE_NOT_FOUND
  fi
  privacy_content=$(cat "$privacy_policy_file")
  payload=$(echo "$payload" | jq --arg content "$privacy_content" '. + {privacy_policy_content: $content}')
fi

# Add terms of use content if provided
if [ -n "$terms_of_use_file" ]; then
  if [ ! -f "$terms_of_use_file" ]; then
    error "Terms of use file not found: $terms_of_use_file" $EXIT_FILE_NOT_FOUND
  fi
  terms_content=$(cat "$terms_of_use_file")
  payload=$(echo "$payload" | jq --arg content "$terms_content" '. + {terms_of_use_content: $content}')
fi

# Add bounds if provided
if [ -n "$bounds_file" ]; then
  if [ ! -f "$bounds_file" ]; then
    error "Bounds file not found: $bounds_file" $EXIT_FILE_NOT_FOUND
  fi

  bounds=$(cat "$bounds_file")

  # Validate JSON
  if ! echo "$bounds" | jq empty 2>/dev/null; then
    error "Invalid JSON in bounds file: $bounds_file" $EXIT_USAGE
  fi

  payload=$(echo "$payload" | jq --argjson bounds "$bounds" '. + {bounds: $bounds}')
fi

action_desc="Updating"
if [ "$IS_CREATE" = true ]; then
  action_desc="Creating"
fi

[ "$OUTPUT_FORMAT" != "quiet" ] && info "$action_desc jurisdiction: $jurisdiction_id"
[ "$OUTPUT_FORMAT" != "quiet" ] && info "Payload: $(echo "$payload" | jq -c .)"

# Debug output with masked token
if [ -n "$AUTH_TOKEN" ]; then
  debug "AUTH_TOKEN (masked): ${AUTH_TOKEN:0:10}...${AUTH_TOKEN: -10}"
fi

# Set API URL and method
API_METHOD="PATCH"
API_URL="${API_BASE_URL}/api/tenant-admin/jurisdictions/${jurisdiction_id}?tenant_id=${TENANT_ID}"

if [ "$IS_CREATE" = true ]; then
  API_METHOD="POST"
  API_URL="${API_BASE_URL}/api/tenant-admin/jurisdictions?tenant_id=${TENANT_ID}"
fi

debug "API_BASE_URL: ${API_BASE_URL}"
debug "TENANT_ID: ${TENANT_ID}"
debug "Full URL: ${API_URL}"

# Handle dry-run mode
if [ "$DRY_RUN" = true ]; then
  if [ "$OUTPUT_FORMAT" = "quiet" ]; then
    exit $EXIT_SUCCESS
  fi
  info "Dry run - no changes will be made"
  echo ""
  echo "Would send $API_METHOD to: $API_URL"
  echo ""
  echo "Payload:"
  echo "$payload" | jq .
  exit $EXIT_SUCCESS
fi

# Confirmation prompt for updates (unless --yes or quiet mode)
if [ "$SKIP_CONFIRM" = false ] && [ "$OUTPUT_FORMAT" != "quiet" ]; then
  echo ""
  read -p "$action_desc jurisdiction '$jurisdiction_id'? [y/N] " confirm
  if [[ ! "$confirm" =~ ^[Yy] ]]; then
    info "Cancelled."
    exit $EXIT_SUCCESS
  fi
fi

[ "$OUTPUT_FORMAT" != "quiet" ] && echo ""

# Make API call
if [ "$VERBOSE" = true ]; then
  debug "Making $API_METHOD API call with verbose output..."
  response=$(curl -v -w "\n%{http_code}" -X "$API_METHOD" \
    "$API_URL" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${AUTH_TOKEN}" \
    -d "$payload" 2>&1)
else
  response=$(curl -s -w "\n%{http_code}" -X "$API_METHOD" \
    "$API_URL" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${AUTH_TOKEN}" \
    -d "$payload")
fi

http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

# Helper function for table output format
print_table() {
  local json="$1"
  echo "$json" | jq -r '
    to_entries |
    .[] |
    "\(.key): \(.value | if type == "object" or type == "array" then (. | tostring) else . end)"
  ' 2>/dev/null || echo "$json"
}

if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
  [ "$OUTPUT_FORMAT" != "quiet" ] && success "[OK] Jurisdiction ${action_desc,,} successfully (HTTP $http_code)"

  # Update remote hosts if provided
  if [ -n "$remote_hosts" ]; then
    hosts_json=$(echo "$remote_hosts" | jq -R 'split(",") | map(select(length > 0))')
    
    if [ "$DRY_RUN" = true ]; then
      info "Dry run - would update remote hosts to: $remote_hosts"
    else
      [ "$OUTPUT_FORMAT" != "quiet" ] && info "Updating remote hosts..."
      if [ "$VERBOSE" = true ]; then
        rh_response=$(curl -v -w "\n%{http_code}" -X POST \
          "${API_BASE_URL}/api/tenant-admin/jurisdictions/${jurisdiction_id}/remote_hosts?tenant_id=${TENANT_ID}" \
          -H "Content-Type: application/json" \
          -H "Authorization: Bearer ${AUTH_TOKEN}" \
          -d "$hosts_json" 2>&1)
      else
        rh_response=$(curl -s -w "\n%{http_code}" -X POST \
          "${API_BASE_URL}/api/tenant-admin/jurisdictions/${jurisdiction_id}/remote_hosts?tenant_id=${TENANT_ID}" \
          -H "Content-Type: application/json" \
          -H "Authorization: Bearer ${AUTH_TOKEN}" \
          -d "$hosts_json")
      fi
      
      rh_http_code=$(echo "$rh_response" | tail -n1)
      if [ "$rh_http_code" -ge 200 ] && [ "$rh_http_code" -lt 300 ]; then
        [ "$OUTPUT_FORMAT" != "quiet" ] && success "[OK] Remote hosts updated successfully"
      else
        error "HTTP $rh_http_code - Failed to update remote hosts" $EXIT_API_ERROR
      fi
    fi
  fi

  case "$OUTPUT_FORMAT" in
    quiet)
      # Just exit successfully
      ;;
    json)
      echo ""
      echo "${action_desc} jurisdiction:"
      echo "$body" | jq .
      ;;
    table)
      echo ""
      print_table "$body"
      ;;
  esac
  exit $EXIT_SUCCESS
else
  error "HTTP $http_code - Failed to ${action_desc,,} jurisdiction\n$(echo "$body" | jq . 2>/dev/null || echo "$body")" $EXIT_API_ERROR
fi
