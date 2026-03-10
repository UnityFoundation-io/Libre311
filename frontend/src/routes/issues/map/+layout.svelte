<script lang="ts">
	import RequestListMap from '$lib/components/RequestListMap.svelte';
	import {
		useSelectedServiceRequestStore,
		useServiceRequestsResponseStore,
		useProjectsStore,
		useSelectedProjectSlugStore
	} from '$lib/context/ServiceRequestsContext';

	// Map imports
	import MapComponent from '$lib/components/MapComponent.svelte';
	import MapMarkerCircle from '$lib/components/MapMarkerCircle.svelte';
	import MapMarkerWaypoint from '$lib/components/MapMarkerWaypoint.svelte';
	import MapListToggle from '$lib/components/MapListToggle.svelte';
	import ProjectBoundary from '$lib/components/ProjectBoundary.svelte';

	// Type imports
	import type { LatLngTuple } from 'leaflet';
	import type { Maybe } from '$lib/utils/types';
	import {
		type ServiceRequest,
		type ServiceRequestsResponse,
		type Project
	} from '$lib/services/Libre311/Libre311';
	import type { AsyncResult } from '$lib/services/http';

	import { goto } from '$app/navigation';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { page } from '$app/stores';
	import { matchesDesktopMedia } from '$lib/utils/functions';
	import CreateServiceRequestButton from '$lib/components/CreateServiceRequestButton.svelte';
	import { mapCenterControlFactory } from '$lib/components/MapCenterControl';
	import { mapStatusLegendControlFactory } from '$lib/components/MapStatusLegendControl';
	import { KEYBOARD_PAN_DELTA_COARSE, SELECTION_ZOOM_LEVEL } from '$lib/constants/map';

	import { MapOrList, type MapOrListToggle } from '$lib/components/map_or_list_toggle';

	const linkResolver = useLibre311Context().linkResolver;
	const libre311 = useLibre311Context().service;
	const serviceRequestsResponseStore = useServiceRequestsResponseStore();
	const selectedServiceRequestStore = useSelectedServiceRequestStore();
	const projectsStore = useProjectsStore();
	const selectedProjectSlugStore = useSelectedProjectSlugStore();

	import Breakpoint from '$lib/components/Breakpoint.svelte';
	import { mediaQuery } from '$lib/components/media';
	import { useJurisdiction } from '$lib/context/JurisdictionContext';

	const jurisdiction = useJurisdiction();

	$: project = $projectsStore.find((p) => p.slug === $selectedProjectSlugStore);
	$: projectBounds = createProjectBounds($projectsStore, $selectedProjectSlugStore);

	$: mapBounds =
		$selectedProjectSlugStore && projectBounds
			? projectBounds
			: createMapBounds($serviceRequestsResponseStore);

	$: if ($selectedProjectSlugStore && projectBounds) {
		// We want to set min zoom after initial fit
		// This is tricky because MapComponent handles fitBounds
		// For now we'll just use a reasonable default or let Leaflet handle it
	}

	function createProjectBounds(
		projects: Project[],
		project_slug: Maybe<string>
	): LatLngTuple[] | undefined {
		if (!project_slug) return undefined;
		const project = projects.find((p) => p.slug === project_slug);
		if (!project) return undefined;
		return project.bounds as LatLngTuple[];
	}

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

	function createMapBounds(res: AsyncResult<ServiceRequestsResponse>): LatLngTuple[] {
		if (
			res.type !== 'success' ||
			(res.type === 'success' && res.value.serviceRequests.length === 0)
		) {
			return libre311.getJurisdictionConfig().bounds;
		}
		// Note: lat/long are strings from the API, convert to numbers for Leaflet
		return res.value.serviceRequests.map(
			(req) => [Number(req.lat), Number(req.long)] as LatLngTuple
		);
	}

	function handleMarkerClick(serviceRequest: ServiceRequest) {
		if (matchesDesktopMedia()) {
			goto(linkResolver.issueDetailsDesktop($page.url, serviceRequest.service_request_id));
		} else {
			goto(linkResolver.issueDetailsMobile($page.url, serviceRequest.service_request_id));
		}
	}

	let listHidden = false;
	let mapHidden = false;

	export let toggleState: MapOrListToggle = MapOrList.Map;

	function handleToggle(toggled: MapOrListToggle) {
		toggleState = toggled;

		listHidden = toggled === MapOrList.Map;
		mapHidden = toggled === MapOrList.List;
	}

	const isNarrow = mediaQuery('(max-width: 768px)');

	$: {
		// callback-like behavior
		if ($isNarrow) {
			listHidden = toggleState === MapOrList.Map;
			mapHidden = toggleState === MapOrList.List;
		} else {
			listHidden = false;
			mapHidden = false;
		}
	}
</script>

<RequestListMap {listHidden} {mapHidden}>
	<div slot="list-slot">
		{#if project}
			<div class="border-b-2 border-info bg-info/10 px-4 py-2">
				<div class="text-sm font-bold">Project Mode: {project.name}</div>
				<div class="text-xs">Viewing issues for this specific project.</div>
			</div>
		{/if}
		<Breakpoint>
			<div slot="is-mobile-or-tablet" class="my-4 flex justify-center">
				<MapListToggle toggled={toggleState} on:change={(e) => handleToggle(e.detail)} />
			</div>
		</Breakpoint>
		<slot />
	</div>
	<div slot="map-slot" class="relative flex h-full">
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
			{#if $jurisdiction.project_feature && $jurisdiction.project_feature !== 'DISABLED'}
				{#each $projectsStore.filter((p) => p.status === 'OPEN') as project (project.id)}
					<ProjectBoundary {project} />
				{/each}
			{/if}
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
		<CreateServiceRequestButton projectSlug={$selectedProjectSlugStore} />
	</div>
</RequestListMap>
