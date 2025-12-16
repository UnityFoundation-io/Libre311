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
// Additional markers can be found here:
// https://carbon-icons-svelte.onrender.com/
// These icons are rendered in white on a colored background circle
const iconPaths: Record<ServiceRequestStatus, string | null> = {
	// Solid circle - no overlay icon needed
	open: null,
	// UserAvatarFilledAlt - person silhouette in circle
	assigned:
		'<path d="M16,8a5,5,0,1,0,5,5A5,5,0,0,0,16,8Z"/><path d="M16,2A14,14,0,1,0,30,16,14.0158,14.0158,0,0,0,16,2Zm7.9925,22.9258A5.0016,5.0016,0,0,0,19,20H13a5.0016,5.0016,0,0,0-4.9925,4.9258,12,12,0,1,1,15.985,0Z"/>',
	// InProgress - pie chart style progress indicator
	in_progress:
		'<path d="M16,2A14,14,0,1,0,30,16,14.0158,14.0158,0,0,0,16,2Zm0,26A12,12,0,0,1,16,4V16l8.4812,8.4814A11.9625,11.9625,0,0,1,16,28Z"/>',
	// Checkmark - simple checkmark (no circle)
	closed: '<path d="M13 24L4 15L5.414 13.586L13 21.171L26.586 7.586L28 9L13 24Z"/>'
};

/**
 * Creates a data URL for a status-colored Carbon icon
 * @param status - The service request status
 * @param size - Icon size in pixels (default: DEFAULT_MARKER_SIZE)
 * @returns Data URL string for use with Leaflet L.icon()
 */
export function getStatusIconDataUrl(
	status: ServiceRequestStatus,
	size: number = DEFAULT_MARKER_SIZE
): string {
	const color = statusColors[status];
	const iconPath = iconPaths[status];

	// Background circle 4px larger than inner icon's r=14
	const backgroundCircle = `<circle cx="20" cy="20" r="18" fill="${color}"/>`;

	// For open status, just show the colored circle
	// For other statuses, overlay the white icon centered in the 40x40 viewBox
	const whiteIcon = iconPath ? `<g transform="translate(4,4)" fill="white">${iconPath}</g>` : '';

	const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40" width="${size}" height="${size}">${backgroundCircle}${whiteIcon}</svg>`;

	return `data:image/svg+xml,${encodeURIComponent(svg)}`;
}