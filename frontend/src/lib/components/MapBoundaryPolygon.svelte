<script lang="ts">
	import { onMount, onDestroy, getContext } from 'svelte';
	import L from 'leaflet';

	export let bounds: L.LatLngExpression[];
	export let color: string = '#66b3ff';
	export let weight: number = 3;
	export let ariaLabel: string = 'Jurisdiction boundary displayed on map';

	let polygon: L.Polygon | undefined;

	const mapContext = getContext<{ getMap: () => L.Map } | undefined>('map');

	onMount(() => {
		if (!mapContext) {
			console.warn('MapBoundaryPolygon: must be used within a MapComponent');
			return;
		}

		if (!bounds || bounds.length === 0) {
			console.warn('MapBoundaryPolygon: bounds prop is required and must not be empty');
			return;
		}

		const map = mapContext.getMap();
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

<span class="sr-only">{ariaLabel}</span>
