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

		// Make it tabbable and handle keyboard events
		const element = polygon.getElement() as HTMLElement | null;
		if (element) {
			element.setAttribute('tabindex', '0');
			element.setAttribute('role', 'button');
			element.setAttribute('aria-label', `Project boundary for ${project.name}`);
			element.style.outline = 'none';

			// Add visual focus indicator
			element.addEventListener('focus', () => {
				polygon?.setStyle({ weight: weight + 2, opacity: 1 });
				if (polygon) {
					map.fitBounds(polygon.getBounds());
				}
			});
			element.addEventListener('blur', () => {
				polygon?.setStyle({ weight, opacity: 0.6 });
			});
		}

		polygon.on('keydown', (e: L.LeafletEvent) => {
			const keyboardEvent = (e as L.LeafletKeyboardEvent).originalEvent;
			if (keyboardEvent.key === 'Enter' || keyboardEvent.key === ' ') {
				polygon?.openPopup();
			}
		});

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
