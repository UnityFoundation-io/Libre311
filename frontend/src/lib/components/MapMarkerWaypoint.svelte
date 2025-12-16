<script lang="ts">
	import L from 'leaflet';
	import type { ServiceRequest } from '$lib/services/Libre311/Libre311';
	import { iconPositionOpts } from '$lib/utils/functions';
	import { getWaypointIconDataUrl, DEFAULT_WAYPOINT_SIZE } from '$lib/utils/iconToDataUrl';
	import MapMarker from './MapMarker.svelte';

	export let serviceRequest: ServiceRequest;

	// Waypoint pin aspect ratio: 24/32 = 0.75
	const icon = L.icon({
		iconUrl: getWaypointIconDataUrl(serviceRequest.status, DEFAULT_WAYPOINT_SIZE),
		...iconPositionOpts(24 / 32, DEFAULT_WAYPOINT_SIZE, 'bottom-center')
	});
</script>

<MapMarker on:click latLng={[+serviceRequest.lat, +serviceRequest.long]} options={{ icon }} />
