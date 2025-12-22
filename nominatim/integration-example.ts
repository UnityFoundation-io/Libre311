/**
 * Leaflet-Geosearch Integration Example for Local Nominatim
 * =========================================================
 *
 * This file demonstrates how to configure leaflet-geosearch to use
 * a local Nominatim instance instead of the public OSM API.
 */

import { OpenStreetMapProvider } from 'leaflet-geosearch';

// Configuration for local Nominatim instance
const LOCAL_NOMINATIM_URL = 'http://localhost:8088';

/**
 * Option 1: Custom Provider Configuration
 * Uses leaflet-geosearch's OpenStreetMapProvider with custom endpoint
 */
export const localNominatimProvider = new OpenStreetMapProvider({
	params: {
		// Optional: Add email for identification (good practice even for local)
		email: 'dev@example.com',
		// Limit results to St. Louis metro area bounding box
		viewbox: '-90.7,38.4,-89.8,38.9',
		bounded: 1,
		// Include address breakdown in results
		addressdetails: 1,
		// Response format
		format: 'json'
	},
	// Override the default search URL
	searchUrl: `${LOCAL_NOMINATIM_URL}/search`,
	// Override the default reverse URL
	reverseUrl: `${LOCAL_NOMINATIM_URL}/reverse`
});

/**
 * Option 2: Environment-based Configuration
 * Automatically switches between local and public API based on environment
 */
export function createGeoSearchProvider() {
	const isLocalDev = import.meta.env.DEV || process.env.NODE_ENV === 'development';
	const nominatimUrl = import.meta.env.VITE_NOMINATIM_URL || LOCAL_NOMINATIM_URL;

	if (isLocalDev) {
		console.log(`[Geocoding] Using local Nominatim: ${nominatimUrl}`);
		return new OpenStreetMapProvider({
			params: {
				viewbox: '-90.7,38.4,-89.8,38.9',
				bounded: 1,
				addressdetails: 1
			},
			searchUrl: `${nominatimUrl}/search`,
			reverseUrl: `${nominatimUrl}/reverse`
		});
	}

	// Production: use public OSM API (with rate limiting considerations)
	console.log('[Geocoding] Using public OpenStreetMap Nominatim');
	return new OpenStreetMapProvider({
		params: {
			countrycodes: 'us',
			addressdetails: 1
		}
	});
}

/**
 * Usage Example: Forward Geocoding
 */
export async function searchAddress(query: string) {
	const provider = createGeoSearchProvider();

	try {
		const results = await provider.search({ query });

		if (results.length === 0) {
			console.log('No results found for:', query);
			return null;
		}

		const bestMatch = results[0];
		console.log('Found:', {
			label: bestMatch.label,
			lat: bestMatch.y,
			lon: bestMatch.x,
			bounds: bestMatch.bounds
		});

		return {
			label: bestMatch.label,
			lat: bestMatch.y,
			lng: bestMatch.x,
			bounds: bestMatch.bounds,
			raw: bestMatch.raw
		};
	} catch (error) {
		console.error('Geocoding failed:', error);
		throw error;
	}
}

/**
 * Usage Example: Reverse Geocoding
 * Note: leaflet-geosearch doesn't have built-in reverse geocoding,
 * so we use fetch directly
 */
export async function reverseGeocode(lat: number, lon: number) {
	const nominatimUrl = import.meta.env.VITE_NOMINATIM_URL || LOCAL_NOMINATIM_URL;

	try {
		const response = await fetch(
			`${nominatimUrl}/reverse?lat=${lat}&lon=${lon}&format=json&addressdetails=1`
		);

		if (!response.ok) {
			throw new Error(`Reverse geocoding failed: ${response.status}`);
		}

		const data = await response.json();

		return {
			label: data.display_name,
			address: data.address,
			lat: parseFloat(data.lat),
			lng: parseFloat(data.lon)
		};
	} catch (error) {
		console.error('Reverse geocoding failed:', error);
		throw error;
	}
}

/**
 * Integration with Leaflet Map
 * Shows how to add geosearch control to a Leaflet map
 */
export function addGeoSearchToMap(map: L.Map) {
	// Import required modules
	const { GeoSearchControl } = require('leaflet-geosearch');

	const provider = createGeoSearchProvider();

	const searchControl = new GeoSearchControl({
		provider: provider,
		style: 'bar',
		showMarker: true,
		showPopup: true,
		autoClose: true,
		retainZoomLevel: false,
		animateZoom: true,
		keepResult: true,
		searchLabel: 'Enter address...'
	});

	map.addControl(searchControl);

	// Listen for search results
	map.on('geosearch/showlocation', (result: any) => {
		console.log('Location selected:', result.location);
	});

	return searchControl;
}

/**
 * Example: Svelte Component Integration
 * For use in Libre311's SvelteKit frontend
 */
export const svelteExample = `
<script lang="ts">
  import { onMount } from 'svelte';
  import { OpenStreetMapProvider } from 'leaflet-geosearch';

  let searchQuery = '';
  let results: any[] = [];
  let isLoading = false;

  // Configure provider for local Nominatim
  const provider = new OpenStreetMapProvider({
    params: {
      viewbox: '-90.7,38.4,-89.8,38.9',
      bounded: 1,
      addressdetails: 1
    },
    searchUrl: 'http://localhost:8088/search',
    reverseUrl: 'http://localhost:8088/reverse'
  });

  async function handleSearch() {
    if (!searchQuery.trim()) return;

    isLoading = true;
    try {
      results = await provider.search({ query: searchQuery });
    } catch (error) {
      console.error('Search failed:', error);
      results = [];
    } finally {
      isLoading = false;
    }
  }

  function selectResult(result: any) {
    // Emit coordinates for map centering
    const event = new CustomEvent('locationSelected', {
      detail: { lat: result.y, lng: result.x, label: result.label }
    });
    window.dispatchEvent(event);
  }
</script>

<div class="geosearch">
  <input
    type="text"
    bind:value={searchQuery}
    on:keyup={(e) => e.key === 'Enter' && handleSearch()}
    placeholder="Search address..."
  />
  <button on:click={handleSearch} disabled={isLoading}>
    {isLoading ? 'Searching...' : 'Search'}
  </button>

  {#if results.length > 0}
    <ul class="results">
      {#each results as result}
        <li on:click={() => selectResult(result)}>
          {result.label}
        </li>
      {/each}
    </ul>
  {/if}
</div>
`;

/**
 * Environment Variables (.env) for Frontend
 */
export const envExample = `
# Add to frontend/.env or frontend/.env.local

# For local development with local Nominatim
VITE_NOMINATIM_URL=http://localhost:8088

# For production (use public API or your hosted instance)
# VITE_NOMINATIM_URL=https://nominatim.openstreetmap.org
`;