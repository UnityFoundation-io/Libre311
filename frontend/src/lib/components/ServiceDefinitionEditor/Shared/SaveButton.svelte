<script lang="ts">
	import { createEventDispatcher } from 'svelte';

	/**
	 * Whether the button is disabled
	 */
	export let disabled = false;

	/**
	 * Whether a save operation is in progress
	 */
	export let isSaving = false;

	/**
	 * Button text when not saving
	 */
	export let label = 'Save';

	/**
	 * Button text while saving
	 */
	export let savingLabel = 'Saving...';

	/**
	 * Additional CSS classes
	 */
	let className = '';
	export { className as class };

	const dispatch = createEventDispatcher<{
		click: void;
	}>();

	function handleClick() {
		if (!disabled && !isSaving) {
			dispatch('click');
		}
	}
</script>

<button
	type="button"
	class="inline-flex items-center justify-center gap-2 rounded-md px-4 py-2 text-sm font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 {disabled ||
	isSaving
		? 'cursor-not-allowed bg-gray-200 text-gray-500 focus:ring-gray-300'
		: 'bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-500'} {className}"
	disabled={disabled || isSaving}
	on:click={handleClick}
	aria-busy={isSaving}
>
	{#if isSaving}
		<svg
			class="h-4 w-4 animate-spin"
			xmlns="http://www.w3.org/2000/svg"
			fill="none"
			viewBox="0 0 24 24"
			aria-hidden="true"
		>
			<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"
			></circle>
			<path
				class="opacity-75"
				fill="currentColor"
				d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
			></path>
		</svg>
		<span>{savingLabel}</span>
	{:else}
		<span>{label}</span>
	{/if}
</button>
