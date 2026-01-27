# Quickstart: Service Definition Editor

**Feature**: 005-service-definition-editor
**Date**: 2026-01-05

## Prerequisites

- Node.js 18+ installed
- Backend API running (port 8080) with UnityAuth
- Valid admin credentials for a jurisdiction

## Development Setup

### 1. Start Backend

```bash
# Terminal 1: Start backend API
source setenv.sh
./gradlew app:run
```

### 2. Start Frontend

```bash
# Terminal 2: Start frontend dev server
cd frontend
npm install
npm run dev
```

### 3. Access Application

1. Open http://localhost:3000
2. Log in with admin credentials
3. Navigate to **Groups** in the admin menu

## Feature Entry Points

### List View (Existing Route)
```
/groups/
```
- Browse all service groups
- Expand/collapse groups to see services
- Click "Edit" to open editor

### Editor View (New Route)
```
/groups/[group_id]/services/[service_id]/edit
```
- Full-page Google Forms-style editor
- Header card with service name/description
- Attribute cards for managing fields

## Key Development Files

### New Components to Create

```
frontend/src/lib/components/ServiceDefinitionEditor/EditorView/
├── EditorContainer.svelte      # Main page layout
├── HeaderCard.svelte           # Service name/description card
├── AttributeCard.svelte        # Accordion wrapper
├── AttributeCardCollapsed.svelte
├── AttributeCardExpanded.svelte
├── AttributeTypeSelector.svelte
├── OptionsList.svelte          # For list-type attributes
├── AttributeFooter.svelte      # Actions, required toggle
├── AddFieldButton.svelte       # FAB
└── SaveToast.svelte            # Auto-save notification
```

### New Route

```
frontend/src/routes/groups/[group_id]/services/[service_id]/edit/
└── +page.svelte
```

### Existing Files to Modify

```
frontend/src/routes/groups/+page.svelte           # Enhance group list
frontend/src/lib/components/ServiceDefinitionEditor/
├── GroupListItem.svelte        # Add expand/collapse
└── ServiceListItem.svelte      # Add Edit navigation
```

## API Client Usage

All methods available via `useLibre311Service()`:

```typescript
import { useLibre311Service } from '$lib/context/Libre311Context';

const libre311 = useLibre311Service();

// Load service definition
const serviceDef = await libre311.getServiceDefinition({ service_code: 42 });

// Update attribute
await libre311.editAttribute({
  service_code: 42,
  attribute_code: 101,
  description: 'Updated question',
  datatype_description: 'Help text',
  required: true,
  values: [{ key: '1', name: 'Option A' }]
});

// Reorder attributes
await libre311.updateAttributesOrder({
  service_code: 42,
  attributes: [
    { code: 102, order: 1 },
    { code: 101, order: 2 }
  ]
});
```

## Testing

### Unit Tests

```bash
cd frontend
npm run test:unit
```

Create co-located test files:
```
AttributeCard.svelte
AttributeCard.test.ts
```

### Integration Tests

```bash
cd frontend
npm run test:integration
```

Test files location:
```
frontend/tests/service-definition-editor/
├── browse-groups.spec.ts
├── edit-service.spec.ts
└── manage-attributes.spec.ts
```

## Styling Guidelines

### Component Structure

```svelte
<script lang="ts">
  // TypeScript with strict types
</script>

<!-- Use STWUI components where applicable -->
<Card bordered={true}>
  <!-- Tailwind for custom styling -->
  <div class="p-4 bg-white rounded-lg shadow">
    ...
  </div>
</Card>

<style>
  /* Scoped CSS for component-specific styles */
</style>
```

### Tailwind Classes Reference

```html
<!-- Card styling -->
<div class="bg-white rounded-lg shadow-sm border border-gray-200">

<!-- Collapsed attribute card -->
<div class="p-4 hover:bg-gray-50 cursor-pointer">

<!-- Expanded attribute card -->
<div class="p-4 border-l-4 border-blue-500">

<!-- Header card with colored banner -->
<div class="border-t-8 border-purple-600">

<!-- Required indicator -->
<span class="text-red-500">*</span>

<!-- Toast notification -->
<div class="fixed bottom-6 left-1/2 transform -translate-x-1/2 bg-gray-800 text-white px-6 py-3 rounded">
```

## Accessibility Checklist

- [ ] All interactive elements keyboard accessible
- [ ] Tab order follows visual order
- [ ] Escape key collapses expanded card
- [ ] Focus visible on all interactive elements
- [ ] ARIA labels on accordion buttons
- [ ] Live region for save notifications
- [ ] Color contrast 4.5:1 minimum

## Common Tasks

### Add a new attribute type

1. Update `DATATYPE_MAP` in data model
2. Add UI in `AttributeTypeSelector.svelte`
3. Handle type-specific rendering in `AttributeCardExpanded.svelte`

### Implement auto-save

```typescript
let saveTimeout: ReturnType<typeof setTimeout>;

function handleFieldChange(value: string) {
  localValue = value;  // Optimistic update
  clearTimeout(saveTimeout);
  saveTimeout = setTimeout(async () => {
    try {
      await libre311.editAttribute({ ... });
      showToast('Changes saved');
    } catch (error) {
      showToast('Save failed', 'error');
    }
  }, 500);
}
```

### Add drag-and-drop

```svelte
<script>
  import DragAndDrop from '$lib/components/DragAndDrop.svelte';

  async function handleReorder(event: CustomEvent<Attribute[]>) {
    const newOrder = event.detail.map((attr, i) => ({
      code: attr.code,
      order: i
    }));
    await libre311.updateAttributesOrder({
      service_code: serviceCode,
      attributes: newOrder
    });
  }
</script>

<DragAndDrop items={attributes} on:itemsChanged={handleReorder}>
  <svelte:fragment slot="item" let:item>
    <AttributeCard attribute={item} />
  </svelte:fragment>
</DragAndDrop>
```

## Troubleshooting

### API returns 401 Unauthorized
- Ensure you're logged in
- Check JWT token hasn't expired
- Verify admin permissions

### Changes not persisting
- Check browser console for API errors
- Verify network requests in DevTools
- Ensure jurisdiction_id is correct

### Drag-and-drop not working
- Verify `draggable="true"` on elements
- Check for CSS `pointer-events: none` blocking
- Ensure `itemsChanged` event handler is connected
