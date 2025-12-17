import { z } from 'zod';
import axios from 'axios';
import type { Mode } from '../mode';

// Import fixtures for mock mode
import reverseGeocodeFixture from './__fixtures__/reverse-geocode-st-louis.json';
import searchFixture from './__fixtures__/search-st-louis.json';

// Full Nominatim reverse geocode response schema (jsonv2 format)
export const NominatimAddressSchema = z.object({
	house_number: z.string().optional(),
	road: z.string().optional(),
	neighbourhood: z.string().optional(),
	suburb: z.string().optional(),
	city: z.string().optional(),
	town: z.string().optional(),
	village: z.string().optional(),
	county: z.string().optional(),
	state: z.string().optional(),
	'ISO3166-2-lvl4': z.string().optional(),
	postcode: z.string().optional(),
	country: z.string().optional(),
	country_code: z.string().optional()
});
export type NominatimAddress = z.infer<typeof NominatimAddressSchema>;

export const NominatimReverseResponseSchema = z.object({
	place_id: z.number(),
	licence: z.string(),
	osm_type: z.string(),
	osm_id: z.number(),
	lat: z.string(),
	lon: z.string(),
	category: z.string().optional(),
	type: z.string().optional(),
	place_rank: z.number(),
	importance: z.number(),
	addresstype: z.string(),
	name: z.string(),
	display_name: z.string(),
	address: NominatimAddressSchema.optional(),
	boundingbox: z.array(z.string()).optional()
});
export type NominatimReverseResponse = z.infer<typeof NominatimReverseResponseSchema>;

// Minimal schema for the display_name only (backwards compatible)
export const ReverseGeocodeResponseSchema = z.object({
	display_name: z.string()
});
export type ReverseGeocodeResponse = z.infer<typeof ReverseGeocodeResponseSchema>;

// Search response schema
export const NominatimSearchResultSchema = z.object({
	place_id: z.number(),
	licence: z.string(),
	osm_type: z.string(),
	osm_id: z.number(),
	lat: z.string(),
	lon: z.string(),
	class: z.string().optional(),
	type: z.string().optional(),
	place_rank: z.number(),
	importance: z.number(),
	addresstype: z.string(),
	name: z.string(),
	display_name: z.string(),
	boundingbox: z.array(z.string()).optional()
});
export type NominatimSearchResult = z.infer<typeof NominatimSearchResultSchema>;

export const NominatimSearchResponseSchema = z.array(NominatimSearchResultSchema);
export type NominatimSearchResponse = z.infer<typeof NominatimSearchResponseSchema>;

// Service interface
export interface NominatimService {
	reverseGeocode(lat: number, lon: number): Promise<NominatimReverseResponse>;
	search(query: string): Promise<NominatimSearchResponse>;
}

// Configuration
export interface NominatimConfig {
	baseUrl?: string;
	userAgent?: string;
}

const DEFAULT_CONFIG: Required<NominatimConfig> = {
	baseUrl: 'https://nominatim.openstreetmap.org',
	userAgent: 'Libre311/1.0 (https://github.com/UnityFoundation-io/Libre311)'
};

// Real implementation
export class NominatimServiceImpl implements NominatimService {
	private config: Required<NominatimConfig>;

	constructor(config: NominatimConfig = {}) {
		this.config = { ...DEFAULT_CONFIG, ...config };
	}

	private getBaseUrl(): string {
		// In development, use Vite proxy to bypass CORS
		if (typeof window !== 'undefined' && window.location.hostname === 'localhost') {
			return '/nominatim';
		}
		return this.config.baseUrl;
	}

	async reverseGeocode(lat: number, lon: number): Promise<NominatimReverseResponse> {
		console.log('NominatimServiceImpl.reverseGeocode called with:', { lat, lon });
		const baseUrl = this.getBaseUrl();
		const url = `${baseUrl}/reverse?format=jsonv2&lat=${lat}&lon=${lon}`;

		const res = await axios.get<unknown>(url);
		return NominatimReverseResponseSchema.parse(res.data);
	}

	async search(query: string): Promise<NominatimSearchResponse> {
		const baseUrl = this.getBaseUrl();
		const encodedQuery = encodeURIComponent(query);
		const url = `${baseUrl}/search?format=json&q=${encodedQuery}`;

		const res = await axios.get<unknown>(url);
		return NominatimSearchResponseSchema.parse(res.data);
	}
}

// Mock implementation using fixtures
export class MockNominatimService implements NominatimService {
	private reverseResponses: Map<string, NominatimReverseResponse> = new Map();
	private searchResponses: Map<string, NominatimSearchResponse> = new Map();
	private defaultReverseResponse: NominatimReverseResponse | null = null;
	private defaultSearchResponse: NominatimSearchResponse | null = null;

	// Set a specific response for coordinates
	setReverseResponse(lat: number, lon: number, response: NominatimReverseResponse): void {
		const key = `${lat.toFixed(6)},${lon.toFixed(6)}`;
		this.reverseResponses.set(key, response);
	}

	// Set a default response for any coordinates
	setDefaultReverseResponse(response: NominatimReverseResponse): void {
		this.defaultReverseResponse = response;
	}

	// Set a specific response for a query
	setSearchResponse(query: string, response: NominatimSearchResponse): void {
		this.searchResponses.set(query.toLowerCase(), response);
	}

	// Set a default response for any query
	setDefaultSearchResponse(response: NominatimSearchResponse): void {
		this.defaultSearchResponse = response;
	}

	async reverseGeocode(lat: number, lon: number): Promise<NominatimReverseResponse> {
		console.log('MockNominatimService.reverseGeocode called with:', { lat, lon });

		const key = `${lat.toFixed(6)},${lon.toFixed(6)}`;
		const response = this.reverseResponses.get(key) || this.defaultReverseResponse;

		if (!response) {
			throw new Error(`No mock response configured for coordinates: ${lat}, ${lon}`);
		}

		return response;
	}

	async search(query: string): Promise<NominatimSearchResponse> {
		const response = this.searchResponses.get(query.toLowerCase()) || this.defaultSearchResponse;

		if (!response) {
			throw new Error(`No mock response configured for query: ${query}`);
		}

		return response;
	}

	// Reset all mocks
	reset(): void {
		this.reverseResponses.clear();
		this.searchResponses.clear();
		this.defaultReverseResponse = null;
		this.defaultSearchResponse = null;
	}
}

// Factory function to create a pre-configured mock service
export function createMockNominatimService(): MockNominatimService {
	const mock = new MockNominatimService();
	// Pre-configure with fixtures so it works out of the box
	mock.setDefaultReverseResponse(reverseGeocodeFixture as NominatimReverseResponse);
	mock.setDefaultSearchResponse(searchFixture as NominatimSearchResponse);
	console.log('MockNominatimService created with default fixtures');
	return mock;
}

// Factory function
export function nominatimServiceFactory(mode: Mode, config?: NominatimConfig): NominatimService {
	// Could either be in 'test' mode or explicitly set to 'mock' via env variable
	const nominatimMode = import.meta.env.VITE_NOMINATIM_MODE;
	console.log('NominatimService factory:', { mode, VITE_NOMINATIM_MODE: nominatimMode });

	const useMock = mode === 'test' || nominatimMode === 'mock';
	return useMock ? createMockNominatimService() : new NominatimServiceImpl(config);
}
