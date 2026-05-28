<script lang="ts">
	import SelectLocation from '$lib/components/CreateServiceRequest/SelectLocation.svelte';
	import UploadFile from '$lib/components/CreateServiceRequest/UploadFile.svelte';
	import MapComponent from '$lib/components/MapComponent.svelte';
	import ContactInformation from '$lib/components/CreateServiceRequest/ContactInformation.svelte';
	import ReviewServiceRequest from '$lib/components/CreateServiceRequest/ReviewServiceRequest.svelte';
	import PhotoVoiceDetailsForm from '$lib/components/CreateServiceRequest/PhotoVoiceDetailsForm.svelte';

	import WaypointOpen from '$lib/assets/waypoint-open.png';
	import type { CreateServiceRequestParams, Service } from '$lib/services/Libre311/Libre311';
	import { iconPositionOpts } from '$lib/utils/functions';

	import L, { type PointTuple } from 'leaflet';
	import MapMarker from '$lib/components/MapMarker.svelte';
	import MapBoundaryPolygon from '$lib/components/MapBoundaryPolygon.svelte';
	import { KEYBOARD_PAN_DELTA_FINE } from '$lib/constants/map';
	import type { ComponentType } from 'svelte';
	import {
		CreateServiceRequestSteps,
		type CreateServiceRequestUIParams
	} from '$lib/components/CreateServiceRequest/shared';
	import { goto } from '$app/navigation';
	import MapGeosearch from '$lib/components/MapGeosearch.svelte';
	import type { ComponentEvents } from 'svelte';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';
	import { useJurisdiction } from '$lib/context/JurisdictionContext';
	import Breakpoint from '$lib/components/Breakpoint.svelte';
	import { Button } from 'stwui';
	import { page } from '$app/stores';
	import CreateServiceRequestLayout from '$lib/components/CreateServiceRequest/CreateServiceRequestLayout.svelte';
	import { mapCenterControlFactory } from '$lib/components/MapCenterControl';
	import * as turf from '@turf/turf';
	import { onMount } from 'svelte';
	import messages from '$media/messages.json';

	const libre311 = useLibre311Service();
	const libre311Context = useLibre311Context();
	const linkResolver = libre311Context.linkResolver;
	const alertError = libre311Context.alertError;
	const isOnline = libre311Context.networkStatus.isOnline;
	const jurisdictionStore = useJurisdiction();

	let params: Partial<CreateServiceRequestUIParams> = {};
	let geosearchJustFired = false;
	let photoVoiceService: Service | undefined;
	let serviceLoading = true;

	let centerPos: PointTuple = getStartingCenterPos();
	let locationFailed = false;

	$: step = linkResolver.createIssuePageGetCurrentStep($page.url);
	$: mapBounds = locationFailed ? libre311.getJurisdictionConfig().bounds : undefined;

	const icon = L.icon({
		iconUrl: WaypointOpen,
		...iconPositionOpts(128 / 169, 45, 'bottom-center')
	});

	const componentMap: Map<CreateServiceRequestSteps, ComponentType> = new Map();
	componentMap.set(CreateServiceRequestSteps.PHOTO, UploadFile);
	componentMap.set(CreateServiceRequestSteps.CONTACT_INFO, ContactInformation);

	onMount(async () => {
		try {
			const photoVoiceCode = $jurisdictionStore.photo_voice_service_code;
			if (photoVoiceCode) {
				const services = await libre311.getServiceList();
				photoVoiceService = services.find((s) => s.service_code === photoVoiceCode);
			}
		} catch (err) {
			alertError(err);
		} finally {
			serviceLoading = false;
		}
	});

	function getStartingCenterPos(): PointTuple {
		const center = L.latLngBounds(libre311.getJurisdictionConfig().bounds).getCenter();
		return [center.lat, center.lng];
	}

	function handleChange(e: CustomEvent<Partial<CreateServiceRequestParams>>) {
		params = { ...params, ...e.detail };
		goto(linkResolver.createIssuePageNext($page.url));
	}

	function handleLocationFound(e: CustomEvent<L.LatLng>) {
		const { lat, lng } = e.detail;
		const turfPoint = turf.point([lat, lng]);
		const boundsPoly = turf.polygon([libre311.getJurisdictionConfig().bounds]);
		if (!turf.booleanPointInPolygon(turfPoint, boundsPoly)) {
			locationFailed = true;
		}
	}

	function handleLocationError() {
		locationFailed = true;
	}

	function boundsChanged(e: CustomEvent<L.LatLngBounds>) {
		const center = e.detail.getCenter();
		centerPos = [center.lat, center.lng];
		if (step !== CreateServiceRequestSteps.LOCATION) return;
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

	function isPhotoVoiceUIParams(
		partial: Partial<CreateServiceRequestUIParams>
	): partial is CreateServiceRequestUIParams {
		return !!(partial?.address_string && partial?.attributeMap && partial?.service);
	}

	async function cancel() {
		await goto(linkResolver.issuesMap($page.url));
	}
</script>

<CreateServiceRequestLayout {step}>
	<div slot="side-bar" class="h-full">
		<div class="mx-4 h-full pb-2 pt-4">
			<h3 class="ml-4 text-lg font-semibold">{messages['photoVoice']['create']}</h3>
			{#if serviceLoading}
				<p class="mt-4 text-sm text-gray-500">Loading...</p>
			{:else if !photoVoiceService}
				<p class="mt-4 text-sm text-gray-500">
					Photo Voice is not currently enabled. An administrator must enable it from System
					Administration.
				</p>
			{:else if step === CreateServiceRequestSteps.LOCATION}
				<SelectLocation on:confirmLocation={confirmLocation} on:cancel={cancel} />
			{:else if step === CreateServiceRequestSteps.DETAILS}
				<PhotoVoiceDetailsForm
					{params}
					service={photoVoiceService}
					on:stepChange={handleChange}
				/>
			{:else if step === CreateServiceRequestSteps.REVIEW}
				{#if isPhotoVoiceUIParams(params)}
					<ReviewServiceRequest
					{params}
					title={messages['photoVoice']['review_title']}
					submitLabel={messages['photoVoice']['button_submit']}
					on:submitted={() => {}}
				/>
				{:else}
					<p class="mt-4 text-sm text-gray-500">
						Something went wrong. <button class="underline" on:click={cancel}>Start over</button>.
					</p>
				{/if}
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
			locateOpts={{ enableHighAccuracy: true }}
			on:boundsChanged={boundsChanged}
			on:locationfound={handleLocationFound}
			on:locationerror={handleLocationError}
		>
			<MapBoundaryPolygon bounds={libre311.getJurisdictionConfig().bounds} />
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
				<Button type="primary" on:click={cancel}>Cancel</Button>
				<Button on:click={confirmLocation} type="primary">Select Location</Button>
			</div>
		</Breakpoint>
	</div>
</CreateServiceRequestLayout>
