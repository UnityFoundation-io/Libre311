/// <reference types="@sveltejs/kit" />
/// <reference no-default-lib="true"/>
/// <reference lib="esnext" />
/// <reference lib="webworker" />

import { build, files, version } from '$service-worker';

const sw = self as unknown as ServiceWorkerGlobalScope;

const APP_CACHE = `app-cache-${version}`;
const TILE_CACHE = 'tile-cache-v1';
const TILE_CACHE_LIMIT = 500;

// App shell: built assets + static files
const appShellAssets = [...build, ...files];

sw.addEventListener('install', (event) => {
	event.waitUntil(
		caches
			.open(APP_CACHE)
			.then((cache) => cache.addAll(appShellAssets))
			.then(() => sw.skipWaiting())
	);
});

sw.addEventListener('activate', (event) => {
	event.waitUntil(
		caches.keys().then((keys) => {
			return Promise.all(
				keys
					.filter((key) => key.startsWith('app-cache-') && key !== APP_CACHE)
					.map((key) => caches.delete(key))
			);
		}).then(() => sw.clients.claim())
	);
});

function isTileRequest(url: URL): boolean {
	return url.hostname.includes('basemaps.cartocdn.com') ||
		url.hostname.includes('tile.openstreetmap.org');
}

function isApiRequest(url: URL): boolean {
	return url.pathname.startsWith('/api/') ||
		url.pathname.startsWith('/requests') ||
		url.pathname.startsWith('/services') ||
		url.pathname.startsWith('/config') ||
		url.pathname.startsWith('/image') ||
		url.pathname.startsWith('/jurisdiction-admin');
}

async function trimCache(cacheName: string, maxItems: number) {
	const cache = await caches.open(cacheName);
	const keys = await cache.keys();
	if (keys.length > maxItems) {
		// Remove oldest entries (first in list)
		const toDelete = keys.slice(0, keys.length - maxItems);
		await Promise.all(toDelete.map((key) => cache.delete(key)));
	}
}

sw.addEventListener('fetch', (event) => {
	const url = new URL(event.request.url);

	// Skip non-GET requests
	if (event.request.method !== 'GET') return;

	// API calls: network-only
	if (isApiRequest(url)) return;

	// Map tiles: cache-first
	if (isTileRequest(url)) {
		event.respondWith(
			caches.open(TILE_CACHE).then(async (cache) => {
				const cached = await cache.match(event.request);
				if (cached) return cached;

				try {
					const response = await fetch(event.request);
					if (response.ok) {
						cache.put(event.request, response.clone());
						trimCache(TILE_CACHE, TILE_CACHE_LIMIT);
					}
					return response;
				} catch {
					// Tile not available offline - return empty response
					return new Response('', { status: 408 });
				}
			})
		);
		return;
	}

	// App shell: cache-first, fallback to network
	if (appShellAssets.includes(url.pathname)) {
		event.respondWith(
			caches.match(event.request).then((cached) => {
				return cached || fetch(event.request);
			})
		);
		return;
	}

	// Navigation requests: try network, fall back to cached index.html
	if (event.request.mode === 'navigate') {
		event.respondWith(
			fetch(event.request).catch(() => {
				return caches.match('/index.html').then((cached) => {
					return cached || new Response('Offline', { status: 503 });
				});
			})
		);
		return;
	}
});
