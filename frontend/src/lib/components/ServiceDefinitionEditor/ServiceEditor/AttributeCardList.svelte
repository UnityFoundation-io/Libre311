<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import { flip } from 'svelte/animate';
	import type { ServiceDefinitionAttribute } from '$lib/services/Libre311/Libre311';
	import type { AttributeFormData } from '../stores/types';
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
	 * Map of pending attribute values (unsaved changes)
	 */
	export let pendingAttributeValues: Map<number, AttributeFormData> = new Map();

	/**
	 * Whether drag-drop (now arrow button) reordering is enabled
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
			data: AttributeFormData;
		};
		cancel: { index: number };
		copy: { index: number; attribute: ServiceDefinitionAttribute; suggestedDescription: string };
		deleteConfirm: { index: number; attribute: ServiceDefinitionAttribute };
		dirty: {
			index: number;
			code: number;
			isDirty: boolean;
			pendingValues?: AttributeFormData;
		};
		reorder: { fromIndex: number; toIndex: number };
	}>();

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

	function handleSave(event: CustomEvent<AttributeFormData>, index: number) {
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

	function handleDirtyChange(
		event: CustomEvent<{
			isDirty: boolean;
			pendingValues?: AttributeFormData;
		}>,
		index: number
	) {
		const attr = attributes[index];
		dispatch('dirty', {
			index,
			code: attr.code,
			isDirty: event.detail.isDirty,
			pendingValues: event.detail.pendingValues
		});
	}

	function moveUp(index: number) {
		if (index > 0) {
			dispatch('reorder', { fromIndex: index, toIndex: index - 1 });
		}
	}

	function moveDown(index: number) {
		if (index < attributes.length - 1) {
			dispatch('reorder', { fromIndex: index, toIndex: index + 1 });
		}
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
			class="flex items-start gap-2"
			role="listitem"
			animate:flip={{ duration: prefersReducedMotion ? 0 : 200 }}
		>
			<!-- Reorder Buttons -->
			{#if reorderEnabled && attributes.length > 1}
				<div class="flex flex-col gap-1 pt-1">
					<button
						type="button"
						class="flex h-8 w-8 items-center justify-center rounded border border-gray-200 bg-white text-gray-500 hover:border-gray-300 hover:bg-gray-50 hover:text-gray-700 disabled:opacity-30 disabled:hover:border-gray-200 disabled:hover:bg-white"
						disabled={index === 0}
						on:click={() => moveUp(index)}
						aria-label="Move question up"
						title="Move question up"
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
					<button
						type="button"
						class="flex h-8 w-8 items-center justify-center rounded border border-gray-200 bg-white text-gray-500 hover:border-gray-300 hover:bg-gray-50 hover:text-gray-700 disabled:opacity-30 disabled:hover:border-gray-200 disabled:hover:bg-white"
						disabled={index === attributes.length - 1}
						on:click={() => moveDown(index)}
						aria-label="Move question down"
						title="Move question down"
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
				</div>
			{/if}

			<div class="min-w-0 flex-1">
				<AttributeCard
					bind:this={cardRefs[index]}
					{attribute}
					isExpanded={expandedIndex === index}
					isDirty={dirtyAttributes.has(attribute.code)}
					isSaving={savingAttributes.has(attribute.code)}
					isDeleting={deletingAttributes.has(attribute.code)}
					pendingValues={pendingAttributeValues.get(attribute.code)}
					on:expand={() => handleExpand(index)}
					on:collapse={handleCollapse}
					on:save={(e) => handleSave(e, index)}
					on:cancel={() => handleCancel(index)}
					on:copy={(e) => handleCopy(e, index)}
					on:deleteConfirm={() => handleDeleteConfirm(index)}
					on:dirty={(e) => handleDirtyChange(e, index)}
				/>
			</div>
		</div>
	{/each}
</div>

<style>
</style>
