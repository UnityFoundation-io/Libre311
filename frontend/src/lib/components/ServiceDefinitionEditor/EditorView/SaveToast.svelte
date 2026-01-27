<script lang="ts">
	import { createEventDispatcher, onDestroy } from 'svelte';
	import { fly } from 'svelte/transition';
	import { saveStatus, clearSaveStatus } from '../stores/editorStore';
	import type { SaveStatus } from '../types';

	/**
	 * Auto-dismiss timeout in milliseconds for success messages
	 */
	export let autoDismissMs = 3000;

	/**
	 * Whether to show the toast
	 */
	export let visible = true;

	const dispatch = createEventDispatcher<{
		retry: void;
	}>();

	let dismissTimeout: ReturnType<typeof setTimeout> | null = null;
	let currentStatus: SaveStatus = { type: 'idle' };

	// Subscribe to save status changes
	const unsubscribe = saveStatus.subscribe((status) => {
		currentStatus = status;

		// Clear any existing timeout
		if (dismissTimeout) {
			clearTimeout(dismissTimeout);
			dismissTimeout = null;
		}

		// Auto-dismiss success messages
		if (status.type === 'success' && autoDismissMs > 0) {
			dismissTimeout = setTimeout(() => {
				clearSaveStatus();
			}, autoDismissMs);
		}
	});

	onDestroy(() => {
		unsubscribe();
		if (dismissTimeout) {
			clearTimeout(dismissTimeout);
		}
	});

	function handleDismiss() {
		clearSaveStatus();
	}

	function handleRetry() {
		dispatch('retry');
	}

	$: isVisible = visible && currentStatus.type !== 'idle';
	$: isSuccess = currentStatus.type === 'success';
	$: isError = currentStatus.type === 'error';
	$: isSaving = currentStatus.type === 'saving';
	$: message =
		currentStatus.type === 'success'
			? currentStatus.message || 'Changes saved'
			: currentStatus.type === 'error'
				? currentStatus.message
				: 'Saving...';
</script>

{#if isVisible}
	<div
		class="fixed bottom-6 left-1/2 z-50 -translate-x-1/2 transform"
		role="status"
		aria-live="polite"
		aria-atomic="true"
		transition:fly={{ y: 50, duration: 200 }}
	>
		<div
			class="flex items-center gap-3 rounded-lg px-4 py-3 shadow-lg {isSuccess
				? 'bg-green-800 text-white'
				: isError
					? 'bg-red-800 text-white'
					: 'bg-gray-800 text-white'}"
		>
			<!-- Icon -->
			{#if isSaving}
				<svg
					class="h-5 w-5 animate-spin"
					xmlns="http://www.w3.org/2000/svg"
					fill="none"
					viewBox="0 0 24 24"
				>
					<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"
					></circle>
					<path
						class="opacity-75"
						fill="currentColor"
						d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
					></path>
				</svg>
			{:else if isSuccess}
				<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M5 13l4 4L19 7"
					/>
				</svg>
			{:else if isError}
				<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
					/>
				</svg>
			{/if}

			<!-- Message -->
			<span class="text-sm font-medium">{message}</span>

			<!-- Actions -->
			{#if isError}
				<button
					type="button"
					class="ml-2 rounded bg-white/20 px-2 py-1 text-xs font-medium hover:bg-white/30"
					on:click={handleRetry}
				>
					Retry
				</button>
			{/if}

			<!-- Dismiss button -->
			{#if !isSaving}
				<button
					type="button"
					class="ml-1 rounded p-1 hover:bg-white/20"
					on:click={handleDismiss}
					aria-label="Dismiss notification"
				>
					<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M6 18L18 6M6 6l12 12"
						/>
					</svg>
				</button>
			{/if}
		</div>
	</div>
{/if}
