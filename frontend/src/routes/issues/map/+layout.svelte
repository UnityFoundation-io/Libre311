<script lang="ts">
	import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';
	import {
		useSelectedServiceRequestStore,
		useServiceRequestsResponseStore
	} from '$lib/context/ServiceRequestsContext';

	// Map imports
	import L from 'leaflet';
	import MapComponent from '$lib/components/MapComponent.svelte';
	import MapMarkerCircle from '$lib/components/MapMarkerCircle.svelte';
	import MapMarkerWaypoint from '$lib/components/MapMarkerWaypoint.svelte';
	import Breakpoint from '$lib/components/Breakpoint.svelte';
	import MapListToggle from '$lib/components/MapListToggle.svelte';

	// Type imports
	import type { LatLngExpression } from 'leaflet';
	import type { Maybe } from '$lib/utils/types';
	import type { ServiceRequest, ServiceRequestsResponse } from '$lib/services/Libre311/Libre311';
	import type { AsyncResult } from '$lib/services/http';

	import { goto } from '$app/navigation';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { page } from '$app/stores';
	import { matchesDesktopMedia } from '$lib/utils/functions';

	const linkResolver = useLibre311Context().linkResolver;
	const serviceRequestsResponseStore = useServiceRequestsResponseStore();
	const selectedServiceRequestStore = useSelectedServiceRequestStore();

	const initialView: LatLngExpression = [41.3083092093462, -72.9258607025516];

	$: mapBounds = createMapBounds($serviceRequestsResponseStore);

	function isSelected(
		serviceRequest: ServiceRequest,
		selectedServiceRequest: Maybe<ServiceRequest>
	) {
		return serviceRequest.service_request_id === selectedServiceRequest?.service_request_id;
	}

	function createMapBounds(res: AsyncResult<ServiceRequestsResponse>) {
		if (res.type !== 'success') {
			return L.latLngBounds([initialView]);
		}
		const latLngs: LatLngExpression[] = res.value.serviceRequests.map((req) => [
			+req.lat,
			+req.long
		]);
		return L.latLngBounds(latLngs);
	}

	function handleMarkerClick(serviceRequest: ServiceRequest) {
		if (matchesDesktopMedia()) {
			goto(linkResolver.issueDetailsDesktop($page.url, serviceRequest.service_request_id));
		} else {
			// todo for mobile show preview of issue
		}
	}
</script>

<SideBarMainContentLayout>
	<slot slot="side-bar" />
	<div slot="main-content" class="relative flex h-full">
		<Breakpoint>
			<div class="absolute left-1/2 top-5 z-[1] -translate-x-1/2" slot="is-mobile">
				<MapListToggle />
			</div>
		</Breakpoint>
		<MapComponent bounds={mapBounds}>
			{#if $serviceRequestsResponseStore.type === 'success'}
				{#each $serviceRequestsResponseStore.value.serviceRequests as req (req.service_request_id)}
					{#if isSelected(req, $selectedServiceRequestStore)}
						<MapMarkerWaypoint serviceRequest={req} />
					{:else}
						<MapMarkerCircle on:click={() => handleMarkerClick(req)} serviceRequest={req} />
					{/if}
				{/each}
			{/if}
		</MapComponent>
	</div>
</SideBarMainContentLayout>
