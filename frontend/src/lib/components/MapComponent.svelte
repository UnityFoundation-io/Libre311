<script lang="ts" context="module">
	export type Events = {
		boundsChanged: L.LatLngBounds;
	};
</script>

<script lang="ts">
	import { onMount, onDestroy, setContext, createEventDispatcher, tick } from 'svelte';
	import L, { type PointTuple } from 'leaflet';
	import 'leaflet/dist/leaflet.css';

	export let locateOpts: L.LocateOptions | undefined = undefined;
	export let bounds: L.LatLngBoundsExpression | undefined = undefined;
	export let center: L.LatLngExpression | undefined = undefined;
	export let zoom: number | undefined = undefined;

	const dispatch = createEventDispatcher<Events>();

	let map: L.Map;
	let mapElement: HTMLElement;

	onMount(() => {
		map = L.map(mapElement);
		map.addEventListener('zoom', () => dispatch('boundsChanged', map.getBounds()));
		map.addEventListener('drag', () => dispatch('boundsChanged', map.getBounds()));

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

	$: if (map) {
		if (bounds) {
			map.fitBounds(bounds);
		} else if (center && zoom) {
			map.setView(center, zoom);
		} else if (locateOpts) map.locate(locateOpts);
	}
</script>

<div class="z-0 h-full w-full" bind:this={mapElement}>
	{#if map}
		<slot />
	{/if}
</div>
