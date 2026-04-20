<script lang="ts">
	import { goto } from '$app/navigation';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { useJurisdiction } from '$lib/context/JurisdictionContext';
	import SaveButton from '$lib/components/ServiceDefinitionEditor/Shared/SaveButton.svelte';
	import type { ProjectFeature } from '$lib/services/Libre311/Libre311';

	const { service, alertError, alert, user } = useLibre311Context();
	const jurisdiction = useJurisdiction();

	$: if ($user !== undefined) {
		if (!$user?.permissions.some((p) =>
			['LIBRE311_ADMIN_EDIT-SYSTEM', 'LIBRE311_ADMIN_EDIT-TENANT', 'LIBRE311_ADMIN_EDIT-SUBTENANT'].includes(p))) {
			goto('/');
		}
	}

	let savedFeature: ProjectFeature = $jurisdiction.project_feature ?? 'DISABLED';
	let selectedFeature: ProjectFeature = savedFeature;
	let isSaving = false;

	$: isDirty = selectedFeature !== savedFeature;

	const featureOptions: { value: ProjectFeature; label: string; description: string }[] = [
		{
			value: 'DISABLED',
			label: 'Disabled',
			description:
				'Projects are not available. Service requests cannot be associated with a project.'
		},
		{
			value: 'OPTIONAL',
			label: 'Optional',
			description:
				'Projects are available. Service requests may optionally be associated with a project.'
		},
		{
			value: 'REQUIRED',
			label: 'Required',
			description: 'Projects are available. All service requests must be associated with a project.'
		}
	];

	async function handleSave() {
		isSaving = true;
		try {
			await service.updateJurisdiction({
				name: $jurisdiction.name,
				project_feature: selectedFeature
			});
			savedFeature = selectedFeature;
			jurisdiction.update((j) => ({ ...j, project_feature: selectedFeature }));
			alert({
				type: 'success',
				title: 'Setting saved.',
				description: 'Project feature setting has been updated.'
			});
		} catch (err) {
			alertError(err);
		} finally {
			isSaving = false;
		}
	}

	function handleCancel() {
		selectedFeature = savedFeature;
	}
</script>

<svelte:head>
	<title>System Administration</title>
</svelte:head>

<div class="mx-auto max-w-2xl px-4 py-6">
	<div class="mb-6">
		<h1 class="text-2xl font-semibold text-gray-900">System Administration</h1>
		<p class="mt-1 text-sm text-gray-500">
			Manage system-level settings for <span class="font-medium">{$jurisdiction.name}</span>.
		</p>
	</div>

	<div class="rounded-lg border border-gray-200 bg-white shadow-sm">
		<div class="border-b border-gray-200 px-6 py-4">
			<h2 class="text-base font-semibold text-gray-900">Project Feature</h2>
			<p class="mt-1 text-sm text-gray-500">
				Controls whether service requests can be associated with time-bounded projects.
			</p>
		</div>

		<div class="px-6 py-4">
			<fieldset>
				<legend class="sr-only">Project Feature Setting</legend>
				<div class="space-y-3">
					{#each featureOptions as option}
						<label
							class="flex cursor-pointer items-start gap-3 rounded-md border p-4 transition-colors {selectedFeature ===
							option.value
								? 'border-blue-500 bg-blue-50'
								: 'border-gray-200 hover:border-gray-300 hover:bg-gray-50'}"
						>
							<input
								type="radio"
								name="project_feature"
								value={option.value}
								bind:group={selectedFeature}
								class="mt-0.5 h-4 w-4 border-gray-300 text-blue-600 focus:ring-blue-500"
							/>
							<div>
								<span class="block text-sm font-medium text-gray-900">{option.label}</span>
								<span class="block text-sm text-gray-500">{option.description}</span>
							</div>
						</label>
					{/each}
				</div>
			</fieldset>
		</div>

		<div class="flex items-center justify-end gap-3 border-t border-gray-200 px-6 py-4">
			<button
				type="button"
				class="rounded-md px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
				on:click={handleCancel}
				disabled={!isDirty || isSaving}
			>
				Cancel
			</button>
			<span title={!isDirty ? 'Make changes to enable saving' : ''}>
				<SaveButton disabled={!isDirty} {isSaving} on:click={handleSave} />
			</span>
		</div>
	</div>
</div>
