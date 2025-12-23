# Quickstart: Admin Table View with Contextual Detail Pane

**Feature**: 002-admin-table-detail-pane
**Date**: 2025-12-23

## Overview

This feature refactors the admin service request table view to remove the redundant left sidebar card list and implement a contextual detail pane that slides in when a row is selected.

## Prerequisites

- Node.js (see `.nvmrc` for version)
- Frontend dependencies installed: `cd frontend && npm install`
- Backend running (for API data): `./gradlew app:run`

## Development Setup

```bash
# Start frontend dev server
cd frontend
npm run dev

# Navigate to table view
open http://localhost:3000/issues/table
```

## Key Files to Modify

| File | Change |
|------|--------|
| `frontend/src/lib/components/TableWithDetailPane.svelte` | NEW - Layout component |
| `frontend/src/routes/issues/table/+layout.svelte` | Replace SideBarMainContentLayout |
| `frontend/src/routes/issues/table/+page.svelte` | Remove card list rendering |
| `frontend/src/routes/issues/table/[issue_id]/+page.svelte` | Add Close button |

## Testing

```bash
# Run unit tests
npm run test:unit

# Run integration tests
npm run test:integration

# Type check
npm run check

# Lint and format
npm run lint
npm run format
```

## Verification Checklist

- [ ] Table displays full-width when no request selected
- [ ] Card list is not visible on left side
- [ ] Clicking row opens detail pane with slide animation
- [ ] Selected row is highlighted in table
- [ ] Detail pane shows all request information
- [ ] Close button returns to full-width table
- [ ] Update button allows editing request
- [ ] Delete button removes request with confirmation
- [ ] Filters work and persist when detail pane is open
- [ ] CSV download works from table footer
- [ ] Mobile view shows full-screen detail on row tap
- [ ] Back button on mobile returns to table

## Architecture Notes

- **State**: URL-based via SvelteKit routing (`/issues/table/[issue_id]`)
- **Animation**: Svelte `slide` transition
- **Layout**: CSS Grid with conditional column template
- **Breakpoint**: 769px for mobile/desktop switch
- **Component Reuse**: Existing `ServiceRequest.svelte` in detail pane
