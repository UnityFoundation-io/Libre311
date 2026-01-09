<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import SaveButton from '../Shared/SaveButton.svelte';

	/**
	 * Whether the attribute is required
	 */
	export let required = false;

	/**
	 * Whether save button should be enabled
	 */
	export let canSave = false;

	/**
	 * Whether a save operation is in progress
	 */
	export let isSaving = false;

	/**
	 * Whether cancel should be enabled (has unsaved changes)
	 */
	export let canCancel = false;

	const dispatch = createEventDispatcher<{
		save: void;
		cancel: void;
		copy: void;
		delete: void;
		requiredChange: boolean;
	}>();

	function handleRequiredToggle() {
		dispatch('requiredChange', !required);
	}
</script>

<div class="flex items-center justify-between border-t border-gray-200 px-4 py-3">
	<!-- Left side: Copy, Delete actions -->
	<div class="flex items-center gap-2">
		<button
			type="button"
			class="flex items-center gap-1 rounded px-2 py-1 text-sm text-gray-600 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-purple-500"
			on:click={() => dispatch('copy')}
			disabled={isSaving}
			aria-label="Copy attribute"
		>
			<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="2"
					d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"
				/>
			</svg>
			<span>Copy</span>
		</button>

		<button
			type="button"
			class="flex items-center gap-1 rounded px-2 py-1 text-sm text-red-600 hover:bg-red-50 focus:outline-none focus:ring-2 focus:ring-red-500"
			on:click={() => dispatch('delete')}
			disabled={isSaving}
			aria-label="Delete attribute"
		>
			<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="2"
					d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
				/>
			</svg>
			<span>Delete</span>
		</button>

		<!-- Divider -->
		<div class="mx-2 h-6 w-px bg-gray-200"></div>

		<!-- Required Toggle -->
		<label class="flex cursor-pointer items-center gap-2">
			<span class="text-sm text-gray-600">Required</span>
			<button
				type="button"
				role="switch"
				aria-checked={required}
				class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 {required
					? 'bg-purple-600'
					: 'bg-gray-200'}"
				on:click={handleRequiredToggle}
				disabled={isSaving}
			>
				<span
					class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out {required
						? 'translate-x-5'
						: 'translate-x-0'}"
				></span>
			</button>
		</label>
	</div>

	<!-- Right side: Cancel, Save buttons -->
	<div class="flex items-center gap-3">
		<button
			type="button"
			class="rounded-md px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
			on:click={() => dispatch('cancel')}
			disabled={!canCancel || isSaving}
		>
			Cancel
		</button>
		<SaveButton disabled={!canSave} {isSaving} on:click={() => dispatch('save')} />
	</div>
</div>
