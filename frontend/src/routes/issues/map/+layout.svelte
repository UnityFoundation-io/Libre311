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
	import MapListToggle from '$lib/components/MapListToggle.svelte';

	// Type imports
	import type { LatLngTuple } from 'leaflet';
	import type { Maybe } from '$lib/utils/types';
	import type { ServiceRequest, ServiceRequestsResponse } from '$lib/services/Libre311/Libre311';
	import type { AsyncResult } from '$lib/services/http';

	import { goto } from '$app/navigation';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { page } from '$app/stores';
	import { matchesDesktopMedia } from '$lib/utils/functions';
	import CreateServiceRequestButton from '$lib/components/CreateServiceRequestButton.svelte';
	import { mapCenterControlFactory } from '$lib/components/MapCenterControl';
	import { mapStatusLegendControlFactory } from '$lib/components/MapStatusLegendControl';
	import { KEYBOARD_PAN_DELTA_COARSE, SELECTION_ZOOM_LEVEL } from '$lib/constants/map';
	import messages from '$media/messages.json';
	import ServiceRequestPreview from '$lib/components/ServiceRequestPreview.svelte';
	import Pagination from '$lib/components/Pagination.svelte';

	import { MapOrList, type MapOrListToggle } from '$lib/components/map_or_list_toggle';

	const linkResolver = useLibre311Context().linkResolver;
	const libre311 = useLibre311Context().service;
	const serviceRequestsResponseStore = useServiceRequestsResponseStore();
	const selectedServiceRequestStore = useSelectedServiceRequestStore();

	import { useServiceRequestsContext } from '$lib/context/ServiceRequestsContext';
	import Breakpoint from '$lib/components/Breakpoint.svelte';
	import { mediaQuery } from '$lib/components/media';

	const ctx = useServiceRequestsContext();
	const serviceRequestsRes = ctx.serviceRequestsResponse;

	let listElement: HTMLElement;

	function scrollToTop() {
		listElement.scrollIntoView();
	}

	$: mapBounds = createMapBounds($serviceRequestsResponseStore);

	// Compute flyTo target when a service request is selected
	// Note: lat/long are strings from the API, convert to numbers for Leaflet
	$: flyToTarget = $selectedServiceRequestStore
		? {
				latLng: [
					Number($selectedServiceRequestStore.lat),
					Number($selectedServiceRequestStore.long)
				] as LatLngTuple,
				zoom: SELECTION_ZOOM_LEVEL
			}
		: undefined;

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
		// Note: lat/long are strings from the API, convert to numbers for Leaflet
		let latLngs: LatLngTuple[] = res.value.serviceRequests.map((req) => [
			Number(req.lat),
			Number(req.long)
		]);
		return L.latLngBounds(latLngs);
	}

	function handleMarkerClick(serviceRequest: ServiceRequest) {
		if (matchesDesktopMedia()) {
			goto(linkResolver.issueDetailsDesktop($page.url, serviceRequest.service_request_id));
		} else {
			goto(linkResolver.issueDetailsMobile($page.url, serviceRequest.service_request_id));
		}
	}

	let sideBarHidden = false;
	let mainContentHidden = false;

	export let toggleState: MapOrListToggle = MapOrList.Map;

	function handleToggle(toggled: MapOrListToggle) {
		toggleState = toggled;

		sideBarHidden = toggled === MapOrList.Map;
		mainContentHidden = toggled === MapOrList.List;
	}

	const isNarrow = mediaQuery('(max-width: 768px)');

	$: {
		// callback-like behavior
		if ($isNarrow) {
			sideBarHidden = true;
			mainContentHidden = false;
		} else {
			sideBarHidden = false;
			mainContentHidden = false;
			toggleState = MapOrList.Map;
		}
	}
</script>

<SideBarMainContentLayout {sideBarHidden} {mainContentHidden}>
	<div slot="side-bar">
		{#if $serviceRequestsRes.type === 'success'}
			<Breakpoint>
				<div slot="is-mobile-or-tablet" class="my-4 flex justify-center">
					<MapListToggle toggled={toggleState} on:change={(e) => handleToggle(e.detail)} />
				</div>
			</Breakpoint>
			<div bind:this={listElement}>
				<div class="sticky top-0 border-b-2 bg-white">
					<div class="flex items-center justify-between">
						<div>
							<h3 class="ml-4 text-base">{messages['sidebar']['title']}</h3>
						</div>

						<div>
							<Pagination
								pagination={$serviceRequestsRes.value.metadata.pagination}
								nextPage={linkResolver.nextIssuesPage(
									$serviceRequestsRes.value.metadata.pagination,
									$page.url
								)}
								prevPage={linkResolver.prevIssuesPage(
									$serviceRequestsRes.value.metadata.pagination,
									$page.url
								)}
								on:pageChange={scrollToTop}
							/>
						</div>
					</div>
				</div>

				<ul>
					{#each $serviceRequestsRes.value.serviceRequests as serviceRequest}
						<li class="m-3">
							<ServiceRequestPreview
								{serviceRequest}
								detailsLink={linkResolver.issueDetailsDesktop(
									$page.url,
									serviceRequest.service_request_id
								)}
							/>
						</li>
					{/each}
				</ul>
			</div>
		{/if}
	</div>
	<div slot="main-content" class="relative flex h-full">
		<Breakpoint>
			<div slot="is-mobile-or-tablet" class="absolute left-1/2 top-5 z-[1] -translate-x-1/2">
				<MapListToggle toggled={toggleState} on:change={(e) => handleToggle(e.detail)} />
			</div>
		</Breakpoint>
		<MapComponent
			mapDescription="Request Map"
			keyboardPanDelta={KEYBOARD_PAN_DELTA_COARSE}
			controlFactories={[mapCenterControlFactory, mapStatusLegendControlFactory]}
			bounds={mapBounds}
			{flyToTarget}
		>
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
		<CreateServiceRequestButton />
	</div>
</SideBarMainContentLayout>
