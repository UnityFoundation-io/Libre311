<script lang="ts">
  import { onMount, onDestroy, getContext, setContext } from 'svelte';
  import L from 'leaflet';

  export let latLng: L.LatLngExpression;

  let marker: L.Marker | undefined;
  let markerElement: HTMLElement;

  const { getMap }: { getMap: () => L.Map | undefined } = getContext('map');
  const map = getMap();

  setContext('layer', {
    getLayer: () => marker
  });

  onMount(() => {
    if (map) {
      let icon = L.divIcon({
        html: markerElement,
        className: 'map-marker',
        iconSize: [12, 12],
        iconAnchor: [12, 12]
      });

      marker = L.marker(latLng, { icon }).addTo(map);
    }
  });

  onDestroy(() => {
    marker?.remove();
    marker = undefined;
  })

</script>

<div bind:this={markerElement}>
  {#if marker}
    <slot />
  {/if}
</div>