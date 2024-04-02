<script lang="ts" context="module">
	export type Events = {
		boundsChanged: L.LatLngBounds;
	};

	type ControlFactory = (opts: L.ControlOptions) => L.Control;
</script>

<script lang="ts">
	import { onMount, onDestroy, setContext, createEventDispatcher, tick } from 'svelte';
	import L from 'leaflet';
	import 'leaflet/dist/leaflet.css';

	export let locateOpts: L.LocateOptions | undefined = undefined;
	export let bounds: L.LatLngBoundsExpression | undefined = undefined;
	export let center: L.LatLngExpression | undefined = undefined;
	export let zoom: number | undefined = undefined;
	export let disabled: boolean = false;
	export let controlFactories: Array<ControlFactory> = [];
	export let controlOps: L.ControlOptions = { position: 'topleft' };

	const dispatch = createEventDispatcher<Events>();

	let map: L.Map;
	let mapElement: HTMLElement;

	onMount(() => {
		map = L.map(mapElement);
		map.addEventListener('moveend', (e) => {
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

	$: if (map) {
		if (bounds) {
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
