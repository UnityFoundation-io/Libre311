<script lang="ts">
	import ProjectEditor from '$lib/components/ProjectEditor.svelte';
	import AuthGuard from '$lib/components/AuthGuard.svelte';
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import type { Project } from '$lib/services/Libre311/Libre311';

	const { projects: allProjectsStore, fetchProjectsAdmin } = useLibre311Context();
	let project: Project | undefined;
	let isLoading = true;

	onMount(async () => {
		try {
			await fetchProjectsAdmin();
			project = $allProjectsStore.find((p) => p.id === Number($page.params.project_id));
		} catch (error) {
			console.error('Failed to load project:', error);
		} finally {
			isLoading = false;
		}
	});
</script>

<svelte:head>
	<title>Edit Project {project?.name ?? ''}</title>
</svelte:head>

<AuthGuard
	requires={[
		'LIBRE311_ADMIN_EDIT-TENANT',
		'LIBRE311_ADMIN_EDIT-SYSTEM',
		'LIBRE311_ADMIN_EDIT-SUBTENANT'
	]}
>
	{#if isLoading}
		<div class="flex h-full items-center justify-center p-8">
			<p>Loading project details...</p>
		</div>
	{:else if project}
		<div class="h-full text-left">
			<ProjectEditor {project} />
		</div>
	{:else}
		<div class="flex h-full items-center justify-center p-8 text-red-600">
			<p>Project not found.</p>
		</div>
	{/if}
</AuthGuard>
