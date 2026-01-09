<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import { flip } from 'svelte/animate';
	import type {
		ServiceDefinitionAttribute,
		DatatypeUnion,
		AttributeValue
	} from '$lib/services/Libre311/Libre311';
	import AttributeCard from '../AttributeCard/AttributeCard.svelte';

	/**
	 * List of attributes to display
	 */
	export let attributes: ServiceDefinitionAttribute[] = [];

	/**
	 * Index of the currently expanded card (null if none)
	 */
	export let expandedIndex: number | null = null;

	/**
	 * Set of attribute codes that are currently dirty
	 */
	export let dirtyAttributes: Set<number> = new Set();

	/**
	 * Set of attribute codes that are currently saving
	 */
	export let savingAttributes: Set<number> = new Set();

	/**
	 * Whether drag-drop reordering is enabled
	 */
	export let reorderEnabled = true;

	/**
	 * Set of attribute codes that are currently being deleted
	 */
	export let deletingAttributes: Set<number> = new Set();

	const dispatch = createEventDispatcher<{
		expand: { index: number };
		collapse: void;
		save: {
			index: number;
			code: number;
			data: {
				description: string;
				datatype: DatatypeUnion;
				required: boolean;
				datatypeDescription: string;
				values?: AttributeValue[];
			};
		};
		cancel: { index: number };
		copy: { index: number; attribute: ServiceDefinitionAttribute; suggestedDescription: string };
		deleteConfirm: { index: number; attribute: ServiceDefinitionAttribute };
		dirty: { index: number; code: number; isDirty: boolean };
		reorder: { fromIndex: number; toIndex: number };
	}>();

	// Drag state
	let draggedIndex: number | null = null;
	let dropTargetIndex: number | null = null;
	let dropPosition: 'before' | 'after' | null = null;

	// Reduced motion preference
	let prefersReducedMotion = false;
	if (typeof window !== 'undefined') {
		prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
	}

	function handleExpand(index: number) {
		dispatch('expand', { index });
	}

	function handleCollapse() {
		dispatch('collapse');
	}

	function handleSave(
		event: CustomEvent<{
			description: string;
			datatype: DatatypeUnion;
			required: boolean;
			datatypeDescription: string;
			values?: AttributeValue[];
		}>,
		index: number
	) {
		const attr = attributes[index];
		dispatch('save', {
			index,
			code: attr.code,
			data: event.detail
		});
	}

	function handleCancel(index: number) {
		dispatch('cancel', { index });
	}

	function handleCopy(event: CustomEvent<{ suggestedDescription: string }>, index: number) {
		dispatch('copy', {
			index,
			attribute: attributes[index],
			suggestedDescription: event.detail.suggestedDescription
		});
	}

	function handleDeleteConfirm(index: number) {
		dispatch('deleteConfirm', { index, attribute: attributes[index] });
	}

	function handleDirtyChange(event: CustomEvent<{ isDirty: boolean }>, index: number) {
		const attr = attributes[index];
		dispatch('dirty', { index, code: attr.code, isDirty: event.detail.isDirty });
	}

	// ========== Drag and Drop Handlers ==========

	function handleDragStart(event: DragEvent, index: number) {
		if (!reorderEnabled || !event.dataTransfer) return;

		draggedIndex = index;
		event.dataTransfer.effectAllowed = 'move';
		event.dataTransfer.setData('text/plain', String(index));
	}

	function handleDragOver(event: DragEvent, index: number) {
		if (!reorderEnabled || draggedIndex === null || draggedIndex === index) return;

		event.preventDefault();
		event.dataTransfer!.dropEffect = 'move';

		// Determine if dropping before or after
		const target = event.currentTarget as HTMLElement;
		const rect = target.getBoundingClientRect();
		const midpoint = rect.top + rect.height / 2;

		dropTargetIndex = index;
		dropPosition = event.clientY < midpoint ? 'before' : 'after';
	}

	function handleDragLeave() {
		// Don't clear state on dragleave - let dragover and dragend handle it
		// This prevents flickering when moving between child elements
	}

	function handleDrop(event: DragEvent, index: number) {
		event.preventDefault();

		if (draggedIndex === null || draggedIndex === index) {
			resetDragState();
			return;
		}

		// Calculate the final position
		let toIndex = index;
		if (dropPosition === 'after') {
			toIndex = index + 1;
		}

		// Adjust if dragging from before to after
		if (draggedIndex < toIndex) {
			toIndex -= 1;
		}

		if (draggedIndex !== toIndex) {
			dispatch('reorder', { fromIndex: draggedIndex, toIndex });
		}

		resetDragState();
	}

	function handleDragEnd() {
		resetDragState();
	}

	function resetDragState() {
		draggedIndex = null;
		dropTargetIndex = null;
		dropPosition = null;
	}

	// Ref to cards for resetting after save
	let cardRefs: (AttributeCard | null)[] = [];

	/**
	 * Reset a specific card to saved values
	 */
	export function resetCardToSaved(index: number, savedAttr: ServiceDefinitionAttribute) {
		cardRefs[index]?.resetToSaved(savedAttr);
	}

	/**
	 * Close the delete modal for a specific card (after delete completes)
	 */
	export function closeDeleteModal(index: number) {
		cardRefs[index]?.closeDeleteModal();
	}
</script>

<div class="space-y-4" role="list" aria-label="Attribute questions">
	{#each attributes as attribute, index (attribute.code)}
		<div
			class="relative"
			role="listitem"
			draggable={reorderEnabled && expandedIndex !== index}
			on:dragstart={(e) => handleDragStart(e, index)}
			on:dragover={(e) => handleDragOver(e, index)}
			on:dragleave={handleDragLeave}
			on:drop={(e) => handleDrop(e, index)}
			on:dragend={handleDragEnd}
			animate:flip={{ duration: prefersReducedMotion ? 0 : 200 }}
		>
			<!-- Drop indicator BEFORE this card -->
			{#if dropTargetIndex === index && dropPosition === 'before'}
				<div class="drop-indicator" />
			{/if}

			<AttributeCard
				bind:this={cardRefs[index]}
				{attribute}
				isExpanded={expandedIndex === index}
				isDirty={dirtyAttributes.has(attribute.code)}
				isSaving={savingAttributes.has(attribute.code)}
				isDeleting={deletingAttributes.has(attribute.code)}
				isDragging={draggedIndex === index}
				on:expand={() => handleExpand(index)}
				on:collapse={handleCollapse}
				on:save={(e) => handleSave(e, index)}
				on:cancel={() => handleCancel(index)}
				on:copy={(e) => handleCopy(e, index)}
				on:deleteConfirm={() => handleDeleteConfirm(index)}
				on:dirty={(e) => handleDirtyChange(e, index)}
			/>

			<!-- Drop indicator AFTER this card -->
			{#if dropTargetIndex === index && dropPosition === 'after'}
				<div class="drop-indicator drop-indicator-after" />
			{/if}
		</div>
	{/each}
</div>

<style>
	/* Drop indicator - blue line */
	.drop-indicator {
		height: 4px;
		background: rgb(59 130 246);
		border-radius: 2px;
		margin-bottom: 8px;
	}

	.drop-indicator-after {
		margin-bottom: 0;
		margin-top: 8px;
	}
</style>
