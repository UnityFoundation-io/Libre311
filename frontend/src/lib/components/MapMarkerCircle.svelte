<script lang="ts">
	import L from 'leaflet';
	import Closed from '$lib/assets/closed.png';
	import Open from '$lib/assets/open.png';
	import InProgress from '$lib/assets/in_progress.png';
	import Assigned from '$lib/assets/assigned.png';
	import type { ServiceRequest } from '$lib/services/Libre311/Libre311';
	import { iconPositionOpts } from '$lib/utils/functions';
	import MapMarker from './MapMarker.svelte';

	interface Props {
		serviceRequest: ServiceRequest;
	}

	let { serviceRequest }: Props = $props();

	const circleLookupMap = {
		closed: Closed,
		open: Open,
		in_progress: InProgress,
		assigned: Assigned
	};

	const icon = L.icon({
		iconUrl: circleLookupMap[serviceRequest.status],
		...iconPositionOpts(1 / 1, 20, 'center')
	});
</script>

<MapMarker on:click latLng={[+serviceRequest.lat, +serviceRequest.long]} options={{ icon }} />
