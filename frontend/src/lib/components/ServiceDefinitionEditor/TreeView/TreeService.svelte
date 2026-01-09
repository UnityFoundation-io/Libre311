<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { Service } from '$lib/services/Libre311/Libre311';
	import DragHandle from '../Shared/DragHandle.svelte';

	/**
	 * The service to display
	 */
	export let service: Service;

	/**
	 * Whether this service is selected
	 */
	export let isSelected = false;

	/**
	 * Level in the tree hierarchy (for ARIA)
	 */
	export let level = 2;

	/**
	 * Whether drag-drop is enabled
	 */
	export let draggable = false;

	/**
	 * Whether this item is being dragged
	 */
	export let isDragging = false;

	const dispatch = createEventDispatcher<{
		select: void;
		dragstart: void;
		dragend: void;
		delete: void;
	}>();

	function handleClick() {
		dispatch('select');
	}

	function handleKeydown(event: KeyboardEvent) {
		if (event.key === 'Enter' || event.key === ' ') {
			event.preventDefault();
			dispatch('select');
		}
	}

	function handleDragStart() {
		dispatch('dragstart');
	}

	function handleDragEnd() {
		dispatch('dragend');
	}

	function handleDelete(event: MouseEvent) {
		event.stopPropagation(); // Prevent triggering select
		dispatch('delete');
	}
</script>

<div
	role="treeitem"
	aria-level={level}
	aria-selected={isSelected}
	class="group flex cursor-pointer items-center gap-1 rounded-md px-2 py-1.5 transition-colors hover:bg-gray-100 {isSelected
		? 'bg-blue-50 ring-1 ring-blue-500'
		: ''} {isDragging ? 'opacity-50' : ''}"
	on:click={handleClick}
	on:keydown={handleKeydown}
	tabindex="0"
>
	<!-- Drag Handle (visible on hover if draggable) -->
	{#if draggable}
		<div
			class="opacity-0 transition-opacity group-hover:opacity-100 {isDragging ? 'opacity-100' : ''}"
		>
			<DragHandle
				class="h-4 w-4"
				{isDragging}
				ariaLabel="Drag to reorder {service.service_name}"
				on:mousedown={handleDragStart}
				on:mouseup={handleDragEnd}
			/>
		</div>
	{/if}

	<!-- Document Icon -->
	<svg
		class="h-4 w-4 flex-shrink-0 text-gray-400"
		fill="none"
		stroke="currentColor"
		viewBox="0 0 24 24"
	>
		<path
			stroke-linecap="round"
			stroke-linejoin="round"
			stroke-width="2"
			d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
		/>
	</svg>

	<!-- Service Name -->
	<span class="flex-1 truncate text-sm text-gray-700">{service.service_name}</span>

	<!-- Delete Button (visible on hover) -->
	<button
		type="button"
		class="rounded p-1 text-gray-400 opacity-0 transition-opacity hover:bg-red-50 hover:text-red-600 focus:opacity-100 focus:outline-none focus:ring-2 focus:ring-red-500 group-hover:opacity-100"
		on:click={handleDelete}
		aria-label="Delete {service.service_name}"
		title="Delete service"
	>
		<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
			<path
				stroke-linecap="round"
				stroke-linejoin="round"
				stroke-width="2"
				d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
			/>
		</svg>
	</button>
</div>
