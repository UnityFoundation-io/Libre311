<script lang="ts">
	import { onMount } from 'svelte';
	import { browser } from '$app/environment';
	import { goto } from '$app/navigation';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { Button, Table } from 'stwui';
	import { plusCircleIcon } from '$lib/components/Svg/outline/plusCircleIcon';
	import { useJurisdiction } from '$lib/context/JurisdictionContext';
	import type { TableColumn } from 'stwui/types';
	import AuthGuard from '$lib/components/AuthGuard.svelte';
	import { shouldShowProject } from '$lib/utils/functions';

	const jurisdiction = useJurisdiction();
	const {
		projects: allProjectsStore,
		user,
		linkResolver,
		fetchProjectsAdmin
	} = useLibre311Context();

	$: if (browser && $jurisdiction.project_feature === 'DISABLED') {
		goto(linkResolver.issuesTable(new URL(window.location.href)));
	}

	onMount(async () => {
		await fetchProjectsAdmin();
	});

	const columns: TableColumn[] = [
		{ column: 'name', label: 'Name', class: 'w-1/4', placement: 'left' },
		{ column: 'start_date', label: 'Start Date', class: 'w-1/6', placement: 'left' },
		{ column: 'end_date', label: 'End Date', class: 'w-1/6', placement: 'left' },
		{ column: 'closed_date', label: 'Closed Date', class: 'w-1/6', placement: 'left' },
		{ column: 'status', label: 'Status', class: 'w-1/6', placement: 'left' },
		{ column: 'request_count', label: 'Requests', class: 'w-1/12', placement: 'left' }
	];

	let showAllClosed = false;
	let currentPage = 1;
	const pageSize = 10;

	$: isAdmin = !!$user?.permissions.some((p) =>
		[
			'LIBRE311_ADMIN_VIEW-SYSTEM',
			'LIBRE311_ADMIN_VIEW-TENANT',
			'LIBRE311_ADMIN_VIEW-SUBTENANT'
		].includes(p)
	);

	$: filteredProjects = $allProjectsStore.filter((project) => {
		if (showAllClosed) return true;
		return shouldShowProject(project, isAdmin);
	});

	$: totalPages = Math.ceil(filteredProjects.length / pageSize);
	$: paginatedProjects = filteredProjects.slice((currentPage - 1) * pageSize, currentPage * pageSize);

	function getClosedDate(project: Project) {
		if (project.status === 'CLOSED') {
			return project.closed_date || project.end_date;
		}
		return null;
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
			<div class="flex items-center gap-4">
				<label class="flex cursor-pointer items-center gap-2">
					<span class="text-sm text-gray-600">Show All Closed</span>
					<button
						type="button"
						role="switch"
						aria-checked={showAllClosed}
						class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 {showAllClosed
							? 'bg-blue-600'
							: 'bg-gray-200'}"
						on:click={() => {
							showAllClosed = !showAllClosed;
							currentPage = 1;
						}}
					>
						<span
							class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out {showAllClosed
								? 'translate-x-5'
								: 'translate-x-0'}"
						></span>
					</button>
				</label>
				<Button variant="primary" on:click={() => goto('/projects/create')}>
					<Button.Leading data={plusCircleIcon} slot="leading" />
					Create Project
				</Button>
			</div>
		</div>

		{#if filteredProjects.length === 0}
			<p>No projects found.</p>
		{:else}
			<Table {columns}>
				<Table.Header slot="header" orderBy="name" />
				<Table.Body slot="body">
					{#each paginatedProjects as project}
						<Table.Body.Row id={project.id.toString()}>
							<Table.Body.Row.Cell column={0}>
								<a href="/projects/{project.id}" class="text-primary hover:underline"
									>{project.name}</a
								>
							</Table.Body.Row.Cell>
							<Table.Body.Row.Cell column={1}>
								{new Date(project.start_date).toLocaleDateString()}
							</Table.Body.Row.Cell>
							<Table.Body.Row.Cell column={2}>
								{new Date(project.end_date).toLocaleDateString()}
							</Table.Body.Row.Cell>
							<Table.Body.Row.Cell column={3}>
								{#if getClosedDate(project)}
									{new Date(getClosedDate(project)).toLocaleDateString()}
								{:else}
									-
								{/if}
							</Table.Body.Row.Cell>
							<Table.Body.Row.Cell column={4}>
								<div
									class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold {project.status ===
									'OPEN'
										? 'bg-green-100 text-green-800'
										: 'bg-red-100 text-red-800'}"
								>
									{project.status}
								</div>
							</Table.Body.Row.Cell>
							<Table.Body.Row.Cell column={5}>
								<div class="ml-2 flex items-center justify-start">
									{project.request_count ?? 0}
								</div>
							</Table.Body.Row.Cell>
						</Table.Body.Row>
					{/each}
				</Table.Body>
				<Table.Footer slot="footer">
					<div class="flex w-full items-center justify-between p-2">
						<span class="text-sm text-gray-600"
							>Showing {(currentPage - 1) * pageSize + 1} to {Math.min(
								currentPage * pageSize,
								filteredProjects.length
							)} of {filteredProjects.length} projects</span
						>
						<div class="flex gap-2">
							<Button
								variant="ghost"
								disabled={currentPage === 1}
								on:click={() => (currentPage -= 1)}>Previous</Button
							>
							<Button
								variant="ghost"
								disabled={currentPage >= totalPages}
								on:click={() => (currentPage += 1)}>Next</Button
							>
						</div>
					</div>
				</Table.Footer>
			</Table>
		{/if}
	</div>
</AuthGuard>
