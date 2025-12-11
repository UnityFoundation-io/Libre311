import { error } from '@sveltejs/kit';
import type { PageLoad } from './$types';

type PolicyType = 'terms' | 'privacy';

interface PolicyConfig {
	field: string;
	title: string;
	defaultContent: string;
	errorMessage: string;
}

const policyConfigs: Record<PolicyType, PolicyConfig> = {
	terms: {
		field: 'terms_of_use_content',
		title: 'Terms of Use',
		defaultContent:
			'# Terms of Use\n\nWe were unable to load the terms of use. Please try again later or contact support.',
		errorMessage: 'Unable to load terms of use. Please try again later.'
	},
	privacy: {
		field: 'privacy_policy_content',
		title: 'Privacy Policy',
		defaultContent:
			'# Privacy Policy\n\nWe were unable to load the privacy policy. Please try again later or contact support.',
		errorMessage: 'Unable to load privacy policy. Please try again later.'
	}
};

function isValidPolicyType(type: string): type is PolicyType {
	return type in policyConfigs;
}

export const load: PageLoad = async ({ fetch, params }) => {
	const { type } = params;

	if (!isValidPolicyType(type)) {
		throw error(404, 'Policy not found');
	}

	const config = policyConfigs[type];
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
			policyContent: jurisdiction[config.field],
			policyTitle: config.title,
			jurisdictionName: jurisdiction.name || 'This Jurisdiction',
			loadError: null
		};
	} catch (err) {
		console.error(`Error loading ${config.title.toLowerCase()}:`, err);
		return {
			policyContent: config.defaultContent,
			policyTitle: config.title,
			jurisdictionName: 'This Jurisdiction',
			loadError: config.errorMessage
		};
	}
};
