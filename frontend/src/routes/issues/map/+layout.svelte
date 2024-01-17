<script lang="ts">
	import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';

  // Map imports
  import OpenStreetMap from '$lib/components/OpenStreetMap.svelte';
  import type { LatLngExpression } from 'leaflet';

  // Initial View for map
  const initialView: LatLngExpression = [38.79046, -90.48648];
	// create the issues context / or load issues and set to state value
</script>

<SideBarMainContentLayout>
	<slot slot="side-bar" />
	<div slot="main-content">
    <div class="w-full h-screen">
      <OpenStreetMap view={initialView} zoom={14}>
      </OpenStreetMap>
    </div>
  </div>
</SideBarMainContentLayout>
