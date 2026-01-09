<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { GroupWithServices, EditorSelection } from '../stores/types';
	import type { Service, ServiceDefinitionAttribute, Group } from '$lib/services/Libre311/Libre311';
	import TreePanel from './TreePanel.svelte';
	import EditorPanel from './EditorPanel.svelte';

	/**
	 * Groups with their services for the tree panel
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
	 * Currently selected group (when selection.type === 'group')
	 */
	export let selectedGroup: Group | null = null;

	/**
	 * Currently selected service (when selection.type === 'service')
	 */
	export let selectedService: Service | null = null;

	/**
	 * Attributes for the selected service
	 */
	export let attributes: ServiceDefinitionAttribute[] = [];

	/**
	 * Whether the tree panel is loading
	 */
	export let isTreeLoading = false;

	/**
	 * Whether the editor panel is loading
	 */
	export let isEditorLoading = false;

	/**
	 * Error message for the editor panel
	 */
	export let editorError: string | null = null;

	/**
	 * Width of the left panel in pixels
	 */
	export let leftPanelWidth = 320;

	/**
	 * Minimum width for the left panel
	 */
	const MIN_LEFT_WIDTH = 240;

	/**
	 * Maximum width for the left panel
	 */
	const MAX_LEFT_WIDTH = 480;

	const dispatch = createEventDispatcher<{
		toggleGroup: { groupId: number };
		selectGroup: { groupId: number };
		selectService: { groupId: number; serviceCode: number };
		loadService: { serviceCode: number };
		createGroup: void;
		addService: { groupId: number };
		reorderService: {
			serviceCode: number;
			fromGroupId: number;
			toGroupId: number;
			newIndex: number;
		};
	}>();

	function handleCreateGroup() {
		dispatch('createGroup');
	}

	function handleAddService(event: CustomEvent<{ groupId: number }>) {
		dispatch('addService', event.detail);
	}

	function handleReorderService(
		event: CustomEvent<{
			serviceCode: number;
			fromGroupId: number;
			toGroupId: number;
			newIndex: number;
		}>
	) {
		dispatch('reorderService', event.detail);
	}

	// Resizer state
	let isResizing = false;
	let startX = 0;
	let startWidth = 0;

	function startResize(event: MouseEvent) {
		isResizing = true;
		startX = event.clientX;
		startWidth = leftPanelWidth;

		document.addEventListener('mousemove', handleResize);
		document.addEventListener('mouseup', stopResize);
		document.body.style.cursor = 'col-resize';
		document.body.style.userSelect = 'none';
	}

	function handleResize(event: MouseEvent) {
		if (!isResizing) return;

		const delta = event.clientX - startX;
		const newWidth = startWidth + delta;

		leftPanelWidth = Math.max(MIN_LEFT_WIDTH, Math.min(MAX_LEFT_WIDTH, newWidth));
	}

	function stopResize() {
		isResizing = false;
		document.removeEventListener('mousemove', handleResize);
		document.removeEventListener('mouseup', stopResize);
		document.body.style.cursor = '';
		document.body.style.userSelect = '';
	}

	function handleToggleGroup(event: CustomEvent<{ groupId: number }>) {
		dispatch('toggleGroup', event.detail);
	}

	function handleSelectGroup(event: CustomEvent<{ groupId: number }>) {
		dispatch('selectGroup', event.detail);
	}

	function handleSelectService(event: CustomEvent<{ groupId: number; serviceCode: number }>) {
		dispatch('selectService', event.detail);
	}

	function handleLoadService(event: CustomEvent<{ serviceCode: number }>) {
		dispatch('loadService', event.detail);
	}
</script>

<div class="flex h-full overflow-hidden bg-white">
	<!-- Left Panel (Tree) -->
	<div
		class="flex-shrink-0 overflow-hidden border-r border-gray-200"
		style="width: {leftPanelWidth}px"
	>
		<TreePanel
			{groups}
			{expandedGroupIds}
			{selection}
			isLoading={isTreeLoading}
			on:toggleGroup={handleToggleGroup}
			on:selectGroup={handleSelectGroup}
			on:selectService={handleSelectService}
			on:createGroup={handleCreateGroup}
			on:addService={handleAddService}
			on:reorderService={handleReorderService}
		/>
	</div>

	<!-- Resizer -->
	<div
		class="group relative w-1 flex-shrink-0 cursor-col-resize bg-gray-200 transition-colors hover:bg-purple-400 {isResizing
			? 'bg-purple-500'
			: ''}"
		on:mousedown={startResize}
		role="separator"
		aria-orientation="vertical"
		aria-label="Resize panels"
		tabindex="0"
		on:keydown={(e) => {
			if (e.key === 'ArrowLeft') {
				leftPanelWidth = Math.max(MIN_LEFT_WIDTH, leftPanelWidth - 10);
			} else if (e.key === 'ArrowRight') {
				leftPanelWidth = Math.min(MAX_LEFT_WIDTH, leftPanelWidth + 10);
			}
		}}
	>
		<!-- Resize handle indicator -->
		<div
			class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 rounded-full bg-gray-400 p-1 opacity-0 transition-opacity group-hover:opacity-100"
		>
			<svg class="h-4 w-4 text-white" fill="currentColor" viewBox="0 0 20 20">
				<circle cx="6" cy="10" r="1.5" />
				<circle cx="10" cy="10" r="1.5" />
				<circle cx="14" cy="10" r="1.5" />
			</svg>
		</div>
	</div>

	<!-- Right Panel (Editor) -->
	<div class="flex-1 overflow-hidden">
		<EditorPanel
			{selection}
			{selectedGroup}
			{selectedService}
			{attributes}
			isLoading={isEditorLoading}
			error={editorError}
			on:loadService={handleLoadService}
		>
			<slot name="group-editor" slot="group-editor" />
			<slot name="service-editor" slot="service-editor" />
		</EditorPanel>
	</div>
</div>
