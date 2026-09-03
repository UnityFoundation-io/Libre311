import type { Mode } from './mode';
import { z } from 'zod';
import axios from 'axios';

export const CartoServicePropsSchema = z.object({
	cartoApiKey: z.string()
});
export type CartoServiceProps = z.infer<typeof CartoServicePropsSchema>;

export async function loadCartoProps(mode: Mode): Promise<CartoServiceProps> {
	let cartoApiKey: string = '';
	if (import.meta.env.VITE_CARTO_API_KEY) {
		cartoApiKey = String(import.meta.env.VITE_CARTO_API_KEY);
	}

	if (!cartoApiKey && mode == 'production') {
		const res = await axios.get<string>('/carto/carto-key');
		cartoApiKey = res.data;
	}

	return { cartoApiKey };
}
