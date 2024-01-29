<script lang="ts">
	import SelectLocation from '$lib/components/CreateServiceRequest/SelectLocation.svelte';
	import UploadFile from '$lib/components/CreateServiceRequest/UploadFileDesktop.svelte';
	import MapComponent from '$lib/components/MapComponent.svelte';

	import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';
	import WaypointOpen from '$lib/assets/waypoint-open.png';
	import type { CreateServiceRequestParams } from '$lib/services/Libre311/Libre311';
	import { iconPositionOpts } from '$lib/utils/functions';

	import L, { type PointTuple } from 'leaflet';
	import MapMarker from '$lib/components/MapMarker.svelte';
	import type { ComponentType } from 'svelte';
	import { CreateServiceRequestSteps } from '$lib/components/CreateServiceRequest/types';
	import { goto } from '$app/navigation';

	let step: CreateServiceRequestSteps = CreateServiceRequestSteps.LOCATION;
	let params: Readonly<Partial<CreateServiceRequestParams>> = {};
	let centerPos: PointTuple = [41.308281, -72.924164];

	const componentMap: Map<CreateServiceRequestSteps, ComponentType> = new Map();
	componentMap.set(CreateServiceRequestSteps.LOCATION, SelectLocation);
	componentMap.set(CreateServiceRequestSteps.PHOTO, UploadFile);

	function handleChange(e: CustomEvent<Partial<CreateServiceRequestParams>>) {
		const changedParams = e.detail;
		params = { ...params, ...changedParams };
		goto(`/issue/create?step=${++step}`);
	}

	const icon = L.icon({
		iconUrl: WaypointOpen,
		...iconPositionOpts(128 / 169, 35, 'bottom-center')
	});
</script>

<SideBarMainContentLayout sideBarBreakpointActive={step == CreateServiceRequestSteps.LOCATION}>
	<div slot="side-bar" class="h-full">
		{#if step == CreateServiceRequestSteps.LOCATION}
			<SelectLocation on:confirmLocation={() => console.log('confirm location')} />
		{:else if step == CreateServiceRequestSteps.REVIEW}
			<h1>review component</h1>
		{:else}
			<svelte:component this={componentMap.get(step)} {params} on:stepChange={handleChange} />
		{/if}
	</div>
	<div slot="main-content" class="relative h-full">
		<MapComponent
			center={[41.308281, -72.924164]}
			zoom={14}
			on:centerChanged={(e) => (centerPos = [e.detail.lat, e.detail.lng])}
		>
			<MapMarker on:click latLng={centerPos} options={{ icon }} />
		</MapComponent>
	</div>
</SideBarMainContentLayout>
