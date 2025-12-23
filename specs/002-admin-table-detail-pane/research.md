# Research: Admin Table View with Contextual Detail Pane

**Feature**: 002-admin-table-detail-pane
**Date**: 2025-12-23

## Research Summary

This feature is a frontend-only UI refactoring with no significant unknowns. The existing codebase provides all necessary patterns and components. Research confirms implementation approach.

---

## R1: Svelte Slide Transition for Detail Pane Animation

**Decision**: Use Svelte's built-in `slide` transition

**Rationale**:
- Already used in the codebase (`+layout.svelte` line 166) for filter panel animation
- Provides smooth, performant CSS-based transitions
- No additional dependencies required
- Constitution Principle VII (Minimal Dependencies) compliance

**Alternatives Considered**:
- Custom CSS animations: Rejected - more code, same result
- JavaScript-based animation libraries: Rejected - adds dependencies, overkill for simple slide

**Code Reference**:
```svelte
import { slide } from 'svelte/transition';
<div transition:slide={{ duration: 300 }}>
```

---

## R2: Layout Component Pattern

**Decision**: Create new `TableWithDetailPane.svelte` component with CSS Grid

**Rationale**:
- Existing `SideBarMainContentLayout.svelte` uses CSS Grid with fixed 400px sidebar
- New component will use same pattern but with conditional column template
- Keeps layout logic encapsulated and reusable
- Follows existing component organization patterns

**Alternatives Considered**:
- Modify existing `SideBarMainContentLayout`: Rejected - would affect other views using it
- Inline styles in route file: Rejected - less maintainable, not reusable

**Implementation Pattern**:
```svelte
<div class="layout" style:grid-template-columns={open ? '400px 1fr' : '1fr'}>
  <aside class="detail-pane" class:hidden={!open}>
    <slot name="detail-pane" />
  </aside>
  <main class="table-area">
    <slot name="table" />
  </main>
</div>
```

---

## R3: URL-Based State Management for Detail Pane

**Decision**: Use existing SvelteKit routing pattern (`/issues/table/[issue_id]`)

**Rationale**:
- Already implemented - clicking a row navigates to `/issues/table/[issue_id]`
- Deep-linkable - users can share/bookmark specific request views
- Browser back/forward navigation works automatically
- `selectedServiceRequestStore` already populated by context on route change
- No additional state management needed

**Alternatives Considered**:
- Svelte store for open/closed state: Rejected - would lose URL-based navigation benefits
- Query parameters: Rejected - current URL pattern already works

**Current Flow** (no changes needed):
1. User clicks row → `selectRow()` → `goto('/issues/table/{id}')`
2. Route change triggers context update → `selectedServiceRequestStore` populated
3. Layout detects `$page.params.issue_id` → shows detail pane

---

## R4: Mobile Responsive Breakpoint

**Decision**: Use existing 769px breakpoint from `SideBarMainContentLayout.svelte`

**Rationale**:
- Constitution recommends using existing responsive breakpoints (~768px)
- Existing layout already uses `@media (min-width: 769px)` for sidebar visibility
- Consistency with existing responsive behavior

**Implementation**:
```css
@media (max-width: 768px) {
  .layout { grid-template-columns: 1fr; }
  .detail-pane { /* Full screen overlay or replace table */ }
  .table-area { display: none; } /* When detail open on mobile */
}
```

---

## R5: Close Button Navigation

**Decision**: Navigate to `/issues/table` to close detail pane

**Rationale**:
- Existing "Back" button already uses `href={back}` where `back = linkResolver.issuesTable($page.url)`
- This navigates to `/issues/table`, clearing the `issue_id` param
- Context will clear `selectedServiceRequestStore`
- Layout will detect no `issue_id` and hide detail pane

**Existing Code** (`ServiceRequest.svelte` line 198):
```svelte
<Button slot="left" href={back}>
  {messages['updateServiceRequest']['button_back']}
</Button>
```

**Enhancement**: Rename button label from "Back" to "Close" in detail pane context (or add separate Close button).

---

## R6: Table Row Selection Highlighting

**Decision**: Reuse existing `#selected` CSS styling

**Rationale**:
- Already implemented in `+layout.svelte` lines 334-342
- `resolveStyleId()` function returns 'selected' or 'item-id' based on match
- CSS applies blue highlight background to `#selected` row

**Existing Code**:
```css
.issues-table-override :global(#selected) {
  --tw-bg-opacity: 0.15;
  background-color: hsl(var(--primary) / var(--tw-bg-opacity));
}
```

No changes needed - works as-is.

---

## R7: Filter and CSV Functionality Preservation

**Decision**: Keep filter panel and CSV button in table area, outside detail pane

**Rationale**:
- Filters affect table content, should remain with table
- CSV download exports filtered data, belongs with table
- Detail pane is for viewing/editing single request only

**Implementation Note**:
- Filter panel currently in `+layout.svelte` above table card
- Position unchanged - filter area stays above table in right portion of split view
- On mobile, filters hidden per FR-015

---

## R8: Update/Delete Flow After Action

**Decision**: Keep existing navigation patterns

**After Delete** (`ServiceRequest.svelte` line 49):
```typescript
goto('/issues/table');
```
- Navigates to table, closes detail pane
- Table refreshes showing remaining requests

**After Update** (`ServiceRequest.svelte` line 67):
```typescript
goto(back); // back = '/issues/table'
```
- Navigates back to table
- Need to ensure table data refreshes to show updated values

**Enhancement Needed**: After update, stay in detail view with refreshed data instead of navigating away. This improves UX - admin can see changes applied without losing context.

---

## Findings Summary

| Research Area | Status | Action Required |
|---------------|--------|-----------------|
| Animation | Resolved | Use Svelte `slide` transition |
| Layout Pattern | Resolved | New `TableWithDetailPane` component |
| State Management | Resolved | Existing URL routing sufficient |
| Responsive | Resolved | Reuse 769px breakpoint |
| Close Button | Resolved | Existing navigation works |
| Row Highlight | Resolved | Existing CSS works |
| Filters/CSV | Resolved | Keep in table area |
| Update/Delete | Resolved | Minor UX enhancement optional |

**All research items resolved. No NEEDS CLARIFICATION items remain.**
