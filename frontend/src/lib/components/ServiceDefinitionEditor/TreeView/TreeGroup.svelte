<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import { slide } from 'svelte/transition';
	import type { Group, Service } from '$lib/services/Libre311/Libre311';

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
	 * Whether services can be reordered
	 */
	export let draggableServices = false;

	/**
	 * Whether this group has keyboard focus (for visual indicator)
	 */
	export let isFocused = false;

	/**
	 * Index of the service that has keyboard focus within this group (null if none)
	 */
	export let focusedServiceIndex: number | null = null;

	const dispatch = createEventDispatcher<{
		toggle: void;
		selectGroup: void;
		selectService: { serviceCode: number };
		addService: void;
		deleteService: { serviceCode: number; serviceName: string };
		keyboardReorder: { serviceCode: number; direction: 'up' | 'down' };
	}>();

	function handleAddService(event: MouseEvent) {
		event.stopPropagation();
		dispatch('addService');
	}

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
			event.stopPropagation();
			dispatch('selectGroup');
		}
		if (event.key === 'ArrowRight' && !isExpanded) {
			event.preventDefault();
			event.stopPropagation();
			dispatch('toggle');
		}
		if (event.key === 'ArrowLeft' && isExpanded) {
			event.preventDefault();
			event.stopPropagation();
			dispatch('toggle');
		}
	}

	function handleServiceClick(serviceCode: number) {
		dispatch('selectService', { serviceCode });
	}

	function handleServiceKeydown(event: KeyboardEvent, serviceCode: number) {
		if (event.key === 'Enter' || event.key === ' ') {
			event.preventDefault();
			event.stopPropagation();
			dispatch('selectService', { serviceCode });
		}

		// Alt+Arrow keys for keyboard reordering
		if (draggableServices && event.altKey) {
			if (event.key === 'ArrowUp') {
				event.preventDefault();
				dispatch('keyboardReorder', { serviceCode, direction: 'up' });
			} else if (event.key === 'ArrowDown') {
				event.preventDefault();
				dispatch('keyboardReorder', { serviceCode, direction: 'down' });
			}
		}
	}

	function handleDeleteService(event: MouseEvent, serviceCode: number, serviceName: string) {
		event.stopPropagation(); // Prevent triggering select
		dispatch('deleteService', { serviceCode, serviceName });
	}
</script>

<div
	id="tree-group-{group.id}"
	role="treeitem"
	aria-expanded={isExpanded}
	aria-level={level}
	aria-selected={isSelected}
	class="select-none"
>
	<!-- Group Header -->
	<div
		class="flex cursor-pointer items-center gap-2 rounded-md px-2 py-1.5 transition-colors hover:bg-gray-100 {isSelected
			? 'bg-blue-50 ring-1 ring-blue-500'
			: ''} {isFocused ? 'ring-2 ring-blue-400 ring-offset-1' : ''}"
		on:click={handleSelectGroup}
		on:keydown={handleKeydown}
		tabindex="0"
		role="button"
		aria-label="{group.name}, {services.length} services"
	>
		<!-- Expand/Collapse Arrow -->
		<button
			type="button"
			class="flex h-5 w-5 items-center justify-center rounded text-gray-500 hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-blue-500"
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
	{#if isExpanded}
		<div
			role="group"
			aria-label="Services in {group.name}"
			class="ml-6 mt-1 space-y-0.5 border-l border-gray-200 pl-2"
			transition:slide={{ duration: prefersReducedMotion ? 0 : 150 }}
		>
			{#each services as service, index (service.service_code)}
				<div
					id="tree-service-{group.id}-{service.service_code}"
					role="treeitem"
					aria-level={level + 1}
					aria-selected={selectedServiceCode === service.service_code}
					aria-label={draggableServices
						? `${service.service_name}. Use Up/Down buttons to reorder.`
						: service.service_name}
					class="group flex cursor-pointer items-center gap-2 rounded-md px-2 py-1.5 transition-colors hover:bg-gray-100 {selectedServiceCode ===
					service.service_code
						? 'bg-blue-50 ring-1 ring-blue-500'
						: ''} {focusedServiceIndex === index ? 'ring-2 ring-blue-400 ring-offset-1' : ''}"
					on:click={() => handleServiceClick(service.service_code)}
					on:keydown={(e) => handleServiceKeydown(e, service.service_code)}
					tabindex="0"
				>
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

					<!-- Action Buttons (Up/Down/Trash) -->
					{#if draggableServices}
						<div class="flex items-center">
							<!-- Up Button -->
							<button
								type="button"
								class="flex h-8 w-8 items-center justify-center rounded-full text-gray-500 hover:bg-gray-200 hover:text-blue-600 focus:bg-gray-200 focus:text-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-30 disabled:hover:bg-transparent disabled:hover:text-gray-500"
								disabled={index === 0}
								on:click|stopPropagation={() =>
									dispatch('keyboardReorder', {
										serviceCode: service.service_code,
										direction: 'up'
									})}
								on:keydown|stopPropagation={() => {}}
								aria-label="Move {service.service_name} up"
								title="Move up"
							>
								<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M5 15l7-7 7 7"
									/>
								</svg>
							</button>

							<!-- Down Button -->
							<button
								type="button"
								class="flex h-8 w-8 items-center justify-center rounded-full text-gray-500 hover:bg-gray-200 hover:text-blue-600 focus:bg-gray-200 focus:text-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-30 disabled:hover:bg-transparent disabled:hover:text-gray-500"
								disabled={index === services.length - 1}
								on:click|stopPropagation={() =>
									dispatch('keyboardReorder', {
										serviceCode: service.service_code,
										direction: 'down'
									})}
								on:keydown|stopPropagation={() => {}}
								aria-label="Move {service.service_name} down"
								title="Move down"
							>
								<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M19 9l-7 7-7-7"
									/>
								</svg>
							</button>

							<!-- Delete Button -->
							<button
								type="button"
								class="flex h-8 w-8 items-center justify-center rounded-full text-gray-500 hover:bg-red-50 hover:text-red-600 focus:bg-red-50 focus:text-red-600 focus:outline-none focus:ring-2 focus:ring-red-500"
								on:click={(e) => handleDeleteService(e, service.service_code, service.service_name)}
								on:keydown|stopPropagation={() => {}}
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
					{/if}
				</div>
			{/each}

			<!-- Add Service Button -->
			<button
				type="button"
				class="flex w-full items-center gap-1 rounded-md px-2 py-1.5 text-sm text-blue-600 hover:bg-blue-50"
				on:click={handleAddService}
				aria-label="Add service to {group.name}"
			>
				<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M12 4v16m8-8H4"
					/>
				</svg>
				<span>Add Svc</span>
			</button>
		</div>
	{/if}
</div>
