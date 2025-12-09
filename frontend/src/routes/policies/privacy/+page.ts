import type { PageLoad } from './$types';

export const load: PageLoad = async ({ fetch }) => {
	try {
		// Fetch jurisdiction data
		const response = await fetch('/api/jurisdiction', {
			headers: {
				Referer: typeof window !== 'undefined' ? window.location.href : ''
			}
		});

		if (!response.ok) {
			throw new Error('Failed to fetch jurisdiction');
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
			jurisdictionName: jurisdiction.name || 'This Jurisdiction'
		};
	} catch (error) {
		console.error('Error loading privacy policy:', error);
		// Fallback to default on error
		const defaultResponse = await fetch('/defaults/privacy.md');
		const policyContent = await defaultResponse.text();
		return {
			policyContent,
			jurisdictionName: 'This Jurisdiction'
		};
	}
};
