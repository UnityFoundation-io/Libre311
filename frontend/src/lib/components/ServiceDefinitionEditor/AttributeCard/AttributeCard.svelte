<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import { slide } from 'svelte/transition';
	import type {
		ServiceDefinitionAttribute,
		DatatypeUnion,
		AttributeValue
	} from '$lib/services/Libre311/Libre311';
	import AttributeCardCollapsed from './AttributeCardCollapsed.svelte';
	import AttributeCardExpanded from './AttributeCardExpanded.svelte';

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
	 * Whether the card is being dragged
	 */
	export let isDragging = false;

	const dispatch = createEventDispatcher<{
		expand: void;
		collapse: void;
		save: {
			description: string;
			datatype: DatatypeUnion;
			required: boolean;
			datatypeDescription: string;
			values?: AttributeValue[];
		};
		cancel: void;
		copy: void;
		delete: void;
		dirty: { isDirty: boolean };
		dragstart: void;
		dragend: void;
	}>();

	function handleExpand() {
		if (!isExpanded) {
			dispatch('expand');
		}
	}

	function handleCollapse() {
		dispatch('collapse');
	}

	function handleDirtyChange(event: CustomEvent<{ isDirty: boolean }>) {
		isDirty = event.detail.isDirty;
		dispatch('dirty', event.detail);
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

	// Reduce motion preference
	let prefersReducedMotion = false;
	if (typeof window !== 'undefined') {
		prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
	}
</script>

<div
	class="overflow-hidden rounded-lg border border-gray-200 bg-white shadow-sm transition-shadow {isExpanded
		? 'ring-2 ring-purple-500'
		: ''} {isDragging ? 'opacity-50' : ''}"
	aria-label="Attribute: {attribute.description}"
>
	{#if isExpanded}
		<!-- Expanded View -->
		<div transition:slide={{ duration: prefersReducedMotion ? 0 : 200 }}>
			<AttributeCardExpanded
				bind:this={expandedComponent}
				{attribute}
				{isSaving}
				{isDragging}
				on:save
				on:cancel
				on:copy
				on:delete
				on:dirty={handleDirtyChange}
				on:collapse={handleCollapse}
				on:dragstart
				on:dragend
			/>
		</div>
	{:else}
		<!-- Collapsed View -->
		<AttributeCardCollapsed {attribute} {isDirty} on:click={handleExpand} />
	{/if}
</div>
