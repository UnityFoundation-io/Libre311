# Research: Service Definition Editor

**Feature**: 005-service-definition-editor
**Date**: 2026-01-05

## Research Summary

This feature is **frontend-only**. All backend API endpoints and data models already exist. Research focused on understanding existing assets and identifying the optimal implementation approach.

## Research Areas

### 1. Existing API Endpoints

**Question**: What admin API endpoints exist for service definition management?

**Decision**: All required endpoints already exist in `JurisdictionAdminController.java`

**Findings**:
- Service Group CRUD: `GET/POST/PATCH/DELETE /jurisdiction-admin/groups/`
- Service CRUD: `GET/POST/PATCH/DELETE /jurisdiction-admin/services/`
- Attribute CRUD: `POST/PATCH/DELETE /jurisdiction-admin/services/{code}/attributes/`
- Reorder: `PATCH .../services-order` and `.../attributes-order`

**Rationale**: No backend development needed; focus entirely on frontend UI.

**Alternatives considered**: None - backend is complete.

---

### 2. Frontend API Client

**Question**: Does the Libre311 service client support all required operations?

**Decision**: Yes, all methods exist in `Libre311.ts`

**Findings**:
| Operation | Method | Exists |
|-----------|--------|--------|
| List groups | `getGroupList()` | ✅ |
| Create group | `createGroup()` | ✅ |
| Edit group | `editGroup()` | ✅ |
| Create service | `createService()` | ✅ |
| Edit service | `editService()` | ✅ |
| Delete service | `deleteService()` | ✅ |
| Reorder services | `updateServicesOrder()` | ✅ |
| Get service definition | `getServiceDefinition()` | ✅ |
| Create attribute | `createAttribute()` | ✅ |
| Edit attribute | `editAttribute()` | ✅ |
| Delete attribute | `deleteAttribute()` | ✅ |
| Reorder attributes | `updateAttributesOrder()` | ✅ |

**Rationale**: All API integration already implemented; new components only need to call existing methods.

---

### 3. Existing UI Components

**Question**: What existing components can be reused?

**Decision**: Reuse DragAndDrop, enhance existing list components, create new editor components

**Findings**:

| Component | Status | Strategy |
|-----------|--------|----------|
| `DragAndDrop.svelte` | Reusable | Generic drag-drop with `itemsChanged` event |
| `GroupListItem.svelte` | Enhance | Add expand/collapse animation |
| `ServiceListItem.svelte` | Enhance | Add Edit button with navigation |
| `DisplayListItem.svelte` | Reusable | For collapsed attribute preview |
| `EditListItem.svelte` | Reusable | For inline name editing |
| `ListItemContainer.svelte` | Reusable | Common wrapper styling |

**New components needed**:
- `EditorContainer.svelte` - Page layout
- `HeaderCard.svelte` - Service name/description
- `AttributeCard.svelte` - Accordion container
- `AttributeCardCollapsed.svelte` - Preview state
- `AttributeCardExpanded.svelte` - Edit state
- `AttributeTypeSelector.svelte` - Datatype dropdown
- `OptionsList.svelte` - List values management
- `AttributeFooter.svelte` - Actions/required toggle
- `AddFieldButton.svelte` - FAB to add attributes
- `SaveToast.svelte` - Auto-save notification

---

### 4. Data Type Mapping

**Question**: How do UI field types map to backend datatypes?

**Decision**: Direct mapping with user-friendly labels

**Findings**:

| UI Label | Backend `AttributeDataType` | UI Behavior |
|----------|----------------------------|-------------|
| Short answer | `STRING` | Single-line text input |
| Paragraph | `TEXT` | Multi-line textarea |
| Multiple choice | `MULTIVALUELIST` | Checkboxes with options |
| Dropdown | `SINGLEVALUELIST` | Single-select dropdown |
| Number | `NUMBER` | Numeric input |
| Date | `DATETIME` | Date picker |

**Rationale**: Matches existing `DatatypeUnionSchema` in Libre311.ts; user-friendly labels match Google Forms.

---

### 5. Route Structure

**Question**: Where should the new editor view live in the route hierarchy?

**Decision**: `/groups/[group_id]/services/[service_id]/edit`

**Findings**:
- Existing routes: `/groups/`, `/groups/[group_id]/`, `/groups/[group_id]/services/[service_id]/`
- Current service view shows attribute list in table format
- Adding `/edit` route preserves existing functionality

**Rationale**: Non-breaking change; users can access both list view and new editor.

**Alternatives considered**:
1. Replace existing `/groups/[group_id]/services/[service_id]/` with new editor - Rejected: breaking change
2. Add modal-based editor - Rejected: wireframes specify full-page view

---

### 6. Auto-Save Implementation

**Question**: How should auto-save work for optimal UX?

**Decision**: Debounced save with optimistic UI and toast feedback

**Findings**:
Best practices from Google Forms and similar editors:
1. Save on blur (field loses focus)
2. Save on explicit change (dropdown selection, toggle)
3. Debounce rapid changes (500ms delay for text inputs)
4. Show toast on success ("Changes saved")
5. Show error toast with retry on failure
6. Optimistic UI updates (don't wait for API)

**Implementation pattern**:
```typescript
// In component
let saveTimeout: ReturnType<typeof setTimeout>;
function handleChange(field: string, value: unknown) {
  updateLocalState(field, value); // Optimistic
  clearTimeout(saveTimeout);
  saveTimeout = setTimeout(() => saveToApi(field, value), 500);
}
```

---

### 7. Accessibility Requirements

**Question**: What WCAG 2.1 AA requirements apply to this feature?

**Decision**: Implement full keyboard navigation, ARIA labels, focus management

**Findings**:

Per Constitution Principle IX and spec clarification:

| Requirement | Implementation |
|-------------|----------------|
| Keyboard navigation | Tab through cards, Enter to expand, Escape to collapse |
| Focus indicators | 2px outline on focused elements |
| ARIA labels | `aria-expanded`, `aria-controls` on accordions |
| Live regions | `aria-live="polite"` for save notifications |
| Color contrast | 4.5:1 ratio (use existing STWUI colors) |
| Touch targets | 24x24px minimum (STWUI buttons comply) |

**Key patterns**:
- Accordion: `role="button"`, `aria-expanded`, `aria-controls`
- Options list: `role="listbox"`, `role="option"`, `aria-selected`
- Toast: `role="status"`, `aria-live="polite"`

---

### 8. Delete Group Behavior

**Question**: How should group deletion work with contained services?

**Decision**: Prevent deletion if group contains services (per spec clarification)

**Findings**:
- Spec clarification explicitly states: "Prevent deletion - show error requiring services to be moved or deleted first"
- Backend likely needs update to return appropriate error (to verify during implementation)
- Frontend shows modal explaining requirement

**Implementation**:
1. User clicks delete on group
2. Check if group has services
3. If yes: show error modal "Cannot delete group with services. Please delete or move all services first."
4. If no: show confirmation modal, proceed with delete

---

## Unresolved Items

None. All technical questions answered through codebase exploration.

## Dependencies

| Dependency | Version | Status |
|------------|---------|--------|
| SvelteKit | Existing | ✅ Installed |
| STWUI | Existing | ✅ Installed |
| Tailwind CSS | Existing | ✅ Installed |
| axios | Existing | ✅ Installed |
| Vitest | Existing | ✅ Installed |
| Playwright | Existing | ✅ Installed |

No new dependencies required.
