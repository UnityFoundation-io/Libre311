<script lang="ts">
	import { onMount, tick } from 'svelte';
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import type { Service, ServiceDefinitionAttribute, Group } from '$lib/services/Libre311/Libre311';
	import type {
		GroupWithServices,
		EditorSelection
	} from '$lib/components/ServiceDefinitionEditor/stores/types';
	import SplitPaneLayout from '$lib/components/ServiceDefinitionEditor/SplitPaneEditor/SplitPaneLayout.svelte';
	import ServiceHeaderCard from '$lib/components/ServiceDefinitionEditor/ServiceEditor/ServiceHeaderCard.svelte';
	import AttributeCardList from '$lib/components/ServiceDefinitionEditor/ServiceEditor/AttributeCardList.svelte';
	import GroupEditor from '$lib/components/ServiceDefinitionEditor/GroupEditor/GroupEditor.svelte';
	import UnsavedChangesModal from '$lib/components/ServiceDefinitionEditor/Shared/UnsavedChangesModal.svelte';
	import ConfirmDeleteModal from '$lib/components/ServiceDefinitionEditor/Shared/ConfirmDeleteModal.svelte';
	import {
		splitPaneStore,
		hasAnyUnsavedChanges,
		showSaveError
	} from '$lib/components/ServiceDefinitionEditor/stores/editorStore';
	import SaveToast from '$lib/components/ServiceDefinitionEditor/EditorView/SaveToast.svelte';

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
	let pendingAttributeValues: Map<
		number,
		{
			description: string;
			datatype: string;
			required: boolean;
			datatypeDescription: string;
			values?: { key: string; name: string }[];
		}
	> = new Map();
	let savingAttributes: Set<number> = new Set();
	let deletingAttributes: Set<number> = new Set();
	let isHeaderDirty = false;
	let isHeaderSaving = false;
	let pendingHeaderValues: { serviceName: string; description: string } | null = null;

	// Group editing state
	let isGroupDirty = false;
	let isGroupSaving = false;
	let isGroupDeleting = false;

	// Service deletion state
	let showDeleteServiceModal = false;
	let serviceToDelete: { groupId: number; serviceCode: number; serviceName: string } | null = null;
	let isServiceDeleting = false;

	// Expanded attribute index
	let expandedAttributeIndex: number | null = null;

	// Ref to AttributeCardList for resetting after save
	let attributeCardListRef: AttributeCardList;

	// Unsaved changes modal
	let showUnsavedModal = false;
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

		// Check for unsaved changes (both store and local state)
		if ($hasAnyUnsavedChanges || isHeaderDirty || isGroupDirty || dirtyAttributes.size > 0) {
			// Show modal with callback to complete navigation after user decision
			splitPaneStore.showUnsavedChangesModal(() => doSelectGroup(groupId));
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

		// Check for unsaved changes (both store and local state)
		if ($hasAnyUnsavedChanges || isHeaderDirty || isGroupDirty || dirtyAttributes.size > 0) {
			// Show modal with callback to complete navigation after user decision
			splitPaneStore.showUnsavedChangesModal(() => doSelectService(groupId, serviceCode));
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
		pendingAttributeValues = new Map();
		savingAttributes = new Set();
		deletingAttributes = new Set();
		isHeaderDirty = false;
		isHeaderSaving = false;
		pendingHeaderValues = null;
		splitPaneStore.clearAllDirty();
	}

	// Header card handlers
	function handleHeaderDirty(
		event: CustomEvent<{ isDirty: boolean; serviceName: string; description: string }>
	) {
		isHeaderDirty = event.detail.isDirty;
		if (event.detail.isDirty) {
			pendingHeaderValues = {
				serviceName: event.detail.serviceName,
				description: event.detail.description
			};
		} else {
			pendingHeaderValues = null;
		}
	}

	async function handleHeaderSave(
		event: CustomEvent<{ serviceName: string; description: string }>
	) {
		if (!selectedService) return;

		isHeaderSaving = true;
		try {
			const updated = await libre311.editService({
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
			showSaveError('Failed to save service. Please try again.');
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

	function handleAttributeDirty(
		event: CustomEvent<{
			index: number;
			code: number;
			isDirty: boolean;
			pendingValues?: {
				description: string;
				datatype: string;
				required: boolean;
				datatypeDescription: string;
				values?: { key: string; name: string }[];
			};
		}>
	) {
		const { code, isDirty, pendingValues } = event.detail;
		const newDirty = new Set(dirtyAttributes);
		const newPending = new Map(pendingAttributeValues);

		if (isDirty && pendingValues) {
			newDirty.add(code);
			newPending.set(code, pendingValues);
		} else {
			newDirty.delete(code);
			newPending.delete(code);
		}

		dirtyAttributes = newDirty;
		pendingAttributeValues = newPending;
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
		if (!selectedService) {
			console.error('Cannot save attribute: no service selected');
			return;
		}

		const { code, data } = event.detail;

		savingAttributes = new Set([...savingAttributes, code]);
		try {
			await libre311.editAttribute({
				attribute_code: code,
				service_code: selectedService.service_code,
				description: data.description,
				datatype: data.datatype,
				datatype_description: data.datatypeDescription,
				required: data.required,
				values: data.values
			});

			// Update local state - reload service definition to get updated attributes
			const definition = await libre311.getServiceDefinition({
				service_code: selectedService.service_code
			});
			attributes = definition.attributes || [];

			// Reset the card's internal state to match saved values
			await tick();
			const savedAttrIndex = attributes.findIndex((a) => a.code === code);
			if (savedAttrIndex !== -1 && attributeCardListRef) {
				attributeCardListRef.resetCardToSaved(savedAttrIndex, attributes[savedAttrIndex]);
			}

			// Clear dirty state for this attribute
			const newDirty = new Set(dirtyAttributes);
			newDirty.delete(code);
			dirtyAttributes = newDirty;
		} catch (err) {
			console.error('Failed to save attribute:', err);
			showSaveError('Failed to save question. Please try again.');
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
		if (!selectedService) {
			console.error('Cannot delete attribute: no service selected');
			return;
		}

		const { attribute } = event.detail;

		deletingAttributes = new Set([...deletingAttributes, attribute.code]);
		try {
			await libre311.deleteAttribute({
				serviceCode: selectedService.service_code,
				attributeCode: attribute.code
			});

			// Remove from local state
			attributes = attributes.filter((a) => a.code !== attribute.code);
			expandedAttributeIndex = null;
		} catch (err) {
			console.error('Failed to delete attribute:', err);
			showSaveError('Failed to delete question. Please try again.');
		} finally {
			const newDeleting = new Set(deletingAttributes);
			newDeleting.delete(attribute.code);
			deletingAttributes = newDeleting;
		}
	}

	async function handleAttributeReorder(
		event: CustomEvent<{ fromIndex: number; toIndex: number }>
	) {
		if (!selectedService) return;

		const { fromIndex, toIndex } = event.detail;

		// Reorder locally
		const newAttributes = [...attributes];
		const [moved] = newAttributes.splice(fromIndex, 1);
		newAttributes.splice(toIndex, 0, moved);
		attributes = newAttributes;

		// Persist the new order to the API
		try {
			await libre311.updateAttributesOrder({
				service_code: selectedService.service_code,
				attributes: newAttributes.map((attr, index) => ({
					code: attr.code,
					order: index
				}))
			});
		} catch (err) {
			console.error('Failed to reorder questions:', err);
			showSaveError('Failed to save question order. Please try again.');
			// Reload to restore server state
			await loadServiceDefinition(selectedService.service_code);
		}
	}

	// Service reorder in tree
	async function handleServiceReorder(
		event: CustomEvent<{
			serviceCode: number;
			fromGroupId: number;
			toGroupId: number;
			newIndex: number;
		}>
	) {
		const { serviceCode, fromGroupId, toGroupId, newIndex } = event.detail;

		// Only support reordering within the same group for now
		if (fromGroupId !== toGroupId) {
			console.warn('Moving services between groups is not yet supported');
			return;
		}

		try {
			// Get the group's services
			const group = groups.find((g) => g.id === fromGroupId);
			if (!group) return;

			// Build the new order
			const reorderedCodes = group.services
				.filter((s) => s.service_code !== serviceCode)
				.map((s) => s.service_code);
			reorderedCodes.splice(newIndex, 0, serviceCode);

			// Call API to persist order
			await libre311.updateServicesOrder({
				group_id: fromGroupId,
				services: reorderedCodes.map((code, idx) => ({ service_code: code, order_position: idx }))
			});

			// Update local state
			groups = groups.map((g) =>
				g.id === fromGroupId
					? {
							...g,
							services: reorderedCodes
								.map((code) => g.services.find((s) => s.service_code === code))
								.filter((s): s is Service => s !== undefined)
						}
					: g
			);
		} catch (err) {
			console.error('Failed to reorder service:', err);
			showSaveError('Failed to reorder service. Please try again.');
		}
	}

	// ========== Group Management Handlers ==========

	async function handleCreateGroup() {
		try {
			const newGroup = await libre311.createGroup({ name: 'New Group' });

			// Add to local state
			const groupWithServices: GroupWithServices = {
				...newGroup,
				services: [],
				serviceCount: 0
			};
			groups = [...groups, groupWithServices];

			// Expand and select the new group
			expandedGroupIds = new Set([...expandedGroupIds, newGroup.id]);
			doSelectGroup(newGroup.id);
		} catch (err) {
			console.error('Failed to create group:', err);
			showSaveError('Failed to create group. Please try again.');
		}
	}

	function handleGroupDirty(event: CustomEvent<{ isDirty: boolean }>) {
		isGroupDirty = event.detail.isDirty;
	}

	async function handleGroupSave(event: CustomEvent<{ name: string }>) {
		if (!selectedGroup) return;

		isGroupSaving = true;
		try {
			const updated = await libre311.editGroup({
				id: selectedGroup.id,
				name: event.detail.name
			});

			// Update local state
			selectedGroup = { ...selectedGroup, ...updated };
			groups = groups.map((g) => (g.id === updated.id ? { ...g, name: updated.name } : g));
			isGroupDirty = false;
		} catch (err) {
			console.error('Failed to save group:', err);
			showSaveError('Failed to save group. Please try again.');
		} finally {
			isGroupSaving = false;
		}
	}

	async function handleGroupDelete() {
		if (!selectedGroup) return;

		const groupId = selectedGroup.id;
		isGroupDeleting = true;
		try {
			await libre311.deleteGroup({ group_id: groupId });

			// Remove from local state
			groups = groups.filter((g) => g.id !== groupId);

			// Clear selection
			selection = { type: null, groupId: null, serviceCode: null };
			selectedGroup = null;
		} catch (err) {
			console.error('Failed to delete group:', err);
			showSaveError('Failed to delete group. Please try again.');
		} finally {
			isGroupDeleting = false;
		}
	}

	// ========== Service Creation Handler ==========

	async function handleAddService(event: CustomEvent<{ groupId: number }>) {
		const { groupId } = event.detail;

		try {
			const newService = await libre311.createService({
				service_name: 'New Service',
				group_id: groupId
			});

			// Add to local state
			groups = groups.map((g) =>
				g.id === groupId
					? {
							...g,
							services: [...g.services, newService],
							serviceCount: g.serviceCount + 1
						}
					: g
			);

			// Select the new service
			await doSelectService(groupId, newService.service_code);
		} catch (err) {
			console.error('Failed to create service:', err);
			showSaveError('Failed to create service. Please try again.');
		}
	}

	// ========== Service Deletion Handlers ==========

	function handleDeleteServiceRequest(
		event: CustomEvent<{ groupId: number; serviceCode: number; serviceName: string }>
	) {
		serviceToDelete = event.detail;
		showDeleteServiceModal = true;
	}

	async function handleDeleteServiceConfirm() {
		if (!serviceToDelete) return;

		const { groupId, serviceCode } = serviceToDelete;
		isServiceDeleting = true;
		try {
			await libre311.deleteService({ service_code: serviceCode });

			// Remove from local state
			groups = groups.map((g) =>
				g.id === groupId
					? {
							...g,
							services: g.services.filter((s) => s.service_code !== serviceCode),
							serviceCount: g.serviceCount - 1
						}
					: g
			);

			// Clear selection if the deleted service was selected
			if (selection.type === 'service' && selection.serviceCode === serviceCode) {
				selection = { type: null, groupId: null, serviceCode: null };
				selectedService = null;
				attributes = [];
			}
		} catch (err) {
			console.error('Failed to delete service:', err);
			showSaveError('Failed to delete service. Please try again.');
		} finally {
			isServiceDeleting = false;
			showDeleteServiceModal = false;
			serviceToDelete = null;
		}
	}

	function handleDeleteServiceCancel() {
		showDeleteServiceModal = false;
		serviceToDelete = null;
	}

	// ========== Attribute Creation Handler ==========

	async function handleAddAttribute() {
		if (!selectedService) return;

		try {
			const response = await libre311.createAttribute({
				service_code: selectedService.service_code,
				description: 'New question',
				datatype: 'string',
				required: false,
				variable: true,
				datatype_description: '',
				order: attributes.length
			});

			// Get the newly created attribute from the response
			const newAttribute = response.attributes[response.attributes.length - 1];
			if (!newAttribute) return;

			// Add to local state
			attributes = [...attributes, newAttribute];

			// Expand and focus the new attribute
			expandedAttributeIndex = attributes.length - 1;
		} catch (err) {
			console.error('Failed to create attribute:', err);
			showSaveError('Failed to create question. Please try again.');
		}
	}

	// Unsaved changes modal handlers
	async function handleUnsavedSave() {
		isSavingBeforeNav = true;

		try {
			// Save pending header changes (service name/description)
			if (pendingHeaderValues && selectedService) {
				const updated = await libre311.editService({
					service_code: selectedService.service_code,
					service_name: pendingHeaderValues.serviceName,
					description: pendingHeaderValues.description
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
			}

			// Save all pending attribute changes
			if (pendingAttributeValues.size > 0 && selectedService) {
				const currentServiceCode = selectedService.service_code;
				const savePromises = Array.from(pendingAttributeValues.entries()).map(
					async ([code, data]) => {
						await libre311.editAttribute({
							attribute_code: code,
							service_code: currentServiceCode,
							description: data.description,
							datatype: data.datatype,
							datatype_description: data.datatypeDescription,
							required: data.required,
							values: data.values
						});
					}
				);
				await Promise.all(savePromises);
			}

			// Clear dirty state and proceed
			clearDirtyState();
			splitPaneStore.proceedWithNavigation();
		} catch (err) {
			console.error('Failed to save changes:', err);
			showSaveError('Failed to save changes. Your edits may not have been saved.');
			// Still proceed even on error (user can re-edit later)
			splitPaneStore.proceedWithNavigation();
		} finally {
			isSavingBeforeNav = false;
		}
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
			{isTreeLoading}
			{isEditorLoading}
			{editorError}
			on:toggleGroup={handleToggleGroup}
			on:selectGroup={handleSelectGroup}
			on:selectService={handleSelectService}
			on:reorderService={handleServiceReorder}
			on:createGroup={handleCreateGroup}
			on:addService={handleAddService}
			on:deleteService={handleDeleteServiceRequest}
		>
			<!-- Group Editor Slot -->
			<svelte:fragment slot="group-editor">
				{#if selectedGroup}
					<GroupEditor
						group={selectedGroup}
						isSaving={isGroupSaving}
						isDeleting={isGroupDeleting}
						canDelete={groups.find((g) => g.id === selectedGroup?.id)?.serviceCount === 0}
						serviceCount={groups.find((g) => g.id === selectedGroup?.id)?.serviceCount ?? 0}
						on:save={handleGroupSave}
						on:delete={handleGroupDelete}
						on:dirty={handleGroupDirty}
					/>
				{/if}
			</svelte:fragment>

			<!-- Service Editor Slot -->
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
					<div class="mt-6">
						<h3 class="mb-4 text-sm font-medium text-gray-700">
							Questions ({attributes.length})
						</h3>

						{#if attributes.length > 0}
							<AttributeCardList
								bind:this={attributeCardListRef}
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
						{/if}

						<!-- Add Question Card -->
						<button
							type="button"
							class="mt-4 flex w-full items-center justify-center gap-2 rounded-lg border-2 border-dashed border-gray-300 p-4 text-sm font-medium text-gray-600 hover:border-purple-400 hover:text-purple-600 focus:outline-none focus:ring-2 focus:ring-purple-500"
							on:click={handleAddAttribute}
						>
							<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M12 4v16m8-8H4"
								/>
							</svg>
							Add question
						</button>
					</div>
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

<!-- Delete Service Confirmation Modal -->
<ConfirmDeleteModal
	open={showDeleteServiceModal}
	title="Delete Service"
	message="Are you sure you want to delete the service '{serviceToDelete?.serviceName}'? This action cannot be undone."
	isDeleting={isServiceDeleting}
	on:confirm={handleDeleteServiceConfirm}
	on:cancel={handleDeleteServiceCancel}
/>

<!-- Save Toast for error/success notifications -->
<SaveToast />
