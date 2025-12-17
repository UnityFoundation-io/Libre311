import { describe, it, expect, beforeEach } from 'vitest';
import {
	MockNominatimService,
	NominatimReverseResponseSchema,
	NominatimSearchResponseSchema,
	type NominatimReverseResponse,
	type NominatimSearchResponse
} from './NominatimService';

// Import recorded fixtures
import reverseGeocodeStLouis from './__fixtures__/reverse-geocode-st-louis.json';
import searchStLouis from './__fixtures__/search-st-louis.json';

describe('Nominatim Response Contracts', () => {
	describe('Fixture Schema Validation', () => {
		it('reverse geocode fixture matches NominatimReverseResponseSchema', () => {
			// Validates that our recorded fixture matches the expected schema
			const parsed = NominatimReverseResponseSchema.safeParse(reverseGeocodeStLouis);

			expect(parsed.success).toBe(true);
			if (parsed.success) {
				expect(parsed.data.display_name).toBe(reverseGeocodeStLouis.display_name);
				expect(parsed.data.lat).toBe(reverseGeocodeStLouis.lat);
				expect(parsed.data.lon).toBe(reverseGeocodeStLouis.lon);
			}
		});

		it('search fixture matches NominatimSearchResponseSchema', () => {
			// Validates that our recorded fixture matches the expected schema
			const parsed = NominatimSearchResponseSchema.safeParse(searchStLouis);

			expect(parsed.success).toBe(true);
			if (parsed.success) {
				expect(parsed.data.length).toBeGreaterThan(0);
				expect(parsed.data[0].display_name).toBe(searchStLouis[0].display_name);
			}
		});
	});

	describe('Reverse Geocode Response Structure', () => {
		it('response contains required fields for display', () => {
			const fixture = reverseGeocodeStLouis as NominatimReverseResponse;

			// Required fields for Libre311 functionality
			expect(fixture).toHaveProperty('display_name');
			expect(typeof fixture.display_name).toBe('string');
			expect(fixture.display_name.length).toBeGreaterThan(0);

			// Coordinate fields (as strings per Nominatim API)
			expect(fixture).toHaveProperty('lat');
			expect(fixture).toHaveProperty('lon');
			expect(typeof fixture.lat).toBe('string');
			expect(typeof fixture.lon).toBe('string');

			// Verify coordinates are parseable as numbers
			expect(parseFloat(fixture.lat)).not.toBeNaN();
			expect(parseFloat(fixture.lon)).not.toBeNaN();
		});

		it('response contains address breakdown', () => {
			const fixture = reverseGeocodeStLouis as NominatimReverseResponse;

			expect(fixture).toHaveProperty('address');
			expect(fixture.address).toBeDefined();

			// Common address components
			if (fixture.address) {
				expect(fixture.address).toHaveProperty('country');
				expect(fixture.address).toHaveProperty('country_code');
			}
		});

		it('response matches Libre311 ReverseGeocodeResponse shape', () => {
			const fixture = reverseGeocodeStLouis as NominatimReverseResponse;

			// The minimal shape needed by Libre311
			expect(fixture).toMatchObject({
				display_name: expect.any(String)
			});
		});
	});

	describe('Search Response Structure', () => {
		it('response is an array of results', () => {
			const fixture = searchStLouis as NominatimSearchResponse;

			expect(Array.isArray(fixture)).toBe(true);
			expect(fixture.length).toBeGreaterThan(0);
		});

		it('each result contains required fields', () => {
			const fixture = searchStLouis as NominatimSearchResponse;

			for (const result of fixture) {
				expect(result).toHaveProperty('place_id');
				expect(result).toHaveProperty('display_name');
				expect(result).toHaveProperty('lat');
				expect(result).toHaveProperty('lon');
				expect(result).toHaveProperty('importance');

				expect(typeof result.place_id).toBe('number');
				expect(typeof result.display_name).toBe('string');
				expect(typeof result.lat).toBe('string');
				expect(typeof result.lon).toBe('string');
			}
		});

		it('results can be converted to leaflet-geosearch format', () => {
			const fixture = searchStLouis as NominatimSearchResponse;

			// leaflet-geosearch OpenStreetMapProvider transforms Nominatim responses
			// to this format: { x: lon, y: lat, label: display_name, raw: originalResult }
			const transformed = fixture.map((result) => ({
				x: parseFloat(result.lon),
				y: parseFloat(result.lat),
				label: result.display_name,
				raw: result
			}));

			for (const item of transformed) {
				expect(item).toMatchObject({
					x: expect.any(Number),
					y: expect.any(Number),
					label: expect.any(String),
					raw: expect.objectContaining({
						lat: expect.any(String),
						lon: expect.any(String),
						display_name: expect.any(String)
					})
				});
			}
		});
	});
});

describe('MockNominatimService', () => {
	let mockService: MockNominatimService;

	beforeEach(() => {
		mockService = new MockNominatimService();
	});

	describe('reverseGeocode', () => {
		it('returns configured response for specific coordinates', async () => {
			const fixture = reverseGeocodeStLouis as NominatimReverseResponse;
			mockService.setReverseResponse(38.627, -90.199, fixture);

			const result = await mockService.reverseGeocode(38.627, -90.199);

			expect(result.display_name).toBe(fixture.display_name);
			expect(result.lat).toBe(fixture.lat);
		});

		it('returns default response when no specific match', async () => {
			const fixture = reverseGeocodeStLouis as NominatimReverseResponse;
			mockService.setDefaultReverseResponse(fixture);

			const result = await mockService.reverseGeocode(40.0, -100.0);

			expect(result.display_name).toBe(fixture.display_name);
		});

		it('throws error when no response configured', async () => {
			await expect(mockService.reverseGeocode(40.0, -100.0)).rejects.toThrow(
				'No mock response configured'
			);
		});

		it('mock response passes schema validation', async () => {
			const fixture = reverseGeocodeStLouis as NominatimReverseResponse;
			mockService.setDefaultReverseResponse(fixture);

			const result = await mockService.reverseGeocode(38.627, -90.199);
			const parsed = NominatimReverseResponseSchema.safeParse(result);

			expect(parsed.success).toBe(true);
		});
	});

	describe('search', () => {
		it('returns configured response for specific query', async () => {
			const fixture = searchStLouis as NominatimSearchResponse;
			mockService.setSearchResponse('St Louis MO', fixture);

			const result = await mockService.search('St Louis MO');

			expect(result.length).toBe(fixture.length);
			expect(result[0].display_name).toBe(fixture[0].display_name);
		});

		it('search is case-insensitive', async () => {
			const fixture = searchStLouis as NominatimSearchResponse;
			mockService.setSearchResponse('st louis mo', fixture);

			const result = await mockService.search('ST LOUIS MO');

			expect(result.length).toBe(fixture.length);
		});

		it('returns default response when no specific match', async () => {
			const fixture = searchStLouis as NominatimSearchResponse;
			mockService.setDefaultSearchResponse(fixture);

			const result = await mockService.search('Chicago IL');

			expect(result.length).toBe(fixture.length);
		});

		it('throws error when no response configured', async () => {
			await expect(mockService.search('Unknown City')).rejects.toThrow(
				'No mock response configured'
			);
		});

		it('mock response passes schema validation', async () => {
			const fixture = searchStLouis as NominatimSearchResponse;
			mockService.setDefaultSearchResponse(fixture);

			const result = await mockService.search('St Louis');
			const parsed = NominatimSearchResponseSchema.safeParse(result);

			expect(parsed.success).toBe(true);
		});
	});

	describe('reset', () => {
		it('clears all configured responses', async () => {
			const fixture = reverseGeocodeStLouis as NominatimReverseResponse;
			mockService.setDefaultReverseResponse(fixture);
			mockService.setDefaultSearchResponse(searchStLouis as NominatimSearchResponse);

			mockService.reset();

			await expect(mockService.reverseGeocode(38.627, -90.199)).rejects.toThrow();
			await expect(mockService.search('St Louis')).rejects.toThrow();
		});
	});
});
