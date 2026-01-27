# Quickstart: Service Definition Editor Refinement

**Feature**: 010-service-editor-refinement
**Date**: 2026-01-09

## Prerequisites

- Node.js 18+ installed
- Backend API running (or Docker environment)
- Access to a jurisdiction with admin permissions

## Development Setup

### 1. Start the Backend

Option A - Local:
```bash
# From project root
source setenv.sh
./gradlew app:run
```

Option B - Docker:
```bash
docker compose -f docker-compose.local.yml up
```

### 2. Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

The dev server starts at `http://localhost:3000`

### 3. Access the Editor

1. Login with admin credentials
2. Navigate to Admin section
3. Click "Service Definition Configuration"
4. (Or go directly to `/groups/config` when implemented)

## Development Workflow

### Running Tests

```bash
# Unit tests (watch mode)
npm run test:unit

# Unit tests (single run)
npm run test:unit -- --run

# Integration tests (requires running app)
npm run test:integration

# All tests
npm run test
```

### Code Quality

```bash
# Type checking
npm run check

# Linting (includes Prettier check)
npm run lint

# Format code
npm run format
```

### Before Committing

```bash
npm run format && npm run lint && npm run check && npm run test:unit -- --run
```

## Component Development

### Creating a New Component

1. Create component file in appropriate directory:
   ```
   frontend/src/lib/components/ServiceDefinitionEditor/[Category]/ComponentName.svelte
   ```

2. Create co-located test file:
   ```
   frontend/src/lib/components/ServiceDefinitionEditor/[Category]/ComponentName.test.ts
   ```

3. Export from component (if needed for external use)

### Component Template

```svelte
<script lang="ts">
	import { createEventDispatcher } from 'svelte';

	// Props
	export let propName: string;

	// Events
	const dispatch = createEventDispatcher<{
		eventName: { detail: string };
	}>();

	// Logic
	function handleAction() {
		dispatch('eventName', { detail: 'value' });
	}
</script>

<div class="component-class" role="region" aria-label="Component label">
	<!-- Content -->
</div>

<style>
	/* Scoped styles if needed */
</style>
```

### Test Template

```typescript
import { describe, it, expect } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/svelte';
import ComponentName from './ComponentName.svelte';

describe('ComponentName', () => {
	it('renders with required props', () => {
		render(ComponentName, { props: { propName: 'value' } });
		expect(screen.getByRole('region')).toBeInTheDocument();
	});

	it('dispatches event on action', async () => {
		const { component } = render(ComponentName, { props: { propName: 'value' } });

		let eventFired = false;
		component.$on('eventName', () => { eventFired = true; });

		await fireEvent.click(screen.getByRole('button'));
		expect(eventFired).toBe(true);
	});
});
```

## Key Files Reference

### Existing Components to Study

| Component | Location | Pattern |
|-----------|----------|---------|
| EditorContainer | `lib/components/ServiceDefinitionEditor/EditorView/EditorContainer.svelte` | Container layout, loading/error states |
| SaveToast | `lib/components/ServiceDefinitionEditor/EditorView/SaveToast.svelte` | Toast notifications |
| GroupListItem | `lib/components/ServiceDefinitionEditor/GroupListItem.svelte` | List item with actions |
| DragAndDrop | `lib/components/ServiceDefinitionEditor/DragAndDrop.svelte` | Drag-drop implementation |

### API Service

```typescript
// frontend/src/lib/services/Libre311/Libre311.ts
import { getLibre311Context } from '$lib/context/Libre311Context';

const libre311 = getLibre311Context();

// Group operations
await libre311.getGroups();
await libre311.createGroup({ name: 'New Group' });
await libre311.updateGroup(groupId, { name: 'Updated' });
await libre311.deleteGroup(groupId);

// Service operations
await libre311.createService({ serviceName: 'New', groupId: 1 });
await libre311.updateService(serviceCode, { serviceName: 'Updated' });
await libre311.deleteService(serviceCode);

// Attribute operations
await libre311.createAttribute(serviceCode, attributeData);
await libre311.updateAttribute(serviceCode, attrCode, attributeData);
await libre311.deleteAttribute(serviceCode, attrCode);
```

### Store Usage

```typescript
// In component
import { editorStore } from '../stores/editorStore';

// Read state
$: selectedType = $editorStore.selection.type;
$: isDirty = $editorStore.cardStates.get('header')?.isDirty;

// Update state
editorStore.selectService(groupId, serviceCode);
editorStore.setCardDirty('header', true, originalValue, currentValue);
editorStore.setSaving('header', true);
```

## Accessibility Checklist

Before marking a component complete:

- [ ] All interactive elements keyboard accessible
- [ ] Focus indicators visible (min 2px)
- [ ] Proper ARIA roles and labels
- [ ] Color contrast 4.5:1 (normal text) / 3:1 (large text)
- [ ] Works with screen reader (VoiceOver/NVDA)
- [ ] Respects `prefers-reduced-motion`

### Testing Keyboard Navigation

1. Start at page top, press Tab
2. Verify focus moves through all interactive elements
3. Verify focus order is logical
4. Test Enter/Space for activation
5. Test Escape for modal/dropdown close
6. Test arrow keys for tree navigation

## Troubleshooting

### "Cannot find module" errors

```bash
npm install
npm run check
```

### Tests failing with DOM errors

Ensure `@testing-library/svelte` is installed:
```bash
npm install -D @testing-library/svelte @testing-library/jest-dom
```

### API calls failing

1. Check backend is running
2. Check `VITE_BACKEND_URL` in environment
3. Check browser Network tab for actual error
4. Verify JWT token is valid (check cookies)

### Prettier/Lint errors on commit

```bash
npm run format
npm run lint -- --fix
```
