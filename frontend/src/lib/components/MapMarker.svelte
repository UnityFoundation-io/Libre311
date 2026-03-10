<script lang="ts">
	import { onMount, onDestroy, getContext, createEventDispatcher } from 'svelte';
	import L from 'leaflet';

	export let latLng: L.LatLngExpression;
	export let options: L.MarkerOptions | undefined = undefined;

	let marker: L.Marker | undefined;

	const dispatch = createEventDispatcher<{ click: L.LeafletMouseEvent }>();
	const map = getContext<{ getMap: () => L.Map }>('map').getMap();

	onMount(() => {
		marker = L.marker(latLng, options).addTo(map);
		marker.addEventListener('click', (e) => dispatch('click', e));

		const element = marker.getElement();
		if (element) {
			element.addEventListener('focus', () => {
				if (marker) {
					map.setView(marker.getLatLng(), map.getZoom());
				}
			});
		}
	});

	onDestroy(() => {
		marker?.remove();
	});

	$: updateLatLng(latLng);

	function updateLatLng(latLng: L.LatLngExpression) {
		marker?.setLatLng(latLng);
	}
</script>
