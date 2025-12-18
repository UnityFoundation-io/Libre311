/**
 * Fixture Recording Utility for Nominatim API
 *
 * This script records real API responses from Nominatim and saves them as fixtures.
 * Run with: npx tsx scripts/record-nominatim-fixtures.ts
 *
 * Use this when:
 * - Setting up contract tests for the first time
 * - Updating fixtures after Nominatim API changes
 * - Adding new test locations
 */

import * as fs from 'fs';
import * as path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Output to the Nominatim service fixtures directory
const FIXTURES_DIR = path.join(__dirname, '../src/lib/services/Nominatim/__fixtures__');
const NOMINATIM_BASE_URL = 'https://nominatim.openstreetmap.org';
const USER_AGENT = 'Libre311/1.0 (https://github.com/UnityFoundation-io/Libre311)';

// Respect Nominatim usage policy: max 1 request per second
const RATE_LIMIT_MS = 1000;

interface RecordConfig {
	reverseGeocode?: Array<{
		name: string;
		lat: number;
		lon: number;
	}>;
	search?: Array<{
		name: string;
		query: string;
	}>;
}

// Default locations to record
const DEFAULT_CONFIG: RecordConfig = {
	reverseGeocode: [
		{
			name: 'st-louis',
			lat: 38.6270025,
			lon: -90.1994042
		},
		{
			name: 'nyc-times-square',
			lat: 40.758896,
			lon: -73.98513
		}
	],
	search: [
		{
			name: 'st-louis',
			query: 'St Louis MO'
		},
		{
			name: 'nyc',
			query: 'New York City NY'
		}
	]
};

function sleep(ms: number): Promise<void> {
	return new Promise((resolve) => setTimeout(resolve, ms));
}

async function fetchWithRetry(url: string, retries = 3): Promise<unknown> {
	for (let i = 0; i < retries; i++) {
		try {
			const response = await fetch(url, {
				headers: {
					'User-Agent': USER_AGENT
				}
			});

			if (!response.ok) {
				throw new Error(`HTTP ${response.status}: ${response.statusText}`);
			}

			return await response.json();
		} catch (error) {
			if (i === retries - 1) throw error;
			console.info(`  Retry ${i + 1}/${retries - 1}...`);
			await sleep(RATE_LIMIT_MS * 2);
		}
	}
}

async function recordReverseGeocode(name: string, lat: number, lon: number): Promise<void> {
	const url = `${NOMINATIM_BASE_URL}/reverse?format=jsonv2&lat=${lat}&lon=${lon}`;
	console.info(`Recording reverse geocode for ${name}...`);

	const data = await fetchWithRetry(url);
	const filename = `reverse-geocode-${name}.json`;
	const filepath = path.join(FIXTURES_DIR, filename);

	fs.writeFileSync(filepath, JSON.stringify(data, null, '\t'));
	console.info(`  Saved to: ${filename}`);
}

async function recordSearch(name: string, query: string): Promise<void> {
	const encodedQuery = encodeURIComponent(query);
	const url = `${NOMINATIM_BASE_URL}/search?format=json&q=${encodedQuery}`;
	console.info(`Recording search for "${query}"...`);
	console.info(`  URL: ${url}`);

	const data = await fetchWithRetry(url);
	const filename = `search-${name}.json`;
	const filepath = path.join(FIXTURES_DIR, filename);

	fs.writeFileSync(filepath, JSON.stringify(data, null, '\t'));
	console.info(`  Saved to: ${filename}`);
}

async function main(): Promise<void> {
	// Parse command line arguments for custom config file
	const args = process.argv.slice(2);
	let config = DEFAULT_CONFIG;

	if (args.length > 0 && args[0] !== '--help') {
		const configPath = args[0];
		if (fs.existsSync(configPath)) {
			const customConfig = JSON.parse(fs.readFileSync(configPath, 'utf-8'));
			config = { ...DEFAULT_CONFIG, ...customConfig };
			console.info(`Using custom config from: ${configPath}\n`);
		} else {
			console.error(`Config file not found: ${configPath}`);
			process.exit(1);
		}
	}

	if (args.includes('--help')) {
		console.info(`
Nominatim Fixture Recording Utility

Usage:
  npx tsx scripts/record-nominatim-fixtures.ts [config.json]

Options:
  --help    Show this help message

Config file format:
{
  "reverseGeocode": [
    { "name": "location-name", "lat": 38.627, "lon": -90.199 }
  ],
  "search": [
    { "name": "location-name", "query": "St Louis MO" }
  ]
}

Default locations are used if no config file is provided.
		`);
		process.exit(0);
	}

	// Ensure fixtures directory exists
	if (!fs.existsSync(FIXTURES_DIR)) {
		fs.mkdirSync(FIXTURES_DIR, { recursive: true });
	}

	console.info('Recording Nominatim API fixtures...\n');
	console.info(`Output directory: ${FIXTURES_DIR}\n`);

	// Record reverse geocode responses
	if (config.reverseGeocode) {
		console.info('=== Reverse Geocode ===');
		for (const item of config.reverseGeocode) {
			await recordReverseGeocode(item.name, item.lat, item.lon);
			await sleep(RATE_LIMIT_MS);
		}
		console.info();
	}

	// Record search responses
	if (config.search) {
		console.info('=== Search ===');
		for (const item of config.search) {
			await recordSearch(item.name, item.query);
			await sleep(RATE_LIMIT_MS);
		}
		console.info();
	}

	console.info('Done! Fixtures have been recorded.');
	console.info('\nNote: Run contract tests to verify fixtures match expected schemas:');
	console.info('  npm run test:unit -- NominatimService.contract');
}

main().catch((error) => {
	console.error('Error recording fixtures:', error);
	process.exit(1);
});
