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
		let policyContent = jurisdiction.privacy_policy_content;

		// Fallback to default if content is null or empty
		if (!policyContent || policyContent.trim() === '') {
			const defaultResponse = await fetch('/defaults/privacy.md');
			if (!defaultResponse.ok) {
				throw new Error('Failed to load default privacy policy');
			}
			policyContent = await defaultResponse.text();
		}

		return {
			policyContent,
			jurisdictionName: jurisdiction.name || 'This Jurisdiction',
			loadError: null
		};
	} catch (error) {
		console.error('Error loading privacy policy:', error);

		// Attempt to load fallback content
		try {
			const defaultResponse = await fetch('/defaults/privacy.md');
			if (!defaultResponse.ok) {
				throw new Error('Failed to load default privacy policy');
			}
			const policyContent = await defaultResponse.text();
			return {
				policyContent,
				jurisdictionName: 'This Jurisdiction',
				loadError: 'Unable to load jurisdiction-specific privacy policy. Showing default content.'
			};
		} catch (fallbackError) {
			console.error('Error loading fallback privacy policy:', fallbackError);
			return {
				policyContent: '# Privacy Policy\n\nWe were unable to load the privacy policy. Please try again later or contact support.',
				jurisdictionName: 'This Jurisdiction',
				loadError: 'Unable to load privacy policy. Please try again later.'
			};
		}
	}
};
