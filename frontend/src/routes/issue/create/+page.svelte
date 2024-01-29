<script lang="ts">
	import SelectLocation from '$lib/components/CreateServiceRequest/SelectLocation.svelte';
	import MapComponent from '$lib/components/MapComponent.svelte';
	import ContactInformation from '$lib/components/CreateServiceRequest/ContactInformation.svelte';
	import ReviewServiceRequest from '$lib/components/CreateServiceRequest/ReviewServiceRequest.svelte';

	import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';
	import WaypointOpen from '$lib/assets/waypoint-open.png';
	import type { CreateServiceRequestParams } from '$lib/services/Libre311/Libre311';
	import { iconPositionOpts } from '$lib/utils/functions';

	import L, { type PointTuple } from 'leaflet';
	import MapMarker from '$lib/components/MapMarker.svelte';
	import type { ComponentType } from 'svelte';
	import { CreateServiceRequestSteps } from '$lib/components/CreateServiceRequest/types';
	import { goto } from '$app/navigation';
	import MapGeosearch from '$lib/components/MapGeosearch.svelte';
	import type { ComponentEvents } from 'svelte';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';
	import Breakpoint from '$lib/components/Breakpoint.svelte';
	import { Button } from 'stwui';
	import { page } from '$app/stores';

	let step: CreateServiceRequestSteps = CreateServiceRequestSteps.LOCATION;
	let params: Partial<CreateServiceRequestParams> = {};
	let centerPos: PointTuple = [41.308281, -72.924164]; // todo we need to set the starting point on per tenant basis. decide if bounds or center/zoom or both will be used.
	let loadingLocation: boolean = false;

	const libre311 = useLibre311Service();
	const linkResolver = useLibre311Context().linkResolver;
	const icon = L.icon({
		iconUrl: WaypointOpen,
		...iconPositionOpts(128 / 169, 45, 'bottom-center')
	});
	const componentMap: Map<CreateServiceRequestSteps, ComponentType> = new Map();
	componentMap.set(CreateServiceRequestSteps.LOCATION, SelectLocation);
	componentMap.set(CreateServiceRequestSteps.CONTACT_INFO, ContactInformation);
	componentMap.set(CreateServiceRequestSteps.REVIEW, ReviewServiceRequest);

	function gotoNextStep() {
		goto(`/issue/create?step=${++step}`);
	}

	function handleChange(e: CustomEvent<Partial<CreateServiceRequestParams>>) {
		const changedParams = e.detail;
		params = { ...params, ...changedParams };
		gotoNextStep();
	}

	function boundsChanged(e: CustomEvent<L.LatLngBounds>) {
		const center = e.detail.getCenter();
		centerPos = [center.lat, center.lng];
	}

	async function confirmLocation() {
		params.lat = String(centerPos[0]);
		params.lng = String(centerPos[1]);
		if (!params.address_string) {
			loadingLocation = true;
			const res = await libre311.reverseGeocode(centerPos);
			loadingLocation = false;
			params.address_string = res.display_name;
		}
		params = params;
		gotoNextStep();
	}

	function handleGeosearch(e: ComponentEvents<MapGeosearch>['geosearch']) {
		const location = e.detail.location;
		centerPos = [location.y, location.x];
		params.address_string = location.label;
	}
</script>

<SideBarMainContentLayout sideBarBreakpointActive={step == CreateServiceRequestSteps.LOCATION}>
	<div slot="side-bar" class="h-full">
		{#if step == CreateServiceRequestSteps.LOCATION}
			<SelectLocation loading={loadingLocation} on:confirmLocation={confirmLocation} />
		{:else if step == CreateServiceRequestSteps.REVIEW}
			<ReviewServiceRequest {params} />
		{:else}
			<svelte:component this={componentMap.get(step)} {params} on:stepChange={handleChange} />
		{/if}
	</div>
	<div slot="main-content" class="relative h-full">
		<MapComponent center={[41.308281, -72.924164]} zoom={14} on:boundsChanged={boundsChanged}>
			<MapMarker on:click latLng={centerPos} options={{ icon }} />
			<MapGeosearch on:geosearch={handleGeosearch} />
		</MapComponent>
		<Breakpoint>
			<div class="display absolute inset-x-0 bottom-6 flex justify-center gap-2" slot="is-mobile">
				<Button type="primary" href={linkResolver.issuesMap($page.url)}>Cancel</Button>
				<Button loading={loadingLocation} on:click={confirmLocation} type="primary"
					>Select Location</Button
				>
			</div>
		</Breakpoint>
	</div>
</SideBarMainContentLayout>
