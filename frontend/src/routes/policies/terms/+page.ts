import type { PageLoad } from './$types';

export const load: PageLoad = async ({ fetch }) => {
	const backendUrl = import.meta.env.VITE_BACKEND_URL || '/api';

	try {
		// Fetch jurisdiction data
		const response = await fetch(`${backendUrl}/config`, {
			headers: {
				Referer: typeof window !== 'undefined' ? window.location.href : ''
			}
		});

		if (!response.ok) {
			throw new Error(`Failed to fetch jurisdiction: ${response.status} ${response.statusText}`);
		}

		const jurisdiction = await response.json();
		let policyContent = jurisdiction.terms_of_use_content;

		// Fallback to default if content is null or empty
		if (!policyContent || policyContent.trim() === '') {
			const defaultResponse = await fetch('/defaults/terms.md');
			if (!defaultResponse.ok) {
				throw new Error('Failed to load default terms');
			}
			policyContent = await defaultResponse.text();
		}

		return {
			policyContent,
			jurisdictionName: jurisdiction.name || 'This Jurisdiction',
			loadError: null
		};
	} catch (error) {
		console.error('Error loading terms:', error);

		// Attempt to load fallback content
		try {
			const defaultResponse = await fetch('/defaults/terms.md');
			if (!defaultResponse.ok) {
				throw new Error('Failed to load default terms');
			}
			const policyContent = await defaultResponse.text();
			return {
				policyContent,
				jurisdictionName: 'This Jurisdiction',
				loadError: 'Unable to load jurisdiction-specific terms of use. Showing default content.'
			};
		} catch (fallbackError) {
			console.error('Error loading fallback terms:', fallbackError);
			return {
				policyContent: '# Terms of Use\n\nWe were unable to load the terms of use. Please try again later or contact support.',
				jurisdictionName: 'This Jurisdiction',
				loadError: 'Unable to load terms of use. Please try again later.'
			};
		}
	}
};
