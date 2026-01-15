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
		raw: unknown; // raw provider result
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
	import { GeoSearchControl, OpenStreetMapProvider } from 'leaflet-geosearch';
	import { createEventDispatcher, getContext, onDestroy, onMount } from 'svelte';
	import L from 'leaflet';

	interface SearchResult<TRawResult = any> {
		label?: string;
		raw: TRawResult;
	}

	interface NominatimAddress {
		house_number?: string;
		road?: string;
		city?: string;
		town?: string;
		village?: string;
		hamlet?: string;
		county?: string;
		state?: string;
		postcode?: string;
	}

	interface NominatimRaw {
		address?: NominatimAddress;
		class?: string;
		type?: string;
	}

	function formatAddress(result: SearchResult<NominatimRaw>): SearchResult<NominatimRaw> | null {
		const address = result.raw?.address;
		if (!address) return null;

		const streetNumber = address.house_number;
		const streetName = address.road;
		const city =
			address.city ?? address.town ?? address.village ?? address.hamlet ?? address.county;
		const state = address.state;
		const zip = address.postcode;

		if (!streetName) return null;

		result.label = [streetNumber ? `${streetNumber} ${streetName}` : streetName, city, state, zip]
			.filter(Boolean)
			.join(', ');

		return result;
	}

	const map = getContext<{ getMap: () => L.Map }>('map').getMap();
	const provider = new OpenStreetMapProvider({
		params: {
			addressdetails: 1,
			countrycodes: 'us'
		}
	});
	const control = GeoSearchControl({
		provider,
		style: 'bar',
		showMarker: false,
		resultFormat: ({ result }: { result: SearchResult<NominatimRaw> }) =>
			formatAddress(result)?.label ?? null
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
