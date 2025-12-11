<script lang="ts">
	import { run } from 'svelte/legacy';

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
	import { page } from '$app/stores';
	import ServiceRequestDetailsForm from '$lib/components/CreateServiceRequest/ServiceRequestDetailsForm.svelte';
	import CreateServiceRequestLayout from '$lib/components/CreateServiceRequest/CreateServiceRequestLayout.svelte';
	import { mapCenterControlFactory } from '$lib/components/MapCenterControl';
    import {Button} from "$lib/components/ui/button";
    import {Spinner} from "$lib/components/ui/spinner";

	const libre311 = useLibre311Service();
	const linkResolver = useLibre311Context().linkResolver;

	let params: Partial<CreateServiceRequestUIParams> = $state({});
	let centerPos: PointTuple = $state(getStartingCenterPos());
	let loadingLocation: boolean = $state(false);

	let step = $derived(linkResolver.createIssuePageGetCurrentStep($page.url));

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
		params.lat = String(centerPos[0]);
		params.long = String(centerPos[1]);
		loadingLocation = true;
		const res = await libre311.reverseGeocode(centerPos);
		loadingLocation = false;
		params.address_string = res.display_name;
		params = params;
		goto(linkResolver.createIssuePageNext($page.url));
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
	run(() => {
		if (step == CreateServiceRequestSteps.REVIEW && !isCreateServiceRequestUIParams(params)) {
			goto('/issue/create');
		}
	});
</script>

<svelte:head>
	<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no" />
</svelte:head>

<CreateServiceRequestLayout {step}>
	<!-- @migration-task: migrate this slot by hand, `side-bar` is an invalid identifier -->
	<div slot="side-bar" class="mx-4 h-full">
		{#if step == CreateServiceRequestSteps.LOCATION}
			<SelectLocation loading={loadingLocation} on:confirmLocation={confirmLocation} />
		{:else if step == CreateServiceRequestSteps.REVIEW && isCreateServiceRequestUIParams(params)}
			<ReviewServiceRequest {params} />
		{:else}
			{@const SvelteComponent = componentMap.get(step)}
			<SvelteComponent {params} on:stepChange={handleChange} />
		{/if}
	</div>
	<!-- @migration-task: migrate this slot by hand, `main-content` is an invalid identifier -->
	<div slot="main-content" class="relative h-full">
		<MapComponent
			controlFactories={[mapCenterControlFactory]}
			disabled={step != 0}
			locateOpts={{ setView: true, enableHighAccuracy: true }}
			on:boundsChanged={boundsChanged}
		>
			<MapMarker latLng={centerPos} options={{ icon }} />
			{#if step == CreateServiceRequestSteps.LOCATION}
				<MapGeosearch on:geosearch={handleGeosearch} />
			{/if}
		</MapComponent>
		<Breakpoint>
			<!-- @migration-task: migrate this slot by hand, `is-mobile-or-tablet` is an invalid identifier -->
	<div
				class="display absolute inset-x-0 bottom-6 flex justify-center gap-2"
				slot="is-mobile-or-tablet"
			>
				<Button href={linkResolver.issuesMap($page.url)}>Cancel</Button>
        {#if loadingLocation}
				<Button disabled><Spinner /> Select Location</Button>
            {:else}
            <Button on:click={confirmLocation}>Select Location</Button>
            {/if}
			</div>
		</Breakpoint>
	</div>
</CreateServiceRequestLayout>
