import L, { type PointTuple } from 'leaflet';

export function sleep(ms: number) {
	return new Promise((resolve) => setTimeout(resolve, ms));
}

export function matchesDesktopMedia() {
	return window.matchMedia('(min-width: 64rem)').matches;
}

type AnchorPosition = 'center' | 'bottom-center';

function centerAnchor(iconSize: PointTuple): PointTuple {
	const [w, h] = iconSize;
	return [w / 2, h / 2];
}

function bottomCenterAnchor(iconSize: PointTuple): PointTuple {
	const [w, h] = iconSize;
	return [w / 2, h];
}

function createAnchor(iconSize: PointTuple, position: AnchorPosition): PointTuple {
	switch (position) {
		case 'center':
			return centerAnchor(iconSize);
		case 'bottom-center':
			return bottomCenterAnchor(iconSize);
	}
}

export function iconPositionOpts(
	aspectRatio: number,
	size: number,
	position: AnchorPosition = 'center'
): Pick<L.IconOptions, 'iconSize' | 'iconAnchor'> {
	const w = aspectRatio * size;
	const h = size;
	const iconSize: PointTuple = [w, h];

	return {
		iconSize,
		iconAnchor: createAnchor(iconSize, position)
	};
}

export function toTimeStamp(timeStamp: Date | string | null | undefined) {
	return timeStamp
		? `${new Date(timeStamp).toLocaleDateString()} ${new Date(timeStamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
		: '';
}

export function toAbbreviatedTimeStamp(timeStamp: Date | string | null | undefined) {
	return timeStamp ? `${new Date(timeStamp).toLocaleDateString()}` : '';
}
