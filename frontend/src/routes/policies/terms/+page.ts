import type { PageLoad } from './$types';

export const load: PageLoad = async ({ fetch }) => {
	const backendUrl = import.meta.env.VITE_BACKEND_URL || '/api';

	try {
		const response = await fetch(`${backendUrl}/config`, {
			headers: {
				Referer: typeof window !== 'undefined' ? window.location.href : ''
			}
		});

		if (!response.ok) {
			throw new Error(`Failed to fetch jurisdiction: ${response.status} ${response.statusText}`);
		}

		const jurisdiction = await response.json();

		return {
			policyContent: jurisdiction.terms_of_use_content,
			jurisdictionName: jurisdiction.name || 'This Jurisdiction',
			loadError: null
		};
	} catch (error) {
		console.error('Error loading terms:', error);
		return {
			policyContent:
				'# Terms of Use\n\nWe were unable to load the terms of use. Please try again later or contact support.',
			jurisdictionName: 'This Jurisdiction',
			loadError: 'Unable to load terms of use. Please try again later.'
		};
	}
};
