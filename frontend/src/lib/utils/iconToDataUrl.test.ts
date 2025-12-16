import { describe, it, expect, vi } from 'vitest';

// Mock leaflet to avoid browser environment requirement
vi.mock('leaflet', () => ({}));

import {
	getStatusIconDataUrl,
	getWaypointIconDataUrl,
	DEFAULT_MARKER_SIZE,
	DEFAULT_WAYPOINT_SIZE
} from './iconToDataUrl';
import type { ServiceRequestStatus } from '$lib/services/Libre311/Libre311';

const allStatuses: ServiceRequestStatus[] = ['open', 'assigned', 'in_progress', 'closed'];

describe('getStatusIconDataUrl', () => {
	it('returns a valid data URL', () => {
		const result = getStatusIconDataUrl('open');
		expect(result).toMatch(/^data:image\/svg\+xml,/);
	});

	it('uses default size when not specified', () => {
		const result = getStatusIconDataUrl('open');
		expect(result).toContain(`width%3D%22${DEFAULT_MARKER_SIZE}%22`);
		expect(result).toContain(`height%3D%22${DEFAULT_MARKER_SIZE}%22`);
	});

	it('uses custom size when specified', () => {
		const result = getStatusIconDataUrl('open', 50);
		expect(result).toContain('width%3D%2250%22');
		expect(result).toContain('height%3D%2250%22');
	});

	for (const status of allStatuses) {
		it(`generates valid SVG for status: ${status}`, () => {
			const result = getStatusIconDataUrl(status);
			const decoded = decodeURIComponent(result.replace('data:image/svg+xml,', ''));
			expect(decoded).toContain('<svg');
			expect(decoded).toContain('</svg>');
			expect(decoded).toContain('viewBox="0 0 40 40"');
		});
	}
});

describe('getWaypointIconDataUrl', () => {
	it('returns a valid data URL', () => {
		const result = getWaypointIconDataUrl('open');
		expect(result).toMatch(/^data:image\/svg\+xml,/);
	});

	it('uses default size when not specified', () => {
		const result = getWaypointIconDataUrl('open');
		expect(result).toContain(`height%3D%22${DEFAULT_WAYPOINT_SIZE}%22`);
	});

	it('maintains 0.75 aspect ratio', () => {
		const size = 40;
		const result = getWaypointIconDataUrl('open', size);
		const expectedWidth = size * 0.75;
		expect(result).toContain(`width%3D%22${expectedWidth}%22`);
		expect(result).toContain(`height%3D%22${size}%22`);
	});

	for (const status of allStatuses) {
		it(`generates valid SVG for status: ${status}`, () => {
			const result = getWaypointIconDataUrl(status);
			const decoded = decodeURIComponent(result.replace('data:image/svg+xml,', ''));
			expect(decoded).toContain('<svg');
			expect(decoded).toContain('</svg>');
			expect(decoded).toContain('viewBox="0 0 24 32"');
		});
	}
});