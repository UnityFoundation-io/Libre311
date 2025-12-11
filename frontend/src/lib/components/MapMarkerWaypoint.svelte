<script lang="ts">
	import L from 'leaflet';

	import WaypointClosed from '$lib/assets/waypoint/closed.png';
	import WaypointOpen from '$lib/assets/waypoint/open.png';
	import WaypointInProgress from '$lib/assets/waypoint/in_progress.png';
	import WayPointAssigned from '$lib/assets/waypoint/assigned.png';
	import type { ServiceRequest } from '$lib/services/Libre311/Libre311';

	import { iconPositionOpts } from '$lib/utils/functions';
	import MapMarker from './MapMarker.svelte';

	interface Props {
		serviceRequest: ServiceRequest;
	}

	let { serviceRequest }: Props = $props();

	const waypointLookupMap = {
		closed: WaypointClosed,
		open: WaypointOpen,
		in_progress: WaypointInProgress,
		assigned: WayPointAssigned
	};

	const icon = L.icon({
		iconUrl: waypointLookupMap[serviceRequest.status],
		...iconPositionOpts(128 / 169, 35, 'bottom-center')
	});
</script>

<MapMarker on:click latLng={[+serviceRequest.lat, +serviceRequest.long]} options={{ icon }} />
