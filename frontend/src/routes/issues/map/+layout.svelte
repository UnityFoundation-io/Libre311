<script lang="ts">
	import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';
  import { useSelectedServiceRequestStore, useServiceRequestsResponseStore } from '$lib/context/ServiceRequestsContext';

  // Map imports
  import L from 'leaflet';
  import MapComponent from '$lib/components/MapComponent.svelte';
  import MapMarkerCircle from '$lib/components/MapMarkerCircle.svelte'
  import MapMarkerWaypoint from '$lib/components/MapMarkerWaypoint.svelte';
  import Breakpoint from '$lib/components/Breakpoint.svelte';
  import MapListToggle from '$lib/components/MapListToggle.svelte';

  // Type imports
  import type { LatLngExpression } from 'leaflet';
	import type { Maybe } from '$lib/utils/types';
	import type { ServiceRequest, ServiceRequestsResponse } from '$lib/services/Libre311/Libre311';
	import type { AsyncResult } from '$lib/services/http';

  const serviceRequestsResponseStore = useServiceRequestsResponseStore();
  const selectedServiceRequestStore = useSelectedServiceRequestStore();

  const initialView: LatLngExpression = [41.3083092093462, -72.9258607025516];

  $: mapBounds = createMapBounds($serviceRequestsResponseStore)

  function isSelected(serviceRequest: ServiceRequest, selectedServiceRequest: Maybe<ServiceRequest>) {
    return (serviceRequest.service_request_id === selectedServiceRequest?.service_request_id) 
  }
  
  function createMapBounds(res: AsyncResult<ServiceRequestsResponse>) {
    if (res.type !== 'success') {
      return L.latLngBounds([initialView]);
    }
    const latLngs: LatLngExpression[] = res.value.serviceRequests.map(req => [+req.lat, +req.long]);
    return L.latLngBounds(latLngs);
  }
</script>

<SideBarMainContentLayout>
  <slot slot="side-bar" />
  <div slot="main-content">
    <div class="w-full h-screen relative flex">
      <Breakpoint>
        <div class="absolute left-1/2 top-5 z-[1] -translate-x-1/2" slot="is-mobile">
          <MapListToggle/>
        </div>
      </Breakpoint>
      <MapComponent bounds={mapBounds} zoom={16}>
        {#if $serviceRequestsResponseStore.type === 'success'}
          {#each $serviceRequestsResponseStore.value.serviceRequests as req}
            {#if isSelected(req, $selectedServiceRequestStore)}
              <MapMarkerWaypoint serviceRequest={req} />
            {:else}
              <MapMarkerCircle serviceRequest={req} />
            {/if}
          {/each}
        {/if}
      </MapComponent>
    </div>
  </div>
</SideBarMainContentLayout>
