<script lang="ts">
	import L from 'leaflet';
	import type { ServiceRequest } from '$lib/services/Libre311/Libre311';
	import { iconPositionOpts } from '$lib/utils/functions';
	import { getStatusIconDataUrl, DEFAULT_MARKER_SIZE } from '$lib/utils/iconToDataUrl';
	import MapMarker from './MapMarker.svelte';

	export let serviceRequest: ServiceRequest;

	const icon = L.icon({
		iconUrl: getStatusIconDataUrl(serviceRequest.status),
		...iconPositionOpts(1 / 1, DEFAULT_MARKER_SIZE, 'center')
	});
</script>

<MapMarker on:click latLng={[+serviceRequest.lat, +serviceRequest.long]} options={{ icon }} />
