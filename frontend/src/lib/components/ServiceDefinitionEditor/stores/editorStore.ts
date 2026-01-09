import { writable, derived, get } from 'svelte/store';
import type { EditorState, AttributeCardState, SaveStatus } from '../types';
import type { SplitPaneEditorState, EditorSelection, CardId, CardDirtyState } from './types';

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
			attr.code === code ? ({ ...attr, ...updates, isDirty: true } as AttributeCardState) : attr
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

// ============================================================================
// Split-Pane Editor Store (for the new config page)
// ============================================================================

/**
 * Initial state for the split-pane editor
 */
const initialSplitPaneState: SplitPaneEditorState = {
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
	error: null,
	pendingNavigation: null,
	showUnsavedModal: false
};

/**
 * Create a serializable copy of the state for Svelte reactivity
 */
function cloneState(state: SplitPaneEditorState): SplitPaneEditorState {
	return {
		...state,
		expandedGroupIds: new Set(state.expandedGroupIds),
		cardStates: new Map(state.cardStates),
		isSaving: new Map(state.isSaving)
	};
}

/**
 * Split-pane editor store with selection, dirty tracking, and modal state
 */
function createSplitPaneStore() {
	const { subscribe, set, update } = writable<SplitPaneEditorState>(initialSplitPaneState);

	return {
		subscribe,

		// ========== Selection Actions ==========

		/**
		 * Select a group in the tree panel
		 */
		selectGroup: (groupId: number) =>
			update((state) => {
				const newState = cloneState(state);
				newState.selection = { type: 'group', groupId, serviceCode: null };
				newState.expandedAttributeId = null;
				return newState;
			}),

		/**
		 * Select a service in the tree panel
		 */
		selectService: (groupId: number, serviceCode: number) =>
			update((state) => {
				const newState = cloneState(state);
				newState.selection = { type: 'service', groupId, serviceCode };
				newState.expandedAttributeId = null;
				return newState;
			}),

		/**
		 * Clear the current selection
		 */
		clearSelection: () =>
			update((state) => {
				const newState = cloneState(state);
				newState.selection = { type: null, groupId: null, serviceCode: null };
				return newState;
			}),

		// ========== Tree Expansion ==========

		/**
		 * Toggle expansion of a group in the tree
		 */
		toggleGroupExpansion: (groupId: number) =>
			update((state) => {
				const newState = cloneState(state);
				if (newState.expandedGroupIds.has(groupId)) {
					newState.expandedGroupIds.delete(groupId);
				} else {
					newState.expandedGroupIds.add(groupId);
				}
				return newState;
			}),

		/**
		 * Expand a specific group
		 */
		expandGroup: (groupId: number) =>
			update((state) => {
				const newState = cloneState(state);
				newState.expandedGroupIds.add(groupId);
				return newState;
			}),

		// ========== Attribute Card Expansion (Accordion) ==========

		/**
		 * Expand an attribute card (collapses any other)
		 */
		expandAttribute: (attributeId: number) =>
			update((state) => {
				const newState = cloneState(state);
				newState.expandedAttributeId = attributeId;
				return newState;
			}),

		/**
		 * Collapse the currently expanded attribute card
		 */
		collapseAttribute: () =>
			update((state) => {
				const newState = cloneState(state);
				newState.expandedAttributeId = null;
				return newState;
			}),

		// ========== Dirty State Tracking ==========

		/**
		 * Mark a card as dirty with original and current values
		 */
		setCardDirty: (cardId: CardId, isDirty: boolean, original?: unknown, current?: unknown) =>
			update((state) => {
				const newState = cloneState(state);
				if (isDirty) {
					newState.cardStates.set(cardId, {
						isDirty: true,
						originalValue: original,
						currentValue: current
					});
				} else {
					newState.cardStates.delete(cardId);
				}
				return newState;
			}),

		/**
		 * Clear dirty state for a card
		 */
		clearCardDirty: (cardId: CardId) =>
			update((state) => {
				const newState = cloneState(state);
				newState.cardStates.delete(cardId);
				return newState;
			}),

		/**
		 * Clear all dirty states
		 */
		clearAllDirty: () =>
			update((state) => {
				const newState = cloneState(state);
				newState.cardStates.clear();
				return newState;
			}),

		/**
		 * Get dirty state for a specific card
		 */
		getCardDirtyState: (cardId: CardId): CardDirtyState | undefined => {
			return get({ subscribe }).cardStates.get(cardId);
		},

		// ========== Loading & Saving States ==========

		/**
		 * Set global loading state
		 */
		setLoading: (isLoading: boolean) =>
			update((state) => {
				const newState = cloneState(state);
				newState.isLoading = isLoading;
				return newState;
			}),

		/**
		 * Set saving state for a specific card
		 */
		setSaving: (cardId: CardId, isSaving: boolean) =>
			update((state) => {
				const newState = cloneState(state);
				if (isSaving) {
					newState.isSaving.set(cardId, true);
				} else {
					newState.isSaving.delete(cardId);
				}
				return newState;
			}),

		// ========== Error State ==========

		/**
		 * Set error message
		 */
		setError: (error: string | null) =>
			update((state) => {
				const newState = cloneState(state);
				newState.error = error;
				return newState;
			}),

		// ========== Unsaved Changes Modal ==========

		/**
		 * Show unsaved changes modal with pending navigation callback
		 */
		showUnsavedChangesModal: (onProceed: () => void) =>
			update((state) => {
				const newState = cloneState(state);
				newState.showUnsavedModal = true;
				newState.pendingNavigation = onProceed;
				return newState;
			}),

		/**
		 * Hide unsaved changes modal
		 */
		hideUnsavedChangesModal: () =>
			update((state) => {
				const newState = cloneState(state);
				newState.showUnsavedModal = false;
				newState.pendingNavigation = null;
				return newState;
			}),

		/**
		 * Execute pending navigation and close modal
		 */
		proceedWithNavigation: () =>
			update((state) => {
				const newState = cloneState(state);
				if (state.pendingNavigation) {
					state.pendingNavigation();
				}
				newState.showUnsavedModal = false;
				newState.pendingNavigation = null;
				newState.cardStates.clear();
				return newState;
			}),

		// ========== Reset ==========

		/**
		 * Reset store to initial state
		 */
		reset: () => set(cloneState(initialSplitPaneState))
	};
}

export const splitPaneStore = createSplitPaneStore();

/**
 * Derived store: check if any card has unsaved changes
 */
export const hasAnyUnsavedChanges = derived(splitPaneStore, ($state): boolean => {
	return Array.from($state.cardStates.values()).some((card) => card.isDirty);
});

/**
 * Derived store: check if a specific card is saving
 */
export function isCardSaving(cardId: CardId) {
	return derived(splitPaneStore, ($state): boolean => {
		return $state.isSaving.get(cardId) ?? false;
	});
}

/**
 * Derived store: check if a specific card is dirty
 */
export function isCardDirty(cardId: CardId) {
	return derived(splitPaneStore, ($state): boolean => {
		return $state.cardStates.get(cardId)?.isDirty ?? false;
	});
}

/**
 * Derived store: get current selection
 */
export const currentSelection = derived(splitPaneStore, ($state): EditorSelection => {
	return $state.selection;
});
