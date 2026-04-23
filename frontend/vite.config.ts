import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vitest/config';

export default defineConfig({
	plugins: [sveltekit()],
	test: {
		include: ['src/**/*.{test,spec}.{js,ts}'],
		environment: 'jsdom',
		setupFiles: ['./vitest.setup.ts']
	},
	server: {
		port: 3000,
		host: true,
		allowedHosts: ['stlma.localhost', 'lomocomo.localhost'],
		proxy: {
			'/api': 'http://localhost:8080',
			'/auth': 'http://localhost:9090'
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
			'carta-md',
			'dompurify'
		]
	}
});
