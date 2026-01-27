# Data Model: Service Definition Editor Refinement

**Feature**: 010-service-editor-refinement
**Date**: 2026-01-09

## Overview

This document defines the frontend TypeScript types and state management for the split-pane service definition editor. No backend database changes are required - the feature uses existing entities via the JurisdictionAdminController API.

## Existing Backend Entities (Reference Only)

These entities already exist in the backend and are accessed via API. See [service-definition-model.md](../005-service-definition-editor/mockup/service-definition-model.md) for full details.

```
Jurisdiction (1) ─── (N) ServiceGroup (1) ─── (N) Service (1) ─── (N) ServiceDefinitionAttribute (1) ─── (N) AttributeValue
```

## Frontend Type Definitions

### Existing Types (from Libre311.ts)

These types already exist in `frontend/src/lib/services/Libre311/Libre311.ts`:

```typescript
// Group
export type Group = {
  id: number;
  name: string;
};

// Service
export type Service = {
  service_code: number;
  service_name: string;
  description?: string;
  metadata: boolean;
  type: 'realtime' | 'other';
  group_id: number;
};

// Attribute Data Types
export type DatatypeUnion =
  | 'string'
  | 'number'
  | 'datetime'
  | 'text'
  | 'singlevaluelist'
  | 'multivaluelist';

// Attribute
export type BaseServiceDefinitionAttribute = {
  variable: boolean;
  code: number;
  datatype: DatatypeUnion;
  required: boolean;
  datatype_description: string | null;
  order: number;
  description: string;
};

// Attribute Value (for list types)
export type AttributeValue = {
  key: string;
  name: string;
};

// List-based attribute with values
export type ListBasedServiceDefinitionAttribute = BaseServiceDefinitionAttribute & {
  datatype: 'singlevaluelist' | 'multivaluelist';
  values: AttributeValue[];
};
```

### New Types for Editor State

Create in `frontend/src/lib/components/ServiceDefinitionEditor/stores/types.ts`:

```typescript
/**
 * Type of item selected in the tree panel
 */
export type SelectionType = 'group' | 'service' | null;

/**
 * Unique identifier for a card (for dirty tracking)
 */
export type CardId =
  | 'group'           // Group editor card
  | 'header'          // Service header card
  | `attr-${number}`; // Attribute card by ID

/**
 * Editor selection state
 */
export interface EditorSelection {
  type: SelectionType;
  groupId: number | null;
  serviceCode: number | null;
}

/**
 * Unsaved changes state for a single card
 */
export interface CardDirtyState {
  isDirty: boolean;
  originalValue: unknown;
  currentValue: unknown;
}

/**
 * Complete editor state
 */
export interface EditorState {
  // Selection
  selection: EditorSelection;

  // Tree expansion state (which groups are expanded)
  expandedGroupIds: Set<number>;

  // Which attribute card is currently expanded (only one at a time)
  expandedAttributeId: number | null;

  // Per-card dirty state tracking
  cardStates: Map<CardId, CardDirtyState>;

  // Loading states
  isLoading: boolean;
  isSaving: Map<CardId, boolean>;

  // Error state
  error: string | null;
}

/**
 * Tree panel data structure (groups with their services)
 */
export interface TreeData {
  groups: GroupWithServices[];
}

export interface GroupWithServices extends Group {
  services: Service[];
  serviceCount: number;
}

/**
 * Service with full definition (including attributes)
 */
export interface ServiceWithDefinition extends Service {
  attributes: ServiceDefinitionAttribute[];
}

/**
 * Union type for all attribute types
 */
export type ServiceDefinitionAttribute =
  | BaseServiceDefinitionAttribute
  | ListBasedServiceDefinitionAttribute;

/**
 * Form data for creating/updating a group
 */
export interface GroupFormData {
  name: string;
}

/**
 * Form data for creating/updating a service header
 */
export interface ServiceHeaderFormData {
  serviceName: string;
  description: string;
}

/**
 * Form data for creating/updating an attribute
 */
export interface AttributeFormData {
  description: string;           // Question text
  datatype: DatatypeUnion;
  required: boolean;
  datatypeDescription: string;   // Help text
  values?: AttributeValue[];     // Only for list types
}

/**
 * Drag operation state
 */
export interface DragState {
  isDragging: boolean;
  dragType: 'service' | 'attribute' | null;
  draggedId: number | null;
  dropTargetId: number | null;
  dropPosition: 'before' | 'after' | null;
}
```

## State Management

### Editor Store

Create in `frontend/src/lib/components/ServiceDefinitionEditor/stores/editorStore.ts`:

```typescript
import { writable, derived, type Readable } from 'svelte/store';
import type {
  EditorState,
  CardId,
  SelectionType,
  TreeData,
  ServiceWithDefinition
} from './types';

// Initial state
const initialState: EditorState = {
  selection: {
    type: null,
    groupId: null,
    serviceCode: null
  },
  expandedGroupIds: new Set(),
  expandedAttributeId: null,
  cardStates: new Map(),
  isLoading: false,
  isSaving: new Map(),
  error: null
};

// Create the store
function createEditorStore() {
  const { subscribe, set, update } = writable<EditorState>(initialState);

  return {
    subscribe,

    // Selection actions
    selectGroup: (groupId: number) => update(state => ({
      ...state,
      selection: { type: 'group', groupId, serviceCode: null },
      expandedAttributeId: null
    })),

    selectService: (groupId: number, serviceCode: number) => update(state => ({
      ...state,
      selection: { type: 'service', groupId, serviceCode },
      expandedAttributeId: null
    })),

    clearSelection: () => update(state => ({
      ...state,
      selection: { type: null, groupId: null, serviceCode: null }
    })),

    // Tree expansion
    toggleGroupExpansion: (groupId: number) => update(state => {
      const expanded = new Set(state.expandedGroupIds);
      if (expanded.has(groupId)) {
        expanded.delete(groupId);
      } else {
        expanded.add(groupId);
      }
      return { ...state, expandedGroupIds: expanded };
    }),

    // Attribute card expansion (accordion behavior)
    expandAttribute: (attributeId: number) => update(state => ({
      ...state,
      expandedAttributeId: attributeId
    })),

    collapseAttribute: () => update(state => ({
      ...state,
      expandedAttributeId: null
    })),

    // Dirty state tracking
    setCardDirty: (cardId: CardId, isDirty: boolean, original?: unknown, current?: unknown) =>
      update(state => {
        const cardStates = new Map(state.cardStates);
        cardStates.set(cardId, { isDirty, originalValue: original, currentValue: current });
        return { ...state, cardStates };
      }),

    clearCardDirty: (cardId: CardId) => update(state => {
      const cardStates = new Map(state.cardStates);
      cardStates.delete(cardId);
      return { ...state, cardStates };
    }),

    // Check if any card has unsaved changes
    hasUnsavedChanges: derived<Readable<EditorState>, boolean>(
      { subscribe },
      $state => Array.from($state.cardStates.values()).some(card => card.isDirty)
    ),

    // Loading states
    setLoading: (isLoading: boolean) => update(state => ({ ...state, isLoading })),

    setSaving: (cardId: CardId, isSaving: boolean) => update(state => {
      const saving = new Map(state.isSaving);
      if (isSaving) {
        saving.set(cardId, true);
      } else {
        saving.delete(cardId);
      }
      return { ...state, isSaving: saving };
    }),

    // Error handling
    setError: (error: string | null) => update(state => ({ ...state, error })),

    // Reset store
    reset: () => set(initialState)
  };
}

export const editorStore = createEditorStore();
```

### Derived Stores

```typescript
// Derived store for whether the current selection has unsaved changes
export const currentCardDirty = derived(
  editorStore,
  $state => {
    if ($state.selection.type === 'group') {
      return $state.cardStates.get('group')?.isDirty ?? false;
    }
    if ($state.selection.type === 'service') {
      return $state.cardStates.get('header')?.isDirty ?? false;
    }
    return false;
  }
);

// Derived store for checking if a specific attribute is dirty
export function isAttributeDirty(attributeId: number): Readable<boolean> {
  return derived(
    editorStore,
    $state => $state.cardStates.get(`attr-${attributeId}`)?.isDirty ?? false
  );
}
```

## Data Flow

### Loading Tree Data

```
1. Page mount
2. Fetch groups with services from API
3. Transform to TreeData structure
4. Render tree panel
```

### Selecting an Item

```
1. User clicks group/service in tree
2. Check for unsaved changes
   - If dirty: Show UnsavedChangesModal
   - User chooses Save/Discard
3. Update selection in store
4. If service: Fetch full service definition with attributes
5. Render appropriate editor in right panel
```

### Editing and Saving

```
1. User modifies field value
2. Store original and current values
3. Mark card as dirty (enables Save button)
4. User clicks Save
5. Set saving state (show spinner, disable button)
6. Call API to persist changes
7. Clear dirty state
8. Show success toast
9. Update tree if name changed
```

### Drag and Drop Reorder

```
1. User starts drag on handle
2. Set drag state, apply visual styles
3. User drags over potential drop targets
4. Calculate and show drop indicator
5. User drops
6. Call reorder API
7. Update local data
8. Reset drag state
```

## Validation Rules

Inherited from backend - enforced on save:

| Field | Rule |
|-------|------|
| Group name | Required, unique per jurisdiction |
| Service name | Required, not blank |
| Attribute description | Required (question text) |
| Attribute values | At least 1 for list types |

Frontend validation (immediate feedback):
- Show validation errors inline
- Disable Save button if required fields empty
- Prevent deletion of last option in list types
