<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import { slide } from 'svelte/transition';
	import type { ServiceDefinitionAttribute } from '$lib/services/Libre311/Libre311';
	import type { AttributeFormData } from '../stores/types';
	import AttributeCardCollapsed from './AttributeCardCollapsed.svelte';
	import AttributeCardExpanded from './AttributeCardExpanded.svelte';
	import ConfirmDeleteModal from '../Shared/ConfirmDeleteModal.svelte';

	/**
	 * The attribute to display/edit
	 */
	export let attribute: ServiceDefinitionAttribute;

	/**
	 * Whether this card is expanded
	 */
	export let isExpanded = false;

	/**
	 * Whether this card has unsaved changes
	 */
	export let isDirty = false;

	/**
	 * Whether a save operation is in progress
	 */
	export let isSaving = false;

	/**
	 * Pending values (unsaved changes) to restore if component is re-rendered
	 */
	export let pendingValues: AttributeFormData | undefined = undefined;

	/**
	 * Whether a delete operation is in progress
	 */
	export let isDeleting = false;

	const dispatch = createEventDispatcher<{
		expand: void;
		collapse: void;
		save: AttributeFormData;
		cancel: void;
		copy: { suggestedDescription: string };
		delete: void;
		deleteConfirm: void;
		dirty: {
			isDirty: boolean;
			pendingValues?: AttributeFormData;
		};
		dragstart: void;
		dragend: void;
	}>();

	// Delete confirmation modal state
	let showDeleteModal = false;

	function handleExpand() {
		if (!isExpanded) {
			dispatch('expand');
		}
	}

	function handleCollapse() {
		dispatch('collapse');
	}

	function handleDirtyChange(
		event: CustomEvent<{
			isDirty: boolean;
			pendingValues?: AttributeFormData;
		}>
	) {
		isDirty = event.detail.isDirty;
		dispatch('dirty', event.detail);
	}

	/**
	 * Handle copy request - adds "(copy)" suffix to description
	 */
	function handleCopy() {
		const copySuffix = ' (copy)';
		const suggestedDescription = attribute.description.endsWith(copySuffix)
			? attribute.description
			: attribute.description + copySuffix;
		dispatch('copy', { suggestedDescription });
	}

	/**
	 * Handle delete request - shows confirmation modal
	 */
	function handleDeleteRequest() {
		showDeleteModal = true;
	}

	/**
	 * Handle delete confirmation - dispatches delete event
	 */
	function handleDeleteConfirm() {
		dispatch('deleteConfirm');
	}

	/**
	 * Handle delete cancel - closes modal
	 */
	function handleDeleteCancel() {
		showDeleteModal = false;
	}

	// Ref to expanded component for reset
	let expandedComponent: AttributeCardExpanded;

	/**
	 * Reset form to saved values after successful save
	 */
	export function resetToSaved(savedAttr: ServiceDefinitionAttribute) {
		if (expandedComponent) {
			expandedComponent.resetToSaved(savedAttr);
		}
	}

	/**
	 * Close delete modal (called after delete completes)
	 */
	export function closeDeleteModal() {
		showDeleteModal = false;
	}

	// Reduce motion preference
	let prefersReducedMotion = false;
	if (typeof window !== 'undefined') {
		prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
	}
</script>

<div
	class="overflow-hidden rounded-lg border border-gray-200 bg-white shadow-sm transition-shadow {isExpanded
		? 'ring-2 ring-blue-500'
		: ''}"
	aria-label="Attribute: {attribute.description}"
>
	{#if isExpanded}
		<!-- Expanded View -->
		<div transition:slide={{ duration: prefersReducedMotion ? 0 : 200 }}>
			<AttributeCardExpanded
				bind:this={expandedComponent}
				{attribute}
				{isSaving}
				{pendingValues}
				on:save
				on:cancel
				on:copy={handleCopy}
				on:delete={handleDeleteRequest}
				on:dirty={handleDirtyChange}
				on:collapse={handleCollapse}
			/>
		</div>
	{:else}
		<!-- Collapsed View -->
		<AttributeCardCollapsed {attribute} {isDirty} on:click={handleExpand} />
	{/if}
</div>

<!-- Delete Confirmation Modal -->
<ConfirmDeleteModal
	open={showDeleteModal}
	title="Delete Question"
	message="Are you sure you want to delete this question? This action cannot be undone."
	{isDeleting}
	on:confirm={handleDeleteConfirm}
	on:cancel={handleDeleteCancel}
/>
