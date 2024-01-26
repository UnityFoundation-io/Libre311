<script lang="ts" context="module">
	// instead do boundsChanged and pass L.LatLngBounds which provides a method to get the center
	export type Events = {
		centerChanged: L.LatLng;
	};
</script>

<script lang="ts">
	import { onMount, onDestroy, setContext, createEventDispatcher, tick } from 'svelte';
	import L, { type PointTuple } from 'leaflet';
	import 'leaflet/dist/leaflet.css';

	export let bounds: L.LatLngBoundsExpression | undefined = undefined;
	export let center: L.LatLngExpression | undefined = undefined;
	export let zoom: number | undefined = undefined;

	const dispatch = createEventDispatcher<Events>();

	let map: L.Map;
	let mapElement: HTMLElement;

	onMount(() => {
		if (!bounds && !center) {
			throw new Error('Must set either bounds, or view and zoom.');
		}

		map = L.map(mapElement);
		// todo center doesn't change? can we derive the center from the bounds?
		map.addEventListener('zoomend', () => dispatch('centerChanged', map.getCenter()));
		map.addEventListener('dragend', () => dispatch('centerChanged', map.getCenter()));

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
		}
	}
</script>

<div class="z-0 h-full w-full" bind:this={mapElement}>
	{#if map}
		<slot />
	{/if}
</div>
