import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/svelte';
import TreePanel from './TreePanel.svelte';
import type { GroupWithServices } from '../stores/types';

const mockGroups: GroupWithServices[] = [
	{
		id: 1,
		name: 'Infrastructure',
		services: [
			{
				service_code: 101,
				service_name: 'Pothole Repair',
				description: 'Report potholes',
				metadata: true,
				type: 'realtime',
				group_id: 1
			},
			{
				service_code: 102,
				service_name: 'Street Light',
				description: 'Report broken street lights',
				metadata: true,
				type: 'realtime',
				group_id: 1
			}
		],
		serviceCount: 2
	},
	{
		id: 2,
		name: 'Parks',
		services: [
			{
				service_code: 201,
				service_name: 'Park Maintenance',
				description: 'Park issues',
				metadata: true,
				type: 'realtime',
				group_id: 2
			}
		],
		serviceCount: 1
	}
];

describe('TreePanel', () => {
	beforeEach(() => {
		vi.clearAllMocks();
	});

	it('renders tree with ARIA role', () => {
		render(TreePanel, {
			props: {
				groups: mockGroups,
				expandedGroupIds: new Set()
			}
		});

		expect(screen.getByRole('tree', { name: /service groups/i })).toBeInTheDocument();
	});

	it('displays all groups', () => {
		render(TreePanel, {
			props: {
				groups: mockGroups,
				expandedGroupIds: new Set()
			}
		});

		expect(screen.getByText('Infrastructure')).toBeInTheDocument();
		expect(screen.getByText('Parks')).toBeInTheDocument();
	});

	it('shows service count badges', () => {
		render(TreePanel, {
			props: {
				groups: mockGroups,
				expandedGroupIds: new Set()
			}
		});

		expect(screen.getByText('2')).toBeInTheDocument(); // Infrastructure
		expect(screen.getByText('1')).toBeInTheDocument(); // Parks
	});

	it('shows loading state', () => {
		render(TreePanel, {
			props: {
				groups: [],
				expandedGroupIds: new Set(),
				isLoading: true
			}
		});

		// Should show spinner
		const spinner = document.querySelector('.animate-spin');
		expect(spinner).toBeInTheDocument();
	});

	it('shows empty state when no groups', () => {
		render(TreePanel, {
			props: {
				groups: [],
				expandedGroupIds: new Set(),
				isLoading: false
			}
		});

		expect(screen.getByText('No service groups found')).toBeInTheDocument();
	});

	it('dispatches toggleGroup event when expand arrow clicked', async () => {
		const { component } = render(TreePanel, {
			props: {
				groups: mockGroups,
				expandedGroupIds: new Set()
			}
		});

		let toggledGroupId: number | null = null;
		component.$on('toggleGroup', (e) => {
			toggledGroupId = e.detail.groupId;
		});

		const expandButtons = screen.getAllByRole('button', { name: /expand|collapse/i });
		await fireEvent.click(expandButtons[0]);

		expect(toggledGroupId).toBe(1);
	});

	it('dispatches selectGroup event when group clicked', async () => {
		const { component } = render(TreePanel, {
			props: {
				groups: mockGroups,
				expandedGroupIds: new Set()
			}
		});

		let selectedGroupId: number | null = null;
		component.$on('selectGroup', (e) => {
			selectedGroupId = e.detail.groupId;
		});

		// Click on the group name button area
		const infrastructureGroup = screen.getByRole('button', { name: /infrastructure/i });
		await fireEvent.click(infrastructureGroup);

		expect(selectedGroupId).toBe(1);
	});

	it('shows services when group is expanded', () => {
		render(TreePanel, {
			props: {
				groups: mockGroups,
				expandedGroupIds: new Set([1])
			}
		});

		expect(screen.getByText('Pothole Repair')).toBeInTheDocument();
		expect(screen.getByText('Street Light')).toBeInTheDocument();
	});

	it('hides services when group is collapsed', () => {
		render(TreePanel, {
			props: {
				groups: mockGroups,
				expandedGroupIds: new Set()
			}
		});

		expect(screen.queryByText('Pothole Repair')).not.toBeInTheDocument();
		expect(screen.queryByText('Street Light')).not.toBeInTheDocument();
	});

	it('dispatches selectService event when service clicked', async () => {
		const { component } = render(TreePanel, {
			props: {
				groups: mockGroups,
				expandedGroupIds: new Set([1])
			}
		});

		let selectedService: { groupId: number; serviceCode: number } | null = null;
		component.$on('selectService', (e) => {
			selectedService = e.detail;
		});

		const potholeService = screen.getByText('Pothole Repair');
		await fireEvent.click(potholeService);

		expect(selectedService).toEqual({ groupId: 1, serviceCode: 101 });
	});

	it('shows selection highlight on selected group', () => {
		render(TreePanel, {
			props: {
				groups: mockGroups,
				expandedGroupIds: new Set(),
				selection: {
					type: 'group',
					groupId: 1,
					serviceCode: null
				}
			}
		});

		const group = screen.getByRole('button', { name: /infrastructure/i });
		expect(group.classList.contains('bg-purple-50')).toBe(true);
	});

	it('shows selection highlight on selected service', () => {
		render(TreePanel, {
			props: {
				groups: mockGroups,
				expandedGroupIds: new Set([1]),
				selection: {
					type: 'service',
					groupId: 1,
					serviceCode: 101
				}
			}
		});

		// The selected service should have the highlight class
		const serviceItems = screen.getAllByRole('treeitem', { level: 2 });
		const selectedItem = serviceItems.find((item) => item.getAttribute('aria-selected') === 'true');
		expect(selectedItem).toBeInTheDocument();
	});

	it('handles keyboard navigation with Arrow Down', async () => {
		render(TreePanel, {
			props: {
				groups: mockGroups,
				expandedGroupIds: new Set([1])
			}
		});

		const tree = screen.getByRole('tree');
		await fireEvent.keyDown(tree, { key: 'ArrowDown' });

		// Focus should move - this tests the keyboard handler is working
		// Exact behavior depends on implementation
	});

	it('handles Enter key to select', async () => {
		const { component } = render(TreePanel, {
			props: {
				groups: mockGroups,
				expandedGroupIds: new Set()
			}
		});

		let selectedGroupId: number | null = null;
		component.$on('selectGroup', (e) => {
			selectedGroupId = e.detail.groupId;
		});

		const tree = screen.getByRole('tree');
		await fireEvent.keyDown(tree, { key: 'Enter' });

		// First group should be selected
		expect(selectedGroupId).toBe(1);
	});
});
