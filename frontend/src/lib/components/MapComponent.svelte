<script lang="ts" module>
	export type Events = {
		boundsChanged: L.LatLngBounds;
	};

	type ControlFactory = (opts: L.ControlOptions) => L.Control;
</script>

<script lang="ts">
	import { run } from 'svelte/legacy';

	import { onMount, onDestroy, setContext, createEventDispatcher } from 'svelte';
	import L from 'leaflet';
	import 'leaflet/dist/leaflet.css';

	interface Props {
		locateOpts?: L.LocateOptions | undefined;
		bounds?: L.LatLngBoundsExpression | undefined;
		center?: L.LatLngExpression | undefined;
		zoom?: number | undefined;
		disabled?: boolean;
		controlFactories?: Array<ControlFactory>;
		controlOps?: L.ControlOptions;
		children?: import('svelte').Snippet;
	}

	let {
		locateOpts = undefined,
		bounds = undefined,
		center = undefined,
		zoom = undefined,
		disabled = false,
		controlFactories = [],
		controlOps = { position: 'topleft' },
		children
	}: Props = $props();

	const dispatch = createEventDispatcher<Events>();

	let map: L.Map = $state();
	let mapElement: HTMLElement = $state();

	onMount(() => {
		map = L.map(mapElement);
		map.addEventListener('moveend', () => {
			dispatch('boundsChanged', map.getBounds());
		});
		map.addEventListener('drag', () => dispatch('boundsChanged', map.getBounds()));
		map.on('locationfound', function (e) {
			map.setView(e.latlng, 16);
		});

		controlFactories.forEach((factory) => factory(controlOps).addTo(map));

		L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
			attribution: `&copy;<a href="https://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>,&copy;<a href="https://carto.com/attributions" target="_blank">CARTO</a>`
		}).addTo(map);
	});

	onDestroy(() => {
		map?.remove();
	});

	setContext('map', {
		getMap: () => map
	});


	function toggleDisabled(map: L.Map | undefined, disabled: boolean) {
		if (!map) return;
		const controlContainer = document.querySelector<HTMLElement>('.leaflet-control-container');
		if (disabled) {
			if (controlContainer) {
				controlContainer.style.display = 'none';
			}
			map.touchZoom.disable();
			map.doubleClickZoom.disable();
			map.scrollWheelZoom.disable();
			map.dragging.disable();
		} else {
			if (controlContainer) {
				controlContainer.style.display = 'unset';
			}
			map.touchZoom.enable();
			map.doubleClickZoom.enable();
			map.scrollWheelZoom.enable();
			map.dragging.enable();
		}
	}

	run(() => {
		toggleDisabled(map, disabled);
	});
	run(() => {
		if (map) {
			if (bounds) {
				map.fitBounds(bounds);
			} else if (center && zoom) {
				map.setView(center, zoom);
			} else if (locateOpts) {
				map.locate(locateOpts);
			}
		}
	});
</script>

<div class="z-0 h-full w-full" bind:this={mapElement}>
	{#if map}
		{@render children?.()}
	{/if}
</div>
