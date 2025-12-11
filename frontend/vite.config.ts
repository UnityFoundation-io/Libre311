import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vitest/config';

export default defineConfig({
	plugins: [sveltekit()],
	test: {
		include: ['src/**/*.{test,spec}.{js,ts}']
	},
	server: {
		port: 3000,
		host: true
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
