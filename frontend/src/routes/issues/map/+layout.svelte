<script lang="ts">
	import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';
	import ServiceRequestsContextProvider from '$lib/context/ServiceRequestsContextProvider.svelte';
  import { useServiceRequestsContext } from '$lib/context/ServiceRequestsContext';

	const ctx = useServiceRequestsContext();
	const resStore = ctx.serviceRequestsResponse;

	$: console.log($resStore);

  // Map imports
  import MapComponent from '$lib/components/MapComponent.svelte';
  import MapMarker from '$lib/components/MapMarker.svelte';
  import type { LatLngExpression } from 'leaflet';

  // Icon imports
  import WaypointAcknowledged from '$lib/assets/waypoint-acknowledged.png';
  import WaypointArchived from '$lib/assets/waypoint-archived.png';
  import WaypointClosed from '$lib/assets/waypoint-closed.png';
  import WaypointOpen from '$lib/assets/waypoint-open.png';

  const initialView: LatLngExpression = [38.79046, -90.48648];
  const markerLocations: Array<LatLngExpression> = [
    [38.79046, -90.48648],
    [38.79125, -90.47737],
    [38.78201, -90.48866]
  ]
</script>

<ServiceRequestsContextProvider>
	<SideBarMainContentLayout>
		<slot slot="side-bar" />
		<div slot="main-content">
      <div class="w-full h-screen">
        <MapComponent view={initialView} zoom={14}>
          {#each markerLocations as latLng}
            <MapMarker {latLng}>
              <img alt="acknowledged" 
                class="size-12"
                src={WaypointAcknowledged} 
              />
            </MapMarker>
          {/each}
        </MapComponent>
      </div>
    </div>
	</SideBarMainContentLayout>
</ServiceRequestsContextProvider>

