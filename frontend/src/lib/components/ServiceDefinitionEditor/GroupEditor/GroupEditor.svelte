<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { Group } from '$lib/services/Libre311/Libre311';
	import SaveButton from '../Shared/SaveButton.svelte';
	import ConfirmDeleteModal from '../Shared/ConfirmDeleteModal.svelte';

	/**
	 * The group being edited
	 */
	export let group: Group;

	/**
	 * Whether a save operation is in progress
	 */
	export let isSaving = false;

	/**
	 * Whether a delete operation is in progress
	 */
	export let isDeleting = false;

	/**
	 * Whether the group can be deleted (empty groups only)
	 */
	export let canDelete = true;

	/**
	 * Number of services in this group (for delete warning)
	 */
	export let serviceCount = 0;

	const dispatch = createEventDispatcher<{
		save: { name: string };
		delete: void;
		dirty: { isDirty: boolean };
	}>();

	// Local form state
	let editedName = group.name;
	let showDeleteModal = false;

	// Track dirty state - only dispatch when value actually changes
	let previousIsDirty = false;
	$: isDirty = editedName !== group.name;
	$: if (isDirty !== previousIsDirty) {
		previousIsDirty = isDirty;
		dispatch('dirty', { isDirty });
	}

	// Reset when group changes
	$: if (group) {
		editedName = group.name;
	}

	function handleSave() {
		if (!isDirty || !editedName.trim()) return;
		dispatch('save', { name: editedName.trim() });
	}

	function handleCancel() {
		editedName = group.name;
	}

	function handleDelete() {
		if (!canDelete) return;
		showDeleteModal = true;
	}

	function handleConfirmDelete() {
		showDeleteModal = false;
		dispatch('delete');
	}

	function handleCancelDelete() {
		showDeleteModal = false;
	}

	function handleKeydown(event: KeyboardEvent) {
		if (event.key === 'Enter' && !event.shiftKey) {
			event.preventDefault();
			handleSave();
		}
		if (event.key === 'Escape') {
			handleCancel();
		}
	}
</script>

<div class="space-y-6">
	<!-- Group Header Card -->
	<div class="overflow-hidden rounded-lg border border-gray-200 bg-white shadow-sm">
		<!-- Blue top border -->
		<div class="h-1 bg-blue-600"></div>

		<div class="p-6">
			<h3 class="mb-4 text-lg font-semibold text-gray-900">Edit Group</h3>

			<!-- Group Name Field -->
			<div class="space-y-2">
				<label for="group-name" class="block text-sm font-medium text-gray-700"> Group Name </label>
				<input
					id="group-name"
					type="text"
					bind:value={editedName}
					on:keydown={handleKeydown}
					class="block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
					placeholder="Enter group name"
				/>
			</div>

			<!-- Action Buttons -->
			<div class="mt-6 flex items-center justify-between">
				<div class="flex items-center gap-3">
					<SaveButton disabled={!isDirty || !editedName.trim()} {isSaving} on:click={handleSave} />
					{#if isDirty}
						<button
							type="button"
							class="rounded-md px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100"
							on:click={handleCancel}
						>
							Cancel
						</button>
					{/if}
				</div>

				<button
					type="button"
					class="flex items-center gap-2 rounded-md px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-50"
					disabled={!canDelete || isDeleting}
					on:click={handleDelete}
					title={!canDelete ? 'Cannot delete group with services' : 'Delete group'}
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
						<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
							/>
						</svg>
						Delete Group
					{/if}
				</button>
			</div>

			{#if !canDelete && serviceCount > 0}
				<p class="mt-3 text-sm text-amber-600">
					This group has {serviceCount} service{serviceCount === 1 ? '' : 's'}. Move or delete all
					services before deleting the group.
				</p>
			{/if}
		</div>
	</div>
</div>

<!-- Delete Confirmation Modal -->
<ConfirmDeleteModal
	open={showDeleteModal}
	title="Delete Group"
	message="Are you sure you want to delete this group? This action cannot be undone."
	{isDeleting}
	on:confirm={handleConfirmDelete}
	on:cancel={handleCancelDelete}
/>
