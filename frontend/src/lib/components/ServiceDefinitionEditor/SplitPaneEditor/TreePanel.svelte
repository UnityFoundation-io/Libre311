<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { GroupWithServices, EditorSelection } from '../stores/types';
	import TreeGroup from '../TreeView/TreeGroup.svelte';

	/**
	 * Groups with their services to display in the tree
	 */
	export let groups: GroupWithServices[] = [];

	/**
	 * Set of expanded group IDs
	 */
	export let expandedGroupIds: Set<number> = new Set();

	/**
	 * Current selection state
	 */
	export let selection: EditorSelection = {
		type: null,
		groupId: null,
		serviceCode: null
	};

	/**
	 * Whether the panel is loading data
	 */
	export let isLoading = false;

	/**
	 * Whether service reordering is enabled
	 */
	export let reorderEnabled = true;

	const dispatch = createEventDispatcher<{
		toggleGroup: { groupId: number };
		selectGroup: { groupId: number };
		selectService: { groupId: number; serviceCode: number };
		reorderService: {
			serviceCode: number;
			fromGroupId: number;
			toGroupId: number;
			newIndex: number;
		};
		createGroup: void;
		addService: { groupId: number };
		deleteService: { groupId: number; serviceCode: number; serviceName: string };
	}>();

	function handleCreateGroup() {
		dispatch('createGroup');
	}

	// Drag state for service reordering
	let draggedServiceCode: number | null = null;
	let draggedFromGroupId: number | null = null;
	let dropTargetGroupId: number | null = null;
	let dropTargetIndex: number | null = null;
	let dropPosition: 'before' | 'after' | null = null;

	// Track focused index for keyboard navigation
	let focusedGroupIndex = 0;
	let focusedServiceIndex: number | null = null;

	function handleToggleGroup(groupId: number) {
		dispatch('toggleGroup', { groupId });
	}

	function handleSelectGroup(groupId: number) {
		dispatch('selectGroup', { groupId });
	}

	function handleSelectService(groupId: number, serviceCode: number) {
		dispatch('selectService', { groupId, serviceCode });
	}

	// Keyboard navigation
	function handleKeydown(event: KeyboardEvent) {
		if (groups.length === 0) return;

		const currentGroup = groups[focusedGroupIndex];
		const isGroupExpanded = expandedGroupIds.has(currentGroup.id);

		switch (event.key) {
			case 'ArrowDown':
				event.preventDefault();
				navigateDown(currentGroup, isGroupExpanded);
				break;

			case 'ArrowUp':
				event.preventDefault();
				navigateUp();
				break;

			case 'ArrowRight':
				event.preventDefault();
				if (focusedServiceIndex === null && !isGroupExpanded) {
					handleToggleGroup(currentGroup.id);
				} else if (
					focusedServiceIndex === null &&
					isGroupExpanded &&
					currentGroup.services.length > 0
				) {
					focusedServiceIndex = 0;
				}
				break;

			case 'ArrowLeft':
				event.preventDefault();
				if (focusedServiceIndex !== null) {
					focusedServiceIndex = null;
				} else if (isGroupExpanded) {
					handleToggleGroup(currentGroup.id);
				}
				break;

			case 'Enter':
			case ' ':
				event.preventDefault();
				if (focusedServiceIndex !== null) {
					const service = currentGroup.services[focusedServiceIndex];
					if (service) {
						handleSelectService(currentGroup.id, service.service_code);
					}
				} else {
					handleSelectGroup(currentGroup.id);
				}
				break;

			case 'Home':
				event.preventDefault();
				focusedGroupIndex = 0;
				focusedServiceIndex = null;
				break;

			case 'End':
				event.preventDefault();
				focusedGroupIndex = groups.length - 1;
				focusedServiceIndex = null;
				break;
		}
	}

	function navigateDown(currentGroup: GroupWithServices, isGroupExpanded: boolean) {
		if (focusedServiceIndex !== null) {
			// Within a group's services
			if (focusedServiceIndex < currentGroup.services.length - 1) {
				focusedServiceIndex++;
			} else {
				// Move to next group
				focusedServiceIndex = null;
				if (focusedGroupIndex < groups.length - 1) {
					focusedGroupIndex++;
				}
			}
		} else if (isGroupExpanded && currentGroup.services.length > 0) {
			// Enter group's services
			focusedServiceIndex = 0;
		} else {
			// Move to next group
			if (focusedGroupIndex < groups.length - 1) {
				focusedGroupIndex++;
			}
		}
	}

	function navigateUp() {
		if (focusedServiceIndex !== null) {
			if (focusedServiceIndex > 0) {
				focusedServiceIndex--;
			} else {
				focusedServiceIndex = null;
			}
		} else {
			if (focusedGroupIndex > 0) {
				focusedGroupIndex--;
				const prevGroup = groups[focusedGroupIndex];
				if (expandedGroupIds.has(prevGroup.id) && prevGroup.services.length > 0) {
					focusedServiceIndex = prevGroup.services.length - 1;
				}
			}
		}
	}

	// ========== Service Drag and Drop Handlers ==========

	function handleServiceDragStart(groupId: number, serviceCode: number) {
		if (!reorderEnabled) return;

		draggedServiceCode = serviceCode;
		draggedFromGroupId = groupId;
	}

	function handleServiceDragOver(
		event: CustomEvent<{ groupId: number; serviceIndex: number; position: 'before' | 'after' }>
	) {
		if (!reorderEnabled || draggedServiceCode === null) return;

		dropTargetGroupId = event.detail.groupId;
		dropTargetIndex = event.detail.serviceIndex;
		dropPosition = event.detail.position;
	}

	function handleServiceDragLeave() {
		dropTargetGroupId = null;
		dropTargetIndex = null;
		dropPosition = null;
	}

	function handleServiceDrop(groupId: number, serviceIndex: number) {
		if (!reorderEnabled || draggedServiceCode === null || draggedFromGroupId === null) {
			resetDragState();
			return;
		}

		// Calculate final index based on drop position
		let newIndex = serviceIndex;
		if (dropPosition === 'after') {
			newIndex = serviceIndex + 1;
		}

		// If dragging within the same group and from before to after, adjust index
		if (draggedFromGroupId === groupId) {
			const fromIndex = groups
				.find((g) => g.id === draggedFromGroupId)
				?.services.findIndex((s) => s.service_code === draggedServiceCode);

			if (fromIndex !== undefined && fromIndex !== -1 && fromIndex < newIndex) {
				newIndex -= 1;
			}

			// Don't dispatch if dropping at the same position
			if (fromIndex === newIndex) {
				resetDragState();
				return;
			}
		}

		dispatch('reorderService', {
			serviceCode: draggedServiceCode,
			fromGroupId: draggedFromGroupId,
			toGroupId: groupId,
			newIndex
		});

		resetDragState();
	}

	function handleServiceDragEnd() {
		resetDragState();
	}

	function resetDragState() {
		draggedServiceCode = null;
		draggedFromGroupId = null;
		dropTargetGroupId = null;
		dropTargetIndex = null;
		dropPosition = null;
	}
</script>

<div
	class="flex h-full flex-col overflow-hidden"
	role="tree"
	aria-label="Service groups and services"
	tabindex="0"
	on:keydown={handleKeydown}
>
	<!-- Panel Header -->
	<div class="flex items-center justify-between border-b border-gray-200 bg-white px-4 py-3">
		<h2 class="text-sm font-semibold text-gray-900">Service Groups</h2>
		<button
			type="button"
			class="flex items-center gap-1 rounded-md px-2 py-1 text-sm font-medium text-blue-600 hover:bg-blue-50 focus:outline-none focus:ring-2 focus:ring-blue-500"
			on:click={handleCreateGroup}
			aria-label="Create new group"
		>
			<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
			</svg>
			Group
		</button>
	</div>

	<!-- Tree Content -->
	<div class="flex-1 overflow-y-auto p-2">
		{#if isLoading}
			<div class="flex items-center justify-center py-8">
				<svg
					class="h-6 w-6 animate-spin text-blue-600"
					xmlns="http://www.w3.org/2000/svg"
					fill="none"
					viewBox="0 0 24 24"
				>
					<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"
					></circle>
					<path
						class="opacity-75"
						fill="currentColor"
						d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
					></path>
				</svg>
			</div>
		{:else if groups.length === 0}
			<div class="py-8 text-center text-sm text-gray-500">No service groups found</div>
		{:else}
			<div class="space-y-1">
				{#each groups as group (group.id)}
					<TreeGroup
						{group}
						services={group.services}
						isExpanded={expandedGroupIds.has(group.id)}
						isSelected={selection.type === 'group' && selection.groupId === group.id}
						selectedServiceCode={selection.type === 'service' && selection.groupId === group.id
							? selection.serviceCode
							: null}
						draggableServices={reorderEnabled}
						{draggedServiceCode}
						{dropTargetGroupId}
						{dropTargetIndex}
						{dropPosition}
						on:toggle={() => handleToggleGroup(group.id)}
						on:selectGroup={() => handleSelectGroup(group.id)}
						on:selectService={(e) => handleSelectService(group.id, e.detail.serviceCode)}
						on:serviceDragStart={(e) => handleServiceDragStart(group.id, e.detail.serviceCode)}
						on:serviceDragOver={handleServiceDragOver}
						on:serviceDragLeave={handleServiceDragLeave}
						on:serviceDrop={(e) => handleServiceDrop(group.id, e.detail.serviceIndex)}
						on:serviceDragEnd={handleServiceDragEnd}
						on:addService={() => dispatch('addService', { groupId: group.id })}
						on:deleteService={(e) =>
							dispatch('deleteService', {
								groupId: group.id,
								serviceCode: e.detail.serviceCode,
								serviceName: e.detail.serviceName
							})}
					/>
				{/each}
			</div>
		{/if}
	</div>
</div>
