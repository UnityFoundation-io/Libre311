<script lang="ts">
	import { goto } from '$app/navigation';
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import type { CreateProjectParams, Project } from '$lib/services/Libre311/Libre311';
	import { Button, Input } from 'stwui';
	import MapComponent from '$lib/components/MapComponent.svelte';
	import BoundaryEditor from '$lib/components/BoundaryEditor.svelte';
	import { useJurisdiction } from '$lib/context/JurisdictionContext';
	import { toDatetimeLocal } from '$lib/utils/functions';
	import { arrowPath } from '$lib/components/Svg/outline/arrowPath';
	import { useLibre311Context } from '$lib/context/Libre311Context';

	const libre311 = useLibre311Service();
	const jurisdiction = useJurisdiction();
	const { fetchProjectsAdmin } = useLibre311Context();

	export let project: Project | undefined = undefined;

	let isEditing = !!project;
	let isSaving = false;

	let currentProject: Partial<Project> = project
		? {
				...project,
				start_date: toDatetimeLocal(project.start_date),
				end_date: toDatetimeLocal(project.end_date)
			}
		: {
				name: '',
				description: '',
				start_date: toDatetimeLocal(new Date()),
				end_date: toDatetimeLocal(new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)),
				bounds: []
			};

	async function closeProject() {
		if (
			confirm('Are you sure you want to close this project? This will mark the study as concluded.')
		) {
			isSaving = true;
			try {
				await libre311.updateProject({
					id: project!.id,
					closed_date: new Date().toISOString()
				});
				await fetchProjectsAdmin();
				goto('/projects');
			} catch (error) {
				console.error('Failed to close project:', error);
				alert('Failed to close project');
			} finally {
				isSaving = false;
			}
		}
	}

	async function reopenProject() {
		isSaving = true;
		try {
			await libre311.updateProject({
				id: project!.id,
				closed_date: null
			});
			await fetchProjectsAdmin();
			goto(`/projects/${project!.id}`);
		} catch (error) {
			console.error('Failed to reopen project:', error);
			alert('Failed to reopen project');
		} finally {
			isSaving = false;
		}
	}

	async function saveProject() {
		isSaving = true;
		try {
			const params: CreateProjectParams = {
				name: currentProject.name!,
				description: currentProject.description,
				bounds: currentProject.bounds!,
				start_date: new Date(currentProject.start_date!).toISOString(),
				end_date: new Date(currentProject.end_date!).toISOString()
			};

			if (isEditing) {
				await libre311.updateProject({ id: project!.id, ...params });
			} else {
				if (!params.bounds || params.bounds.length < 4) {
					alert('Please draw a boundary on the map (at least 4 points).');
					isSaving = false;
					return;
				}
				await libre311.createProject(params);
			}
			goto('/projects');
		} catch (error) {
			console.error('Failed to save project:', error);
			alert('Failed to save project');
		} finally {
			isSaving = false;
		}
	}

	function handleBoundsUpdate(event: CustomEvent<[]>) {
		currentProject.bounds = event.detail;
	}

	$: canReopen =
		isEditing && project?.status === 'CLOSED' && new Date(project.end_date) > new Date();
</script>

<div class="flex h-full flex-col gap-4 p-6">
	<div class="flex items-center justify-between">
		<h1 class="text-2xl font-bold">{isEditing ? 'Edit Project' : 'Create Project'}</h1>
	</div>

	<div class="flex flex-grow flex-col gap-4">
		<div class="flex flex-col gap-4 md:flex-row">
			<div class="flex-1">
				<Input bind:value={currentProject.name}>
					<Input.Label slot="label">Name</Input.Label>
				</Input>
			</div>
			<div class="flex-1">
				<Input bind:value={currentProject.description}>
					<Input.Label slot="label">Description</Input.Label>
				</Input>
			</div>
		</div>
		<div class="flex flex-col gap-4 md:flex-row">
			<div class="flex-1">
				<label class="mb-1 block text-sm font-medium" for="start_date">Start Date</label>
				<input
					id="start_date"
					type="datetime-local"
					class="w-full rounded-md border border-gray-300 p-2"
					bind:value={currentProject.start_date}
				/>
			</div>
			<div class="flex-1">
				<label class="mb-1 block text-sm font-medium" for="end_date">End Date</label>
				<input
					id="end_date"
					type="datetime-local"
					class="w-full rounded-md border border-gray-300 p-2"
					bind:value={currentProject.end_date}
				/>
			</div>
		</div>
		<div class="w-full flex-grow overflow-hidden rounded-md border">
			<MapComponent bounds={$jurisdiction.bounds}>
				<BoundaryEditor
					bounds={currentProject.bounds ?? []}
					constraintBounds={$jurisdiction.bounds}
					on:update={handleBoundsUpdate}
				/>
			</MapComponent>
		</div>
	</div>

	<div class="flex w-full items-center justify-between border-t pt-4">
		<div>
			{#if isEditing && project?.status === 'OPEN'}
				<Button variant="danger" on:click={closeProject} loading={isSaving}>Close Project</Button>
			{/if}
			{#if canReopen}
				<Button variant="ghost" on:click={reopenProject} loading={isSaving}>
					<Button.Leading data={arrowPath} slot="leading" />
					Reopen Project
				</Button>
			{/if}
		</div>
		<div class="flex gap-2">
			<Button on:click={() => goto('/projects')}>Cancel</Button>
			<Button variant="primary" on:click={saveProject} loading={isSaving}>Save Project</Button>
		</div>
	</div>
</div>
