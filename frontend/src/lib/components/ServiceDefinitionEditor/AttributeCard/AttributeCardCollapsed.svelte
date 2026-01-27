<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { ServiceDefinitionAttribute, DatatypeUnion } from '$lib/services/Libre311/Libre311';
	import { DATATYPE_LABEL_MAP, isListDatatype } from '../types';

	/**
	 * The attribute to display
	 */
	export let attribute: ServiceDefinitionAttribute;

	/**
	 * Whether this card has unsaved changes
	 */
	export let isDirty = false;

	const dispatch = createEventDispatcher<{
		reorder: { direction: 'up' | 'down' };
	}>();

	// Get the display label for the datatype
	$: typeLabel = DATATYPE_LABEL_MAP[attribute.datatype];

	// Check if this is a list-type attribute
	$: isList = isListDatatype(attribute.datatype);

	// Get values for list types
	$: values = isList && 'values' in attribute ? attribute.values : [];

	// Get the appropriate indicator for list types
	function getListIndicator(datatype: DatatypeUnion): string {
		if (datatype === 'multivaluelist') {
			return '[ ]'; // Checkbox indicator for multiple choice
		}
		if (datatype === 'singlevaluelist') {
			return 'O'; // Circle indicator for dropdown
		}
		return '';
	}

	function handleKeydown(event: KeyboardEvent) {
		if (event.key === 'Enter' || event.key === ' ') {
			event.preventDefault();
			const target = event.currentTarget as HTMLElement;
			target.click();
		} else if (event.altKey) {
			if (event.key === 'ArrowUp') {
				event.preventDefault();
				dispatch('reorder', { direction: 'up' });
			} else if (event.key === 'ArrowDown') {
				event.preventDefault();
				dispatch('reorder', { direction: 'down' });
			}
		}
	}
</script>

<div
	class="flex cursor-pointer items-start gap-3 p-4 transition-colors hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-blue-500"
	role="button"
	tabindex="0"
	aria-expanded="false"
	aria-label="Edit question: {attribute.description}"
	on:click
	on:keydown={handleKeydown}
>
	<!-- Question Text -->
	<div class="min-w-0 flex-1">
		<p class="truncate text-sm font-medium text-gray-900">
			{attribute.description}
			{#if attribute.required}
				<span class="text-red-500" aria-label="Required">*</span>
			{/if}
			{#if isDirty}
				<span class="ml-1 text-xs text-amber-600" aria-label="Unsaved changes">(unsaved)</span>
			{/if}
		</p>

		<!-- List type options preview -->
		{#if isList && values.length > 0}
			<div class="mt-1 flex flex-wrap gap-2">
				{#each values.slice(0, 3) as value}
					<span class="inline-flex items-center gap-1 text-xs text-gray-500">
						<span class="font-mono">{getListIndicator(attribute.datatype)}</span>
						<span class="max-w-[100px] truncate">{value.name}</span>
					</span>
				{/each}
				{#if values.length > 3}
					<span class="text-xs text-gray-400">+{values.length - 3} more</span>
				{/if}
			</div>
		{/if}
	</div>

	<!-- Type Indicator -->
	<div class="flex-shrink-0">
		<span class="rounded-full bg-gray-100 px-2 py-1 text-xs text-gray-600">
			{typeLabel}
		</span>
	</div>

	<!-- Expand Icon -->
	<div class="flex-shrink-0 text-gray-400">
		<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
			<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
		</svg>
	</div>
</div>
