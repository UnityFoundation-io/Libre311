<script lang="ts">
	import { onMount } from 'svelte';
	import { browser } from '$app/environment';
	import { goto } from '$app/navigation';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';
	import type { CreateProjectParams, Project } from '$lib/services/Libre311/Libre311';
	import { Button, Card, Input, Table, Modal, Portal } from 'stwui';
	import { plusCircleIcon } from '$lib/components/Svg/outline/plusCircleIcon';
	import { pencilIcon } from '$lib/components/Svg/outline/pencilIcon';
	import { trashIcon } from '$lib/components/Svg/outline/trashIcon';
	import MapComponent from '$lib/components/MapComponent.svelte';
	import BoundaryEditor from '$lib/components/BoundaryEditor.svelte';
	import { useJurisdiction } from '$lib/context/JurisdictionContext';
	import type { TableColumn } from 'stwui/types';

	const libre311 = useLibre311Service();
	const jurisdiction = useJurisdiction();
	const linkResolver = useLibre311Context().linkResolver;

	$: if (browser && $jurisdiction.project_feature === 'DISABLED') {
		goto(linkResolver.issuesTable(new URL(window.location.href)));
	}

	const columns: TableColumn[] = [
		{ column: 'name', label: 'Name', class: 'w-1/3', placement: 'left' },
		{ column: 'start_date', label: 'Start Date', class: 'w-1/4', placement: 'left' },
		{ column: 'end_date', label: 'End Date', class: 'w-1/4', placement: 'left' },
		{ column: 'actions', label: 'Actions', class: 'w-1/6', placement: 'center' }
	];

	let projects: Project[] = [];
	let isLoading = true;
	let isModalOpen = false;
	let isEditing = false;
	let isSaving = false;

	let currentProject: Partial<Project> = {
		name: '',
		description: '',
		start_date: '',
		end_date: '',
		bounds: []
	};

	/**
	 * Converts a date object or string into the format required by a datetime-local input,
	 * which is `YYYY-MM-DDTHH:MM`.
	 * @param {Date | string} dateValue - The date to format.
	 * @returns {string} The formatted string for the input value.
	 */
	function toDatetimeLocal(dateValue: Date | string): string {
		if (!dateValue) return '';
		const date = new Date(dateValue);
		// Adjust for timezone offset to display local time in the input
		date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
		return date.toISOString().slice(0, 16);
	}

	onMount(async () => {
		await loadProjects();
	});

	async function loadProjects() {
		isLoading = true;
		try {
			projects = await libre311.getProjects();
		} catch (error) {
			console.error('Failed to load projects:', error);
		} finally {
			isLoading = false;
		}
	}

	function openCreateModal() {
		isEditing = false;
		const startDate = new Date();
		const endDate = new Date(startDate.getTime() + 30 * 24 * 60 * 60 * 1000);

		currentProject = {
			name: '',
			description: '',
			start_date: toDatetimeLocal(startDate),
			end_date: toDatetimeLocal(endDate),
			bounds: []
		};
		isModalOpen = true;
	}

	function openEditModal(project: Project) {
		isEditing = true;
		currentProject = {
			...project,
			start_date: toDatetimeLocal(project.start_date),
			end_date: toDatetimeLocal(project.end_date)
		};
		isModalOpen = true;
	}

	function handleClose() {
		isModalOpen = false;
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
				await libre311.updateProject({ id: currentProject.id!, ...params });
			} else {
				if (!params.bounds || params.bounds.length < 4) {
					alert('Please draw a boundary on the map (at least 4 points).');
					isSaving = false;
					return;
				}
				await libre311.createProject(params);
			}
			isModalOpen = false;
			await loadProjects();
		} catch (error) {
			console.error('Failed to save project:', error);
			alert('Failed to save project');
		} finally {
			isSaving = false;
		}
	}

	async function deleteProject(id: number) {
		if (confirm('Are you sure you want to delete this project?')) {
			try {
				await libre311.deleteProject(id);
				await loadProjects();
			} catch (error) {
				console.error('Failed to delete project:', error);
				alert('Failed to delete project');
			}
		}
	}

	function handleBoundsUpdate(event: CustomEvent<[]>) {
		if (currentProject) {
			currentProject.bounds = event.detail;
		}
	}
</script>

<svelte:head>
	<title>Project Management</title>
</svelte:head>

<div class="p-6">
	<div class="mb-6 flex items-center justify-between">
		<h1 class="text-2xl font-bold">Project Management</h1>
		<Button variant="primary" on:click={openCreateModal}>
			<Button.Leading data={plusCircleIcon} slot="leading" />
			Create Project
		</Button>
	</div>

	<Card>
		<Card.Content slot="content">
			{#if isLoading}
				<p>Loading projects...</p>
			{:else if projects.length === 0}
				<p>No projects found.</p>
			{:else}
				<Table {columns}>
					<Table.Header slot="header" orderBy="name" />
					<Table.Body slot="body">
						{#each projects as project}
							<Table.Body.Row id={project.id.toString()}>
								<Table.Body.Row.Cell column={0}>{project.name}</Table.Body.Row.Cell>
								<Table.Body.Row.Cell column={1}
									>{new Date(project.start_date).toLocaleString()}</Table.Body.Row.Cell
								>
								<Table.Body.Row.Cell column={2}
									>{new Date(project.end_date).toLocaleString()}</Table.Body.Row.Cell
								>
								<Table.Body.Row.Cell column={3}>
									<div class="flex gap-2">
										<Button variant="ghost" on:click={() => openEditModal(project)}>
											<Button.Icon data={pencilIcon} slot="icon" />
										</Button>
										<Button variant="ghost" on:click={() => deleteProject(project.id)}>
											<Button.Icon data={trashIcon} slot="icon" />
										</Button>
									</div>
								</Table.Body.Row.Cell>
							</Table.Body.Row>
						{/each}
					</Table.Body>
				</Table>
			{/if}
		</Card.Content>
	</Card>
</div>

{#if isModalOpen}
	<Portal>
		<Modal {handleClose}>
			<Modal.Content slot="content" class="w-11/12 max-w-7xl">
				<Modal.Content.Header slot="header">
					{isEditing ? 'Edit Project' : 'Create Project'}
				</Modal.Content.Header>
				<Modal.Content.Body slot="body">
					<div class="flex flex-col gap-4 p-4">
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
						<div class="h-96 w-full">
							<MapComponent bounds={$jurisdiction.bounds}>
								<BoundaryEditor
									bounds={currentProject.bounds ?? []}
									constraintBounds={$jurisdiction.bounds}
									on:update={handleBoundsUpdate}
								/>
							</MapComponent>
						</div>
					</div>
				</Modal.Content.Body>
				<Modal.Content.Footer slot="footer">
					<div class="flex w-full justify-end gap-2">
						<Button on:click={handleClose}>Cancel</Button>
						<Button variant="primary" on:click={saveProject} loading={isSaving}>Save</Button>
					</div>
				</Modal.Content.Footer>
			</Modal.Content>
		</Modal>
	</Portal>
{/if}
