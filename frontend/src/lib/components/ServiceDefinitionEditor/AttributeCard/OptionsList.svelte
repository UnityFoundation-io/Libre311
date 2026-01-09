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
		values[index] = { ...values[index], name: newName };
		emitChange();
	}

	async function handleAddOption() {
		const newOption: AttributeValue = {
			key: crypto.randomUUID(),
			name: `Option ${values.length + 1}`
		};
		values = [...values, newOption];
		emitChange();

		// Auto-focus the new input after DOM update
		await tick();
		const newInput = inputRefs[values.length - 1];
		if (newInput) {
			newInput.focus();
			newInput.select();
		}
	}

	function handleDeleteOption(index: number) {
		// Prevent deleting the last option
		if (values.length <= 1) return;

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

	// Get the indicator character based on type
	$: indicator = isMultiSelect ? '[ ]' : 'O';
</script>

<div class="space-y-2">
	<span class="block text-sm font-medium text-gray-700" id="options-label">Options</span>

	<div class="space-y-2">
		{#each values as option, index (option.key)}
			<div class="flex items-center gap-2">
				<!-- Type indicator -->
				<span class="w-6 text-center font-mono text-sm text-gray-400" aria-hidden="true">
					{indicator}
				</span>

				<!-- Option input -->
				<input
					bind:this={inputRefs[index]}
					type="text"
					value={option.name}
					on:input={(e) => handleValueChange(index, e.currentTarget.value)}
					on:keydown={(e) => handleKeydown(e, index)}
					class="flex-1 rounded-md border border-gray-300 px-3 py-2 text-sm text-gray-900 placeholder-gray-400 focus:border-purple-500 focus:outline-none focus:ring-1 focus:ring-purple-500 disabled:bg-gray-100"
					placeholder="Option text"
					{disabled}
					aria-label="Option {index + 1}"
				/>

				<!-- Delete button -->
				<button
					type="button"
					class="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600 focus:outline-none focus:ring-2 focus:ring-purple-500 disabled:cursor-not-allowed disabled:opacity-50"
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
	</div>

	<!-- Add option button -->
	<button
		type="button"
		class="flex items-center gap-2 rounded px-2 py-1 text-sm text-purple-600 hover:bg-purple-50 focus:outline-none focus:ring-2 focus:ring-purple-500"
		on:click={handleAddOption}
		{disabled}
	>
		<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
			<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
		</svg>
		Add option
	</button>

	{#if values.length === 0}
		<p class="text-sm text-red-600">At least one option is required</p>
	{/if}
</div>
