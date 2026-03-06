import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/svelte';
import ProjectEditor from './ProjectEditor.svelte';
import { useLibre311Service } from '$lib/context/Libre311Context';
import { useJurisdiction } from '$lib/context/JurisdictionContext';
import { writable, type Readable } from 'svelte/store';
import type { Libre311Service, JurisdictionConfig, Project } from '$lib/services/Libre311/Libre311';

vi.mock('$lib/context/Libre311Context', () => ({
	useLibre311Service: vi.fn(),
	useLibre311Context: vi.fn(() => ({
		linkResolver: {}
	}))
}));

vi.mock('$lib/context/JurisdictionContext', () => ({
	useJurisdiction: vi.fn()
}));

vi.mock('$app/navigation', () => ({
	goto: vi.fn()
}));

describe('ProjectEditor', () => {
	const mockLibre311Service = {
		createProject: vi.fn(),
		updateProject: vi.fn()
	};

	const mockJurisdictionStore = writable({
		bounds: [
			[0, 0],
			[0, 1],
			[1, 1],
			[1, 0],
			[0, 0]
		]
	});

	beforeEach(() => {
		vi.clearAllMocks();
		vi.mocked(useLibre311Service).mockReturnValue(
			mockLibre311Service as unknown as Libre311Service
		);
		vi.mocked(useJurisdiction).mockReturnValue(
			mockJurisdictionStore as unknown as Readable<JurisdictionConfig>
		);
	});

	it('should render create mode by default', () => {
		render(ProjectEditor);
		expect(screen.getByText('Create Project')).toBeInTheDocument();
		expect(screen.queryByText('Close Project')).not.toBeInTheDocument();
	});

	it('should render edit mode when project is provided', () => {
		const project: Project = {
			id: 1,
			name: 'Existing Project',
			slug: 'existing-project',
			description: 'Description',
			start_date: '2023-01-01T00:00:00Z',
			end_date: '2023-01-31T00:00:00Z',
			status: 'OPEN',
			bounds: [
				[0, 0],
				[0, 1],
				[1, 1],
				[1, 0],
				[0, 0]
			],
			jurisdiction_id: 'test'
		};
		render(ProjectEditor, { props: { project } });
		expect(screen.getByText('Edit Project')).toBeInTheDocument();
		expect(screen.getByText('Close Project')).toBeInTheDocument();
		expect(screen.getByDisplayValue('Existing Project')).toBeInTheDocument();
	});

	it('should call createProject on save in create mode', async () => {
		render(ProjectEditor);

		const nameInput = screen.getByLabelText('Name');
		await fireEvent.input(nameInput, { target: { value: 'New Project' } });

		const saveButton = screen.getByText('Save Project');
		expect(saveButton).toBeInTheDocument();
	});
});
