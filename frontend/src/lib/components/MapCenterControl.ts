import L from 'leaflet';
import locationSearching from '../assets/location_searching.png';

export class MapCenterControl extends L.Control {
	constructor(opts: L.ControlOptions) {
		super(opts);
	}
	onAdd(map: L.Map): HTMLElement {
		const img = L.DomUtil.create('img');
		img.src = locationSearching;
		img.alt = '';

		const control = L.DomUtil.create('a');
		control.className = 'map-center-control';
		control.href = '#';
		control.setAttribute('role', 'button');
		control.setAttribute('title', 'Center Map');
		control.setAttribute('aria-label', 'Center Map');
		control.appendChild(img);

		control.addEventListener('click', (e) => {
			e.preventDefault();
			map.locate();
		});

		return control;
	}
}

export function mapCenterControlFactory(opts: L.ControlOptions) {
	return new MapCenterControl(opts);
}
