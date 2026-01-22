<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { DatatypeUnion } from '$lib/services/Libre311/Libre311';
	import { ATTRIBUTE_TYPE_OPTIONS, DATATYPE_MAP, DATATYPE_LABEL_MAP } from '../types';
	import type { AttributeTypeLabel } from '../types';

	/**
	 * Current datatype value
	 */
	export let value: DatatypeUnion;

	/**
	 * Whether the selector is disabled
	 */
	export let disabled = false;

	/**
	 * Compact/inline mode - hides label and uses minimal styling
	 */
	export let compact = false;

	/**
	 * Additional classes for the select element
	 */
	export let className = '';

	const dispatch = createEventDispatcher<{
		change: DatatypeUnion;
	}>();

	// Get current label from datatype
	$: currentLabel = DATATYPE_LABEL_MAP[value];

	function handleChange(event: Event) {
		const select = event.target as HTMLSelectElement;
		const label = select.value as AttributeTypeLabel;
		const newDatatype = DATATYPE_MAP[label];
		dispatch('change', newDatatype);
	}
</script>

<div class="relative">
	{#if !compact}
		<label for="attribute-type" class="mb-1 block text-sm font-medium text-gray-700">
			Answer Type
		</label>
	{/if}
	<div class="relative">
		<select
			id="attribute-type"
			value={currentLabel}
			on:change={handleChange}
			{disabled}
			class="appearance-none bg-white text-gray-900 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:cursor-not-allowed disabled:bg-gray-100 {compact
				? 'rounded-full border border-gray-300 py-2 pl-4 pr-10 text-base'
				: 'w-full rounded-md border border-gray-300 py-2 pl-3 pr-10 text-sm focus:border-blue-500'} {className}"
		>
			{#each ATTRIBUTE_TYPE_OPTIONS as option}
				<option value={option}>{option}</option>
			{/each}
		</select>
		<!-- Dropdown icon -->
		<div class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-3">
			<svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
			</svg>
		</div>
	</div>
</div>
