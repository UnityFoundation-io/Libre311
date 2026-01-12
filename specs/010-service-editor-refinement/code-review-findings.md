# Code Review Findings: Service Definition Editor

**Review Date:** 2026-01-11 (Updated)
**Branch:** 010-service-editor-refinement
**Reviewer:** Claude Code Review

## Executive Summary

Overall, this is a well-structured implementation with good separation of concerns and thoughtful component design. However, there are several issues ranging from **critical** to **low** severity that should be addressed before merging.

| Severity | Count | Status |
|----------|-------|--------|
| Critical | 2 | 2 Fixed |
| High | 8 | 6 Fixed, 2 Pending |
| Medium | 10 | 5 Fixed, 5 Pending |
| Low | 8 | 1 Fixed, 7 Pending |

---

## Critical Issues

### C1. Memory Leak: Document Event Listener in SaveToast

- [x] **Fixed** (2026-01-11) - Replaced with proper Svelte event dispatcher pattern

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/EditorView/SaveToast.svelte:49-53`

```typescript
function handleRetry() {
    // Emit retry event - parent component should handle actual retry logic
    const event = new CustomEvent('retry');
    document.dispatchEvent(event);
}
```

**Problem:** The `handleRetry` function dispatches a custom event directly on `document`, which:
- Bypasses Svelte's component communication patterns
- Has no corresponding listener anywhere in the codebase
- Could lead to memory leaks if listeners are attached elsewhere without cleanup

**Impact:** Memory leaks, broken retry functionality, anti-pattern usage.

**Fix Applied:**
```typescript
import { createEventDispatcher } from 'svelte';

const dispatch = createEventDispatcher<{
    retry: void;
}>();

function handleRetry() {
    dispatch('retry');
}
```

---

### C2. Memory Leak: Missing Event Listener Cleanup in SplitPaneLayout

- [x] **Fixed** (2026-01-11) - Added `onDestroy` cleanup for resize event listeners

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/SplitPaneEditor/SplitPaneLayout.svelte:113-138`

```typescript
function startResize(event: MouseEvent) {
    isResizing = true;
    // ...
    document.addEventListener('mousemove', handleResize);
    document.addEventListener('mouseup', stopResize);
    // ...
}
```

**Problem:** Event listeners added to `document` during resize operations are not guaranteed to be cleaned up if the component unmounts mid-resize. This leaves orphaned event listeners on `document` and can cause:
- Memory leaks
- Body style corruption (`cursor: col-resize`, `userSelect: none` stuck)
- Unexpected behavior after component removal

**Impact:** Memory leaks, frozen cursor styles, document-level event handler accumulation.

**Fix Applied:**
```typescript
import { createEventDispatcher, onDestroy } from 'svelte';

// ... existing code ...

// Cleanup: ensure event listeners are removed if component unmounts during resize
onDestroy(() => {
    document.removeEventListener('mousemove', handleResize);
    document.removeEventListener('mouseup', stopResize);
    document.body.style.cursor = '';
    document.body.style.userSelect = '';
});
```

---

## High Severity Issues

### H1. Reactive Statement Causing Infinite Loop Risk

- [x] **Fixed** (2026-01-09) - Added `lastAttributeCode` tracking to detect actual prop changes

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/AttributeCard/AttributeCardExpanded.svelte:64-66`

```svelte
// Initialize from attribute prop
$: if (attribute) {
    initializeForm(attribute);
}
```

**Problem:** This reactive statement runs every time `attribute` changes, but it also runs when any reactive dependency within `initializeForm` changes (due to how Svelte tracks dependencies). When the component's internal state is modified (e.g., `description`), and those variables are read in `initializeForm`, it can cause unexpected re-initialization, losing user edits.

**Impact:** Users may lose their form edits unexpectedly when certain state changes occur.

**Suggested Fix:**
```svelte
// Use a tracked reference to detect actual prop changes
let lastAttributeCode: number | null = null;

$: if (attribute && attribute.code !== lastAttributeCode) {
    lastAttributeCode = attribute.code;
    initializeForm(attribute);
}
```

---

### H2. Dirty State Dispatch on Every Reactive Update

- [x] **Fixed** (2026-01-09) - Added `previousIsDirty` tracking to only dispatch when value changes

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/AttributeCard/AttributeCardExpanded.svelte:93-94`

```svelte
// Notify parent of dirty changes
$: dispatch('dirty', { isDirty });
```

**Problem:** This dispatches an event on every single reactive update, not just when `isDirty` actually changes value. This causes:
1. Excessive event dispatching
2. Potential infinite loops if parent updates state in response
3. Performance degradation

**Impact:** Performance issues, potential infinite render loops.

**Suggested Fix:**
```svelte
let previousIsDirty = false;

$: if (isDirty !== previousIsDirty) {
    previousIsDirty = isDirty;
    dispatch('dirty', { isDirty });
}
```

---

### H3. Global Keyboard Event Handler Without Cleanup

- [x] **Fixed** (2026-01-09) - Moved keyboard handler from `<svelte:window>` to container div with `role="form"`

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/AttributeCard/AttributeCardExpanded.svelte:202`

```svelte
<svelte:window on:keydown={handleKeydown} />
```

**Problem:** This attaches a global keyboard listener that intercepts `Ctrl+S` and `Escape` for ALL pages. When multiple `AttributeCardExpanded` components exist (even collapsed ones), they all compete for the same keystrokes. The `Escape` key will collapse all expanded cards simultaneously.

**Impact:** Unexpected behavior when navigating, keyboard shortcuts captured globally.

**Suggested Fix:**
Only attach the listener when the component is expanded, and use event bubbling:
```svelte
<script>
    // Remove svelte:window handler and attach to the container
</script>

<div
    class="border-t border-gray-200"
    on:keydown={handleKeydown}
    tabindex="-1"
>
```

---

### H4. Missing Error Handling in API Calls

- [x] **Fixed** (2026-01-10) - Added `showSaveError()` calls to all API error handlers with user-friendly messages via SaveToast component

**File:** `frontend/src/routes/groups/config/+page.svelte:241-283`

```typescript
async function handleAttributeSave(...) {
    try {
        await libre311.editAttribute({...});
        // ...
    } catch (err) {
        console.error('Failed to save attribute:', err);
        // No user feedback!
    }
}
```

**Problem:** API errors are logged but not communicated to users. Multiple handlers have this pattern (`handleHeaderSave`, `handleGroupSave`, `handleAddAttribute`, etc.).

**Impact:** Users have no idea when saves fail, leading to data loss.

**Suggested Fix:**
```typescript
import { showSaveError } from '../stores/editorStore';

async function handleAttributeSave(...) {
    try {
        await libre311.editAttribute({...});
    } catch (err) {
        console.error('Failed to save attribute:', err);
        showSaveError('Failed to save changes. Please try again.');
        // Optionally set local error state
    }
}
```

---

### H5. Potential Infinite Loop in GroupEditor Reactive Statements

- [x] **Fixed** (2026-01-11) - Added `previousIsDirty` tracking to only dispatch when value changes

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/GroupEditor/GroupEditor.svelte:43-48`

```svelte
$: isDirty = editedName !== group.name;
$: dispatch('dirty', { isDirty });
```

**Problem:** The reactive statement dispatches an event every time `isDirty` is recalculated (on every reactive cycle), not just when `isDirty` actually changes value. This can cause:
1. Excessive event dispatching to parent components
2. Parent components re-rendering unnecessarily
3. Potential infinite loops if parent updates state in response

**Impact:** Performance degradation, potential infinite render loops.

**Fix Applied:**
```svelte
let previousIsDirty = false;

$: isDirty = editedName !== group.name;
$: if (isDirty !== previousIsDirty) {
    previousIsDirty = isDirty;
    dispatch('dirty', { isDirty });
}
```

---

### H6. Same Reactive Dispatch Issue in ServiceHeaderCard

- [ ] **Pending**

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/ServiceEditor/ServiceHeaderCard.svelte:46`

```svelte
$: dispatch('dirty', { isDirty, serviceName, description });
```

**Problem:** This dispatches on every keystroke in either input field, causing excessive events and potential performance issues as the parent tracks state.

**Impact:** Performance issues, unnecessary re-renders.

**Suggested Fix:**
```svelte
let previousIsDirty = false;

$: if (isDirty !== previousIsDirty) {
    previousIsDirty = isDirty;
    dispatch('dirty', { isDirty, serviceName, description });
}
```

---

### H7. Race Condition in EditorPanel

- [x] **Fixed** (2026-01-11) - Added `loadedServiceCode` tracking to prevent duplicate loads and race conditions

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/SplitPaneEditor/EditorPanel.svelte:35-51`

```svelte
$: if (selection.type === 'service' && selection.serviceCode) {
    dispatch('loadService', { serviceCode: selection.serviceCode });
}
```

**Problem:** This reactive statement fires every time `selection` object changes, even for the same `serviceCode`. This can cause:
- Unnecessary API calls (data re-fetching)
- Race conditions if selection changes rapidly
- Potential data inconsistencies if API responses arrive out of order

**Impact:** Performance degradation, potential data corruption, unnecessary network requests.

**Fix Applied:**
```svelte
let loadedServiceCode: number | null = null;

$: if (
    selection.type === 'service' &&
    selection.serviceCode &&
    selection.serviceCode !== loadedServiceCode
) {
    loadedServiceCode = selection.serviceCode;
    dispatch('loadService', { serviceCode: selection.serviceCode });
}

// Reset when selection type changes away from service
$: if (selection.type !== 'service') {
    loadedServiceCode = null;
}
```

---

### H8. Missing Focus Trap in Modals

- [ ] **Pending**

**Files:**
- `frontend/src/lib/components/ServiceDefinitionEditor/Shared/UnsavedChangesModal.svelte`
- `frontend/src/lib/components/ServiceDefinitionEditor/Shared/ConfirmDeleteModal.svelte`

**Problem:** Modal dialogs don't trap focus within them, which is a critical accessibility issue. Keyboard users can tab outside the modal while it's open, potentially interacting with hidden content.

**Impact:** Major accessibility violation (WCAG 2.1 Level A failure), confusing experience for keyboard users.

**Suggested Fix:** Implement focus trapping:
```svelte
<script>
    import { onMount } from 'svelte';

    let modalElement: HTMLElement;
    let previouslyFocused: HTMLElement | null = null;

    $: if (open) {
        previouslyFocused = document.activeElement as HTMLElement;
        // Focus first focusable element in modal
        setTimeout(() => {
            const firstButton = modalElement?.querySelector('button');
            firstButton?.focus();
        }, 0);
    }

    function handleKeydown(event: KeyboardEvent) {
        if (event.key === 'Tab') {
            // Trap focus within modal
            const focusable = modalElement?.querySelectorAll(
                'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
            );
            if (focusable?.length) {
                const first = focusable[0] as HTMLElement;
                const last = focusable[focusable.length - 1] as HTMLElement;

                if (event.shiftKey && document.activeElement === first) {
                    event.preventDefault();
                    last.focus();
                } else if (!event.shiftKey && document.activeElement === last) {
                    event.preventDefault();
                    first.focus();
                }
            }
        }
    }
</script>
```

---

## Medium Severity Issues

### M1. Array Mutation in Reactive Context

- [x] **Fixed** (2026-01-11) - Changed direct array mutation to use `.map()` for proper reactivity

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/AttributeCard/OptionsList.svelte:31-33`

```typescript
function handleValueChange(index: number, newName: string) {
    values[index] = { ...values[index], name: newName };  // Direct mutation!
    emitChange();
}
```

**Problem:** Direct array index assignment may not trigger Svelte's reactivity in all cases. While it works here because `emitChange()` creates a new array reference, it's an anti-pattern.

**Suggested Fix:**
```typescript
function handleValueChange(index: number, newName: string) {
    values = values.map((v, i) => i === index ? { ...v, name: newName } : v);
    emitChange();
}
```

---

### M2. JSON.stringify for Deep Comparison is Fragile

- [x] **Fixed** (2026-01-11) - Replaced with explicit `areValuesEqual()` function comparing key and name properties

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/AttributeCard/AttributeCardExpanded.svelte:91`

```typescript
$: isDirty = ... || JSON.stringify(values) !== JSON.stringify(originalValues);
```

**Problem:** `JSON.stringify` comparison:
- Fails if object property order differs
- Is expensive for large arrays
- Can produce false positives/negatives

**Suggested Fix:**
```typescript
function areValuesEqual(a: AttributeValue[], b: AttributeValue[]): boolean {
    if (a.length !== b.length) return false;
    return a.every((v, i) => v.key === b[i].key && v.name === b[i].name);
}

$: isDirty = ... || !areValuesEqual(values, originalValues);
```

---

### M3. Incomplete TODO Implementation

- [ ] **Fixed**

**File:** `frontend/src/routes/groups/config/+page.svelte:293-295`

```typescript
function handleAttributeCopy(event: ...) {
    // TODO: Implement copy attribute API call
    console.log('Copy attribute:', event.detail);
}
```

**Problem:** Copy functionality is wired up in UI but not implemented. Users can click "Copy" and nothing happens.

**Impact:** Broken user experience.

**Suggested Fix:** Either implement the functionality or disable/hide the button:
```svelte
<!-- In AttributeCardFooter.svelte -->
<button
    type="button"
    disabled={true}  <!-- or hide entirely until implemented -->
    title="Coming soon"
>
```

---

### M4. Non-null Assertion Operators

- [x] **Fixed** (2026-01-11) - Added null guards and captured values before async operations

**File:** `frontend/src/routes/groups/config/+page.svelte:260-261`

```typescript
await libre311.editAttribute({
    service_code: selectedService!.service_code,  // !
```

**Problem:** Multiple uses of `!` operator bypass TypeScript's null checks. If `selectedService` is unexpectedly null, this will throw at runtime.

**Suggested Fix:**
```typescript
async function handleAttributeSave(...) {
    if (!selectedService) {
        console.error('Cannot save attribute: no service selected');
        return;
    }

    await libre311.editAttribute({
        service_code: selectedService.service_code,
```

---

### M5. Memory Leak in autoSave Utility

- [x] **Fixed** (2026-01-11) - Added comprehensive documentation about `onDestroy` cleanup requirement

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/utils/autoSave.ts:79-176`

```typescript
let timeoutId: ReturnType<typeof setTimeout> | null = null;

function trigger(): void {
    timeoutId = setTimeout(async () => {
        // ...
    }, debounceMs);
}
```

**Problem:** If a component unmounts while a timeout is pending, the callback will still execute, potentially updating unmounted component state.

**Impact:** Memory leaks and "can't update state of unmounted component" errors.

**Suggested Fix:**
```typescript
// In the component using createAutoSave:
import { onDestroy } from 'svelte';

const autoSave = createAutoSave({...});

onDestroy(() => {
    autoSave.cancel();
});
```

---

### M6. Store Uses Map/Set Without Proper Reactivity Documentation

- [x] **Fixed** (2026-01-11) - Added detailed documentation explaining Map/Set reactivity limitations and correct usage patterns

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/stores/editorStore.ts:210-217`

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

**Problem:** While cloning is done correctly, Svelte's reactivity doesn't deeply track Map/Set mutations. The code handles this by always creating new instances, but any direct mutation would break reactivity.

**Impact:** Potential silent bugs if code is modified incorrectly in the future.

**Suggested Fix:** Add documentation or consider using plain objects:
```typescript
/**
 * IMPORTANT: Always clone Map/Set before modifying.
 * Svelte cannot detect mutations within Map/Set.
 */
```

---

### M7. Inconsistent Null Handling with Optional Chaining

- [ ] **Pending**

**File:** `frontend/src/routes/groups/config/+page.svelte:768-769`

```svelte
canDelete={groups.find((g) => g.id === selectedGroup?.id)?.serviceCount === 0}
serviceCount={groups.find((g) => g.id === selectedGroup?.id)?.serviceCount ?? 0}
```

**Problem:**
1. `selectedGroup?.id` is redundant since the component only renders when `selectedGroup` exists
2. Repeated `.find()` calls on every render are inefficient
3. Pattern is harder to read and maintain

**Impact:** Minor performance overhead, reduced code readability.

**Suggested Fix:**
```svelte
<script>
    $: selectedGroupData = selectedGroup ? groups.find((g) => g.id === selectedGroup.id) : null;
</script>

<!-- In template -->
<GroupEditor
    canDelete={selectedGroupData?.serviceCount === 0}
    serviceCount={selectedGroupData?.serviceCount ?? 0}
/>
```

---

### M8. Uncontrolled Input Array Growing in OptionsList

- [ ] **Pending**

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/AttributeCard/OptionsList.svelte:25`

```typescript
let inputRefs: HTMLInputElement[] = [];
```

**Problem:** The `inputRefs` array grows as options are added but is never pruned when options are deleted. Over time with heavy editing, this array accumulates stale references.

**Impact:** Minor memory leak, potential stale reference issues.

**Suggested Fix:** Clean up refs when deleting options:
```typescript
function handleDeleteOption(index: number) {
    if (values.length <= 1) return;

    // Remove the ref for the deleted option
    inputRefs = inputRefs.filter((_, i) => i !== index);
    values = values.filter((_, i) => i !== index);
    emitChange();
}
```

---

### M9. Keyboard Accessibility Gap in DragHandle

- [ ] **Pending**

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/Shared/DragHandle.svelte:26-29`

```svelte
on:mousedown
on:touchstart
on:keydown
on:click
```

**Problem:** The `on:keydown` handler is forwarded but there's no default keyboard interaction for drag-and-drop reordering. Screen reader users cannot reorder items using the keyboard.

**Impact:** Accessibility issue - keyboard-only users cannot reorder items.

**Suggested Fix:** Add keyboard-based reordering with clear instructions:
```svelte
<script>
    import { createEventDispatcher } from 'svelte';

    const dispatch = createEventDispatcher<{
        moveUp: void;
        moveDown: void;
    }>();

    function handleKeydown(event: KeyboardEvent) {
        if (event.key === 'ArrowUp' && (event.altKey || event.metaKey)) {
            event.preventDefault();
            dispatch('moveUp');
        }
        if (event.key === 'ArrowDown' && (event.altKey || event.metaKey)) {
            event.preventDefault();
            dispatch('moveDown');
        }
    }
</script>

<div
    aria-label="{ariaLabel}. Press Alt+Arrow keys to reorder."
    on:keydown={handleKeydown}
>
```

---

### M10. Inconsistent Error Handling Patterns

- [ ] **Pending**

**File:** `frontend/src/routes/groups/config/+page.svelte`

**Problem:** Error handling is inconsistent across the file - some functions show toast errors, others only log to console, and some proceed despite errors:

```typescript
// Line 104-107 - logs but shows generic error to editorError state
} catch (err) {
    console.error('Failed to load groups and services:', err);
    editorError = 'Failed to load data. Please try again.';
}

// Line 696-700 - shows toast but always proceeds (user may lose data)
} catch (err) {
    console.error('Failed to save changes:', err);
    showSaveError('Failed to save changes. Your edits may not have been saved.');
    splitPaneStore.proceedWithNavigation(); // Still proceeds!
}
```

**Impact:** Inconsistent user experience, potential data loss when errors occur.

**Suggested Fix:** Standardize error handling:
1. Always show user-facing error messages via toast
2. Don't proceed with navigation on save errors (let user decide)
3. Consider a centralized error handling utility

---

### M11. Svelte-ignore Comment Suppressing Important Warnings

- [ ] **Pending**

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/SplitPaneEditor/SplitPaneLayout.svelte:179`

```svelte
<!-- svelte-ignore a11y-no-noninteractive-tabindex a11y-no-noninteractive-element-interactions -->
```

**Problem:** The separator element IS actually interactive (handles resize via mouse and keyboard) but is marked to ignore accessibility warnings. The element has `role="separator"` which can be interactive.

**Impact:** Suppressing valid accessibility concerns, incomplete ARIA implementation.

**Suggested Fix:** Add proper ARIA attributes for an interactive slider:
```svelte
<div
    role="separator"
    aria-orientation="vertical"
    aria-label="Resize panels"
    aria-valuenow={leftPanelWidth}
    aria-valuemin={MIN_LEFT_WIDTH}
    aria-valuemax={MAX_LEFT_WIDTH}
    tabindex="0"
>
```

---

## Low Severity Issues

### L1. Hardcoded ID Attributes May Conflict

- [x] **Fixed** (2026-01-09) - Generated unique IDs using attribute code: `question-text-${attribute?.code ?? 'new'}`

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/AttributeCard/AttributeCardExpanded.svelte:225`

```svelte
<input id="question-text" ...>
```

**Problem:** Hardcoded `id` attributes will conflict when multiple instances exist on the page.

**Suggested Fix:**
```svelte
<script>
    const inputId = `question-text-${attribute.code}`;
</script>

<input id={inputId} ...>
```

---

### L2. Accessibility: Missing aria-live for Dynamic Content

- [ ] **Fixed**

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/AttributeCard/OptionsList.svelte:162-164`

```svelte
{#if values.length === 0}
    <p class="text-sm text-red-600">At least one option is required</p>
{/if}
```

**Problem:** Error messages that appear dynamically won't be announced to screen readers.

**Suggested Fix:**
```svelte
<p class="text-sm text-red-600" role="alert" aria-live="polite">
    At least one option is required
</p>
```

---

### L3. Unused Import in Types File

- [ ] **Fixed**

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/types.ts:4-6`

```typescript
import type {
    DatatypeUnion,
    Service,
    ServiceDefinitionAttribute,
    Group  // <-- Group is imported but not used in this file
} from '$lib/services/Libre311/Libre311';
```

**Suggested Fix:** Remove unused import.

---

### L4. Inconsistent Button Styling

- [ ] **Fixed**

**File:** `frontend/src/routes/groups/config/+page.svelte:673-687`

```svelte
class="... hover:border-purple-400 hover:text-purple-600 focus:ring-purple-500"
```

**Problem:** The "Add question" button uses purple accent while the rest of the UI uses blue. This appears intentional but differs from other buttons.

**Suggested Fix:** Ensure consistency or document the design decision:
```svelte
<!-- Uses purple to distinguish from other actions -->
class="... hover:border-blue-400 hover:text-blue-600 focus:ring-blue-500"
```

---

### L5. SSR Check Could Use Browser Check and Listen for Changes

- [ ] **Fixed**

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/AttributeCard/AttributeCard.svelte:123-126`

```typescript
let prefersReducedMotion = false;
if (typeof window !== 'undefined') {
    prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
}
```

**Problem:** This check runs only once at component initialization and won't update if user changes their system preference.

**Suggested Fix:** Use SvelteKit's browser constant or listen for changes:
```typescript
import { browser } from '$app/environment';

let prefersReducedMotion = false;

if (browser) {
    const mediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
    prefersReducedMotion = mediaQuery.matches;
    mediaQuery.addEventListener('change', (e) => {
        prefersReducedMotion = e.matches;
    });
}
```

---

### L6. Duplicate Spinner SVG Code

- [ ] **Pending**

**Files:** Multiple files contain identical spinner SVG markup:
- `SaveToast.svelte`
- `UnsavedChangesModal.svelte`
- `ConfirmDeleteModal.svelte`
- `GroupEditor.svelte`
- `EditorContainer.svelte`
- `TreePanel.svelte`

**Problem:** The same loading spinner SVG is duplicated across 6+ components.

**Impact:** Code duplication, harder to maintain consistent styling.

**Suggested Fix:** Extract to a shared component:
```svelte
<!-- Shared/Spinner.svelte -->
<script lang="ts">
    export let size: 'sm' | 'md' | 'lg' = 'md';

    const sizeClasses = {
        sm: 'h-4 w-4',
        md: 'h-5 w-5',
        lg: 'h-8 w-8'
    };
</script>

<svg
    class="animate-spin {sizeClasses[size]}"
    xmlns="http://www.w3.org/2000/svg"
    fill="none"
    viewBox="0 0 24 24"
>
    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
    <path
        class="opacity-75"
        fill="currentColor"
        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
    />
</svg>
```

---

### L7. Magic Numbers in SplitPaneLayout

- [ ] **Pending**

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/SplitPaneEditor/SplitPaneLayout.svelte:60-65`

```typescript
const MIN_LEFT_WIDTH = 240;
const MAX_LEFT_WIDTH = 480;
```

**Problem:** Panel width constraints are hardcoded. While documented with comments, they're not configurable.

**Impact:** Reduced flexibility for different screen sizes or user preferences.

**Suggested Fix:** Consider making these configurable via props with sensible defaults:
```typescript
export let minLeftWidth = 240;
export let maxLeftWidth = 480;
export let defaultLeftWidth = 320;
```

---

### L8. Unused mouseup Event Forwarding in DragHandle

- [ ] **Pending**

**File:** `frontend/src/lib/components/ServiceDefinitionEditor/Shared/DragHandle.svelte`

**Problem:** The component forwards `on:mouseup` event but doesn't actually emit it from within. The mouseup handler would fire on the div but the event was added thinking it needed explicit forwarding.

**Impact:** Misleading API, unnecessary event forwarding.

**Suggested Fix:** Either remove the unused event forwarding or ensure it's needed by checking all usage sites.

---

### L9. Browser API Access Pattern Duplicated

- [ ] **Pending**

**Files:** Multiple files have identical reduced motion detection:
- `AttributeCard.svelte:152-155`
- `AttributeCardList.svelte:79-82`
- `TreeGroup.svelte:80-84`

```typescript
let prefersReducedMotion = false;
if (typeof window !== 'undefined') {
    prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
}
```

**Problem:** Duplicated code for reduced motion detection across multiple components. None of them reactively update if the user changes their system preference.

**Impact:** Code duplication, inconsistent behavior if user changes preference mid-session.

**Suggested Fix:** Create a shared utility store:
```typescript
// utils/mediaQueries.ts
import { readable } from 'svelte/store';

export const prefersReducedMotion = readable(false, (set) => {
    if (typeof window === 'undefined') return;

    const query = window.matchMedia('(prefers-reduced-motion: reduce)');
    set(query.matches);

    const handler = (e: MediaQueryListEvent) => set(e.matches);
    query.addEventListener('change', handler);

    return () => query.removeEventListener('change', handler);
});
```

---

## Positive Observations

### Well-Implemented Patterns

1. **Good Component Composition** - Components are well-separated with clear responsibilities (AttributeCard -> AttributeCardExpanded/Collapsed -> Footer).

2. **Proper Event Dispatching** - Typed event dispatchers with clear event names and payloads.

3. **Accessibility Basics** - Good use of `role`, `aria-label`, and keyboard navigation in TreePanel.

4. **Keyed Each Blocks** - Correct use of `(option.key)` and `(service.service_code)` for list rendering.

5. **Reduced Motion Support** - Checking `prefers-reduced-motion` for transitions shows accessibility awareness.

6. **TypeScript Types** - Clean type definitions in `types.ts` and `stores/types.ts`.

7. **Separation of State** - Good separation between component-local state and store state.

8. **Test Coverage** - Unit tests exist for key utility functions and component logic.

---

## Priority Order for Fixes

### Phase 1: Critical Memory Leaks ✅ COMPLETE
1. ~~C1 - SaveToast document event dispatch~~ ✓ Fixed (2026-01-11)
2. ~~C2 - SplitPaneLayout resize event cleanup~~ ✓ Fixed (2026-01-11)

### Phase 2: Critical Reactivity Issues ✅ COMPLETE
3. ~~H1 - Reactive statement infinite loop risk~~ ✓ Fixed
4. ~~H2 - Dirty state dispatch on every update~~ ✓ Fixed

### Phase 3: High Severity - User Experience (Partial)
5. ~~H3 - Global keyboard handler~~ ✓ Fixed
6. ~~H4 - Missing error handling~~ ✓ Fixed
7. H5 - GroupEditor reactive dispatch loop
8. H6 - ServiceHeaderCard reactive dispatch loop
9. H7 - EditorPanel race condition
10. H8 - Missing focus trap in modals (Accessibility)

### Phase 4: Code Quality ✅ COMPLETE
11. ~~M4 - Non-null assertions~~ ✓ Fixed
12. ~~M1 - Array mutation~~ ✓ Fixed
13. ~~M2 - JSON.stringify comparison~~ ✓ Fixed

### Phase 5: Medium Severity - Maintenance (Partial)
14. ~~M5 - Memory leak in autoSave~~ ✓ Fixed
15. ~~M6 - Store documentation~~ ✓ Fixed
16. M3 - Incomplete TODO (copy functionality)
17. M7 - Inconsistent null handling
18. M8 - OptionsList inputRefs cleanup
19. M9 - DragHandle keyboard accessibility
20. M10 - Inconsistent error handling patterns
21. M11 - Svelte-ignore comment

### Phase 6: Low Severity - Polish
22. ~~L1~~ ✓ Fixed
23. L2 - aria-live for dynamic content
24. L3 - Unused import
25. L4 - Inconsistent button styling (purple vs blue)
26. L5 - SSR check pattern
27. L6 - Duplicate spinner SVG
28. L7 - Magic numbers
29. L8 - Unused mouseup forwarding
30. L9 - Duplicated reduced motion detection

---

## Known Issue: Tree View Drag and Drop Intermittent Failure

**Status:** In Progress (2026-01-09)

**Problem:** Drag and drop reordering of services in the tree view fails intermittently for manual user drags, while automated testing (MCP drag tool) works consistently.

**Investigation Findings:**
1. The `dropPosition` variable defaults to `'after'` in the middle 40% zone when null
2. Changed default from `'after'` to `'before'` in [TreeGroup.svelte:157](../../frontend/src/lib/components/ServiceDefinitionEditor/TreeView/TreeGroup.svelte#L157)
3. Fix works with automated testing but may still have issues with real user drag behavior

**Console logs added for debugging:**
- `[MOUSEDOWN]` - fires when clicking on service row
- `[DRAG_START]` - fires when drag begins
- `[DRAG_OVER]` - fires when dragging over items (shows index)
- `[TreeGroup DROP]` - fires when drop occurs
- `[DROP]` - fires in TreePanel with position calculation details

**Files Modified:**
- `frontend/src/lib/components/ServiceDefinitionEditor/TreeView/TreeGroup.svelte` - Added debug logs, changed default dropPosition
- `frontend/src/lib/components/ServiceDefinitionEditor/SplitPaneEditor/TreePanel.svelte` - Added debug logs

**Next Steps:**
- [ ] Remove debug console.log statements once issue is resolved
- [ ] Investigate why manual drag behavior differs from automated drag
- [ ] Consider if drag zones (30/40/30 split) need adjustment
- [ ] Test across different browsers
