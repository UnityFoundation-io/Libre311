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
	export let disabled: boolean = false;
	export let controlFactories: Array<ControlFactory> = [];
	export let controlOps: L.ControlOptions = { position: 'topleft' };
	export let keyboardPanDelta: number = KEYBOARD_PAN_DELTA_FINE;

	// Configuration for animated flyTo when selecting a marker
	export let flyToTarget: { latLng: L.LatLngExpression; zoom: number } | undefined = undefined;
	export let flyToOptions: L.ZoomPanOptions = { duration: 0.4 };

	const dispatch = createEventDispatcher<Events>();

	let map: L.Map;
	let mapElement: HTMLElement;

	onMount(() => {
		map = L.map(mapElement, { keyboardPanDelta });
		map.addEventListener('moveend', () => {
			dispatch('boundsChanged', map.getBounds());
		});
		map.addEventListener('drag', () => dispatch('boundsChanged', map.getBounds()));
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

	// Handle map view initialization and updates
	// flyToTarget takes precedence when set (e.g., when a marker is selected)
	$: if (map) {
		if (flyToTarget) {
			// flyTo requires an existing view; use setView for initial positioning
			if (map.getZoom() === undefined) {
				map.setView(flyToTarget.latLng, flyToTarget.zoom);
			} else {
				map.flyTo(flyToTarget.latLng, flyToTarget.zoom, flyToOptions);
			}
		} else if (bounds) {
			map.fitBounds(bounds);
		} else if (center && zoom) {
			map.setView(center, zoom);
		} else if (locateOpts) {
			map.locate(locateOpts);
		}
	}
</script>

<div class="z-0 h-full w-full" bind:this={mapElement}>
	{#if map}
		<slot />
	{/if}
</div>
