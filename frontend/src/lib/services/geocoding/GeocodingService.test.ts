import { describe, it, expect } from 'vitest';
import { getFormattedAddress, type ReverseGeocodeResponse } from './GeocodingService';

function createResult(address: ReverseGeocodeResponse['address']): ReverseGeocodeResponse {
	return {
		displayName: '100 Market Street, St. Louis, Missouri, 63101, United States',
		address,
		latitude: 38.6270025,
		longitude: -90.1994042,
		provider: 'test'
	};
}

describe('getFormattedAddress', () => {
	it('returns displayName when address is null', () => {
		const result = createResult(null);
		expect(getFormattedAddress(result)).toBe(result.displayName);
	});

	it('formats complete address with all fields', () => {
		const result = createResult({
			streetNumber: '100',
			street: 'Market Street',
			neighborhood: 'Downtown',
			city: 'St. Louis',
			county: 'St. Louis City',
			state: 'Missouri',
			postalCode: '63101',
			country: 'United States',
			countryCode: 'us'
		});
		expect(getFormattedAddress(result)).toBe('100 Market Street, St. Louis, Missouri 63101');
	});

	it('handles missing streetNumber', () => {
		const result = createResult({
			streetNumber: null,
			street: 'Market Street',
			neighborhood: null,
			city: 'St. Louis',
			county: null,
			state: 'Missouri',
			postalCode: '63101',
			country: null,
			countryCode: null
		});
		expect(getFormattedAddress(result)).toBe('Market Street, St. Louis, Missouri 63101');
	});

	it('handles missing street', () => {
		const result = createResult({
			streetNumber: '100',
			street: null,
			neighborhood: null,
			city: 'St. Louis',
			county: null,
			state: 'Missouri',
			postalCode: '63101',
			country: null,
			countryCode: null
		});
		expect(getFormattedAddress(result)).toBe('100, St. Louis, Missouri 63101');
	});

	it('handles missing city', () => {
		const result = createResult({
			streetNumber: '100',
			street: 'Market Street',
			neighborhood: null,
			city: null,
			county: null,
			state: 'Missouri',
			postalCode: '63101',
			country: null,
			countryCode: null
		});
		expect(getFormattedAddress(result)).toBe('100 Market Street, Missouri 63101');
	});

	it('handles missing state', () => {
		const result = createResult({
			streetNumber: '100',
			street: 'Market Street',
			neighborhood: null,
			city: 'St. Louis',
			county: null,
			state: null,
			postalCode: '63101',
			country: null,
			countryCode: null
		});
		expect(getFormattedAddress(result)).toBe('100 Market Street, St. Louis, 63101');
	});

	it('handles missing postalCode', () => {
		const result = createResult({
			streetNumber: '100',
			street: 'Market Street',
			neighborhood: null,
			city: 'St. Louis',
			county: null,
			state: 'Missouri',
			postalCode: null,
			country: null,
			countryCode: null
		});
		expect(getFormattedAddress(result)).toBe('100 Market Street, St. Louis, Missouri');
	});

	it('handles city only', () => {
		const result = createResult({
			streetNumber: null,
			street: null,
			neighborhood: null,
			city: 'St. Louis',
			county: null,
			state: null,
			postalCode: null,
			country: null,
			countryCode: null
		});
		expect(getFormattedAddress(result)).toBe('St. Louis');
	});

	it('handles state and postalCode only', () => {
		const result = createResult({
			streetNumber: null,
			street: null,
			neighborhood: null,
			city: null,
			county: null,
			state: 'Missouri',
			postalCode: '63101',
			country: null,
			countryCode: null
		});
		expect(getFormattedAddress(result)).toBe('Missouri 63101');
	});

	it('falls back to displayName when all address fields are null', () => {
		const result = createResult({
			streetNumber: null,
			street: null,
			neighborhood: null,
			city: null,
			county: null,
			state: null,
			postalCode: null,
			country: null,
			countryCode: null
		});
		expect(getFormattedAddress(result)).toBe(result.displayName);
	});

	it('handles undefined fields (omitted by backend)', () => {
		const result = createResult({
			streetNumber: undefined,
			street: 'Market Street',
			neighborhood: undefined,
			city: 'St. Louis',
			county: undefined,
			state: undefined,
			postalCode: undefined,
			country: undefined,
			countryCode: undefined
		});
		expect(getFormattedAddress(result)).toBe('Market Street, St. Louis');
	});
});
