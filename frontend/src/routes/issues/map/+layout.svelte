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
	import type { LatLngExpression, LatLngTuple } from 'leaflet';
	import type { Maybe } from '$lib/utils/types';
	import type { ServiceRequest, ServiceRequestsResponse } from '$lib/services/Libre311/Libre311';
	import type { AsyncResult } from '$lib/services/http';

	import { goto } from '$app/navigation';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { page } from '$app/stores';
	import { matchesDesktopMedia } from '$lib/utils/functions';

	const linkResolver = useLibre311Context().linkResolver;
	const libre311 = useLibre311Context().service;
	const serviceRequestsResponseStore = useServiceRequestsResponseStore();
	const selectedServiceRequestStore = useSelectedServiceRequestStore();

	$: mapBounds = createMapBounds($serviceRequestsResponseStore);

	function isSelected(
		serviceRequest: ServiceRequest,
		selectedServiceRequest: Maybe<ServiceRequest>
	) {
		return serviceRequest.service_request_id === selectedServiceRequest?.service_request_id;
	}

	function createMapBounds(res: AsyncResult<ServiceRequestsResponse>) {
		if (
			res.type !== 'success' ||
			(res.type === 'success' && res.value.serviceRequests.length === 0)
		) {
			return L.latLngBounds(libre311.getJurisdictionConfig().bounds);
		}
		let latLngs: LatLngTuple[] = res.value.serviceRequests.map((req) => [+req.lat, +req.long]);
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
			<div slot="is-mobile-or-tablet" class="absolute left-1/2 top-5 z-[1] -translate-x-1/2">
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
