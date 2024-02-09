<script lang="ts">
	import L from 'leaflet';

	import WaypointClosed from '$lib/assets/waypoint-closed.png';
	import WaypointOpen from '$lib/assets/waypoint-open.png';
	import type { ServiceRequest } from '$lib/services/Libre311/Libre311';

	import { iconPositionOpts } from '$lib/utils/functions';
	import MapMarker from './MapMarker.svelte';

	export let serviceRequest: ServiceRequest;

	const waypointLookupMap = {
		closed: WaypointClosed,
		open: WaypointOpen
	};

	const icon = L.icon({
		iconUrl: waypointLookupMap[serviceRequest.status],
		...iconPositionOpts(128 / 169, 35, 'bottom-center')
	});
</script>

<MapMarker on:click latLng={[+serviceRequest.lat, +serviceRequest.long]} options={{ icon }} />
