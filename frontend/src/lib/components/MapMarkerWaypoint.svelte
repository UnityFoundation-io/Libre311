<script lang="ts">
  import MapMarker from "./MapMarker.svelte";

  // Icons
  import WaypointClosed from '$lib/assets/waypoint-closed.png';
  import WaypointOpen from '$lib/assets/waypoint-open.png';

	import type { ServiceRequest } from "$lib/services/Libre311/Libre311";
  
  export let serviceRequest: ServiceRequest;

  const waypointLookupMap = {
    'Closed': WaypointClosed,
    'Open': WaypointOpen
  }
</script>

<MapMarker latLng={[+serviceRequest.lat, +serviceRequest.long]} >
  <img alt={serviceRequest.status}
    class="size-10"
    src={waypointLookupMap[serviceRequest.status]} 
  />
</MapMarker>