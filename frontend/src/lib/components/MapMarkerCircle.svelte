<script lang="ts">
	import L from 'leaflet';
	import Closed from '$lib/assets/closed.png';
	import Open from '$lib/assets/open.png';
	import type { ServiceRequest } from '$lib/services/Libre311/Libre311';
	import { iconPositionOpts } from '$lib/utils/functions';
	import MapMarker from './MapMarker.svelte';

	export let serviceRequest: ServiceRequest;

	const circleLookupMap = {
		Closed: Closed,
		Open: Open
	};

	const icon = L.icon({
		iconUrl: circleLookupMap[serviceRequest.status],
		...iconPositionOpts(1 / 1, 20, 'center')
	});
</script>

<MapMarker on:click latLng={[+serviceRequest.lat, +serviceRequest.long]} options={{ icon }} />
