<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import { slide } from 'svelte/transition';
	import type { Group, Service } from '$lib/services/Libre311/Libre311';
	import DragHandle from '../Shared/DragHandle.svelte';

	/**
	 * The group to display
	 */
	export let group: Group;

	/**
	 * Services within this group
	 */
	export let services: Service[] = [];

	/**
	 * Whether this group is expanded
	 */
	export let isExpanded = false;

	/**
	 * Currently selected service code (if any)
	 */
	export let selectedServiceCode: number | null = null;

	/**
	 * Whether this group is selected
	 */
	export let isSelected = false;

	/**
	 * Level in the tree hierarchy (for indentation)
	 */
	export let level = 1;

	/**
	 * Whether services can be dragged for reordering
	 */
	export let draggableServices = false;

	/**
	 * Service code currently being dragged (from any group)
	 */
	export let draggedServiceCode: number | null = null;

	/**
	 * Group ID of the current drop target
	 */
	export let dropTargetGroupId: number | null = null;

	/**
	 * Index of the current drop target within the group
	 */
	export let dropTargetIndex: number | null = null;

	/**
	 * Position relative to drop target ('before' or 'after')
	 */
	export let dropPosition: 'before' | 'after' | null = null;

	const dispatch = createEventDispatcher<{
		toggle: void;
		selectGroup: void;
		selectService: { serviceCode: number };
		serviceDragStart: { serviceCode: number };
		serviceDragOver: { groupId: number; serviceIndex: number; position: 'before' | 'after' };
		serviceDragLeave: void;
		serviceDrop: { serviceIndex: number };
		serviceDragEnd: void;
	}>();

	// Reduced motion preference
	let prefersReducedMotion = false;
	if (typeof window !== 'undefined') {
		prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
	}

	function handleToggle(event: MouseEvent | KeyboardEvent) {
		event.stopPropagation();
		dispatch('toggle');
	}

	function handleSelectGroup() {
		dispatch('selectGroup');
	}

	function handleKeydown(event: KeyboardEvent) {
		if (event.key === 'Enter' || event.key === ' ') {
			event.preventDefault();
			dispatch('selectGroup');
		}
		if (event.key === 'ArrowRight' && !isExpanded) {
			event.preventDefault();
			dispatch('toggle');
		}
		if (event.key === 'ArrowLeft' && isExpanded) {
			event.preventDefault();
			dispatch('toggle');
		}
	}

	function handleServiceClick(serviceCode: number) {
		dispatch('selectService', { serviceCode });
	}

	function handleServiceKeydown(event: KeyboardEvent, serviceCode: number) {
		if (event.key === 'Enter' || event.key === ' ') {
			event.preventDefault();
			dispatch('selectService', { serviceCode });
		}
	}

	// ========== Service Drag and Drop Handlers ==========

	function handleServiceDragStart(event: DragEvent, serviceCode: number) {
		if (!draggableServices || !event.dataTransfer) return;

		event.dataTransfer.effectAllowed = 'move';
		event.dataTransfer.setData('text/plain', String(serviceCode));
		dispatch('serviceDragStart', { serviceCode });
	}

	function handleServiceDragOver(event: DragEvent, index: number) {
		if (!draggableServices || draggedServiceCode === null) return;

		event.preventDefault();
		event.dataTransfer!.dropEffect = 'move';

		const target = event.currentTarget as HTMLElement;
		const rect = target.getBoundingClientRect();
		const midpoint = rect.top + rect.height / 2;
		const position = event.clientY < midpoint ? 'before' : 'after';

		dispatch('serviceDragOver', {
			groupId: group.id,
			serviceIndex: index,
			position
		});
	}

	function handleServiceDragLeave() {
		dispatch('serviceDragLeave');
	}

	function handleServiceDrop(event: DragEvent, index: number) {
		event.preventDefault();
		dispatch('serviceDrop', { serviceIndex: index });
	}

	function handleServiceDragEnd() {
		dispatch('serviceDragEnd');
	}

	// Check if this service has a drop indicator
	function getDropIndicatorClass(index: number): string {
		if (dropTargetGroupId !== group.id || dropTargetIndex !== index || !dropPosition) {
			return '';
		}
		return dropPosition === 'before' ? 'drop-indicator-before' : 'drop-indicator-after';
	}
</script>

<div
	role="treeitem"
	aria-expanded={isExpanded}
	aria-level={level}
	aria-selected={isSelected}
	class="select-none"
>
	<!-- Group Header -->
	<div
		class="flex cursor-pointer items-center gap-2 rounded-md px-2 py-1.5 transition-colors hover:bg-gray-100 {isSelected
			? 'bg-purple-50 ring-1 ring-purple-500'
			: ''}"
		on:click={handleSelectGroup}
		on:keydown={handleKeydown}
		tabindex="0"
		role="button"
		aria-label="{group.name}, {services.length} services"
	>
		<!-- Expand/Collapse Arrow -->
		<button
			type="button"
			class="flex h-5 w-5 items-center justify-center rounded text-gray-500 hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-purple-500"
			on:click={handleToggle}
			on:keydown={(e) => e.key === 'Enter' && handleToggle(e)}
			aria-label={isExpanded ? 'Collapse group' : 'Expand group'}
			tabindex="-1"
		>
			<svg
				class="h-4 w-4 transition-transform {isExpanded ? 'rotate-90' : ''}"
				fill="none"
				stroke="currentColor"
				viewBox="0 0 24 24"
			>
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
			</svg>
		</button>

		<!-- Folder Icon -->
		<svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
			{#if isExpanded}
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="2"
					d="M5 19a2 2 0 01-2-2V7a2 2 0 012-2h4l2 2h4a2 2 0 012 2v1M5 19h14a2 2 0 002-2v-5a2 2 0 00-2-2H9a2 2 0 00-2 2v5a2 2 0 01-2 2z"
				/>
			{:else}
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="2"
					d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"
				/>
			{/if}
		</svg>

		<!-- Group Name -->
		<span class="flex-1 truncate text-sm font-medium text-gray-900">{group.name}</span>

		<!-- Service Count Badge -->
		<span class="rounded-full bg-gray-100 px-2 py-0.5 text-xs font-medium text-gray-600">
			{services.length}
		</span>
	</div>

	<!-- Services List (when expanded) -->
	{#if isExpanded && services.length > 0}
		<div
			role="group"
			class="ml-6 mt-1 space-y-0.5 border-l border-gray-200 pl-2"
			transition:slide={{ duration: prefersReducedMotion ? 0 : 150 }}
		>
			{#each services as service, index (service.service_code)}
				<div
					role="treeitem"
					aria-level={level + 1}
					aria-selected={selectedServiceCode === service.service_code}
					class="group relative flex cursor-pointer items-center gap-1 rounded-md px-2 py-1.5 transition-colors hover:bg-gray-100 {selectedServiceCode ===
					service.service_code
						? 'bg-blue-50 ring-1 ring-purple-500'
						: ''} {draggedServiceCode === service.service_code
						? 'opacity-50'
						: ''} {getDropIndicatorClass(index)}"
					draggable={draggableServices}
					on:click={() => handleServiceClick(service.service_code)}
					on:keydown={(e) => handleServiceKeydown(e, service.service_code)}
					on:dragstart={(e) => handleServiceDragStart(e, service.service_code)}
					on:dragover={(e) => handleServiceDragOver(e, index)}
					on:dragleave={handleServiceDragLeave}
					on:drop={(e) => handleServiceDrop(e, index)}
					on:dragend={handleServiceDragEnd}
					tabindex="0"
				>
					<!-- Drag Handle (visible on hover when draggable) -->
					{#if draggableServices}
						<div
							class="flex-shrink-0 opacity-0 transition-opacity group-hover:opacity-100 {draggedServiceCode ===
							service.service_code
								? 'opacity-100'
								: ''}"
						>
							<DragHandle
								class="h-4 w-4"
								isDragging={draggedServiceCode === service.service_code}
								ariaLabel="Drag to reorder {service.service_name}"
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
			{/each}
		</div>
	{/if}
</div>

<style>
	/* Drop indicator styles */
	.drop-indicator-before::before {
		content: '';
		position: absolute;
		top: -2px;
		left: 0;
		right: 0;
		height: 3px;
		background: rgb(147 51 234);
		border-radius: 2px;
		z-index: 10;
	}

	.drop-indicator-before::after {
		content: '';
		position: absolute;
		top: -4px;
		left: -2px;
		width: 8px;
		height: 8px;
		background: rgb(147 51 234);
		border-radius: 50%;
		z-index: 10;
	}

	.drop-indicator-after::before {
		content: '';
		position: absolute;
		bottom: -2px;
		left: 0;
		right: 0;
		height: 3px;
		background: rgb(147 51 234);
		border-radius: 2px;
		z-index: 10;
	}

	.drop-indicator-after::after {
		content: '';
		position: absolute;
		bottom: -4px;
		left: -2px;
		width: 8px;
		height: 8px;
		background: rgb(147 51 234);
		border-radius: 50%;
		z-index: 10;
	}
</style>
