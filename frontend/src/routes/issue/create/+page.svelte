<script lang="ts">
	import SelectLocation from '$lib/components/CreateServiceRequest/SelectLocation.svelte';
	import UploadFile from '$lib/components/CreateServiceRequest/UploadFile.svelte';
	import MapComponent from '$lib/components/MapComponent.svelte';
	import ContactInformation from '$lib/components/CreateServiceRequest/ContactInformation.svelte';
	import ReviewServiceRequest from '$lib/components/CreateServiceRequest/ReviewServiceRequest.svelte';

	import WaypointOpen from '$lib/assets/waypoint-open.png';
	import type { CreateServiceRequestParams } from '$lib/services/Libre311/Libre311';
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
	import Breakpoint from '$lib/components/Breakpoint.svelte';
	import { Button } from 'stwui';
	import { page } from '$app/stores';
	import ServiceRequestDetailsForm from '$lib/components/CreateServiceRequest/ServiceRequestDetailsForm.svelte';
	import CreateServiceRequestLayout from '$lib/components/CreateServiceRequest/CreateServiceRequestLayout.svelte';
	import { mapCenterControlFactory } from '$lib/components/MapCenterControl';

	const libre311 = useLibre311Service();
	const linkResolver = useLibre311Context().linkResolver;

	let params: Partial<CreateServiceRequestUIParams> = {};
	let centerPos: PointTuple = getStartingCenterPos();
	let loadingLocation: boolean = false;

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
	}

	async function confirmLocation() {
		const startTime = performance.now();
		console.log('[confirmLocation] Started at:', new Date().toISOString());

		params.lat = String(centerPos[0]);
		params.long = String(centerPos[1]);
		console.log(
			'[confirmLocation] Coords set:',
			{ lat: params.lat, long: params.long },
			`(+${(performance.now() - startTime).toFixed(1)}ms)`
		);

		loadingLocation = true;

		try {
			console.log('[confirmLocation] Starting reverseGeocode...');
			const geocodeStart = performance.now();
			const res = await libre311.reverseGeocode(centerPos);
			console.log(
				'[confirmLocation] reverseGeocode completed in:',
				`${(performance.now() - geocodeStart).toFixed(1)}ms`,
				'Result:',
				res.display_name
			);

			params.address_string = res.display_name;
			params = params;
			console.log(
				'[confirmLocation] Params updated, starting navigation...',
				`(+${(performance.now() - startTime).toFixed(1)}ms)`
			);

			const navStart = performance.now();
			const nextUrl = linkResolver.createIssuePageNext($page.url);
			console.log('[confirmLocation] Navigating to:', nextUrl);
			await goto(nextUrl);
			console.log(
				'[confirmLocation] Navigation completed in:',
				`${(performance.now() - navStart).toFixed(1)}ms`
			);
			console.log('[confirmLocation] Total time:', `${(performance.now() - startTime).toFixed(1)}ms`);
		} catch (error) {
			console.error('[confirmLocation] Error:', error);
			// Use fallback coordinates as address if geocoding fails completely
			params.address_string = `${centerPos[0].toFixed(6)}, ${centerPos[1].toFixed(6)}`;
			params = params;
			// Still navigate to next step - don't block the user
			await goto(linkResolver.createIssuePageNext($page.url));
		} finally {
			loadingLocation = false;
		}
	}

	function handleGeosearch(e: ComponentEvents<MapGeosearch>['geosearch']) {
		const location = e.detail.location;
		centerPos = [location.y, location.x];
		params.address_string = location.label;
	}

	function isCreateServiceRequestUIParams(
		partial: Partial<CreateServiceRequestUIParams>
	): partial is CreateServiceRequestUIParams {
		if (partial?.address_string && partial?.attributeMap && partial?.service) return true;
		return false;
	}

	// Redirect user because they navigated to an invalid step
	$: if (step == CreateServiceRequestSteps.REVIEW && !isCreateServiceRequestUIParams(params)) {
		goto('/issue/create');
	}
</script>

<svelte:head>
	<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no" />
</svelte:head>

<CreateServiceRequestLayout {step}>
	<div slot="side-bar" class="mx-4 h-full">
		{#if step == CreateServiceRequestSteps.LOCATION}
			<SelectLocation loading={loadingLocation} on:confirmLocation={confirmLocation} />
		{:else if step == CreateServiceRequestSteps.REVIEW && isCreateServiceRequestUIParams(params)}
			<ReviewServiceRequest {params} />
		{:else}
			<svelte:component this={componentMap.get(step)} {params} on:stepChange={handleChange} />
		{/if}
	</div>
	<div slot="main-content" class="relative h-full">
		<MapComponent
			keyboardPanDelta={KEYBOARD_PAN_DELTA_FINE}
			controlFactories={[mapCenterControlFactory]}
			disabled={step != 0}
			locateOpts={{ setView: true, enableHighAccuracy: true }}
			on:boundsChanged={boundsChanged}
		>
			<MapBoundaryPolygon bounds={libre311.getJurisdictionConfig().bounds} />
			<MapMarker latLng={centerPos} options={{ icon }} />
			{#if step == CreateServiceRequestSteps.LOCATION}
				<MapGeosearch on:geosearch={handleGeosearch} />
			{/if}
		</MapComponent>
		<Breakpoint>
			<div
				class="display absolute inset-x-0 bottom-6 flex justify-center gap-2"
				slot="is-mobile-or-tablet"
			>
				<Button type="primary" href={linkResolver.issuesMap($page.url)}>Cancel</Button>
				<Button loading={loadingLocation} on:click={confirmLocation} type="primary"
					>Select Location</Button
				>
			</div>
		</Breakpoint>
	</div>
</CreateServiceRequestLayout>
