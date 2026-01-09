<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import { fade, scale } from 'svelte/transition';

	/**
	 * Whether the modal is visible
	 */
	export let open = false;

	/**
	 * Whether a save operation is in progress
	 */
	export let isSaving = false;

	const dispatch = createEventDispatcher<{
		save: void;
		discard: void;
		cancel: void;
	}>();

	function handleSave() {
		dispatch('save');
	}

	function handleDiscard() {
		dispatch('discard');
	}

	function handleCancel() {
		dispatch('cancel');
	}

	function handleKeydown(event: KeyboardEvent) {
		if (event.key === 'Escape' && !isSaving) {
			handleCancel();
		}
	}

	function handleBackdropClick(event: MouseEvent) {
		if (event.target === event.currentTarget && !isSaving) {
			handleCancel();
		}
	}
</script>

<svelte:window on:keydown={handleKeydown} />

{#if open}
	<!-- Backdrop -->
	<div
		class="fixed inset-0 z-50 flex items-center justify-center bg-black/50"
		transition:fade={{ duration: 150 }}
		on:click={handleBackdropClick}
		role="presentation"
	>
		<!-- Modal -->
		<div
			class="mx-4 w-full max-w-md rounded-lg bg-white shadow-xl"
			role="alertdialog"
			aria-modal="true"
			aria-labelledby="unsaved-modal-title"
			aria-describedby="unsaved-modal-description"
			transition:scale={{ duration: 150, start: 0.95 }}
		>
			<!-- Header -->
			<div class="border-b border-gray-200 px-6 py-4">
				<h2 id="unsaved-modal-title" class="text-lg font-semibold text-gray-900">
					Unsaved Changes
				</h2>
			</div>

			<!-- Content -->
			<div class="px-6 py-4">
				<p id="unsaved-modal-description" class="text-gray-600">
					You have unsaved changes. Do you want to save them before leaving?
				</p>
			</div>

			<!-- Footer -->
			<div class="flex justify-end gap-3 border-t border-gray-200 px-6 py-4">
				<button
					type="button"
					class="rounded-md px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
					on:click={handleCancel}
					disabled={isSaving}
				>
					Cancel
				</button>
				<button
					type="button"
					class="rounded-md px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
					on:click={handleDiscard}
					disabled={isSaving}
				>
					Discard
				</button>
				<button
					type="button"
					class="inline-flex items-center gap-2 rounded-md bg-purple-600 px-4 py-2 text-sm font-medium text-white hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
					on:click={handleSave}
					disabled={isSaving}
				>
					{#if isSaving}
						<svg
							class="h-4 w-4 animate-spin"
							xmlns="http://www.w3.org/2000/svg"
							fill="none"
							viewBox="0 0 24 24"
						>
							<circle
								class="opacity-25"
								cx="12"
								cy="12"
								r="10"
								stroke="currentColor"
								stroke-width="4"
							></circle>
							<path
								class="opacity-75"
								fill="currentColor"
								d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
							></path>
						</svg>
						Saving...
					{:else}
						Save
					{/if}
				</button>
			</div>
		</div>
	</div>
{/if}
