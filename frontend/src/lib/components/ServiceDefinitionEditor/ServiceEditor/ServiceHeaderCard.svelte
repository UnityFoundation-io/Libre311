<script lang="ts">
	import { createEventDispatcher, onMount } from 'svelte';
	import type { Service } from '$lib/services/Libre311/Libre311';
	import SaveButton from '../Shared/SaveButton.svelte';
	import { handleEditorKeydown } from '../utils/keyboard';

	/**
	 * The service being edited
	 */
	export let service: Service;

	/**
	 * Whether a save operation is in progress
	 */
	export let isSaving = false;

	const dispatch = createEventDispatcher<{
		save: { serviceName: string; description: string };
		cancel: void;
		dirty: { isDirty: boolean; serviceName: string; description: string };
	}>();

	// Local form state
	let serviceName = '';
	let description = '';

	// Track original values for dirty detection
	let originalName = '';
	let originalDescription = '';

	// Track service code to detect actual prop changes
	let lastServiceCode: number | null = null;

	// Initialize form values when service prop changes (but only if it's a different service)
	$: if (service && service.service_code !== lastServiceCode) {
		lastServiceCode = service.service_code;
		initializeForm(service);
	}

	function initializeForm(svc: Service) {
		serviceName = svc.service_name;
		description = svc.description ?? '';
		originalName = svc.service_name;
		originalDescription = svc.description ?? '';
	}

	// Compute dirty state
	$: isDirty = serviceName !== originalName || description !== originalDescription;

	// Notify parent of dirty state changes (include pending values)
	$: dispatch('dirty', { isDirty, serviceName, description });

	// Validation
	$: isValid = serviceName.trim().length > 0;
	$: canSave = isDirty && isValid && !isSaving;

	function handleSave() {
		if (canSave) {
			dispatch('save', {
				serviceName: serviceName.trim(),
				description: description.trim()
			});
		}
	}

	function handleCancel() {
		// Revert to original values
		serviceName = originalName;
		description = originalDescription;
		dispatch('cancel');
	}

	function onKeydown(event: KeyboardEvent) {
		handleEditorKeydown(event, {
			onSave: handleSave,
			onCancel: handleCancel,
			canSave,
			isDirty
		});
	}

	/**
	 * Reset the form to saved values (called after successful save)
	 */
	export function resetToSaved(savedService: Service) {
		originalName = savedService.service_name;
		originalDescription = savedService.description ?? '';
		serviceName = originalName;
		description = originalDescription;
	}

	let nameInput: HTMLInputElement;

	onMount(() => {
		// Focus the name input when the component mounts
		nameInput?.focus();
	});
</script>

<svelte:window on:keydown={onKeydown} />

<div class="rounded-lg border border-gray-200 bg-white shadow-sm">
	<!-- Blue top border indicator -->
	<div class="h-1 rounded-t-lg bg-blue-600"></div>

	<div class="p-6">
		<!-- Service Name Field -->
		<div class="mb-4">
			<label for="service-name" class="mb-1 block text-sm font-medium text-gray-700">
				Service Name <span class="text-red-500">*</span>
			</label>
			<input
				bind:this={nameInput}
				id="service-name"
				type="text"
				bind:value={serviceName}
				class="w-full rounded-md border border-gray-300 px-3 py-2 text-gray-900 placeholder-gray-400 focus:border-blue-500 focus:outline-none"
				class:border-danger={!isValid && serviceName !== originalName}
				class:focus:border-danger={!isValid && serviceName !== originalName}
				placeholder="Enter service name"
				disabled={isSaving}
				aria-required="true"
				aria-invalid={!isValid && serviceName !== originalName}
			/>
			{#if !isValid && serviceName !== originalName}
				<p class="mt-1 text-sm text-danger" role="alert">Service name is required</p>
			{/if}
		</div>

		<!-- Description Field -->
		<div class="mb-6">
			<label for="service-description" class="mb-1 block text-sm font-medium text-gray-700">
				Description
			</label>
			<textarea
				id="service-description"
				bind:value={description}
				rows="3"
				class="w-full resize-none rounded-md border border-gray-300 px-3 py-2 text-gray-900 placeholder-gray-400 focus:border-blue-500 focus:outline-none"
				placeholder="Enter a description for this service"
				disabled={isSaving}
			></textarea>
		</div>

		<!-- Action Buttons -->
		<div class="flex items-center justify-end gap-3">
			<button
				type="button"
				class="rounded-md px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
				on:click={handleCancel}
				disabled={!isDirty || isSaving}
			>
				Cancel
			</button>
			<SaveButton disabled={!canSave} {isSaving} on:click={handleSave} />
		</div>
	</div>
</div>
