<script lang="ts">
	import { onMount } from 'svelte';
	import { browser } from '$app/environment';
	import { goto } from '$app/navigation';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';
	import type { Project } from '$lib/services/Libre311/Libre311';
	import { Button, Table } from 'stwui';
	import { plusCircleIcon } from '$lib/components/Svg/outline/plusCircleIcon';
	import { useJurisdiction } from '$lib/context/JurisdictionContext';
	import type { TableColumn } from 'stwui/types';
	import AuthGuard from '$lib/components/AuthGuard.svelte';

	const libre311 = useLibre311Service();
	const jurisdiction = useJurisdiction();
	const linkResolver = useLibre311Context().linkResolver;

	$: if (browser && $jurisdiction.project_feature === 'DISABLED') {
		goto(linkResolver.issuesTable(new URL(window.location.href)));
	}

	const columns: TableColumn[] = [
		{ column: 'name', label: 'Name', class: 'w-1/4', placement: 'left' },
		{ column: 'start_date', label: 'Start Date', class: 'w-1/5', placement: 'left' },
		{ column: 'end_date', label: 'End Date', class: 'w-1/5', placement: 'left' },
		{ column: 'status', label: 'Status', class: 'w-1/5', placement: 'left' },
		{ column: 'request_count', label: 'Requests', class: 'w-1/6', placement: 'left' }
	];

	let projects: Project[] = [];
	let isLoading = true;

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
</script>

<AuthGuard
	requires={[
		'LIBRE311_ADMIN_VIEW-TENANT',
		'LIBRE311_ADMIN_VIEW-SYSTEM',
		'LIBRE311_ADMIN_VIEW-SUBTENANT'
	]}
>
	<div class="p-6">
		<div class="mb-6 flex items-center justify-between">
			<h1 class="text-2xl font-bold">Project Administration</h1>
			<Button variant="primary" on:click={() => goto('/projects/create')}>
				<Button.Leading data={plusCircleIcon} slot="leading" />
				Create Project
			</Button>
		</div>

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
							<Table.Body.Row.Cell column={0}>
								<a href="/projects/{project.id}" class="text-primary hover:underline"
									>{project.name}</a
								>
							</Table.Body.Row.Cell>
							<Table.Body.Row.Cell column={1}
								>{new Date(project.start_date).toLocaleString()}</Table.Body.Row.Cell
							>
							<Table.Body.Row.Cell column={2}
								>{new Date(project.end_date).toLocaleString()}</Table.Body.Row.Cell
							>
							<Table.Body.Row.Cell column={3}>
								<div
									class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold {project.status ===
									'OPEN'
										? 'bg-green-100 text-green-800'
										: 'bg-red-100 text-red-800'}"
								>
									{project.status}
								</div>
							</Table.Body.Row.Cell>
							<Table.Body.Row.Cell column={4}>
								<div class="ml-2 flex items-center justify-start">
									{project.request_count ?? 0}
								</div>
							</Table.Body.Row.Cell>
						</Table.Body.Row>
					{/each}
				</Table.Body>
			</Table>
		{/if}
	</div>
</AuthGuard>
