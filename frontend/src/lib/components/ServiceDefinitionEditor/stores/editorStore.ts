import { writable, derived } from 'svelte/store';
import type { EditorState, AttributeCardState, SaveStatus } from '../types';

/**
 * Initial state for the editor
 */
const initialEditorState: EditorState = {
	service: null,
	attributes: [],
	expandedCardIndex: null,
	isLoading: true,
	error: null
};

/**
 * Main editor state store
 */
export const editorState = writable<EditorState>(initialEditorState);

/**
 * Save status for toast notifications
 */
export const saveStatus = writable<SaveStatus>({ type: 'idle' });

/**
 * Derived store: currently expanded attribute (or null if none)
 */
export const expandedAttribute = derived(editorState, ($state): AttributeCardState | null => {
	if ($state.expandedCardIndex !== null && $state.attributes[$state.expandedCardIndex]) {
		return $state.attributes[$state.expandedCardIndex];
	}
	return null;
});

/**
 * Derived store: check if any attribute has unsaved changes
 */
export const hasUnsavedChanges = derived(editorState, ($state): boolean => {
	return $state.attributes.some((attr) => attr.isDirty);
});

/**
 * Derived store: check if any attribute is currently saving
 */
export const isSaving = derived(editorState, ($state): boolean => {
	return $state.attributes.some((attr) => attr.isSaving);
});

/**
 * Reset editor to initial state
 */
export function resetEditor(): void {
	editorState.set(initialEditorState);
	saveStatus.set({ type: 'idle' });
}

/**
 * Set loading state
 */
export function setLoading(isLoading: boolean): void {
	editorState.update((state) => ({ ...state, isLoading }));
}

/**
 * Set error state
 */
export function setError(error: string | null): void {
	editorState.update((state) => ({ ...state, error, isLoading: false }));
}

/**
 * Expand a card by index (collapses any currently expanded card)
 */
export function expandCard(index: number): void {
	editorState.update((state) => {
		const attributes = state.attributes.map((attr, i) => ({
			...attr,
			cardState: i === index ? ('expanded' as const) : ('collapsed' as const)
		}));
		return { ...state, attributes, expandedCardIndex: index };
	});
}

/**
 * Collapse the currently expanded card
 */
export function collapseCard(): void {
	editorState.update((state) => {
		const attributes = state.attributes.map((attr) => ({
			...attr,
			cardState: 'collapsed' as const
		}));
		return { ...state, attributes, expandedCardIndex: null };
	});
}

/**
 * Mark an attribute as dirty (has unsaved changes)
 */
export function markAttributeDirty(code: number): void {
	editorState.update((state) => ({
		...state,
		attributes: state.attributes.map((attr) =>
			attr.code === code ? { ...attr, isDirty: true } : attr
		)
	}));
}

/**
 * Mark an attribute as saving
 */
export function markAttributeSaving(code: number, isSaving: boolean): void {
	editorState.update((state) => ({
		...state,
		attributes: state.attributes.map((attr) => (attr.code === code ? { ...attr, isSaving } : attr))
	}));
}

/**
 * Mark an attribute as saved (clear dirty and saving flags)
 */
export function markAttributeSaved(code: number): void {
	editorState.update((state) => ({
		...state,
		attributes: state.attributes.map((attr) =>
			attr.code === code ? { ...attr, isDirty: false, isSaving: false, error: undefined } : attr
		)
	}));
}

/**
 * Set error on a specific attribute
 */
export function setAttributeError(code: number, error: string): void {
	editorState.update((state) => ({
		...state,
		attributes: state.attributes.map((attr) =>
			attr.code === code ? { ...attr, error, isSaving: false } : attr
		)
	}));
}

/**
 * Update a specific attribute's local state
 */
export function updateAttributeLocal(code: number, updates: Partial<AttributeCardState>): void {
	editorState.update((state) => ({
		...state,
		attributes: state.attributes.map((attr) =>
			attr.code === code ? { ...attr, ...updates, isDirty: true } : attr
		)
	}));
}

/**
 * Set toast save status
 */
export function setSaveStatus(status: SaveStatus): void {
	saveStatus.set(status);
}

/**
 * Show success toast
 */
export function showSaveSuccess(message?: string): void {
	saveStatus.set({ type: 'success', message });
}

/**
 * Show error toast
 */
export function showSaveError(message: string): void {
	saveStatus.set({ type: 'error', message });
}

/**
 * Clear save status
 */
export function clearSaveStatus(): void {
	saveStatus.set({ type: 'idle' });
}
