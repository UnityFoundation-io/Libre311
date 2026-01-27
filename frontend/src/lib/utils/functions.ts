import {
	serviceRequestPriorityArray,
	serviceRequestStatusArray
} from '$lib/services/Libre311/Libre311';
import L, { type PointTuple } from 'leaflet';
import type { SelectOption } from 'stwui/types';
import { tick } from 'svelte';

export function sleep(ms: number) {
	return new Promise((resolve) => setTimeout(resolve, ms));
}

export function matchesDesktopMedia() {
	return window.matchMedia('(min-width: 769px)').matches;
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

export function setUpAlertRole(
	input: { error?: string },
	root: HTMLElement,
	selector: string,
	error_id: string
) {
	if (input.error) {
		// runs whenever error becomes truthy
		const component = root?.querySelector<HTMLElement>(selector);
		if (component) {
			component.setAttribute('aria-describedby', error_id);
			component.setAttribute('aria-invalid', 'true');
			component.setAttribute('error', input.error);

			(async () => {
				await tick();
				const p = root?.querySelector('p#' + error_id);
				if (p) {
					p.setAttribute('role', 'alert');
					const error = p.innerHTML;
					await tick;
					// zero-width space accumulates on repeated errors forcing screen readers to repeat
					p.innerHTML = error + '\u200B';
				}
			})();
		}
	}
}

export const statusToColorMap = {
	closed: '#37D39A',
	open: '#FABE23',
	in_progress: '#38BDF9',
	assigned: '#C084FC'
};

export const statusToTextMap = {
	closed: 'Closed',
	open: 'Open',
	in_progress: 'In Progress',
	assigned: 'Assigned'
};

export const serviceRequestStatusSelectOptions: SelectOption[] = serviceRequestStatusArray.map(
	(s) => {
		return {
			value: s,
			label: statusToTextMap[s]
		};
	}
);

export const serviceRequestPrioritySelectOptions = serviceRequestPriorityArray.map((s) => {
	return {
		value: s,
		label: s.toLocaleUpperCase()
	};
});
