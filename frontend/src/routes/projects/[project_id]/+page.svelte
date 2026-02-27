<script lang="ts">
	import MapComponent from '$lib/components/MapComponent.svelte';
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import type { Project } from '$lib/services/Libre311/Libre311';
	import MapBoundaryPolygon from '$lib/components/MapBoundaryPolygon.svelte';

	const libre311 = useLibre311Service();

	let project: Project | undefined;
	let isLoading = true;

	onMount(async () => {
		try {
			const projects = await libre311.getProjects();
			project = projects.find((p) => p.id === Number($page.params.project_id));
		} catch (error) {
			console.error('Failed to load project:', error);
		} finally {
			isLoading = false;
		}
	});
</script>

{#if !isLoading && project}
	<div class="h-full w-full p-2">
		<MapComponent bounds={project.bounds}>
			<MapBoundaryPolygon bounds={project.bounds} color="hsl(var(--primary))" />
		</MapComponent>
	</div>
{:else}
	<div class="flex h-full items-center justify-center">
		<p>Loading map...</p>
	</div>
{/if}
