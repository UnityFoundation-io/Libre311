import L from 'leaflet';
import type { ServiceRequestStatus } from '$lib/services/Libre311/Libre311';
import { statusToTextMap } from '$lib/utils/functions';
import { getStatusIconDataUrl } from '$lib/utils/iconToDataUrl';

const LEGEND_ICON_SIZE = 14;

export class MapStatusLegendControl extends L.Control {
	constructor(opts: L.ControlOptions = { position: 'bottomright' }) {
		super(opts);
	}

	onAdd(): HTMLElement {
		const container = L.DomUtil.create('div');
		container.className = 'map-status-legend';
		container.setAttribute('role', 'region');
		container.setAttribute('aria-labelledby', 'map-legend-title');

		L.DomEvent.disableClickPropagation(container);

		const title = L.DomUtil.create('div');
		title.id = 'map-legend-title';
		title.className = 'map-status-legend-title';
		title.textContent = 'Legend';
		container.appendChild(title);

		const statusOrder: ServiceRequestStatus[] = ['open', 'assigned', 'in_progress', 'closed'];

		statusOrder.forEach((status) => {
			const item = this.createLegendItem(status);
			container.appendChild(item);
		});

		return container;
	}

	private createLegendItem(status: ServiceRequestStatus): HTMLElement {
		const item = L.DomUtil.create('div');
		item.className = 'map-status-legend-item';

		const icon = L.DomUtil.create('img');
		icon.src = getStatusIconDataUrl(status, LEGEND_ICON_SIZE);
		icon.alt = '';
		icon.className = 'map-status-legend-icon';
		icon.setAttribute('aria-hidden', 'true');

		const label = L.DomUtil.create('span');
		label.className = 'map-status-legend-label';
		label.textContent = statusToTextMap[status];

		item.appendChild(icon);
		item.appendChild(label);

		return item;
	}
}

export function mapStatusLegendControlFactory() {
	return new MapStatusLegendControl({ position: 'bottomright' });
}
