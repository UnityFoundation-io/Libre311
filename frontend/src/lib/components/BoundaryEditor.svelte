<script lang="ts">
	import { onMount, createEventDispatcher, getContext, afterUpdate } from 'svelte';
	import L from 'leaflet';
	import 'leaflet-draw';
	import 'leaflet-draw/dist/leaflet.draw.css';
	import * as turf from '@turf/turf';

	export let bounds: L.LatLngExpression[][] = [];
	export let constraintBounds: L.LatLngExpression[][] = [];

	const dispatch = createEventDispatcher();
	const mapContext = getContext<{ getMap: () => L.Map } | undefined>('map');

	let drawnItems: L.FeatureGroup;
	let drawControl: L.Control.Draw;
	let constraintLayer: L.Polygon;
	let lastValidLayer: L.Polygon | null = null;
	let map: L.Map;

	onMount(() => {
		const mapInstance = mapContext?.getMap();
		if (!mapInstance) {
			console.error('BoundaryEditor must be used inside a MapComponent');
			return;
		}
		map = mapInstance;

		drawnItems = new L.FeatureGroup();
		map.addLayer(drawnItems);

		// Draw jurisdiction boundary for context
		if (constraintBounds && constraintBounds.length > 0) {
			constraintLayer = L.polygon(constraintBounds, {
				color: '#ff7800',
				weight: 2,
				opacity: 0.5,
				fill: false,
				interactive: false
			}).addTo(map);
		}

		initializeExistingBounds();

		drawControl = new L.Control.Draw({
			draw: {
				polygon: {
					allowIntersection: false,
					showArea: true
				},
				polyline: false,
				rectangle: false,
				circle: false,
				marker: false,
				circlemarker: false
			},
			edit: {
				featureGroup: drawnItems
			}
		});
		map.addControl(drawControl);

		map.on(L.Draw.Event.CREATED, handleDrawEvent);
		map.on(L.Draw.Event.EDITED, handleDrawEvent);

		map.on(L.Draw.Event.DELETED, () => {
			lastValidLayer = null;
			dispatch('update', []);
		});
	});

	afterUpdate(() => {
		// If bounds are reset from parent, re-initialize
		if (map && bounds.length === 0 && drawnItems.getLayers().length > 0) {
			drawnItems.clearLayers();
			lastValidLayer = null;
		} else if (map && bounds.length > 0 && drawnItems.getLayers().length === 0) {
			initializeExistingBounds();
		}
	});

	function initializeExistingBounds() {
		if (bounds && bounds.length > 0) {
			const polygon = L.polygon(bounds as L.LatLngExpression[]);
			if (validateAndStore(polygon)) {
				drawnItems.addLayer(polygon);
			}
		}
	}

	function handleDrawEvent(e: L.Draw.CreatedEvent | L.Draw.EditedEvent) {
		let layer: L.Polygon;

		if (e.type === 'draw:created') {
			layer = e.layer as L.Polygon;
		} else {
			// For edits, we only handle one layer at a time for simplicity
			const layers = (e as L.Draw.EditedEvent).layers;
			const editedLayer = layers.getLayers()[0];
			if (editedLayer instanceof L.Polygon) {
				layer = editedLayer;
			} else {
				return;
			}
		}

		if (validateAndStore(layer)) {
			if (e.type === 'draw:created') {
				drawnItems.clearLayers();
				drawnItems.addLayer(layer);
			}
			updateBounds(layer);
		} else {
			alert('Project boundary must be completely within the jurisdiction boundary.');
			// Revert the visual change
			if (e.type === 'draw:created') {
				layer.remove(); // remove the invalid new shape
			} else {
				// Revert edit by re-adding the last valid layer state
				drawnItems.clearLayers();
				if (lastValidLayer) {
					drawnItems.addLayer(L.polygon(lastValidLayer.getLatLngs()));
				}
			}
		}
	}

	function validateAndStore(layer: L.Polygon): boolean {
		if (!constraintLayer) return true; // No constraint to validate against

		const newPolygonGeoJSON = layer.toGeoJSON();
		const jurisdictionGeoJSON = constraintLayer.toGeoJSON();

		// Ensure the new polygon is valid
		const cleanPolygon = turf.cleanCoords(newPolygonGeoJSON);
		
		if (turf.booleanWithin(cleanPolygon, jurisdictionGeoJSON)) {
			lastValidLayer = L.polygon(layer.getLatLngs()); // Store a copy
			return true;
		}
		return false;
	}

	function updateBounds(layer: L.Polygon) {
		const latLngs = layer.getLatLngs() as L.LatLng[][];
		if (latLngs.length > 0) {
			const newBounds = latLngs[0].map((latlng) => [latlng.lat, latlng.lng]);
			// leaflet-draw doesn't close the ring, but our backend expects it.
			if (newBounds.length > 0) {
				newBounds.push(newBounds[0]);
			}
			dispatch('update', newBounds);
		}
	}
</script>
