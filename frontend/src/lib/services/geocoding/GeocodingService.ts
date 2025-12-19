import { z } from 'zod';
import axios from 'axios';

// Schema matching the backend's GeocodeAddress record
// Using nullish() to allow both null and undefined (missing fields in JSON)
export const GeocodeAddressSchema = z.object({
	streetNumber: z.string().nullish(),
	street: z.string().nullish(),
	neighborhood: z.string().nullish(),
	city: z.string().nullish(),
	county: z.string().nullish(),
	state: z.string().nullish(),
	postalCode: z.string().nullish(),
	country: z.string().nullish(),
	countryCode: z.string().nullish()
});
export type GeocodeAddress = z.infer<typeof GeocodeAddressSchema>;

// Schema matching the backend's ReverseGeocodeResult record
export const ReverseGeocodeResponseSchema = z.object({
	displayName: z.string(),
	address: GeocodeAddressSchema.nullable(),
	latitude: z.number(),
	longitude: z.number(),
	provider: z.string()
});
export type ReverseGeocodeResponse = z.infer<typeof ReverseGeocodeResponseSchema>;

/**
 * Format a GeocodeAddress into standard US address format.
 * Example: "13630 Mulberry Ln, Maryland Heights, MO 63146"
 */
export function formatAddress(address: GeocodeAddress | null | undefined): string | null {
	if (!address) return null;

	const parts: string[] = [];

	// Street line: "123 Main Street"
	const streetParts: string[] = [];
	if (address.streetNumber) streetParts.push(address.streetNumber);
	if (address.street) streetParts.push(address.street);
	if (streetParts.length > 0) parts.push(streetParts.join(' '));

	// City, State ZIP: "St. Louis, MO 63101"
	const cityStateZip: string[] = [];
	if (address.city) cityStateZip.push(address.city);
	if (address.state || address.postalCode) {
		const stateZip = [address.state, address.postalCode].filter(Boolean).join(' ');
		cityStateZip.push(stateZip);
	}
	if (cityStateZip.length > 0) parts.push(cityStateZip.join(', '));

	return parts.length > 0 ? parts.join(', ') : null;
}

/**
 * Get a formatted address string from a ReverseGeocodeResponse.
 * Falls back to displayName if address formatting fails.
 */
export function getFormattedAddress(result: ReverseGeocodeResponse): string {
	const formatted = formatAddress(result.address);
	return formatted || result.displayName;
}

// Service interface
export interface GeocodingService {
	reverseGeocode(lat: number, lon: number): Promise<ReverseGeocodeResponse>;
}

// Implementation using backend proxy
export class GeocodingServiceImpl implements GeocodingService {
	private baseUrl: string;

	constructor() {
		const backendUrl = import.meta.env.VITE_BACKEND_URL || '/api';
		this.baseUrl = `${backendUrl}/geocode`;
	}

	async reverseGeocode(lat: number, lon: number): Promise<ReverseGeocodeResponse> {
		const url = `${this.baseUrl}/reverse?lat=${lat}&lon=${lon}`;
		const res = await axios.get<unknown>(url);
		return ReverseGeocodeResponseSchema.parse(res.data);
	}
}
