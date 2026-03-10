<script lang="ts" context="module">
	export type Events = {
		boundsChanged: L.LatLngBounds;
	};

	type ControlFactory = (opts: L.ControlOptions) => L.Control;
</script>

<script lang="ts">
	import { onMount, onDestroy, setContext, createEventDispatcher } from 'svelte';
	import L from 'leaflet';
	import 'leaflet/dist/leaflet.css';
	import { KEYBOARD_PAN_DELTA_FINE } from '$lib/constants/map';

	export let locateOpts: L.LocateOptions | undefined = undefined;
	export let bounds: L.LatLngBoundsExpression | undefined = undefined;
	export let center: L.LatLngExpression | undefined = undefined;
	export let zoom: number | undefined = undefined;
	export let minZoom: number | undefined = undefined;
	export let maxBounds: L.LatLngBoundsExpression | undefined = undefined;
	export let disabled: boolean = false;
	export let controlFactories: Array<ControlFactory> = [];
	export let controlOps: L.ControlOptions = { position: 'topleft' };
	export let keyboardPanDelta: number = KEYBOARD_PAN_DELTA_FINE;
	export let mapDescription: string | undefined = undefined;
	export let descriptionLocation: string = 'top';

	// Configuration for animated flyTo when selecting a marker
	export let flyToTarget: { latLng: L.LatLngExpression; zoom: number } | undefined = undefined;
	export let flyToOptions: L.ZoomPanOptions = { duration: 0.4 };

	const dispatch = createEventDispatcher<Events>();

	let map: L.Map;
	let mapElement: HTMLElement;
	let isInteracting = false;

	onMount(() => {
		map = L.map(mapElement, { keyboardPanDelta });
		map.on('movestart', () => {
			isInteracting = true;
		});
		map.on('moveend', () => {
			isInteracting = false;
			dispatch('boundsChanged', map.getBounds());
		});
		map.on('locationfound', function (e) {
			map.setView(e.latlng, 16);
		});

		controlFactories.forEach((factory) => factory(controlOps).addTo(map));

		L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
			attribution: `&copy;<a href="https://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>,&copy;<a href="https://carto.com/attributions" target="_blank">CARTO</a>`
		}).addTo(map);
	});

	onDestroy(() => {
		map?.remove();
	});

	setContext('map', {
		getMap: () => map
	});

	$: toggleDisabled(map, disabled);

	function toggleDisabled(map: L.Map | undefined, disabled: boolean) {
		if (!map) return;
		const controlContainer = document.querySelector<HTMLElement>('.leaflet-control-container');
		if (disabled) {
			if (controlContainer) {
				controlContainer.style.display = 'none';
			}
			map.touchZoom.disable();
			map.doubleClickZoom.disable();
			map.scrollWheelZoom.disable();
			map.dragging.disable();
		} else {
			if (controlContainer) {
				controlContainer.style.display = 'unset';
			}
			map.touchZoom.enable();
			map.doubleClickZoom.enable();
			map.scrollWheelZoom.enable();
			map.dragging.enable();
		}
	}

	let lastBoundsStr: string | undefined = undefined;
	let lastCenterStr: string | undefined = undefined;
	let lastZoom: number | undefined = undefined;
	let lastMaxBoundsStr: string | undefined = undefined;
	let locateApplied = false;

	$: if (map && minZoom !== undefined) {
		map.setMinZoom(minZoom);
	}

	$: if (map) {
		const currentMaxBoundsStr = maxBounds
			? Array.isArray(maxBounds)
				? JSON.stringify(maxBounds)
				: (maxBounds as L.LatLngBounds).toBBoxString()
			: undefined;

		if (currentMaxBoundsStr !== lastMaxBoundsStr) {
			if (maxBounds) {
				map.setMaxBounds(maxBounds);
			} else {
				// @ts-ignore
				map.setMaxBounds(null);
			}
			lastMaxBoundsStr = currentMaxBoundsStr;
		}
	}

	$: if (map && flyToTarget) {
		const currentFlyToStr = JSON.stringify(flyToTarget);
		if (currentFlyToStr !== lastFlyToStr) {
			// flyTo requires an existing view; use setView for initial positioning
			if (map.getZoom() === undefined) {
				map.setView(flyToTarget.latLng, flyToTarget.zoom);
			} else {
				map.flyTo(flyToTarget.latLng, flyToTarget.zoom, flyToOptions);
			}
			lastFlyToStr = currentFlyToStr;
		}
	}
	let lastFlyToStr: string | undefined = undefined;

	$: if (map && bounds && !flyToTarget) {
		const currentBoundsStr = JSON.stringify(bounds);
		if (currentBoundsStr !== lastBoundsStr) {
			map.fitBounds(bounds);
			lastBoundsStr = currentBoundsStr;
		}
	}

	$: if (map && center && zoom && !bounds && !flyToTarget) {
		const currentCenterStr = JSON.stringify(center);
		if (currentCenterStr !== lastCenterStr || zoom !== lastZoom) {
			map.setView(center, zoom);
			lastCenterStr = currentCenterStr;
			lastZoom = zoom;
		}
	}

	$: if (map && locateOpts && !locateApplied && !bounds && !center && !flyToTarget) {
		map.locate(locateOpts);
		locateApplied = true;
	}
</script>

<div class="relative z-0 h-full w-full">
	<div bind:this={mapElement} class="h-full w-full"></div>
	{#if map}
		<div
			class="pointer-events-none absolute left-1/2 z-[1000] -translate-x-1/2"
			class:top-1={descriptionLocation === 'top'}
			class:bottom-1={descriptionLocation === 'bottom'}
		>
			{#if mapDescription}
				<h3 class="sr-only text-base md:not-sr-only">{mapDescription}</h3>
			{/if}
			<slot />
		</div>
	{/if}
</div>
