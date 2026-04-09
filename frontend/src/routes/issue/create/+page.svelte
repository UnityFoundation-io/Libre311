<script lang="ts">
	import SelectLocation from '$lib/components/CreateServiceRequest/SelectLocation.svelte';
	import UploadFile from '$lib/components/CreateServiceRequest/UploadFile.svelte';
	import MapComponent from '$lib/components/MapComponent.svelte';
	import ContactInformation from '$lib/components/CreateServiceRequest/ContactInformation.svelte';
	import ReviewServiceRequest from '$lib/components/CreateServiceRequest/ReviewServiceRequest.svelte';

	import WaypointOpen from '$lib/assets/waypoint-open.png';
	import type { CreateServiceRequestParams, Project } from '$lib/services/Libre311/Libre311';
	import { iconPositionOpts } from '$lib/utils/functions';

	import L, { type PointTuple } from 'leaflet';
	import MapMarker from '$lib/components/MapMarker.svelte';
	import MapBoundaryPolygon from '$lib/components/MapBoundaryPolygon.svelte';
	import ProjectBoundary from '$lib/components/ProjectBoundary.svelte';
	import { KEYBOARD_PAN_DELTA_FINE } from '$lib/constants/map';
	import type { ComponentType } from 'svelte';
	import {
		CreateServiceRequestSteps,
		type CreateServiceRequestUIParams
	} from '$lib/components/CreateServiceRequest/shared';
	import { goto } from '$app/navigation';
	import { onMount } from 'svelte';
	import { createDraftStore } from '$lib/services/DraftStore';
	import MapGeosearch from '$lib/components/MapGeosearch.svelte';
	import type { ComponentEvents } from 'svelte';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';
	import { useProjectsStore } from '$lib/context/ServiceRequestsContext';
	import { useJurisdiction } from '$lib/context/JurisdictionContext';
	import Breakpoint from '$lib/components/Breakpoint.svelte';
	import { Button } from 'stwui';
	import { page } from '$app/stores';
	import ServiceRequestDetailsForm from '$lib/components/CreateServiceRequest/ServiceRequestDetailsForm.svelte';
	import CreateServiceRequestLayout from '$lib/components/CreateServiceRequest/CreateServiceRequestLayout.svelte';
	import { mapCenterControlFactory } from '$lib/components/MapCenterControl';
	import messages from '$media/messages.json';
	import * as turf from '@turf/turf';

	const libre311 = useLibre311Service();
	const libre311Context = useLibre311Context();
	const linkResolver = libre311Context.linkResolver;
	const draftStore = createDraftStore();
	let draftLoaded = false;

	const alertError = libre311Context.alertError;
	const isOnline = libre311Context.networkStatus.isOnline;
	const projectsStore = useProjectsStore();
	const jurisdictionStore = useJurisdiction();

	let params: Partial<CreateServiceRequestUIParams> = {};
	let geosearchJustFired = false;
	$: project = $projectsStore.find((p) => p.slug === $page.url.searchParams.get('project_slug'));

	$: if (project) {
		params.project_id = project.id;
		params.project_slug = project.slug;
	}

	let centerPos: PointTuple = getStartingCenterPos();

	$: mapBounds = createCreationMapBounds($projectsStore, project);

	function createCreationMapBounds(
		projects: Project[],
		selectedProject: Project | undefined
	): L.LatLngTuple[] {
		if (selectedProject) {
			return selectedProject.bounds as L.LatLngTuple[];
		}

		return libre311.getJurisdictionConfig().bounds;
	}

	$: step = linkResolver.createIssuePageGetCurrentStep($page.url);

	const icon = L.icon({
		iconUrl: WaypointOpen,
		...iconPositionOpts(128 / 169, 45, 'bottom-center')
	});
	const componentMap: Map<CreateServiceRequestSteps, ComponentType> = new Map();
	componentMap.set(CreateServiceRequestSteps.PHOTO, UploadFile);
	componentMap.set(CreateServiceRequestSteps.DETAILS, ServiceRequestDetailsForm);
	componentMap.set(CreateServiceRequestSteps.CONTACT_INFO, ContactInformation);

	function getStartingCenterPos(): PointTuple {
		const center = L.latLngBounds(libre311.getJurisdictionConfig().bounds).getCenter();
		return [center.lat, center.lng];
	}

	function handleChange(e: CustomEvent<Partial<CreateServiceRequestParams>>) {
		const changedParams = e.detail;
		params = { ...params, ...changedParams };
		goto(linkResolver.createIssuePageNext($page.url));
	}

	function boundsChanged(e: CustomEvent<L.LatLngBounds>) {
		const center = e.detail.getCenter();
		centerPos = [center.lat, center.lng];
		if (geosearchJustFired) {
			geosearchJustFired = false;
		} else {
			params.address_string = undefined;
		}
	}

	async function confirmLocation() {
		const turfPoint = turf.point([centerPos[0], centerPos[1]]);
		const boundsPoly = turf.polygon([libre311.getJurisdictionConfig().bounds]);

		if (!turf.booleanPointInPolygon(turfPoint, boundsPoly)) {
			alertError(new Error('Location is outside of jurisdiction boundaries.'));
			return;
		}

		params.lat = String(centerPos[0]);
		params.long = String(centerPos[1]);
		if (!params.address_string || params.address_string.startsWith('Location: ')) {
			params.address_string = `Location: ${centerPos[0].toFixed(6)}, ${centerPos[1].toFixed(6)}`;
		}

		await goto(linkResolver.createIssuePageNext($page.url));
	}

	function handleGeosearch(e: ComponentEvents<MapGeosearch>['geosearch']) {
		const location = e.detail.location;
		geosearchJustFired = true;
		centerPos = [location.y, location.x];
		params.address_string = location.label;
	}

	function isCreateServiceRequestUIParams(
		partial: Partial<CreateServiceRequestUIParams>
	): partial is CreateServiceRequestUIParams {
		return !!(partial?.address_string && partial?.attributeMap && partial?.service);
	}

	onMount(async () => {
		const draft = await draftStore.load();
		if (draft) {
			params = { ...params, ...draft.params };
			if (draft.params.lat && draft.params.long) {
				centerPos = [Number(draft.params.lat), Number(draft.params.long)];
			}
			if (draft.step > CreateServiceRequestSteps.LOCATION) {
				const searchParams = new URLSearchParams($page.url.searchParams);
				searchParams.set('step', String(draft.step));
				if (draft.params.project_slug) {
					searchParams.set('project_slug', draft.params.project_slug);
				}
				await goto(`/issue/create?${searchParams.toString()}`);
			}
		}
		draftLoaded = true;
	});

	$: if (draftLoaded && params.lat) draftStore.save(step, params);
</script>

<CreateServiceRequestLayout {step}>
	<div slot="side-bar" class="h-full">
		{#if project}
			<div class="absolute mb-4 w-full border-b-2 border-info bg-info/10 px-4 py-2">
				<div class="text-sm font-bold">Project Mode: {project.name}</div>
				<div class="text-xs">Submitting a request for this specific project.</div>
			</div>
		{/if}
		<div class="mx-4 h-full pb-2 pt-16">
			<h3 class="ml-4 text-base">{messages['serviceRequest']['create']}</h3>
			{#if step === CreateServiceRequestSteps.LOCATION}
				<SelectLocation on:confirmLocation={confirmLocation} />
			{:else if step === CreateServiceRequestSteps.REVIEW && isCreateServiceRequestUIParams(params)}
				<ReviewServiceRequest {params} on:submitted={() => draftStore.clear()} />
			{:else}
				<svelte:component this={componentMap.get(step)} {params} on:stepChange={handleChange} />
			{/if}
		</div>
	</div>
	<div slot="main-content" class="relative h-full">
		<MapComponent
			mapDescription="Request Location"
			descriptionLocation="bottom"
			keyboardPanDelta={KEYBOARD_PAN_DELTA_FINE}
			controlFactories={[mapCenterControlFactory]}
			disabled={step !== 0}
			bounds={mapBounds}
			locateOpts={{ setView: true, enableHighAccuracy: true }}
			on:boundsChanged={boundsChanged}
		>
			<MapBoundaryPolygon bounds={libre311.getJurisdictionConfig().bounds} />
			{#if $jurisdictionStore.project_feature && $jurisdictionStore.project_feature !== 'DISABLED'}
				{#each $projectsStore.filter((p) => p.status === 'OPEN') as project (project.id)}
					<ProjectBoundary {project} interactive={false} />
				{/each}
			{/if}
			<MapMarker latLng={centerPos} options={{ icon, keyboard: false }} />
			{#if step === CreateServiceRequestSteps.LOCATION && $isOnline}
				<MapGeosearch on:geosearch={handleGeosearch} />
			{/if}
		</MapComponent>
		<Breakpoint>
			<div
				class="display absolute inset-x-0 bottom-6 flex justify-center gap-2"
				slot="is-mobile-or-tablet"
			>
				<Button type="primary" href={linkResolver.issuesMap($page.url)}>Cancel</Button>
				<Button on:click={confirmLocation} type="primary">Select Location</Button>
			</div>
		</Breakpoint>
	</div>
</CreateServiceRequestLayout>
