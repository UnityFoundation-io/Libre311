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
			policyContent: jurisdiction.privacy_policy_content,
			jurisdictionName: jurisdiction.name || 'This Jurisdiction',
			loadError: null
		};
	} catch (error) {
		console.error('Error loading privacy policy:', error);
		return {
			policyContent:
				'# Privacy Policy\n\nWe were unable to load the privacy policy. Please try again later or contact support.',
			jurisdictionName: 'This Jurisdiction',
			loadError: 'Unable to load privacy policy. Please try again later.'
		};
	}
};
