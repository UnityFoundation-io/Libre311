import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/svelte';
import ServiceRequestUpdate from './ServiceRequestUpdate.svelte';
import { useLibre311Service } from '$lib/context/Libre311Context';
import { useJurisdiction } from '$lib/context/JurisdictionContext';
import { writable, type Readable } from 'svelte/store';
import type {
	Libre311Service,
	JurisdictionConfig,
	ServiceRequest
} from '$lib/services/Libre311/Libre311';

vi.mock('$lib/context/Libre311Context', () => ({
	useLibre311Service: vi.fn(),
	useLibre311Context: vi.fn(() => ({
		alertError: vi.fn(),
		alert: vi.fn(),
		projects: writable([]),
		fetchProjectsAdmin: vi.fn().mockResolvedValue(undefined),
		user: writable({
			permissions: []
		})
	}))
}));

vi.mock('$lib/context/JurisdictionContext', () => ({
	useJurisdiction: vi.fn()
}));

describe('ServiceRequestUpdate', () => {
	const mockLibre311Service = {
		getProjects: vi.fn().mockResolvedValue([])
	};

	const mockJurisdictionStore = writable<JurisdictionConfig>({
		project_feature: 'REQUIRED'
	} as JurisdictionConfig);

	const mockServiceRequest: ServiceRequest = {
		service_request_id: 1,
		status: 'open',
		service_name: 'Test Service',
		requested_datetime: '2023-01-01T00:00:00Z',
		address: '123 Test St',
		service_code: 1
	} as ServiceRequest;

	beforeEach(() => {
		vi.clearAllMocks();
		vi.mocked(useLibre311Service).mockReturnValue(
			mockLibre311Service as unknown as Libre311Service
		);
		vi.mocked(useJurisdiction).mockReturnValue(
			mockJurisdictionStore as unknown as Readable<JurisdictionConfig>
		);
	});

	it('should show project selection when project_feature is not DISABLED', () => {
		mockJurisdictionStore.set({ project_feature: 'REQUIRED' } as JurisdictionConfig);
		render(ServiceRequestUpdate, { props: { serviceRequest: mockServiceRequest } });
		expect(screen.getByText('Project')).toBeInTheDocument();
	});

	it('should hide project selection when project_feature is DISABLED', () => {
		mockJurisdictionStore.set({ project_feature: 'DISABLED' } as JurisdictionConfig);
		render(ServiceRequestUpdate, { props: { serviceRequest: mockServiceRequest } });
		expect(screen.queryByText('Project')).not.toBeInTheDocument();
	});
});
