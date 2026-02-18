import type { ServiceRequestStatus } from '$lib/services/Libre311/Libre311';
import { statusToColorMap } from './functions';

/** Default rendered size for map marker icons in pixels */
export const DEFAULT_MARKER_SIZE = 30;

/**
 * SVG Geometry Notes:
 * Carbon icons use a 32x32 viewBox with r=14 inner circles.
 * We add 4px padding around the icon for the colored background circle.
 *
 * Derived values (used as literals below):
 * - ViewBox: 32 + (4 * 2) = 40x40
 * - Center: 40 / 2 = 20
 * - Background radius: 14 + 4 = 18
 * - Icon offset: 4 (centers the 32x32 icon in the 40x40 viewBox)
 */

// SVG paths for status icons (sourced from Carbon Design System icons)
// Browse icons at: https://carbon-icons-svelte.onrender.com/
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
	const color = statusToColorMap[status];
	const iconPath = iconPaths[status];

	const whiteCircle = `<circle cx="20" cy="20" r="18" fill="none" stroke="white" stroke-width="6"/>`;
	const backgroundCircle = `<circle cx="20" cy="20" r="18" fill="${color}" stroke="black" stroke-width="2"/>`;

	// For open status, just show the colored circle
	// For other statuses, overlay the white icon centered in the viewBox
	const whiteIcon = iconPath ? `<g transform="translate(4,4)" fill="white" stroke="black">${iconPath}</g>` : '';

	const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="-2 -2 43 43" width="${size}" height="${size}">${whiteCircle}${backgroundCircle}${whiteIcon}</svg>`;

	return `data:image/svg+xml,${encodeURIComponent(svg)}`;
}

/** Default height for waypoint pin icons in pixels */
export const DEFAULT_WAYPOINT_SIZE = 35;

/**
 * Creates a data URL for a waypoint/pin icon (used for selected markers)
 * Pin shape: teardrop with pointed bottom and white center dot
 * ViewBox: 24x32, aspect ratio 0.75
 *
 * @param status - The service request status
 * @param size - Icon height in pixels (default: DEFAULT_WAYPOINT_SIZE)
 * @returns Data URL string for use with Leaflet L.icon()
 */
export function getWaypointIconDataUrl(
	status: ServiceRequestStatus,
	size: number = DEFAULT_WAYPOINT_SIZE
): string {
	const color = statusToColorMap[status];
	const width = size * 0.75;
	const path = "M12 0C5.4 0 0 5.4 0 12c0 7.2 12 20 12 20s12-12.8 12-20c0-6.6-5.4-12-12-12z";
	const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="-4 0 32 34" width="${width}" height="${size}"><path d="${path}" fill="none" stroke="white" stroke-width="6"/><path d="${path}" fill="${color}" stroke="black" stroke-width="2"/><circle cx="12" cy="12" r="5" fill="white"/></svg>`;
	return `data:image/svg+xml,${encodeURIComponent(svg)}`;
}
