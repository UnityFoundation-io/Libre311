<script lang="ts">
	import { onMount, onDestroy, getContext } from 'svelte';
	import L from 'leaflet';

	export let bounds: L.LatLngExpression[];
	export let color: string = '#66b3ff';
	export let weight: number = 3;

	let polygon: L.Polygon | undefined;

	const map = getContext<{ getMap: () => L.Map }>('map').getMap();

	onMount(() => {
		polygon = L.polygon(bounds, {
			color,
			weight,
			fill: false,
			opacity: 0.6,
			interactive: false
		}).addTo(map);
	});

	onDestroy(() => {
		polygon?.remove();
	});
</script>
