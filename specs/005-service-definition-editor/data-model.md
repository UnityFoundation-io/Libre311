# Data Model: Service Definition Editor

**Feature**: 005-service-definition-editor
**Date**: 2026-01-05

## Overview

This is a frontend-only feature. The data model documents:
1. Existing TypeScript types from `Libre311.ts` (backend entities)
2. New frontend-specific types for UI state management
3. Svelte store structure for editor state

## Existing Types (from Libre311.ts)

### Core Entities

```typescript
// Service Group
interface Group {
  id: number;
  name: string;
}

// Service (Service Request Type)
interface Service {
  service_code: number;
  service_name: string;
  description?: string;
  metadata: boolean;
  type: 'realtime' | 'other';
  group_id: number;
}

// Attribute Data Types
type DatatypeUnion =
  | 'string'      // Short answer
  | 'text'        // Paragraph
  | 'number'      // Number
  | 'datetime'    // Date
  | 'singlevaluelist'  // Dropdown
  | 'multivaluelist';  // Multiple choice

// Attribute Value (for list types)
interface AttributeValue {
  key: string;   // Unique ID
  name: string;  // Display text
}

// Service Definition Attribute
interface ServiceDefinitionAttribute {
  code: number;
  variable: boolean;
  datatype: DatatypeUnion;
  required: boolean;
  datatype_description: string | null;
  order: number;
  description: string;  // Question text
  values?: AttributeValue[];  // Only for list types
}

// Service Definition (Service + Attributes)
interface ServiceDefinition {
  service_code: number;
  attributes: ServiceDefinitionAttribute[];
}
```

### API Request/Response Types

```typescript
// Create Service
interface CreateServiceParams {
  service_name: string;
  group_id: number;
}

interface CreateServiceResponse extends Service {
  jurisdiction_id: string;
}

// Edit Service
interface EditServiceParams {
  service_code: number;
  service_name: string;
}

// Create Attribute
interface CreateServiceDefinitionAttributesParams {
  service_code: number;
  description: string;
  datatype_description: string;
  datatype: string;
  variable: boolean;
  required: boolean;
  order: number;
  values?: AttributeValue[];
}

// Edit Attribute
interface EditServiceDefinitionAttributeParams {
  attribute_code: number;
  service_code: number;
  description: string;
  datatype_description: string;
  required: boolean;
  values?: AttributeValue[];
}
```

## New Frontend Types

### Editor State Types

```typescript
// UI-friendly datatype labels
type AttributeTypeLabel =
  | 'Short answer'
  | 'Paragraph'
  | 'Multiple choice'
  | 'Dropdown'
  | 'Number'
  | 'Date';

// Mapping between UI labels and backend types
const DATATYPE_MAP: Record<AttributeTypeLabel, DatatypeUnion> = {
  'Short answer': 'string',
  'Paragraph': 'text',
  'Multiple choice': 'multivaluelist',
  'Dropdown': 'singlevaluelist',
  'Number': 'number',
  'Date': 'datetime'
};

// Editor card state
type CardState = 'collapsed' | 'expanded';

// Attribute with UI state
interface AttributeCardState extends ServiceDefinitionAttribute {
  cardState: CardState;
  isDirty: boolean;
  isSaving: boolean;
  error?: string;
}

// Editor page state
interface EditorState {
  service: Service | null;
  attributes: AttributeCardState[];
  expandedCardIndex: number | null;
  isLoading: boolean;
  error: string | null;
}

// Save status for toast
type SaveStatus =
  | { type: 'idle' }
  | { type: 'saving' }
  | { type: 'success' }
  | { type: 'error'; message: string };
```

### List View State Types

```typescript
// Group with expansion state
interface GroupListState extends Group {
  isExpanded: boolean;
  services: Service[];
  isLoading: boolean;
}

// List view page state
interface ListViewState {
  groups: GroupListState[];
  isLoading: boolean;
  error: string | null;
  newGroupForm: {
    isVisible: boolean;
    name: string;
    error: string | null;
  };
}
```

## Svelte Store Structure

### Editor Store

```typescript
// stores/editorStore.ts
import { writable, derived } from 'svelte/store';

// Main editor state
export const editorState = writable<EditorState>({
  service: null,
  attributes: [],
  expandedCardIndex: null,
  isLoading: true,
  error: null
});

// Derived: currently expanded attribute
export const expandedAttribute = derived(
  editorState,
  ($state) => $state.expandedCardIndex !== null
    ? $state.attributes[$state.expandedCardIndex]
    : null
);

// Derived: has unsaved changes
export const hasUnsavedChanges = derived(
  editorState,
  ($state) => $state.attributes.some(attr => attr.isDirty)
);

// Save status for toast
export const saveStatus = writable<SaveStatus>({ type: 'idle' });
```

### Store Actions

```typescript
// actions/editorActions.ts

// Load service definition
async function loadService(serviceCode: number): Promise<void>;

// Update service header
async function updateServiceName(name: string): Promise<void>;
async function updateServiceDescription(description: string): Promise<void>;

// Attribute card management
function expandCard(index: number): void;
function collapseCard(): void;

// Attribute CRUD
async function addAttribute(datatype: DatatypeUnion): Promise<void>;
async function updateAttribute(code: number, updates: Partial<ServiceDefinitionAttribute>): Promise<void>;
async function deleteAttribute(code: number): Promise<void>;
async function duplicateAttribute(code: number): Promise<void>;

// Attribute values (for list types)
async function addAttributeValue(attributeCode: number, valueName: string): Promise<void>;
async function updateAttributeValue(attributeCode: number, valueKey: string, newName: string): Promise<void>;
async function removeAttributeValue(attributeCode: number, valueKey: string): Promise<void>;

// Reordering
async function reorderAttributes(newOrder: number[]): Promise<void>;
```

## Entity Relationships

```
┌─────────────────────────────────────────────────────────────────┐
│                        FRONTEND STATE                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ListViewState                                                  │
│  ├── groups: GroupListState[]                                   │
│  │   ├── id, name (from Group)                                  │
│  │   ├── isExpanded (UI state)                                  │
│  │   └── services: Service[]                                    │
│  │       └── service_code, service_name, group_id               │
│  └── newGroupForm (UI state)                                    │
│                                                                 │
│  EditorState                                                    │
│  ├── service: Service                                           │
│  │   └── service_code, service_name, description                │
│  ├── attributes: AttributeCardState[]                           │
│  │   ├── code, description, datatype, required, order (data)    │
│  │   ├── values?: AttributeValue[] (for list types)             │
│  │   └── cardState, isDirty, isSaving (UI state)                │
│  └── expandedCardIndex (UI state)                               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ API Calls
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        BACKEND ENTITIES                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Jurisdiction (tenant isolation)                                │
│  └── ServiceGroup                                               │
│      ├── id, name                                               │
│      └── Service (ServiceRequest type)                          │
│          ├── id (service_code), serviceName, description        │
│          └── ServiceDefinitionAttribute                         │
│              ├── id (code), description, datatype               │
│              ├── required, attributeOrder, variable             │
│              └── AttributeValue (for list types)                │
│                  ├── id (key), valueName (name)                 │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Validation Rules

### Service
| Field | Rule | Error Message |
|-------|------|---------------|
| service_name | Required, non-empty | "Service name is required" |
| service_name | Max 200 chars | "Service name must be 200 characters or less" |

### Attribute
| Field | Rule | Error Message |
|-------|------|---------------|
| description | Required, non-empty | "Question text is required" |
| description | Max 500 chars | "Question text must be 500 characters or less" |
| datatype | Valid enum value | "Invalid field type" |

### Attribute Value
| Field | Rule | Error Message |
|-------|------|---------------|
| name | Required, non-empty | "Option text is required" |
| name | Max 200 chars | "Option text must be 200 characters or less" |
| values (list) | Min 1 for list types | "At least one option is required" |

## State Transitions

### Attribute Card Lifecycle

```
┌──────────┐
│ LOADING  │ ──load──→ ┌───────────┐
└──────────┘           │ COLLAPSED │ ◄──────────────────┐
                       └─────┬─────┘                    │
                             │ click                    │
                             ▼                          │
                       ┌───────────┐                    │
                       │ EXPANDED  │ ──click other──────┤
                       └─────┬─────┘                    │
                             │ edit                     │
                             ▼                          │
                       ┌───────────┐                    │
                       │   DIRTY   │ ──auto-save───────►│
                       └─────┬─────┘                    │
                             │ save start              │
                             ▼                          │
                       ┌───────────┐                    │
                       │  SAVING   │ ──success─────────►│
                       └─────┬─────┘                    │
                             │ error                    │
                             ▼                          │
                       ┌───────────┐                    │
                       │   ERROR   │ ──retry/dismiss───►│
                       └───────────┘
```

### Group Expansion

```
┌───────────┐           ┌──────────┐
│ COLLAPSED │ ──click──→│ EXPANDED │
│ (count)   │           │ (list)   │
└───────────┘ ◄──click──└──────────┘
```
