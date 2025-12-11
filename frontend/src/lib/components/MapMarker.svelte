<script lang="ts">
	import { run } from 'svelte/legacy';

	import { onMount, onDestroy, getContext, createEventDispatcher } from 'svelte';
	import L from 'leaflet';

	interface Props {
		latLng: L.LatLngExpression;
		options?: L.MarkerOptions | undefined;
	}

	let { latLng, options = undefined }: Props = $props();

	let marker: L.Marker | undefined;

	const dispatch = createEventDispatcher<{ click: L.LeafletMouseEvent }>();
	const map = getContext<{ getMap: () => L.Map }>('map').getMap();

	onMount(() => {
		marker = L.marker(latLng, options).addTo(map);
		marker.addEventListener('click', (e) => dispatch('click', e));
	});

	onDestroy(() => {
		marker?.remove();
	});


	function updateLatLng(latLng: L.LatLngExpression) {
		marker?.setLatLng(latLng);
	}
	run(() => {
		updateLatLng(latLng);
	});
</script>
