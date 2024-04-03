import L from 'leaflet';
import locationSearching from '../assets/location_searching.png';

export class MapCenterControl extends L.Control {
	constructor(opts: L.ControlOptions) {
		super(opts);
	}
	onAdd(map: L.Map): HTMLElement {
		const img = L.DomUtil.create('img');
		img.src = locationSearching;
		img.style.cursor = 'pointer';
		img.style.width = '25';

		const div = L.DomUtil.create('div');
		div.addEventListener('click', () => map.locate());
		div.appendChild(img);
		div.style.backgroundColor = '#fff';
		div.style.border = '2px solid rgba(0, 0, 0, 0.2)';
		div.style.backgroundClip = 'padding-box';
		div.style.borderRadius = '4px';
		div.style.width = '34px';
		div.style.height = '34px';
		div.style.display = 'flex';
		div.style.justifyContent = 'center';
		div.style.alignItems = 'center';

		div.addEventListener('mouseenter', () => {
			console.log('enter');
			div.style.backgroundColor = '#f4f4f4';
		});

		div.addEventListener('mouseleave', () => {
			div.style.backgroundColor = '#fff';
		});

		return div;
	}
}

export function mapCenterControlFactory(opts: L.ControlOptions) {
	return new MapCenterControl(opts);
}
