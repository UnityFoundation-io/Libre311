# Service Definition Editor Architecture

This document describes the architecture of the Service Definition Editor refinement implementation.

## High-Level Structure

```
┌─────────────────────────────────────────────────────────────────┐
│                    /groups/config/+page.svelte                  │
│                    (Main Orchestrator Page)                     │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────────────┐    ┌──────────────────────────────┐   │
│  │     SplitPaneLayout  │    │       State Management       │   │
│  │  ┌────────┬────────┐ │    │  ┌──────────────────────┐    │   │
│  │  │ Tree   │ Editor │ │    │  │ editorStore.ts       │    │   │
│  │  │ Panel  │ Panel  │ │    │  │ (Svelte writable)    │    │   │
│  │  └────────┴────────┘ │    │  └──────────────────────┘    │   │
│  └──────────────────────┘    └──────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

## Component Hierarchy

### 1. Page Layer

**File:** `frontend/src/routes/groups/config/+page.svelte`

The main orchestrator handling:
- Data fetching via `Libre311` service
- Selection state management
- API calls for CRUD operations
- Combines groups with services into `GroupWithServices[]` for tree display
- Dirty state tracking for cards at the page level

### 2. Layout Layer

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/SplitPaneEditor/SplitPaneLayout.svelte`

Split-pane interface with resizable divider:
- **Left panel:** TreePanel (320px default, 240-480px range)
- **Right panel:** EditorPanel
- Keyboard-accessible resize via arrow keys
- Mouse drag resize with visual feedback

### 3. Tree Panel

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/SplitPaneEditor/TreePanel.svelte`

Hierarchical navigation with expandable groups. Sub-components:

| Component | File | Purpose |
|-----------|------|---------|
| TreeGroup | `TreeView/TreeGroup.svelte` | Group nodes with expand/collapse, drag-drop for services |
| TreeService | `TreeView/TreeService.svelte` | Service list items with selection highlighting |

### 4. Editor Panel

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/SplitPaneEditor/EditorPanel.svelte`

Three states based on selection:
- **Empty:** No selection - displays placeholder message
- **Group Editor:** When a group is selected
- **Service Editor:** When a service is selected

Uses slots for flexibility in rendering different editor types.

### 5. Service Editing Components

| Component | File | Purpose |
|-----------|------|---------|
| ServiceHeaderCard | `ServiceEditor/ServiceHeaderCard.svelte` | Service name/description editing |
| AttributeCardList | `ServiceEditor/AttributeCardList.svelte` | List of attribute cards with drag-drop reordering |
| AttributeCard | `AttributeCard/AttributeCard.svelte` | Accordion-style attribute editing |
| AttributeCardExpanded | `AttributeCard/AttributeCardExpanded.svelte` | Full editing controls when card is active |
| AttributeCardCollapsed | `AttributeCard/AttributeCardCollapsed.svelte` | Compact view with type indicator |
| AttributeCardFooter | `AttributeCard/AttributeCardFooter.svelte` | Save/Cancel/Copy/Delete actions |
| AttributeTypeSelector | `AttributeCard/AttributeTypeSelector.svelte` | Dropdown for selecting attribute type |
| OptionsList | `AttributeCard/OptionsList.svelte` | Option management for list-type attributes |

### 6. Group Editing Components

| Component | File | Purpose |
|-----------|------|---------|
| GroupEditor | `GroupEditor/GroupEditor.svelte` | Group name editing with Save/Delete |

### 7. Shared Components

| Component | File | Purpose |
|-----------|------|---------|
| UnsavedChangesModal | `Shared/UnsavedChangesModal.svelte` | Confirmation dialog for navigation with dirty state |
| ConfirmDeleteModal | `Shared/ConfirmDeleteModal.svelte` | Confirmation dialog for delete operations |
| SaveButton | `Shared/SaveButton.svelte` | Reusable save button with loading state |
| DragHandle | `Shared/DragHandle.svelte` | Drag handle indicator for reorderable items |
| SaveToast | `EditorView/SaveToast.svelte` | Toast notifications for save feedback |

## State Management

### Store Structure

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/stores/editorStore.ts`

```typescript
interface SplitPaneEditorState {
	// Selection
	selection: EditorSelection;      // Current tree selection (group/service)

	// Tree expansion state (which groups are expanded)
	expandedGroupIds: Set<number>;

	// Accordion - one card expanded at a time
	expandedAttributeId: number | null;

	// Per-card dirty tracking
	cardStates: Map<CardId, CardDirtyState>;

	// Loading states
	isLoading: boolean;
	isSaving: Map<CardId, boolean>;  // Per-card save-in-progress

	// Error state
	error: string | null;

	// Unsaved changes modal state
	pendingNavigation: (() => void) | null;
	showUnsavedModal: boolean;
}
```

### Reactivity Pattern

Svelte's reactivity system cannot detect mutations within Map or Set objects. The store uses a `cloneState()` function that creates new Map/Set instances to trigger reactive updates:

```typescript
function cloneState(state: SplitPaneEditorState): SplitPaneEditorState {
	return {
		...state,
		expandedGroupIds: new Set(state.expandedGroupIds),
		cardStates: new Map(state.cardStates),
		isSaving: new Map(state.isSaving)
	};
}
```

### Derived Stores

Common queries are exposed as derived stores:
- `hasAnyUnsavedChanges` - Check if any card has unsaved changes
- `isCardSaving(cardId)` - Check if a specific card is saving
- `isCardDirty(cardId)` - Check if a specific card is dirty
- `currentSelection` - Get current selection state

### Navigation Protection

The `attemptNavigation()` helper intercepts navigation when unsaved changes exist:

```typescript
export function attemptNavigation(navigate: () => void): boolean {
	const state = get(splitPaneStore);
	const hasUnsaved = Array.from(state.cardStates.values()).some((card) => card.isDirty);

	if (hasUnsaved) {
		splitPaneStore.showUnsavedChangesModal(navigate);
		return false;
	} else {
		navigate();
		return true;
	}
}
```

## Type System

### Core Types

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/types.ts`

| Type | Purpose |
|------|---------|
| `AttributeTypeLabel` | UI-friendly names ('Short answer', 'Paragraph', etc.) |
| `DatatypeUnion` | Backend API values ('string', 'text', 'multivaluelist', etc.) |
| `AttributeCardState` | Extends `ServiceDefinitionAttribute` with UI state |
| `CardState` | 'collapsed' \| 'expanded' |
| `SaveStatus` | Toast notification state |

### Datatype Mapping

Bidirectional mapping between UI labels and API values:

```typescript
export const DATATYPE_MAP: Record<AttributeTypeLabel, DatatypeUnion> = {
	'Short answer': 'string',
	'Paragraph': 'text',
	'Multiple choice': 'multivaluelist',
	'Dropdown': 'singlevaluelist',
	'Number': 'number',
	'Date': 'datetime'
};
```

### Store Types

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/stores/types.ts`

| Type | Purpose |
|------|---------|
| `CardId` | `'group' \| 'header' \| 'attr-${number}'` - Unique card identifiers |
| `EditorSelection` | Current selection state (type, groupId, serviceCode) |
| `GroupWithServices` | Group entity with nested services and count |
| `CardDirtyState` | Tracks isDirty, originalValue, currentValue |

## Key Features

| Feature | Implementation |
|---------|----------------|
| **Split-Pane Layout** | Resizable two-panel interface with tree and editor |
| **Accordion Behavior** | Only one attribute card expanded at a time via `expandedAttributeIndex` |
| **Explicit Save** | Per-card Save/Cancel buttons instead of auto-save |
| **Dirty Tracking** | `Map<CardId, CardDirtyState>` tracks original vs current values |
| **Unsaved Changes Modal** | Intercepts navigation via `attemptNavigation()` pattern |
| **Drag & Drop** | Service reordering in tree, attribute reordering in editor |
| **Toast Notifications** | SaveToast component for save feedback |
| **Keyboard Navigation** | Full keyboard support for accessibility |

## Data Flow

```
User Action -> Event Dispatch -> Page Handler -> API Call -> Update State -> Re-render
     |                             |
     +-- Tree/Editor Events -------+
           (CustomEvent pattern)
```

### Event Flow Example: Saving an Attribute

1. User modifies attribute in `AttributeCardExpanded`
2. Change triggers `markDirty` event, propagates to page
3. Page updates `dirtyAttributes` Set and `pendingAttributeValues` Map
4. User clicks Save in `AttributeCardFooter`
5. `save` event dispatches to page handler
6. Page calls `libre311.updateServiceDefinitionAttribute()`
7. On success, page clears dirty state and shows success toast
8. On error, page shows error toast and keeps dirty state

### Event Flow Example: Navigation with Unsaved Changes

1. User clicks different service in TreePanel
2. `selectService` event fires to page
3. Page checks `hasAnyUnsavedChanges` derived store
4. If dirty, calls `splitPaneStore.showUnsavedChangesModal(callback)`
5. Modal displays with Save/Discard options
6. User chooses action:
   - **Save:** Saves changes, then executes pending navigation
   - **Discard:** Clears dirty state, executes pending navigation

## API Integration

The editor integrates with the existing `Libre311` service for all backend operations:

| Operation | API Method |
|-----------|------------|
| Load groups | `getGroupList()` |
| Load services | `getServiceList()` |
| Load service definition | `getServiceDefinition(serviceCode)` |
| Update service | `updateService(serviceCode, data)` |
| Create attribute | `createServiceDefinitionAttribute(serviceCode, data)` |
| Update attribute | `updateServiceDefinitionAttribute(serviceCode, code, data)` |
| Delete attribute | `deleteServiceDefinitionAttribute(serviceCode, code)` |
| Reorder attributes | `updateAttributeOrder(serviceCode, orderedCodes)` |
| Create group | `createGroup(data)` |
| Update group | `updateGroup(groupId, data)` |
| Delete group | `deleteGroup(groupId)` |

## Test Coverage

The implementation includes comprehensive unit tests using Vitest:

| Test File | Coverage |
|-----------|----------|
| `AttributeCard.test.ts` | Card expand/collapse, dirty state, save/cancel |
| `OptionsList.test.ts` | Add/edit/delete options, validation |
| `TreePanel.test.ts` | Group expansion, service selection, drag-drop |
| `TreeGroup.test.ts` | Group rendering, service count badge |
| `TreeService.test.ts` | Service item rendering, selection state |
| `ServiceHeaderCard.test.ts` | Header editing, dirty tracking |
| `AttributeCardList.test.ts` | List rendering, reordering |
| `UnsavedChangesModal.test.ts` | Modal display, save/discard actions |
| `SaveToast.test.ts` | Toast display, auto-dismiss |
| `EditorContainer.test.ts` | Container state management |
| `autoSave.test.ts` | Auto-save utility functions |

## Directory Structure

```
frontend/src/lib/components/ServiceDefinitionEditor/
├── AttributeCard/
│   ├── AttributeCard.svelte
│   ├── AttributeCard.test.ts
│   ├── AttributeCardCollapsed.svelte
│   ├── AttributeCardExpanded.svelte
│   ├── AttributeCardFooter.svelte
│   ├── AttributeTypeSelector.svelte
│   ├── OptionsList.svelte
│   └── OptionsList.test.ts
├── EditorView/
│   ├── EditorContainer.svelte
│   ├── EditorContainer.test.ts
│   ├── SaveToast.svelte
│   └── SaveToast.test.ts
├── GroupEditor/
│   └── GroupEditor.svelte
├── ServiceEditor/
│   ├── AttributeCardList.svelte
│   ├── AttributeCardList.test.ts
│   ├── ServiceHeaderCard.svelte
│   └── ServiceHeaderCard.test.ts
├── Shared/
│   ├── ConfirmDeleteModal.svelte
│   ├── DragHandle.svelte
│   ├── SaveButton.svelte
│   ├── UnsavedChangesModal.svelte
│   └── UnsavedChangesModal.test.ts
├── SplitPaneEditor/
│   ├── EditorPanel.svelte
│   ├── SplitPaneLayout.svelte
│   ├── TreePanel.svelte
│   └── TreePanel.test.ts
├── TreeView/
│   ├── TreeGroup.svelte
│   ├── TreeGroup.test.ts
│   ├── TreeService.svelte
│   └── TreeService.test.ts
├── stores/
│   ├── editorStore.ts
│   ├── listViewStore.ts
│   └── types.ts
├── utils/
│   ├── autoSave.ts
│   └── autoSave.test.ts
└── types.ts
```
