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
</script>

<div
	role="treeitem"
	aria-level={level}
	aria-selected={isSelected}
	class="group flex cursor-pointer items-center gap-1 rounded-md px-2 py-1.5 transition-colors hover:bg-gray-100 {isSelected
		? 'bg-blue-50 ring-1 ring-purple-500'
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
</div>
