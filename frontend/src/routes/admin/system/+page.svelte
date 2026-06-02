<script lang="ts">
	import { goto } from '$app/navigation';
	import { onMount } from 'svelte';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { useJurisdiction } from '$lib/context/JurisdictionContext';
	import SaveButton from '$lib/components/ServiceDefinitionEditor/Shared/SaveButton.svelte';
	import type { ProjectFeature, Service, ServiceDefinitionAttribute } from '$lib/services/Libre311/Libre311';
	import { PHOTO_VOICE_SERVICE_NAME, SYSTEM_RESERVED_GROUP_NAME } from '$lib/constants/photoVoice';

	const { service, alertError, alert, user } = useLibre311Context();
	const jurisdiction = useJurisdiction();

	$: if ($user !== undefined) {
		if (
			!$user?.permissions.some((p) =>
				[
					'LIBRE311_ADMIN_EDIT-SYSTEM',
					'LIBRE311_ADMIN_EDIT-TENANT',
					'LIBRE311_ADMIN_EDIT-SUBTENANT'
				].includes(p)
			)
		) {
			goto('/');
		}
	}

	// Photo Voice
	const DEFAULT_DESCRIPTION = '';
	const DEFAULT_QUESTION = 'What would you like to share about this location?';

	let photoVoiceService: Service | undefined;
	let photoVoiceAttribute: ServiceDefinitionAttribute | undefined;
	let photoVoiceLoading = true;
	let pvEnabling = false;
	let pvSavingName = false;
	let pvSavingDescription = false;
	let pvSavingQuestion = false;
	let pvDisabling = false;

	let nameText = PHOTO_VOICE_SERVICE_NAME;
	let savedNameText = PHOTO_VOICE_SERVICE_NAME;
	let descriptionText = DEFAULT_DESCRIPTION;
	let savedDescriptionText = DEFAULT_DESCRIPTION;
	let questionText = DEFAULT_QUESTION;
	let savedQuestionText = DEFAULT_QUESTION;

	$: nameDirty = nameText !== savedNameText;
	$: descriptionDirty = descriptionText !== savedDescriptionText;
	$: questionDirty = questionText !== savedQuestionText;

	onMount(async () => {
		try {
			const photoVoiceCode = $jurisdiction.photo_voice_service_code;
			const services = await service.getServiceList();
			photoVoiceService = photoVoiceCode
				? services.find((s) => s.service_code === photoVoiceCode)
				: undefined;
			if (photoVoiceService) {
				nameText = photoVoiceService.service_name;
				savedNameText = photoVoiceService.service_name;
				descriptionText = photoVoiceService.description ?? DEFAULT_DESCRIPTION;
				savedDescriptionText = descriptionText;
				const def = await service.getServiceDefinition({
					service_code: photoVoiceService.service_code
				});
				photoVoiceAttribute = def.attributes.find((a) => a.datatype === 'text');
				if (photoVoiceAttribute) {
					questionText = photoVoiceAttribute.description;
					savedQuestionText = photoVoiceAttribute.description;
				}
			}
		} catch (err) {
			alertError(err);
		} finally {
			photoVoiceLoading = false;
		}
	});

	async function enablePhotoVoice() {
		pvEnabling = true;
		try {
			const groups = await service.getGroupList();
			const reservedGroup = groups.find((g) => g.name === SYSTEM_RESERVED_GROUP_NAME);
			if (!reservedGroup) {
				alertError(new Error('"System Reserved" service group not found. Run pending migrations.'));
				return;
			}
			const created = await service.createService({
				service_name: nameText,
				group_id: reservedGroup.id
			});
			const updatedService = await service.editService({
				service_code: created.service_code,
				service_name: nameText,
				description: descriptionText
			});
			savedNameText = nameText;
			savedDescriptionText = descriptionText;
			const attrResponse = await service.createAttribute({
				service_code: created.service_code,
				description: questionText,
				datatype: 'text',
				datatype_description: '',
				variable: true,
				required: true,
				order: 1
			});
			photoVoiceAttribute = attrResponse.attributes.find((a) => a.datatype === 'text');
			savedQuestionText = questionText;
			photoVoiceService = { ...created, description: updatedService.description };
			await service.updateJurisdiction({
				name: $jurisdiction.name,
				photo_voice_service_code: created.service_code
			});
			jurisdiction.update((j) => ({ ...j, photo_voice_service_code: created.service_code }));
			alert({ type: 'success', title: 'Photo Voice enabled.', description: '' });
		} catch (err) {
			alertError(err);
		} finally {
			pvEnabling = false;
		}
	}

	async function saveName() {
		if (!photoVoiceService) return;
		pvSavingName = true;
		try {
			const updated = await service.editService({
				service_code: photoVoiceService.service_code,
				service_name: nameText,
				description: savedDescriptionText
			});
			photoVoiceService = { ...photoVoiceService, service_name: updated.service_name };
			savedNameText = nameText;
			alert({ type: 'success', title: 'Name updated.', description: '' });
		} catch (err) {
			alertError(err);
		} finally {
			pvSavingName = false;
		}
	}

	async function saveDescription() {
		if (!photoVoiceService) return;
		pvSavingDescription = true;
		try {
			const updated = await service.editService({
				service_code: photoVoiceService.service_code,
				service_name: savedNameText,
				description: descriptionText
			});
			photoVoiceService = { ...photoVoiceService, description: updated.description };
			savedDescriptionText = descriptionText;
			alert({ type: 'success', title: 'Description updated.', description: '' });
		} catch (err) {
			alertError(err);
		} finally {
			pvSavingDescription = false;
		}
	}

	async function saveQuestion() {
		if (!photoVoiceService) return;
		if (!photoVoiceAttribute) {
			alertError(new Error('Question attribute not found. Try disabling and re-enabling Photo Voice.'));
			return;
		}
		pvSavingQuestion = true;
		try {
			const res = await service.editAttribute({
				attribute_code: photoVoiceAttribute.code,
				service_code: photoVoiceService.service_code,
				description: questionText,
				datatype_description: photoVoiceAttribute.datatype_description ?? '',
				required: photoVoiceAttribute.required
			});
			photoVoiceAttribute = res.attributes.find((a) => a.datatype === 'text');
			savedQuestionText = questionText;
			alert({ type: 'success', title: 'Question updated.', description: '' });
		} catch (err) {
			alertError(err);
		} finally {
			pvSavingQuestion = false;
		}
	}

	async function disablePhotoVoice() {
		if (!photoVoiceService) return;
		pvDisabling = true;
		try {
			await service.deleteService({ service_code: photoVoiceService.service_code });
			await service.updateJurisdiction({
				name: $jurisdiction.name,
				photo_voice_service_code: 0
			});
			jurisdiction.update((j) => ({ ...j, photo_voice_service_code: null }));
			photoVoiceService = undefined;
			photoVoiceAttribute = undefined;
			nameText = PHOTO_VOICE_SERVICE_NAME;
			savedNameText = PHOTO_VOICE_SERVICE_NAME;
			descriptionText = DEFAULT_DESCRIPTION;
			savedDescriptionText = DEFAULT_DESCRIPTION;
			questionText = DEFAULT_QUESTION;
			savedQuestionText = DEFAULT_QUESTION;
			alert({ type: 'success', title: 'Photo Voice disabled.', description: '' });
		} catch (err) {
			alertError(err);
		} finally {
			pvDisabling = false;
		}
	}

	let savedFeature: ProjectFeature = $jurisdiction.project_feature ?? 'DISABLED';
	let selectedFeature: ProjectFeature = savedFeature;
	let savedShowProjectBoundaries: boolean = $jurisdiction.show_project_boundaries ?? true;
	let showProjectBoundaries: boolean = savedShowProjectBoundaries;
	let savedShowExitProjectMode: boolean = $jurisdiction.show_exit_project_mode ?? true;
	let showExitProjectMode: boolean = savedShowExitProjectMode;
	let isSaving = false;

	$: isDirty =
		selectedFeature !== savedFeature ||
		showProjectBoundaries !== savedShowProjectBoundaries ||
		showExitProjectMode !== savedShowExitProjectMode;

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
				project_feature: selectedFeature,
				show_project_boundaries: showProjectBoundaries,
				show_exit_project_mode: showExitProjectMode
			});
			savedFeature = selectedFeature;
			savedShowProjectBoundaries = showProjectBoundaries;
			savedShowExitProjectMode = showExitProjectMode;
			jurisdiction.update((j) => ({
				...j,
				project_feature: selectedFeature,
				show_project_boundaries: showProjectBoundaries,
				show_exit_project_mode: showExitProjectMode
			}));
			alert({ type: 'success', title: 'Setting saved.', description: 'Project settings have been updated.' });
		} catch (err) {
			alertError(err);
		} finally {
			isSaving = false;
		}
	}

	function handleCancel() {
		selectedFeature = savedFeature;
		showProjectBoundaries = savedShowProjectBoundaries;
		showExitProjectMode = savedShowExitProjectMode;
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

		{#if selectedFeature !== 'DISABLED'}
			<div class="space-y-1 border-t border-gray-200 px-6 py-4">
				<div class="flex items-center justify-between py-2">
					<div>
						<span class="block text-sm font-medium text-gray-900">Show project boundaries on map</span>
						<span class="block text-sm text-gray-500">Open project boundaries are visible to all users on the map when outside of a project.</span>
					</div>
					<button
						type="button"
						role="switch"
						aria-checked={showProjectBoundaries}
						class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 {showProjectBoundaries ? 'bg-blue-600' : 'bg-gray-200'}"
						on:click={() => (showProjectBoundaries = !showProjectBoundaries)}
					>
						<span class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out {showProjectBoundaries ? 'translate-x-5' : 'translate-x-0'}" />
					</button>
				</div>
				<div class="flex items-center justify-between py-2">
					<div>
						<span class="block text-sm font-medium text-gray-900">Show "Exit Project Mode"</span>
						<span class="block text-sm text-gray-500">Users in project mode see a menu item to return to the main map.</span>
					</div>
					<button
						type="button"
						role="switch"
						aria-checked={showExitProjectMode}
						class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 {showExitProjectMode ? 'bg-blue-600' : 'bg-gray-200'}"
						on:click={() => (showExitProjectMode = !showExitProjectMode)}
					>
						<span class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out {showExitProjectMode ? 'translate-x-5' : 'translate-x-0'}" />
					</button>
				</div>
			</div>
		{/if}

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

	<div class="mt-6 rounded-lg border border-gray-200 bg-white shadow-sm">
		<div class="border-b border-gray-200 px-6 py-4">
			<h2 class="text-base font-semibold text-gray-900">Photo Voice</h2>
			<p class="mt-1 text-sm text-gray-500">
				Enables a dedicated submission flow for community photo voice submissions.
			</p>
		</div>

		{#if photoVoiceLoading}
			<div class="px-6 py-4">
				<p class="text-sm text-gray-500">Loading...</p>
			</div>
		{:else}
			<div class="space-y-5 px-6 py-4">
				<div>
					<label for="pv-name" class="block text-sm font-medium text-gray-700">Name</label>
					<p class="mb-1 text-xs text-gray-500">
						Shown in service request lists and submissions.
					</p>
					<input
						id="pv-name"
						type="text"
						bind:value={nameText}
						class="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
					/>
					{#if photoVoiceService && nameDirty}
						<div class="mt-1 flex justify-end">
							<button
								type="button"
								class="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
								disabled={pvSavingName}
								on:click={saveName}
							>
								{pvSavingName ? 'Saving...' : 'Save Name'}
							</button>
						</div>
					{/if}
				</div>

				<div>
					<label for="pv-description" class="block text-sm font-medium text-gray-700"
						>Description</label
					>
					<p class="mb-1 text-xs text-gray-500">
						Shown above the question to provide context to citizens.
					</p>
					<textarea
						id="pv-description"
						bind:value={descriptionText}
						rows="2"
						class="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
					></textarea>
					{#if photoVoiceService && descriptionDirty}
						<div class="mt-1 flex justify-end">
							<button
								type="button"
								class="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
								disabled={pvSavingDescription}
								on:click={saveDescription}
							>
								{pvSavingDescription ? 'Saving...' : 'Save Description'}
							</button>
						</div>
					{/if}
				</div>

				<div>
					<label for="pv-question" class="block text-sm font-medium text-gray-700">Question</label>
					<p class="mb-1 text-xs text-gray-500">The prompt citizens respond to.</p>
					<textarea
						id="pv-question"
						bind:value={questionText}
						rows="2"
						class="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
					></textarea>
					{#if photoVoiceService && questionDirty}
						<div class="mt-1 flex justify-end">
							<button
								type="button"
								class="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
								disabled={pvSavingQuestion}
								on:click={saveQuestion}
							>
								{pvSavingQuestion ? 'Saving...' : 'Save Question'}
							</button>
						</div>
					{/if}
				</div>
			</div>

			<div class="flex items-center justify-between border-t border-gray-200 px-6 py-4">
				{#if photoVoiceService}
					<span class="text-sm text-gray-700">
						Status: <span class="font-medium text-green-700">Enabled</span>
					</span>
					<button
						type="button"
						class="rounded-md bg-red-50 px-4 py-2 text-sm font-medium text-red-700 hover:bg-red-100 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
						disabled={pvDisabling}
						on:click={disablePhotoVoice}
					>
						{pvDisabling ? 'Disabling...' : 'Disable'}
					</button>
				{:else}
					<span class="text-sm text-gray-700">
						Status: <span class="font-medium text-gray-400">Disabled</span>
					</span>
					<button
						type="button"
						class="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
						disabled={pvEnabling}
						on:click={enablePhotoVoice}
					>
						{pvEnabling ? 'Enabling...' : 'Enable'}
					</button>
				{/if}
			</div>
		{/if}
	</div>
</div>
