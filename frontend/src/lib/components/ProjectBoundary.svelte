<script lang="ts">
	import { onMount, onDestroy, getContext } from 'svelte';
	import L from 'leaflet';
	import type { Project } from '$lib/services/Libre311/Libre311';

	export let project: Project;
	export let color: string = '#66b3ff';
	export let weight: number = 3;

	let polygon: L.Polygon | undefined;

	const mapContext = getContext<{ getMap: () => L.Map } | undefined>('map');

	onMount(() => {
		if (!mapContext) {
			console.warn('ProjectBoundary: must be used within a MapComponent');
			return;
		}

		const map = mapContext.getMap();
		const bounds = project.bounds as L.LatLngTuple[];

		polygon = L.polygon(bounds, {
			color,
			weight,
			fill: true,
			fillOpacity: 0.1,
			opacity: 0.6,
			interactive: true
		}).addTo(map);

		const popupContent = `
			<div class="p-2 max-w-xs" role="dialog" aria-labelledby="project-name-${project.id}">
				<h3 id="project-name-${project.id}" class="text-lg font-bold mb-1">${project.name}</h3>
				${project.description ? `<p class="text-sm">${project.description}</p>` : ''}
				<p class="text-xs text-gray-500 mt-2">Status: ${project.status}</p>
			</div>
		`;

		polygon.bindPopup(popupContent, {
			closeButton: true,
			autoPan: true
		});
	});

	onDestroy(() => {
		polygon?.remove();
	});
</script>

<span class="sr-only">Project boundary for {project.name}</span>
