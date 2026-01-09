<script lang="ts">
	import { createEventDispatcher, onMount } from 'svelte';
	import type {
		ServiceDefinitionAttribute,
		DatatypeUnion,
		AttributeValue
	} from '$lib/services/Libre311/Libre311';
	import { isListDatatype } from '../types';
	import AttributeTypeSelector from './AttributeTypeSelector.svelte';
	import AttributeCardFooter from './AttributeCardFooter.svelte';
	import OptionsList from './OptionsList.svelte';
	import DragHandle from '../Shared/DragHandle.svelte';

	/**
	 * The attribute being edited
	 */
	export let attribute: ServiceDefinitionAttribute;

	/**
	 * Whether a save operation is in progress
	 */
	export let isSaving = false;

	/**
	 * Whether the card is being dragged
	 */
	export let isDragging = false;

	const dispatch = createEventDispatcher<{
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
		collapse: void;
		dragstart: void;
		dragend: void;
	}>();

	// Local form state
	let description = '';
	let datatype: DatatypeUnion = 'string';
	let required = false;
	let datatypeDescription = '';
	let values: AttributeValue[] = [];

	// Track original values
	let originalDescription = '';
	let originalDatatype: DatatypeUnion = 'string';
	let originalRequired = false;
	let originalDatatypeDescription = '';
	let originalValues: AttributeValue[] = [];

	// More options expanded state
	let showMoreOptions = false;

	// Initialize from attribute prop
	$: if (attribute) {
		initializeForm(attribute);
	}

	function initializeForm(attr: ServiceDefinitionAttribute) {
		description = attr.description;
		datatype = attr.datatype;
		required = attr.required;
		datatypeDescription = attr.datatype_description ?? '';
		values = 'values' in attr ? [...attr.values] : [];

		originalDescription = attr.description;
		originalDatatype = attr.datatype;
		originalRequired = attr.required;
		originalDatatypeDescription = attr.datatype_description ?? '';
		originalValues = 'values' in attr ? [...attr.values] : [];
	}

	// Compute if list type
	$: isList = isListDatatype(datatype);

	// Compute dirty state
	$: isDirty =
		description !== originalDescription ||
		datatype !== originalDatatype ||
		required !== originalRequired ||
		datatypeDescription !== originalDatatypeDescription ||
		JSON.stringify(values) !== JSON.stringify(originalValues);

	// Notify parent of dirty changes
	$: dispatch('dirty', { isDirty });

	// Validation
	$: isValid = description.trim().length > 0 && (!isList || values.length > 0);
	$: canSave = isDirty && isValid && !isSaving;

	function handleTypeChange(event: CustomEvent<DatatypeUnion>) {
		const newType = event.detail;
		datatype = newType;

		// Initialize values if switching to a list type
		if (isListDatatype(newType) && values.length === 0) {
			values = [{ key: crypto.randomUUID(), name: 'Option 1' }];
		}
	}

	function handleRequiredChange(event: CustomEvent<boolean>) {
		required = event.detail;
	}

	function handleValuesChange(event: CustomEvent<AttributeValue[]>) {
		values = event.detail;
	}

	function handleSave() {
		if (!canSave) return;

		const data: {
			description: string;
			datatype: DatatypeUnion;
			required: boolean;
			datatypeDescription: string;
			values?: AttributeValue[];
		} = {
			description: description.trim(),
			datatype,
			required,
			datatypeDescription: datatypeDescription.trim()
		};

		if (isList) {
			data.values = values;
		}

		dispatch('save', data);
	}

	function handleCancel() {
		// Revert to original values
		description = originalDescription;
		datatype = originalDatatype;
		required = originalRequired;
		datatypeDescription = originalDatatypeDescription;
		values = [...originalValues];
		dispatch('cancel');
	}

	function handleKeydown(event: KeyboardEvent) {
		if ((event.ctrlKey || event.metaKey) && event.key === 's') {
			event.preventDefault();
			if (canSave) {
				handleSave();
			}
		}
		if (event.key === 'Escape') {
			dispatch('collapse');
		}
	}

	/**
	 * Reset form to saved values (called after successful save)
	 */
	export function resetToSaved(savedAttr: ServiceDefinitionAttribute) {
		initializeForm(savedAttr);
	}

	let questionInput: HTMLInputElement;

	onMount(() => {
		questionInput?.focus();
	});
</script>

<svelte:window on:keydown={handleKeydown} />

<div class="border-t border-gray-200">
	<!-- Drag Handle -->
	<div class="flex justify-center border-b border-gray-100 py-2">
		<DragHandle
			{isDragging}
			ariaLabel="Drag to reorder this question"
			on:mousedown={() => dispatch('dragstart')}
			on:mouseup={() => dispatch('dragend')}
		/>
	</div>

	<div class="p-4">
		<!-- Question Text + Type Selector (same row) -->
		<div class="mb-4 flex items-center gap-3">
			<input
				bind:this={questionInput}
				id="question-text"
				type="text"
				bind:value={description}
				class="min-w-0 flex-1 rounded-lg border-0 bg-gray-100 px-4 py-3 text-base text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-purple-500"
				class:ring-2={description.trim().length === 0 && description !== originalDescription}
				class:ring-red-300={description.trim().length === 0 && description !== originalDescription}
				placeholder="Question"
				disabled={isSaving}
				aria-label="Question text"
			/>
			<AttributeTypeSelector
				value={datatype}
				disabled={isSaving}
				compact={true}
				on:change={handleTypeChange}
			/>
		</div>

		<!-- Options List (for list types) -->
		{#if isList}
			<div class="mb-4">
				<OptionsList
					{values}
					isMultiSelect={datatype === 'multivaluelist'}
					disabled={isSaving}
					on:change={handleValuesChange}
				/>
			</div>
		{/if}

		<!-- More Options Toggle -->
		<div class="mb-4">
			<button
				type="button"
				class="flex items-center gap-1 text-sm text-purple-600 hover:text-purple-700"
				on:click={() => (showMoreOptions = !showMoreOptions)}
			>
				<svg
					class="h-4 w-4 transition-transform {showMoreOptions ? 'rotate-90' : ''}"
					fill="none"
					stroke="currentColor"
					viewBox="0 0 24 24"
				>
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
				</svg>
				More options
			</button>

			{#if showMoreOptions}
				<div class="mt-3 rounded-md bg-gray-50 p-3">
					<label for="help-text" class="mb-1 block text-sm font-medium text-gray-700">
						Help Text
					</label>
					<input
						id="help-text"
						type="text"
						bind:value={datatypeDescription}
						class="w-full rounded-md border border-gray-300 px-3 py-2 text-sm text-gray-900 placeholder-gray-400 focus:border-purple-500 focus:outline-none focus:ring-1 focus:ring-purple-500"
						placeholder="Provide additional guidance for this question"
						disabled={isSaving}
					/>
					<p class="mt-1 text-xs text-gray-500">
						This text will appear below the question to help users understand what to enter.
					</p>
				</div>
			{/if}
		</div>
	</div>

	<!-- Footer -->
	<AttributeCardFooter
		{required}
		{canSave}
		{isSaving}
		canCancel={isDirty}
		on:save={handleSave}
		on:cancel={handleCancel}
		on:copy={() => dispatch('copy')}
		on:delete={() => dispatch('delete')}
		on:requiredChange={handleRequiredChange}
	/>
</div>
