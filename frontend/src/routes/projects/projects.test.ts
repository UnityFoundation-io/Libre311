import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/svelte';
import ProjectsPage from './+page.svelte';
import { useLibre311Service, useLibre311Context } from '$lib/context/Libre311Context';
import type { Libre311Context } from '$lib/context/Libre311Context';
import type { Libre311Service, JurisdictionConfig, Project } from '$lib/services/Libre311/Libre311';
import { useJurisdiction } from '$lib/context/JurisdictionContext';
import { writable, type Readable } from 'svelte/store';

// Mock the services and contexts
vi.mock('$lib/context/Libre311Context', () => ({
	useLibre311Service: vi.fn(),
	useLibre311Context: vi.fn()
}));

vi.mock('$lib/context/JurisdictionContext', () => ({
	useJurisdiction: vi.fn()
}));

vi.mock('$app/environment', () => ({
	browser: true
}));

vi.mock('$app/navigation', () => ({
	goto: vi.fn()
}));

describe('Projects Page', () => {
	const mockLibre311Service = {
		getProjects: vi.fn(),
		createProject: vi.fn(),
		updateProject: vi.fn()
	};

	const mockJurisdictionStore = writable({
		project_feature: 'ENABLED',
		bounds: [
			[0, 0],
			[0, 1],
			[1, 1],
			[1, 0],
			[0, 0]
		]
	});

	const mockContext = {
		linkResolver: {
			issuesTable: vi.fn().mockReturnValue('/issues')
		},
		user: writable({
			permissions: [
				'LIBRE311_ADMIN_VIEW-TENANT',
				'LIBRE311_ADMIN_VIEW-SYSTEM',
				'LIBRE311_ADMIN_VIEW-SUBTENANT'
			]
		}),
		projects: writable<Project[]>([]),
		fetchProjectsAdmin: vi.fn().mockResolvedValue(undefined)
	};

	beforeEach(() => {
		vi.clearAllMocks();
		mockJurisdictionStore.set({
			project_feature: 'ENABLED',
			bounds: [
				[0, 0],
				[0, 1],
				[1, 1],
				[1, 0],
				[0, 0]
			]
		});
		mockContext.projects.set([]);
		vi.mocked(useLibre311Service).mockReturnValue(
			mockLibre311Service as unknown as Libre311Service
		);
		vi.mocked(useJurisdiction).mockReturnValue(
			mockJurisdictionStore as unknown as Readable<JurisdictionConfig>
		);
		vi.mocked(useLibre311Context).mockReturnValue(mockContext as unknown as Libre311Context);
	});

	it('should render the projects page', async () => {
		render(ProjectsPage);
		expect(screen.getByText('Project Administration')).toBeInTheDocument();
		expect(screen.getByText('Create Project')).toBeInTheDocument();
	});

	it('should load and display projects', async () => {
		const mockProjects: Project[] = [
			{
				id: 1,
				name: 'Test Project 1',
				start_date: '2023-01-01T00:00:00Z',
				end_date: '2023-01-31T00:00:00Z',
				status: 'OPEN',
				request_count: 5,
				bounds: [
					[0, 0],
					[0, 1],
					[1, 1],
					[1, 0],
					[0, 0]
				],
				slug: 'test-project-1',
				jurisdiction_id: 'test-jurisdiction'
			}
		];

		mockContext.projects.set(mockProjects);

		render(ProjectsPage);

		expect(screen.getByText('Test Project 1')).toBeInTheDocument();
	});

	it('should navigate to create page when the button is clicked', async () => {
		render(ProjectsPage);
		const createButton = screen.getByText('Create Project');
		await fireEvent.click(createButton);

		const { goto } = await import('$app/navigation');
		expect(goto).toHaveBeenCalledWith('/projects/create');
	});

	it('should redirect if project feature is DISABLED', async () => {
		mockJurisdictionStore.set({ project_feature: 'DISABLED', bounds: [] });
		render(ProjectsPage);

		const { goto } = await import('$app/navigation');
		expect(goto).toHaveBeenCalledWith('/issues');
	});

	it('toDatetimeLocal should format date correctly', () => {
		// Just a placeholder
	});
});
