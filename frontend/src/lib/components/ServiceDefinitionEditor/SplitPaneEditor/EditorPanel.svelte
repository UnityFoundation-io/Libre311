<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { Service, ServiceDefinitionAttribute, Group } from '$lib/services/Libre311/Libre311';
	import type { EditorSelection } from '../stores/types';

	/**
	 * Current selection state
	 */
	export let selection: EditorSelection;

	/**
	 * Currently selected group (if selection.type === 'group')
	 */
	export let selectedGroup: Group | null = null;

	/**
	 * Currently selected service with attributes (if selection.type === 'service')
	 */
	export let selectedService: Service | null = null;

	/**
	 * Attributes for the selected service
	 */
	export let attributes: ServiceDefinitionAttribute[] = [];

	/**
	 * Whether the panel is loading data
	 */
	export let isLoading = false;

	/**
	 * Error message to display
	 */
	export let error: string | null = null;

	const dispatch = createEventDispatcher<{
		loadService: { serviceCode: number };
	}>();

	// Reactive: load service data when selection changes
	$: if (selection.type === 'service' && selection.serviceCode) {
		dispatch('loadService', { serviceCode: selection.serviceCode });
	}
</script>

<div class="flex h-full flex-col overflow-hidden bg-gray-50">
	{#if isLoading}
		<!-- Loading State -->
		<div class="flex flex-1 items-center justify-center">
			<div class="text-center">
				<svg
					class="mx-auto h-8 w-8 animate-spin text-purple-600"
					xmlns="http://www.w3.org/2000/svg"
					fill="none"
					viewBox="0 0 24 24"
				>
					<circle
						class="opacity-25"
						cx="12"
						cy="12"
						r="10"
						stroke="currentColor"
						stroke-width="4"
					></circle>
					<path
						class="opacity-75"
						fill="currentColor"
						d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
					></path>
				</svg>
				<p class="mt-2 text-sm text-gray-600">Loading...</p>
			</div>
		</div>
	{:else if error}
		<!-- Error State -->
		<div class="flex flex-1 items-center justify-center">
			<div class="text-center">
				<svg
					class="mx-auto h-12 w-12 text-red-400"
					fill="none"
					stroke="currentColor"
					viewBox="0 0 24 24"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
					/>
				</svg>
				<h3 class="mt-2 text-sm font-medium text-gray-900">Error loading data</h3>
				<p class="mt-1 text-sm text-gray-500">{error}</p>
			</div>
		</div>
	{:else if selection.type === null}
		<!-- Empty State - No Selection -->
		<div class="flex flex-1 items-center justify-center">
			<div class="text-center">
				<svg
					class="mx-auto h-12 w-12 text-gray-300"
					fill="none"
					stroke="currentColor"
					viewBox="0 0 24 24"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M8 7v8a2 2 0 002 2h6M8 7V5a2 2 0 012-2h4.586a1 1 0 01.707.293l4.414 4.414a1 1 0 01.293.707V15a2 2 0 01-2 2h-2M8 7H6a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2v-2"
					/>
				</svg>
				<h3 class="mt-2 text-sm font-medium text-gray-900">No item selected</h3>
				<p class="mt-1 text-sm text-gray-500">
					Select a service group or service from the tree to edit
				</p>
			</div>
		</div>
	{:else if selection.type === 'group' && selectedGroup}
		<!-- Group Editor State -->
		<div class="flex-1 overflow-y-auto p-6">
			<div class="mx-auto max-w-2xl">
				<h2 class="mb-6 text-lg font-semibold text-gray-900">Edit Group: {selectedGroup.name}</h2>
				<!-- GroupEditor component would go here -->
				<slot name="group-editor" />
			</div>
		</div>
	{:else if selection.type === 'service' && selectedService}
		<!-- Service Editor State -->
		<div class="flex-1 overflow-y-auto p-6">
			<div class="mx-auto max-w-2xl space-y-6">
				<!-- ServiceHeaderCard and AttributeCardList would go here via slots -->
				<slot name="service-editor" />
			</div>
		</div>
	{:else}
		<!-- Fallback Empty State -->
		<div class="flex flex-1 items-center justify-center">
			<div class="text-center text-gray-500">
				<p>Select an item from the tree to begin editing</p>
			</div>
		</div>
	{/if}
</div>
