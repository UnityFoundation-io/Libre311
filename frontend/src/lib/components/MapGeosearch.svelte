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

	interface AddressSearchResult {
		label?: string;
		raw: NominatimRaw;
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
		'ISO3166-2-lvl4'?: string;
		postcode?: string;
	}

	interface NominatimRaw {
		address?: NominatimAddress;
		class?: string;
		type?: string;
	}

	function formatAddress(result: AddressSearchResult): AddressSearchResult | null {
		const address = result.raw?.address;
		if (!address) return null;

		const streetNumber = address.house_number;
		const streetName = address.road;
		const city = address.city ?? address.town ?? address.village ?? address.hamlet;
		const stateCode = address['ISO3166-2-lvl4']?.split('-')[1];
		const state = stateCode ?? address.state;
		const zip = address.postcode;

		if (!streetName) return null;

		const street = streetNumber ? `${streetNumber} ${streetName}` : streetName;
		const cityLine = [city, [state, zip].filter(Boolean).join(' ')].filter(Boolean).join(', ');

		result.label = [street, cityLine].filter(Boolean).join(', ');

		return result;
	}

	const map = getContext<{ getMap: () => L.Map }>('map').getMap();
	const provider = new OpenStreetMapProvider({
		params: {
			addressdetails: true,
			countrycodes: 'us'
		}
	});
	const control = GeoSearchControl({
		provider,
		style: 'bar',
		showMarker: false,
		resultFormat: ({ result }: { result: AddressSearchResult }) =>
			formatAddress(result)?.label ?? null
	});

	const dispatch = createEventDispatcher<{
		geosearch: GeosearchShowLocationEvent;
	}>();

	onMount(() => {
		map.addControl(control);
		map.on('geosearch/showlocation', (e) => {
			if (isGeosearchShowLocationEvent(e)) {
				const formatted = formatAddress(e.location as unknown as AddressSearchResult);
				if (formatted?.label) {
					e.location.label = formatted.label;
				}
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
