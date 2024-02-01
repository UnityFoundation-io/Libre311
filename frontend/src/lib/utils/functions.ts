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

export function checkPhoneNumber(phoneNumber: string | undefined): string {
	if (phoneNumber == undefined || phoneNumber == '') {
		return '';
	}

	// if (phoneNumber.length !== 12) {
	// 	return 'Invalid phone number';
	// }

	try {
		// <-- libphonenumber-js -->
		// isValidPhoneNumber(parsePhoneNumberWithError(phoneNumber, 'US').number);

		return '';
	} catch (e: unknown) {
		let errorString = new String(e).toString();

		errorString = errorString.replace(/ParseError\d+:\s/, '');
		errorString = errorString.replaceAll('_', ' ');
		errorString = errorString.toLowerCase();
		errorString = errorString.charAt(0).toUpperCase() + errorString.slice(1);

		return errorString;
	}
}
