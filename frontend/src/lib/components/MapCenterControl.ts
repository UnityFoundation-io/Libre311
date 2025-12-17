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

		const control = L.DomUtil.create('a');
		control.addEventListener('click', () => map.locate());
		control.appendChild(img);
		control.setAttribute('role', 'button');
		control.setAttribute('title', 'Center Map');
		control.setAttribute('aria-label', 'Center Map');
		control.href = "#";
		control.style.backgroundColor = '#fff';
		control.style.border = '2px solid rgba(0, 0, 0, 0.2)';
		control.style.backgroundClip = 'padding-box';
		control.style.borderRadius = '4px';
		control.style.width = '34px';
		control.style.height = '34px';
		control.style.display = 'flex';
		control.style.justifyContent = 'center';
		control.style.alignItems = 'center';

		const highlight = () => {
			control.style.backgroundColor = '#f4f4f4';
		}

		const unhighlight = () => {
			control.style.backgroundColor = '#fff';
		}

		control.addEventListener('mouseenter', highlight);
		control.addEventListener('focus', highlight);

		control.addEventListener('mouseleave', unhighlight);
       	control.addEventListener('blur', unhighlight);

		return control;
	}
}

export function mapCenterControlFactory(opts: L.ControlOptions) {
	return new MapCenterControl(opts);
}
