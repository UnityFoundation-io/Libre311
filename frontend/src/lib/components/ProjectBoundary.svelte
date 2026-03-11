<script lang="ts">
	import { onMount, onDestroy, getContext } from 'svelte';
	import L from 'leaflet';
	import type { Project } from '$lib/services/Libre311/Libre311';
	import { goto } from '$app/navigation';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { useSelectedProjectSlugStore } from '$lib/context/ServiceRequestsContext';
	import { get } from 'svelte/store';

	export let project: Project;
	export let color: string = '#66b3ff';
	export let weight: number = 3;
	export let interactive: boolean = true;

	let polygon: L.Polygon | undefined;
	let openedViaKeyboard = false;

	const mapContext = getContext<{ getMap: () => L.Map } | undefined>('map');
	const linkResolver = useLibre311Context().linkResolver;
	const selectedProjectSlugStore = useSelectedProjectSlugStore();

	function getPopupContent() {
		const activeSlug = get(selectedProjectSlugStore);
		const isActive = activeSlug === project.slug;

		return `
					<div class="p-2 max-w-xs" role="dialog" aria-labelledby="project-name-${project.id}">
						<h3 id="project-name-${project.id}" class="text-lg font-bold mb-1">${project.name}</h3>
						${project.description ? `<p class="text-sm">${project.description}</p>` : ''}
						<p class="text-xs text-gray-500 mt-2">Status: ${project.status}</p>
						${
							isActive
								? `
							<div class="mt-4 w-full bg-info/10 text-info py-2 px-4 rounded-md text-sm font-bold border border-info/20 text-center">
								Currently Active Project
							</div>
						`
								: `
							<button id="enter-project-${project.id}" class="mt-4 w-full bg-primary text-primary-content py-2 px-4 rounded-md text-sm font-bold hover:opacity-90 transition-opacity focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2">
								Enter Project Mode
							</button>
						`
						}
					</div>
				`;
	}

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
			interactive
		}).addTo(map);

		if (interactive) {
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
					// If focused via keyboard (tab), fit bounds
					if (element.matches(':focus-visible') && polygon) {
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
					keyboardEvent.preventDefault();
					keyboardEvent.stopPropagation();
					if (polygon) {
						openedViaKeyboard = true;
						const currentBounds = map.getBounds();
						const targetBounds = polygon.getBounds();

						if (currentBounds.equals(targetBounds)) {
							polygon.openPopup();
						} else {
							map.once('moveend', () => {
								polygon?.openPopup();
							});
							map.fitBounds(targetBounds);
						}
					}
				}
			});

			polygon.bindPopup(getPopupContent, {
				closeButton: true,
				autoPan: true
			});

			polygon.on('popupopen', (e: L.PopupEvent) => {
				const container = e.popup.getElement();
				if (container) {
					const enterButton = container.querySelector(
						`#enter-project-${project.id}`
					) as HTMLElement;
					if (enterButton) {
						enterButton.addEventListener('click', () => {
							const url = new URL(window.location.href);
							url.searchParams.set('project_slug', project.slug || '');
							goto(linkResolver.issuesMap(url));
							polygon?.closePopup();
						});
					}

					if (openedViaKeyboard && enterButton) {
						enterButton.focus();
					} else {
						const closeButton = container.querySelector(
							'.leaflet-popup-close-button'
						) as HTMLElement;
						if (closeButton) {
							closeButton.focus();
						}
					}
					openedViaKeyboard = false;
				}
			});

			polygon.on('popupclose', () => {
				const element = polygon?.getElement() as HTMLElement | null;
				if (element) {
					element.focus();
				}
			});

			// Disable single click popup and move it to double click
			polygon.off('click');
			polygon.on('dblclick', (e: L.LeafletMouseEvent) => {
				L.DomEvent.stopPropagation(e.originalEvent);
				if (polygon) {
					const currentBounds = map.getBounds();
					const targetBounds = polygon.getBounds();

					if (currentBounds.equals(targetBounds)) {
						polygon.openPopup();
					} else {
						map.once('moveend', () => {
							polygon?.openPopup();
						});
						map.fitBounds(targetBounds);
					}
				}
			});
		}
	});

	onDestroy(() => {
		polygon?.remove();
	});
</script>

{#if interactive}
	<span class="sr-only">Project boundary for {project.name}</span>
{/if}
