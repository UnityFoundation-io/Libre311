import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vitest/config';

export default defineConfig({
	plugins: [sveltekit()],
	test: {
		include: ['src/**/*.{test,spec}.{js,ts}']
	},
	server: {
		port: 3000,
		host: true,
		proxy: {
			// Proxy Nominatim requests to bypass CORS during development
			'/nominatim': {
				target: 'https://nominatim.openstreetmap.org',
				changeOrigin: true,
				rewrite: (path) => path.replace(/^\/nominatim/, ''),
				headers: {
					'User-Agent': 'Libre311/1.0 (https://github.com/UnityFoundation-io/Libre311)'
				}
			}
		}
	},
	optimizeDeps: {
		include: [
			'axios',
			'zod',
			'stwui',
			'leaflet',
			'leaflet-geosearch',
			'google-libphonenumber',
			'marked',
			'dompurify'
		]
	}
});
