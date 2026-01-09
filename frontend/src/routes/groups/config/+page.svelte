<script lang="ts">
	import { onMount } from 'svelte';
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import type {
		Service,
		ServiceDefinitionAttribute,
		Group
	} from '$lib/services/Libre311/Libre311';
	import type { GroupWithServices, EditorSelection } from '$lib/components/ServiceDefinitionEditor/stores/types';
	import SplitPaneLayout from '$lib/components/ServiceDefinitionEditor/SplitPaneEditor/SplitPaneLayout.svelte';
	import ServiceHeaderCard from '$lib/components/ServiceDefinitionEditor/ServiceEditor/ServiceHeaderCard.svelte';
	import AttributeCardList from '$lib/components/ServiceDefinitionEditor/ServiceEditor/AttributeCardList.svelte';
	import UnsavedChangesModal from '$lib/components/ServiceDefinitionEditor/Shared/UnsavedChangesModal.svelte';
	import {
		splitPaneStore,
		hasAnyUnsavedChanges,
		attemptSelectGroup,
		attemptSelectService
	} from '$lib/components/ServiceDefinitionEditor/stores/editorStore';

	const libre311 = useLibre311Service();

	// Data state
	let groups: GroupWithServices[] = [];
	let expandedGroupIds: Set<number> = new Set();
	let selection: EditorSelection = { type: null, groupId: null, serviceCode: null };

	// Selected item data
	let selectedGroup: Group | null = null;
	let selectedService: Service | null = null;
	let attributes: ServiceDefinitionAttribute[] = [];

	// Loading states
	let isTreeLoading = true;
	let isEditorLoading = false;
	let editorError: string | null = null;

	// Dirty tracking for cards
	let dirtyAttributes: Set<number> = new Set();
	let savingAttributes: Set<number> = new Set();
	let deletingAttributes: Set<number> = new Set();
	let isHeaderDirty = false;
	let isHeaderSaving = false;

	// Expanded attribute index
	let expandedAttributeIndex: number | null = null;

	// Unsaved changes modal
	let showUnsavedModal = false;
	let pendingNavigation: (() => void) | null = null;
	let isSavingBeforeNav = false;

	// Subscribe to store
	$: showUnsavedModal = $splitPaneStore.showUnsavedModal;

	// Load groups and services on mount
	onMount(async () => {
		await loadGroupsAndServices();
	});

	async function loadGroupsAndServices() {
		isTreeLoading = true;
		try {
			const [groupList, serviceList] = await Promise.all([
				libre311.getGroupList(),
				libre311.getServiceList()
			]);

			// Combine groups with their services
			groups = groupList.map((group) => ({
				...group,
				services: serviceList.filter((s) => s.group_id === group.id),
				serviceCount: serviceList.filter((s) => s.group_id === group.id).length
			}));

			// Expand first group by default if available
			if (groups.length > 0) {
				expandedGroupIds = new Set([groups[0].id]);
			}
		} catch (err) {
			console.error('Failed to load groups and services:', err);
			editorError = 'Failed to load data. Please try again.';
		} finally {
			isTreeLoading = false;
		}
	}

	// Event handlers
	function handleToggleGroup(event: CustomEvent<{ groupId: number }>) {
		const { groupId } = event.detail;
		const newExpanded = new Set(expandedGroupIds);
		if (newExpanded.has(groupId)) {
			newExpanded.delete(groupId);
		} else {
			newExpanded.add(groupId);
		}
		expandedGroupIds = newExpanded;
	}

	function handleSelectGroup(event: CustomEvent<{ groupId: number }>) {
		const { groupId } = event.detail;

		// Check for unsaved changes
		if ($hasAnyUnsavedChanges || isHeaderDirty || dirtyAttributes.size > 0) {
			attemptSelectGroup(groupId);
			return;
		}

		doSelectGroup(groupId);
	}

	function doSelectGroup(groupId: number) {
		selection = { type: 'group', groupId, serviceCode: null };
		selectedGroup = groups.find((g) => g.id === groupId) || null;
		selectedService = null;
		attributes = [];
		expandedAttributeIndex = null;
		clearDirtyState();
	}

	function handleSelectService(event: CustomEvent<{ groupId: number; serviceCode: number }>) {
		const { groupId, serviceCode } = event.detail;

		// Check for unsaved changes
		if ($hasAnyUnsavedChanges || isHeaderDirty || dirtyAttributes.size > 0) {
			attemptSelectService(groupId, serviceCode);
			return;
		}

		doSelectService(groupId, serviceCode);
	}

	async function doSelectService(groupId: number, serviceCode: number) {
		selection = { type: 'service', groupId, serviceCode };
		selectedGroup = null;
		expandedAttributeIndex = null;
		clearDirtyState();

		// Find service in groups
		const group = groups.find((g) => g.id === groupId);
		selectedService = group?.services.find((s) => s.service_code === serviceCode) || null;

		if (selectedService) {
			await loadServiceDefinition(serviceCode);
		}
	}

	async function loadServiceDefinition(serviceCode: number) {
		isEditorLoading = true;
		editorError = null;
		try {
			const definition = await libre311.getServiceDefinition({ service_code: serviceCode });
			attributes = definition.attributes || [];
		} catch (err) {
			console.error('Failed to load service definition:', err);
			editorError = 'Failed to load service definition.';
			attributes = [];
		} finally {
			isEditorLoading = false;
		}
	}

	function clearDirtyState() {
		dirtyAttributes = new Set();
		savingAttributes = new Set();
		deletingAttributes = new Set();
		isHeaderDirty = false;
		isHeaderSaving = false;
		splitPaneStore.clearAllDirty();
	}

	// Header card handlers
	function handleHeaderDirty(event: CustomEvent<{ isDirty: boolean }>) {
		isHeaderDirty = event.detail.isDirty;
	}

	async function handleHeaderSave(event: CustomEvent<{ serviceName: string; description: string }>) {
		if (!selectedService) return;

		isHeaderSaving = true;
		try {
			const updated = await libre311.updateService({
				service_code: selectedService.service_code,
				service_name: event.detail.serviceName,
				description: event.detail.description
			});

			// Update local state
			selectedService = { ...selectedService, ...updated };

			// Update in groups list
			groups = groups.map((g) => ({
				...g,
				services: g.services.map((s) =>
					s.service_code === updated.service_code ? { ...s, ...updated } : s
				)
			}));

			isHeaderDirty = false;
		} catch (err) {
			console.error('Failed to save service header:', err);
		} finally {
			isHeaderSaving = false;
		}
	}

	// Attribute handlers
	function handleAttributeExpand(event: CustomEvent<{ index: number }>) {
		expandedAttributeIndex = event.detail.index;
	}

	function handleAttributeCollapse() {
		expandedAttributeIndex = null;
	}

	function handleAttributeDirty(event: CustomEvent<{ index: number; code: number; isDirty: boolean }>) {
		const { code, isDirty } = event.detail;
		const newDirty = new Set(dirtyAttributes);
		if (isDirty) {
			newDirty.add(code);
		} else {
			newDirty.delete(code);
		}
		dirtyAttributes = newDirty;
	}

	async function handleAttributeSave(
		event: CustomEvent<{
			index: number;
			code: number;
			data: {
				description: string;
				datatype: string;
				required: boolean;
				datatypeDescription: string;
				values?: { key: string; name: string }[];
			};
		}>
	) {
		const { code, data } = event.detail;

		savingAttributes = new Set([...savingAttributes, code]);
		try {
			await libre311.updateServiceDefinitionAttribute({
				code,
				...data,
				datatype_description: data.datatypeDescription
			});

			// Update local state
			attributes = attributes.map((attr) =>
				attr.code === code
					? {
							...attr,
							description: data.description,
							datatype: data.datatype as ServiceDefinitionAttribute['datatype'],
							required: data.required,
							datatype_description: data.datatypeDescription,
							...(data.values ? { values: data.values } : {})
						}
					: attr
			);

			// Clear dirty state for this attribute
			const newDirty = new Set(dirtyAttributes);
			newDirty.delete(code);
			dirtyAttributes = newDirty;
		} catch (err) {
			console.error('Failed to save attribute:', err);
		} finally {
			const newSaving = new Set(savingAttributes);
			newSaving.delete(code);
			savingAttributes = newSaving;
		}
	}

	function handleAttributeCopy(
		event: CustomEvent<{
			index: number;
			attribute: ServiceDefinitionAttribute;
			suggestedDescription: string;
		}>
	) {
		// TODO: Implement copy attribute API call
		console.log('Copy attribute:', event.detail);
	}

	async function handleAttributeDeleteConfirm(
		event: CustomEvent<{ index: number; attribute: ServiceDefinitionAttribute }>
	) {
		const { attribute } = event.detail;

		deletingAttributes = new Set([...deletingAttributes, attribute.code]);
		try {
			await libre311.deleteServiceDefinitionAttribute({ code: attribute.code });

			// Remove from local state
			attributes = attributes.filter((a) => a.code !== attribute.code);
			expandedAttributeIndex = null;
		} catch (err) {
			console.error('Failed to delete attribute:', err);
		} finally {
			const newDeleting = new Set(deletingAttributes);
			newDeleting.delete(attribute.code);
			deletingAttributes = newDeleting;
		}
	}

	function handleAttributeReorder(event: CustomEvent<{ fromIndex: number; toIndex: number }>) {
		const { fromIndex, toIndex } = event.detail;

		// Reorder locally
		const newAttributes = [...attributes];
		const [moved] = newAttributes.splice(fromIndex, 1);
		newAttributes.splice(toIndex, 0, moved);
		attributes = newAttributes;

		// TODO: Call API to persist order
		console.log('Reorder attributes:', fromIndex, '->', toIndex);
	}

	// Service reorder in tree
	function handleServiceReorder(
		event: CustomEvent<{
			serviceCode: number;
			fromGroupId: number;
			toGroupId: number;
			newIndex: number;
		}>
	) {
		// TODO: Implement service reorder API call
		console.log('Reorder service:', event.detail);
	}

	// Unsaved changes modal handlers
	function handleUnsavedSave() {
		// TODO: Save all dirty items then proceed
		isSavingBeforeNav = true;
		// For now, just proceed
		splitPaneStore.proceedWithNavigation();
		isSavingBeforeNav = false;
	}

	function handleUnsavedDiscard() {
		clearDirtyState();
		splitPaneStore.proceedWithNavigation();
	}

	function handleUnsavedCancel() {
		splitPaneStore.hideUnsavedChangesModal();
	}
</script>

<svelte:head>
	<title>Service Definition Configuration</title>
</svelte:head>

<div class="flex h-screen flex-col">
	<!-- Header -->
	<header class="border-b border-gray-200 bg-white px-6 py-4">
		<div class="flex items-center gap-4">
			<a
				href="/groups"
				class="flex items-center gap-2 text-gray-600 hover:text-gray-900"
				aria-label="Back to Admin"
			>
				<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M10 19l-7-7m0 0l7-7m-7 7h18"
					/>
				</svg>
			</a>
			<h1 class="text-xl font-semibold text-gray-900">Service Definition Configuration</h1>
		</div>
	</header>

	<!-- Main Content -->
	<main class="flex-1 overflow-hidden">
		<SplitPaneLayout
			{groups}
			{expandedGroupIds}
			{selection}
			{selectedGroup}
			{selectedService}
			{attributes}
			isTreeLoading={isTreeLoading}
			isEditorLoading={isEditorLoading}
			{editorError}
			on:toggleGroup={handleToggleGroup}
			on:selectGroup={handleSelectGroup}
			on:selectService={handleSelectService}
			on:reorderService={handleServiceReorder}
		>
			<svelte:fragment slot="service-editor">
				{#if selectedService}
					<!-- Service Header Card -->
					<ServiceHeaderCard
						service={selectedService}
						isSaving={isHeaderSaving}
						on:save={handleHeaderSave}
						on:dirty={handleHeaderDirty}
					/>

					<!-- Attributes List -->
					{#if attributes.length > 0}
						<div class="mt-6">
							<h3 class="mb-4 text-sm font-medium text-gray-700">
								Questions ({attributes.length})
							</h3>
							<AttributeCardList
								{attributes}
								expandedIndex={expandedAttributeIndex}
								{dirtyAttributes}
								{savingAttributes}
								{deletingAttributes}
								on:expand={handleAttributeExpand}
								on:collapse={handleAttributeCollapse}
								on:save={handleAttributeSave}
								on:copy={handleAttributeCopy}
								on:deleteConfirm={handleAttributeDeleteConfirm}
								on:dirty={handleAttributeDirty}
								on:reorder={handleAttributeReorder}
							/>
						</div>
					{:else if !isEditorLoading}
						<div class="mt-6 rounded-lg border border-dashed border-gray-300 p-8 text-center">
							<p class="text-sm text-gray-500">No questions defined for this service yet.</p>
							<button
								type="button"
								class="mt-4 rounded-md bg-purple-600 px-4 py-2 text-sm font-medium text-white hover:bg-purple-700"
							>
								+ Add Question
							</button>
						</div>
					{/if}
				{/if}
			</svelte:fragment>
		</SplitPaneLayout>
	</main>
</div>

<!-- Unsaved Changes Modal -->
<UnsavedChangesModal
	open={showUnsavedModal}
	isSaving={isSavingBeforeNav}
	on:save={handleUnsavedSave}
	on:discard={handleUnsavedDiscard}
	on:cancel={handleUnsavedCancel}
/>
