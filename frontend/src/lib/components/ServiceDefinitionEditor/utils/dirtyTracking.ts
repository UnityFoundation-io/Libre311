/**
 * Updates a Set based on a boolean condition, returning a new Set instance
 * for Svelte reactivity.
 */
export function updateDirtySet<T>(currentSet: Set<T>, item: T, isDirty: boolean): Set<T> {
	const newSet = new Set(currentSet);
	if (isDirty) {
		newSet.add(item);
	} else {
		newSet.delete(item);
	}
	return newSet;
}

/**
 * Updates a Map and a Set simultaneously for tracking dirty items with pending values.
 * Returns both the new Set and new Map for Svelte reactivity.
 */
export function updateDirtyState<K, V>(
	currentDirtySet: Set<K>,
	currentPendingMap: Map<K, V>,
	key: K,
	isDirty: boolean,
	pendingValue?: V
): { dirtySet: Set<K>; pendingMap: Map<K, V> } {
	const newDirtySet = new Set(currentDirtySet);
	const newPendingMap = new Map(currentPendingMap);

	if (isDirty && pendingValue !== undefined) {
		newDirtySet.add(key);
		newPendingMap.set(key, pendingValue);
	} else {
		newDirtySet.delete(key);
		newPendingMap.delete(key);
	}

	return {
		dirtySet: newDirtySet,
		pendingMap: newPendingMap
	};
}

/**
 * Toggles an item in a Set (adds if missing, removes if present).
 * Returns a new Set instance for Svelte reactivity.
 */
export function toggleSetItem<T>(currentSet: Set<T>, item: T): Set<T> {
	const newSet = new Set(currentSet);
	if (newSet.has(item)) {
		newSet.delete(item);
	} else {
		newSet.add(item);
	}
	return newSet;
}
