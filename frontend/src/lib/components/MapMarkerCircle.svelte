<script lang="ts">
  import MapMarker from "./MapMarker.svelte";

  // Icons
  import Closed from '$lib/assets/closed.png';
  import Open from '$lib/assets/open.png';

  import type { ServiceRequest } from "$lib/services/Libre311/Libre311";

  export let serviceRequest: ServiceRequest;

  const circleLookupMap = {
    'Closed': Closed,
    'Open': Open
  }
</script>

<MapMarker latLng={[+serviceRequest.lat, +serviceRequest.long]} >
  <img alt={serviceRequest.status}
    class="size-8"
    src={circleLookupMap[serviceRequest.status]} 
  />
</MapMarker>