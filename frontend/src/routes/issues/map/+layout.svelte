<script lang="ts">
	import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';

  // Map imports
  import MapMarker from '$lib/components/MapMarker.svelte';
  import OpenStreetMap from '$lib/components/OpenStreetMap.svelte';
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

	// create the issues context / or load issues and set to state value
</script>

<SideBarMainContentLayout>
	<slot slot="side-bar" />
	<div slot="main-content">
    <div class="w-full h-screen">
      <OpenStreetMap view={initialView} zoom={14}>
        {#each markerLocations as latLng}
          <MapMarker {latLng}>
            <img alt="acknowledged" 
              class="size-12"
              src={WaypointAcknowledged} 
            />
          </MapMarker>
        {/each}

      </OpenStreetMap>
    </div>
  </div>
</SideBarMainContentLayout>


