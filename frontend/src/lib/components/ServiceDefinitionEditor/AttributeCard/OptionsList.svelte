<script lang="ts">
	import { createEventDispatcher, tick } from 'svelte';
	import type { AttributeValue } from '$lib/services/Libre311/Libre311';

	/**
	 * List of option values
	 */
	export let values: AttributeValue[] = [];

	/**
	 * Whether this is a multi-select (checkbox) or single-select (radio)
	 */
	export let isMultiSelect = false;

	/**
	 * Whether the inputs are disabled
	 */
	export let disabled = false;

	const dispatch = createEventDispatcher<{
		change: AttributeValue[];
	}>();

	// Track input refs for auto-focus
	let inputRefs: HTMLInputElement[] = [];

	function emitChange() {
		dispatch('change', [...values]);
	}

	function handleValueChange(index: number, newName: string) {
		values = values.map((v, i) => (i === index ? { ...v, name: newName } : v));
		emitChange();
	}

	async function handleAddOption() {
		const newOption: AttributeValue = {
			key: crypto.randomUUID(),
			name: ''
		};
		values = [...values, newOption];
		emitChange();

		// Auto-focus the new input after DOM update
		await tick();
		const newInput = inputRefs[values.length - 1];
		if (newInput) {
			newInput.focus();
		}
	}

	function handleDeleteOption(index: number) {
		// Prevent deleting the last option
		if (values.length <= 1) return;

		// Remove the ref for the deleted option to prevent stale references
		inputRefs = inputRefs.filter((_, i) => i !== index);
		values = values.filter((_, i) => i !== index);
		emitChange();
	}

	function handleKeydown(event: KeyboardEvent, index: number) {
		// Enter to add new option
		if (event.key === 'Enter') {
			event.preventDefault();
			handleAddOption();
		}
		// Backspace on empty input to delete
		if (event.key === 'Backspace' && values[index].name === '' && values.length > 1) {
			event.preventDefault();
			handleDeleteOption(index);
			// Focus previous input
			tick().then(() => {
				const prevInput = inputRefs[Math.max(0, index - 1)];
				if (prevInput) {
					prevInput.focus();
				}
			});
		}
	}
</script>

<div class="space-y-4 py-2">
	<!-- Key block forces re-render of indicators when type changes -->
	{#key isMultiSelect}
		{#each values as option, index (option.key)}
			<div class="flex items-center gap-3">
				<!-- Type indicator: checkbox for multi-select, circle for single-select -->
				{#if isMultiSelect}
					<!-- Checkbox indicator (square with rounded corners) -->
					<span
						class="flex h-5 w-5 flex-shrink-0 items-center justify-center rounded border-2 border-gray-300"
						aria-hidden="true"
					></span>
				{:else}
					<!-- Radio indicator (circle) -->
					<span
						class="flex h-5 w-5 flex-shrink-0 items-center justify-center rounded-full border-2 border-gray-300"
						aria-hidden="true"
					></span>
				{/if}

				<!-- Option input (borderless) -->
				<input
					bind:this={inputRefs[index]}
					type="text"
					value={option.name}
					on:input={(e) => handleValueChange(index, e.currentTarget.value)}
					on:keydown={(e) => handleKeydown(e, index)}
					class="flex-1 bg-transparent py-1 text-base text-gray-900 placeholder-gray-400 focus:border-blue-500 focus:outline-none disabled:text-gray-500"
					class:border-danger={!option.name.trim()}
					class:focus:border-danger={!option.name.trim()}
					placeholder="Option text"
					{disabled}
					aria-label="Option {index + 1}"
				/>

				<!-- Delete button (X) -->
				<button
					type="button"
					class="rounded p-1 text-gray-400 transition-colors hover:bg-red-50 hover:text-red-600 focus:outline-none focus:ring-2 focus:ring-danger disabled:cursor-not-allowed disabled:opacity-30"
					on:click={() => handleDeleteOption(index)}
					disabled={disabled || values.length <= 1}
					aria-label="Delete option {index + 1}"
					title={values.length <= 1 ? 'Cannot delete the last option' : 'Delete option'}
				>
					<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M6 18L18 6M6 6l12 12"
						/>
					</svg>
				</button>
			</div>
		{/each}

		<!-- Add option button with dashed indicator -->
		<button
			type="button"
			class="flex items-center gap-3 py-1 text-gray-400 hover:text-gray-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
			on:click={handleAddOption}
			{disabled}
		>
			{#if isMultiSelect}
				<!-- Dashed checkbox indicator -->
				<span
					class="flex h-5 w-5 flex-shrink-0 items-center justify-center rounded border-2 border-dashed border-gray-300"
					aria-hidden="true"
				></span>
			{:else}
				<!-- Dashed circle indicator -->
				<span
					class="flex h-5 w-5 flex-shrink-0 items-center justify-center rounded-full border-2 border-dashed border-gray-300"
					aria-hidden="true"
				></span>
			{/if}
			<span class="text-base">Add option</span>
		</button>
	{/key}

	{#if values.length === 0}
		<p class="text-sm text-red-600">At least one option is required</p>
	{/if}
</div>
