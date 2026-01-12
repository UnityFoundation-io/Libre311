import {
	setSaveStatus,
	showSaveSuccess,
	showSaveError,
	markAttributeSaving,
	markAttributeSaved,
	setAttributeError
} from '../stores/editorStore';

/**
 * Default debounce delay in milliseconds
 */
export const DEFAULT_DEBOUNCE_MS = 500;

/**
 * Type for the save function that performs the actual API call
 */
export type SaveFunction<T> = () => Promise<T>;

/**
 * Type for the success callback after save completes
 */
export type OnSuccessCallback<T> = (result: T) => void;

/**
 * Type for the error callback when save fails
 */
export type OnErrorCallback = (error: Error) => void;

/**
 * Options for the auto-save utility
 */
export interface AutoSaveOptions<T> {
	/** The async function that performs the save */
	saveFn: SaveFunction<T>;
	/** Debounce delay in milliseconds (default: 500ms) */
	debounceMs?: number;
	/** Callback on successful save */
	onSuccess?: OnSuccessCallback<T>;
	/** Callback on save error */
	onError?: OnErrorCallback;
	/** Custom success message for toast */
	successMessage?: string;
	/** Whether to show toast notifications (default: true) */
	showToast?: boolean;
	/** Attribute code if saving an attribute (for attribute-specific state tracking) */
	attributeCode?: number;
}

/**
 * Creates a debounced auto-save function with optimistic updates and error handling
 *
 * IMPORTANT: To prevent memory leaks, always call `cancel()` in the component's
 * `onDestroy` lifecycle hook. If a component unmounts while a save is pending,
 * the callback will still execute, potentially updating unmounted component state.
 *
 * @example
 * ```typescript
 * import { onDestroy } from 'svelte';
 *
 * const autoSave = createAutoSave({
 *   saveFn: async () => await api.updateService({ name: newName }),
 *   onSuccess: (result) => console.log('Saved:', result),
 *   successMessage: 'Service name updated'
 * });
 *
 * // IMPORTANT: Cancel pending saves when component unmounts
 * onDestroy(() => {
 *   autoSave.cancel();
 * });
 *
 * // Call trigger() whenever the value changes
 * function handleInput(event: Event) {
 *   const value = (event.target as HTMLInputElement).value;
 *   autoSave.trigger();
 * }
 * ```
 */
export function createAutoSave<T>(options: AutoSaveOptions<T>) {
	const {
		saveFn,
		debounceMs = DEFAULT_DEBOUNCE_MS,
		onSuccess,
		onError,
		successMessage,
		showToast = true,
		attributeCode
	} = options;

	let timeoutId: ReturnType<typeof setTimeout> | null = null;
	let pending = false;
	let aborted = false;

	/**
	 * Check if a save is currently pending (waiting for debounce or in-flight)
	 */
	function isPending(): boolean {
		return pending;
	}

	/**
	 * Cancel any pending save operation
	 */
	function cancel(): void {
		if (timeoutId) {
			clearTimeout(timeoutId);
			timeoutId = null;
		}
		aborted = true;
		pending = false;
	}

	/**
	 * Trigger a debounced save operation
	 */
	function trigger(): void {
		// Clear any existing timeout
		if (timeoutId) {
			clearTimeout(timeoutId);
		}

		pending = true;
		aborted = false;

		timeoutId = setTimeout(async () => {
			if (aborted) {
				pending = false;
				return;
			}

			try {
				// Update UI to show saving state
				if (showToast) {
					setSaveStatus({ type: 'saving' });
				}
				if (attributeCode !== undefined) {
					markAttributeSaving(attributeCode, true);
				}

				// Perform the save
				const result = await saveFn();

				// Check if aborted during save
				if (aborted) {
					pending = false;
					return;
				}

				// Update UI to show success
				if (showToast) {
					showSaveSuccess(successMessage);
				}
				if (attributeCode !== undefined) {
					markAttributeSaved(attributeCode);
				}

				// Call success callback
				if (onSuccess) {
					onSuccess(result);
				}
			} catch (error) {
				// Check if aborted during save
				if (aborted) {
					pending = false;
					return;
				}

				const errorMessage =
					error instanceof Error ? error.message : 'An error occurred while saving';

				// Update UI to show error
				if (showToast) {
					showSaveError(errorMessage);
				}
				if (attributeCode !== undefined) {
					setAttributeError(attributeCode, errorMessage);
				}

				// Call error callback
				if (onError) {
					onError(error instanceof Error ? error : new Error(errorMessage));
				}
			} finally {
				pending = false;
				timeoutId = null;
			}
		}, debounceMs);
	}

	/**
	 * Immediately execute save without debounce
	 */
	async function saveNow(): Promise<T | undefined> {
		// Cancel any pending debounced save
		cancel();
		aborted = false;
		pending = true;

		try {
			if (showToast) {
				setSaveStatus({ type: 'saving' });
			}
			if (attributeCode !== undefined) {
				markAttributeSaving(attributeCode, true);
			}

			const result = await saveFn();

			if (showToast) {
				showSaveSuccess(successMessage);
			}
			if (attributeCode !== undefined) {
				markAttributeSaved(attributeCode);
			}

			if (onSuccess) {
				onSuccess(result);
			}

			return result;
		} catch (error) {
			const errorMessage =
				error instanceof Error ? error.message : 'An error occurred while saving';

			if (showToast) {
				showSaveError(errorMessage);
			}
			if (attributeCode !== undefined) {
				setAttributeError(attributeCode, errorMessage);
			}

			if (onError) {
				onError(error instanceof Error ? error : new Error(errorMessage));
			}

			return undefined;
		} finally {
			pending = false;
		}
	}

	return {
		trigger,
		cancel,
		isPending,
		saveNow
	};
}

/**
 * Simple debounce utility for non-save operations
 */
export function debounce<T extends (...args: unknown[]) => unknown>(
	fn: T,
	delayMs: number
): (...args: Parameters<T>) => void {
	let timeoutId: ReturnType<typeof setTimeout> | null = null;

	return (...args: Parameters<T>) => {
		if (timeoutId) {
			clearTimeout(timeoutId);
		}

		timeoutId = setTimeout(() => {
			fn(...args);
			timeoutId = null;
		}, delayMs);
	};
}

/**
 * Throttle utility - execute at most once per interval
 */
export function throttle<T extends (...args: unknown[]) => unknown>(
	fn: T,
	intervalMs: number
): (...args: Parameters<T>) => void {
	let lastExecution = 0;
	let timeoutId: ReturnType<typeof setTimeout> | null = null;

	return (...args: Parameters<T>) => {
		const now = Date.now();
		const timeSinceLastExecution = now - lastExecution;

		if (timeSinceLastExecution >= intervalMs) {
			// Execute immediately
			lastExecution = now;
			fn(...args);
		} else if (!timeoutId) {
			// Schedule execution
			timeoutId = setTimeout(() => {
				lastExecution = Date.now();
				fn(...args);
				timeoutId = null;
			}, intervalMs - timeSinceLastExecution);
		}
	};
}
