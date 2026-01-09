<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import { fade, scale } from 'svelte/transition';

	/**
	 * Whether the modal is visible
	 */
	export let open = false;

	/**
	 * The title to display in the modal
	 */
	export let title = 'Confirm Delete';

	/**
	 * The message to display in the modal
	 */
	export let message = 'Are you sure you want to delete this item? This action cannot be undone.';

	/**
	 * Whether a delete operation is in progress
	 */
	export let isDeleting = false;

	const dispatch = createEventDispatcher<{
		confirm: void;
		cancel: void;
	}>();

	function handleConfirm() {
		dispatch('confirm');
	}

	function handleCancel() {
		dispatch('cancel');
	}

	function handleKeydown(event: KeyboardEvent) {
		if (event.key === 'Escape' && !isDeleting) {
			handleCancel();
		}
	}

	function handleBackdropClick(event: MouseEvent) {
		if (event.target === event.currentTarget && !isDeleting) {
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
			aria-labelledby="delete-modal-title"
			aria-describedby="delete-modal-description"
			transition:scale={{ duration: 150, start: 0.95 }}
		>
			<!-- Header -->
			<div class="border-b border-gray-200 px-6 py-4">
				<div class="flex items-center gap-3">
					<!-- Warning Icon -->
					<div class="flex h-10 w-10 items-center justify-center rounded-full bg-red-100">
						<svg class="h-6 w-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
							/>
						</svg>
					</div>
					<h2 id="delete-modal-title" class="text-lg font-semibold text-gray-900">
						{title}
					</h2>
				</div>
			</div>

			<!-- Content -->
			<div class="px-6 py-4">
				<p id="delete-modal-description" class="text-gray-600">
					{message}
				</p>
			</div>

			<!-- Footer -->
			<div class="flex justify-end gap-3 border-t border-gray-200 px-6 py-4">
				<button
					type="button"
					class="rounded-md px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
					on:click={handleCancel}
					disabled={isDeleting}
				>
					Cancel
				</button>
				<button
					type="button"
					class="inline-flex items-center gap-2 rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
					on:click={handleConfirm}
					disabled={isDeleting}
				>
					{#if isDeleting}
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
						Deleting...
					{:else}
						Delete
					{/if}
				</button>
			</div>
		</div>
	</div>
{/if}
