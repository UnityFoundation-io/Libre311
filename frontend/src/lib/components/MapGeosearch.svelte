<script lang="ts" context="module">
	// https://smeijer.github.io/leaflet-geosearch/usage#results
	export type GeosearchLocation = {
		x: number; // lon
		y: number; // lat
		label: string; // formatted address
		bounds: [
			[number, number], // south, west - lat, lon
			[number, number] // north, east - lat, lon
		];
		raw: any; // raw provider result
	};

	export type GeosearchShowLocationEvent = {
		type: 'geosearch/showlocation';
		location: GeosearchLocation;
	};

	function isGeosearchShowLocationEvent(e: { type: string }): e is GeosearchShowLocationEvent {
		if (e.type === 'geosearch/showlocation') return true;
		return false;
	}
</script>

<script lang="ts">
	import { GeoSearchControl, OpenStreetMapProvider, SearchControl } from 'leaflet-geosearch';
	import { createEventDispatcher, getContext, onDestroy, onMount } from 'svelte';

	const map = getContext<{ getMap: () => L.Map }>('map').getMap();
	const provider = new OpenStreetMapProvider({
		params: {
			countrycodes: 'us'
		}
	});
	const control = GeoSearchControl({
		provider,
		style: 'bar',
		showMarker: false,
		retainZoomLevel: true
	});

	const dispatch = createEventDispatcher<{
		geosearch: GeosearchShowLocationEvent;
	}>();

	onMount(() => {
		map.addControl(control);
		map.on('geosearch/showlocation', (e) => {
			if (isGeosearchShowLocationEvent(e)) {
				dispatch('geosearch', e);
			} else {
				throw new Error('leaflet-geosearch API Change');
			}
		});
	});

	onDestroy(() => {
		map.removeControl(control);
	});
</script>
