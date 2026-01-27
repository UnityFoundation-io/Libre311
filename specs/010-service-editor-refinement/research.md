# Research: Service Definition Editor Refinement

**Feature**: 010-service-editor-refinement
**Date**: 2026-01-09
**Status**: Complete

## Overview

This research documents the investigation of existing patterns, components, and best practices for implementing the split-pane service definition editor.

## 1. Existing Component Patterns

### Decision: Leverage Existing ServiceDefinitionEditor Structure

**Rationale**: The `frontend/src/lib/components/ServiceDefinitionEditor/` directory already contains well-organized components that can be extended or adapted.

**Existing Components Analyzed**:

| Component | Purpose | Reuse Strategy |
|-----------|---------|----------------|
| `EditorView/EditorContainer.svelte` | Container with breadcrumbs, loading, error states | Adapt for split-pane right panel |
| `EditorView/SaveToast.svelte` | Toast notification for save confirmation | Reuse directly |
| `GroupListItem.svelte` | Group display in list | Adapt for tree node |
| `ServiceListItem.svelte` | Service display in list | Adapt for tree node |
| `SdaListItem.svelte` | Service Definition Attribute list item | Adapt for attribute cards |
| `EditAttributeItem/EditAttributeItem.svelte` | Attribute editing form | Adapt for expanded card state |
| `EditAttributeItem/EditMultiValueList.svelte` | Options list editing | Adapt for OptionsList component |
| `DragAndDrop.svelte` | Drag-drop functionality | Extend for tree and cards |
| `ListItemContainer.svelte` | Generic list item wrapper | Potential reuse |

**Alternatives Considered**:
- Build all new components from scratch - Rejected due to code duplication
- Heavily refactor existing components - Rejected as too risky for existing functionality

## 2. STWUI Component Usage

### Decision: Use STWUI Components for UI Primitives

**Rationale**: STWUI is already the approved component library (Constitution Principle VII). Using it ensures visual consistency and reduces custom CSS.

**STWUI Components to Use**:

| Component | Use Case |
|-----------|----------|
| `Card` | Header card, attribute cards, group editor |
| `Button` | Save, Cancel, Delete, Copy actions |
| `Input` | Service name, group name, question text, options |
| `Toggle` | Required switch |
| `Modal` | Unsaved changes, delete confirmation |
| `Breadcrumbs` | Navigation context (already used) |
| `Dropdown` | Attribute type selector |

**Existing Usage Reference**: See `EditorContainer.svelte` lines 3, 87-132 for Card and Button patterns.

## 3. State Management Approach

### Decision: Svelte Stores for Editor State

**Rationale**: SvelteKit's built-in store system is lightweight and sufficient for the editor's state needs. No additional state management library required.

**Store Structure**:

```typescript
// editorStore.ts
interface EditorState {
  // Selection
  selectedType: 'group' | 'service' | null;
  selectedGroupId: number | null;
  selectedServiceCode: number | null;

  // Tree expansion
  expandedGroupIds: Set<number>;

  // Attribute cards
  expandedAttributeId: number | null;

  // Dirty tracking (per-card)
  dirtyCards: Map<string, boolean>; // key: 'header' | 'group' | `attr-${id}`

  // Original values for cancel/revert
  originalValues: Map<string, unknown>;
}
```

**Alternatives Considered**:
- Context API only - Rejected as stores provide better reactivity for cross-component updates
- External library (Redux-like) - Rejected per Principle VII (minimal dependencies)

## 4. Drag and Drop Implementation

### Decision: Extend Existing DragAndDrop Component

**Rationale**: The project already has a `DragAndDrop.svelte` component. Extending it maintains consistency and avoids new dependencies.

**Implementation Approach**:
1. Abstract the existing drag-drop logic into a reusable utility
2. Create `TreeDragDrop.svelte` for service reordering in tree
3. Use HTML5 Drag and Drop API (already in use)
4. Visual feedback: purple insertion line, 50% opacity during drag

**Key Events to Handle**:
- `dragstart` - Set drag data, apply opacity
- `dragover` - Calculate insertion position, show indicator
- `drop` - Update order, call reorder API
- `dragend` - Reset visual state

**Reference**: Existing pattern in `DragAndDrop.svelte` (already in codebase)

## 5. Accessibility Implementation

### Decision: ARIA Tree Pattern for Navigation

**Rationale**: WCAG 2.2 AA requires proper semantic structure. WAI-ARIA tree pattern is the standard for hierarchical navigation.

**ARIA Roles for Tree Panel**:

```html
<div role="tree" aria-label="Service groups">
  <div role="treeitem" aria-expanded="true" aria-level="1">
    Group Name
    <div role="group">
      <div role="treeitem" aria-level="2" aria-selected="true">
        Service Name
      </div>
    </div>
  </div>
</div>
```

**Keyboard Navigation**:
- `Arrow Down/Up` - Move between tree items
- `Arrow Right` - Expand group / move to first child
- `Arrow Left` - Collapse group / move to parent
- `Enter/Space` - Select item
- `Tab` - Move to editor panel

**Focus Management**:
- When attribute card expands, focus moves to question input
- When modal opens, focus traps within modal
- When modal closes, focus returns to trigger element

## 6. API Integration

### Decision: Use Existing Libre311 Service Methods

**Rationale**: The `Libre311.ts` service already contains type definitions and API methods for all required operations. No new API client code needed.

**Existing API Methods** (from `frontend/src/lib/services/Libre311/Libre311.ts`):

Group Operations:
- `getGroups()` - Fetch all groups with services
- `createGroup()` - Create new group
- `updateGroup()` - Update group name
- `deleteGroup()` - Delete group

Service Operations:
- `createService()` - Create service in group
- `updateService()` - Update service name/description
- `deleteService()` - Delete service
- `updateServicesOrder()` - Reorder services in group

Attribute Operations:
- `createAttribute()` - Add attribute to service
- `updateAttribute()` - Update attribute properties
- `deleteAttribute()` - Delete attribute
- `updateAttributesOrder()` - Reorder attributes

**Type Definitions Already Available**:
- `Group`, `GroupSchema`
- `Service`, `ServiceSchema`
- `ServiceDefinitionAttribute`, `BaseServiceDefinitionAttributeSchema`
- `AttributeValue`, `AttributeValueSchema`

## 7. Error Handling Pattern

### Decision: Toast Notifications with Retry

**Rationale**: Consistent with existing SaveToast pattern. Non-blocking error display allows users to continue working.

**Implementation**:
```typescript
try {
  await saveAttribute(data);
  showToast('success', 'Changes saved');
} catch (error) {
  showToast('error', 'Save failed. Click to retry.', {
    action: () => saveAttribute(data)
  });
}
```

**Error States**:
- Network error: "Unable to save. Check your connection."
- Validation error: "Invalid data. Please check your input."
- Server error: "Server error. Please try again."

## Summary

All research questions resolved. No blockers identified. Ready for Phase 1 design.

| Topic | Decision | Confidence |
|-------|----------|------------|
| Component patterns | Extend existing | High |
| UI library | STWUI (approved) | High |
| State management | Svelte stores | High |
| Drag and drop | Extend existing | High |
| Accessibility | ARIA tree pattern | High |
| API integration | Use existing service | High |
| Error handling | Toast with retry | High |
