import { writable, derived } from 'svelte/store';
import type { ListViewState, GroupListState } from '../types';
import type { Service } from '$lib/services/Libre311/Libre311';

/**
 * Initial state for the list view
 */
const initialListViewState: ListViewState = {
	groups: [],
	isLoading: true,
	error: null,
	newGroupForm: {
		isVisible: false,
		name: '',
		error: null
	}
};

/**
 * Main list view state store
 */
export const listViewState = writable<ListViewState>(initialListViewState);

/**
 * Derived store: all groups
 */
export const groups = derived(listViewState, ($state): GroupListState[] => {
	return $state.groups;
});

/**
 * Derived store: check if list is loading
 */
export const isLoading = derived(listViewState, ($state): boolean => {
	return $state.isLoading;
});

/**
 * Derived store: get any error
 */
export const error = derived(listViewState, ($state): string | null => {
	return $state.error;
});

/**
 * Derived store: get new group form state
 */
export const newGroupForm = derived(listViewState, ($state) => {
	return $state.newGroupForm;
});

/**
 * Derived store: total service count across all groups
 */
export const totalServiceCount = derived(listViewState, ($state): number => {
	return $state.groups.reduce((total, group) => total + group.services.length, 0);
});

/**
 * Reset list view to initial state
 */
export function resetListView(): void {
	listViewState.set(initialListViewState);
}

/**
 * Set loading state
 */
export function setLoading(isLoading: boolean): void {
	listViewState.update((state) => ({ ...state, isLoading }));
}

/**
 * Set error state
 */
export function setError(error: string | null): void {
	listViewState.update((state) => ({ ...state, error, isLoading: false }));
}

/**
 * Toggle a group's expanded state
 */
export function toggleGroupExpanded(groupId: number): void {
	listViewState.update((state) => ({
		...state,
		groups: state.groups.map((group) =>
			group.id === groupId ? { ...group, isExpanded: !group.isExpanded } : group
		)
	}));
}

/**
 * Expand a specific group
 */
export function expandGroup(groupId: number): void {
	listViewState.update((state) => ({
		...state,
		groups: state.groups.map((group) =>
			group.id === groupId ? { ...group, isExpanded: true } : group
		)
	}));
}

/**
 * Collapse a specific group
 */
export function collapseGroup(groupId: number): void {
	listViewState.update((state) => ({
		...state,
		groups: state.groups.map((group) =>
			group.id === groupId ? { ...group, isExpanded: false } : group
		)
	}));
}

/**
 * Collapse all groups
 */
export function collapseAllGroups(): void {
	listViewState.update((state) => ({
		...state,
		groups: state.groups.map((group) => ({ ...group, isExpanded: false }))
	}));
}

/**
 * Set services for a specific group
 */
export function setGroupServices(groupId: number, services: Service[]): void {
	listViewState.update((state) => ({
		...state,
		groups: state.groups.map((group) =>
			group.id === groupId ? { ...group, services, isLoading: false } : group
		)
	}));
}

/**
 * Mark a group as loading
 */
export function setGroupLoading(groupId: number, isLoading: boolean): void {
	listViewState.update((state) => ({
		...state,
		groups: state.groups.map((group) => (group.id === groupId ? { ...group, isLoading } : group))
	}));
}

/**
 * Add a new service to a group
 */
export function addServiceToGroup(groupId: number, service: Service): void {
	listViewState.update((state) => ({
		...state,
		groups: state.groups.map((group) =>
			group.id === groupId ? { ...group, services: [...group.services, service] } : group
		)
	}));
}

/**
 * Remove a service from a group
 */
export function removeServiceFromGroup(groupId: number, serviceCode: number): void {
	listViewState.update((state) => ({
		...state,
		groups: state.groups.map((group) =>
			group.id === groupId
				? { ...group, services: group.services.filter((s) => s.service_code !== serviceCode) }
				: group
		)
	}));
}

/**
 * Show new group form
 */
export function showNewGroupForm(): void {
	listViewState.update((state) => ({
		...state,
		newGroupForm: { isVisible: true, name: '', error: null }
	}));
}

/**
 * Hide new group form
 */
export function hideNewGroupForm(): void {
	listViewState.update((state) => ({
		...state,
		newGroupForm: { isVisible: false, name: '', error: null }
	}));
}

/**
 * Update new group form name
 */
export function setNewGroupName(name: string): void {
	listViewState.update((state) => ({
		...state,
		newGroupForm: { ...state.newGroupForm, name, error: null }
	}));
}

/**
 * Set new group form error
 */
export function setNewGroupError(error: string | null): void {
	listViewState.update((state) => ({
		...state,
		newGroupForm: { ...state.newGroupForm, error }
	}));
}

/**
 * Add a new group to the list
 */
export function addGroup(group: GroupListState): void {
	listViewState.update((state) => ({
		...state,
		groups: [...state.groups, group]
	}));
}

/**
 * Update a group's name
 */
export function updateGroupName(groupId: number, name: string): void {
	listViewState.update((state) => ({
		...state,
		groups: state.groups.map((group) => (group.id === groupId ? { ...group, name } : group))
	}));
}

/**
 * Remove a group from the list
 */
export function removeGroup(groupId: number): void {
	listViewState.update((state) => ({
		...state,
		groups: state.groups.filter((group) => group.id !== groupId)
	}));
}
