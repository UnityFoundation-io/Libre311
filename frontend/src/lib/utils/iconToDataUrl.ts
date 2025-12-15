import type { ServiceRequestStatus } from '$lib/services/Libre311/Libre311';

/** Default size for map marker icons in pixels */
export const DEFAULT_MARKER_SIZE = 25;

// Status color mapping (matches existing statusToColorMap)
const statusColors: Record<ServiceRequestStatus, string> = {
	open: '#FABE23',
	assigned: '#67E8F9',
	in_progress: '#38BDF9',
	closed: '#37D39A'
};

// Carbon icon SVG paths (from carbon-icons-svelte v11.x)
// Source: node_modules/carbon-icons-svelte/lib/*.svelte
const iconPaths: Record<ServiceRequestStatus, string> = {
	// CircleSolid - filled circle
	open: '<circle cx="16" cy="16" r="14"/>',
	// UserAvatarFilledAlt - person silhouette in circle
	assigned:
		'<path d="M16,8a5,5,0,1,0,5,5A5,5,0,0,0,16,8Z"/><path d="M16,2A14,14,0,1,0,30,16,14.0158,14.0158,0,0,0,16,2Zm7.9925,22.9258A5.0016,5.0016,0,0,0,19,20H13a5.0016,5.0016,0,0,0-4.9925,4.9258,12,12,0,1,1,15.985,0Z"/>',
	// InProgress - pie chart style progress indicator
	in_progress:
		'<path d="M16,2A14,14,0,1,0,30,16,14.0158,14.0158,0,0,0,16,2Zm0,26A12,12,0,0,1,16,4V16l8.4812,8.4814A11.9625,11.9625,0,0,1,16,28Z"/>',
	// CheckmarkFilled - checkmark in circle
	closed:
		'<path d="M16,2A14,14,0,1,0,30,16,14,14,0,0,0,16,2ZM14,21.5908l-5-5L10.5906,15,14,18.4092,21.41,11l1.5957,1.5859Z"/>'
};

/**
 * Creates a data URL for a status-colored Carbon icon
 * @param status - The service request status
 * @param size - Icon size in pixels (default 20)
 * @returns Data URL string for use with Leaflet L.icon()
 */
export function getStatusIconDataUrl(
	status: ServiceRequestStatus,
	size: number = DEFAULT_MARKER_SIZE
): string {
	const color = statusColors[status];
	const path = iconPaths[status];

	const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32" width="${size}" height="${size}" fill="${color}">${path}</svg>`;

	return `data:image/svg+xml,${encodeURIComponent(svg)}`;
}