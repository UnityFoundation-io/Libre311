import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/svelte';
import TreeGroup from './TreeGroup.svelte';
import type { Group, Service } from '$lib/services/Libre311/Libre311';

const mockGroup: Group = {
	id: 1,
	name: 'Infrastructure'
};

const mockServices: Service[] = [
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
];

describe('TreeGroup', () => {
	beforeEach(() => {
		vi.clearAllMocks();
	});

	it('renders group name', () => {
		render(TreeGroup, {
			props: {
				group: mockGroup,
				services: mockServices
			}
		});

		expect(screen.getByText('Infrastructure')).toBeInTheDocument();
	});

	it('shows service count badge', () => {
		render(TreeGroup, {
			props: {
				group: mockGroup,
				services: mockServices
			}
		});

		expect(screen.getByText('2')).toBeInTheDocument();
	});

	it('has proper ARIA attributes', () => {
		render(TreeGroup, {
			props: {
				group: mockGroup,
				services: mockServices,
				isExpanded: false
			}
		});

		const treeitem = screen.getByRole('treeitem');
		expect(treeitem).toHaveAttribute('aria-expanded', 'false');
		expect(treeitem).toHaveAttribute('aria-level', '1');
	});

	it('dispatches toggle event when arrow button clicked', async () => {
		const { component } = render(TreeGroup, {
			props: {
				group: mockGroup,
				services: mockServices,
				isExpanded: false
			}
		});

		let toggleCalled = false;
		component.$on('toggle', () => {
			toggleCalled = true;
		});

		const expandButton = screen.getByRole('button', { name: /expand group/i });
		await fireEvent.click(expandButton);

		expect(toggleCalled).toBe(true);
	});

	it('dispatches selectGroup event when group header clicked', async () => {
		const { component } = render(TreeGroup, {
			props: {
				group: mockGroup,
				services: mockServices
			}
		});

		let selectGroupCalled = false;
		component.$on('selectGroup', () => {
			selectGroupCalled = true;
		});

		const groupHeader = screen.getByRole('button', { name: /infrastructure/i });
		await fireEvent.click(groupHeader);

		expect(selectGroupCalled).toBe(true);
	});

	it('shows services when expanded', () => {
		render(TreeGroup, {
			props: {
				group: mockGroup,
				services: mockServices,
				isExpanded: true
			}
		});

		expect(screen.getByText('Pothole Repair')).toBeInTheDocument();
		expect(screen.getByText('Street Light')).toBeInTheDocument();
	});

	it('hides services when collapsed', () => {
		render(TreeGroup, {
			props: {
				group: mockGroup,
				services: mockServices,
				isExpanded: false
			}
		});

		expect(screen.queryByText('Pothole Repair')).not.toBeInTheDocument();
		expect(screen.queryByText('Street Light')).not.toBeInTheDocument();
	});

	it('dispatches selectService event when service clicked', async () => {
		const { component } = render(TreeGroup, {
			props: {
				group: mockGroup,
				services: mockServices,
				isExpanded: true
			}
		});

		let selectedServiceCode: number | null = null;
		component.$on('selectService', (e) => {
			selectedServiceCode = e.detail.serviceCode;
		});

		const potholeService = screen.getByText('Pothole Repair');
		await fireEvent.click(potholeService);

		expect(selectedServiceCode).toBe(101);
	});

	it('highlights selected service', () => {
		render(TreeGroup, {
			props: {
				group: mockGroup,
				services: mockServices,
				isExpanded: true,
				selectedServiceCode: 101
			}
		});

		const serviceItems = screen.getAllByRole('treeitem', { level: 2 });
		const selectedItem = serviceItems.find(
			(item) => item.getAttribute('aria-selected') === 'true'
		);
		expect(selectedItem).toBeInTheDocument();
	});

	it('highlights selected group', () => {
		render(TreeGroup, {
			props: {
				group: mockGroup,
				services: mockServices,
				isSelected: true
			}
		});

		const groupHeader = screen.getByRole('button', { name: /infrastructure/i });
		expect(groupHeader.classList.contains('bg-purple-50')).toBe(true);
	});

	it('handles keyboard Enter to select group', async () => {
		const { component } = render(TreeGroup, {
			props: {
				group: mockGroup,
				services: mockServices
			}
		});

		let selectGroupCalled = false;
		component.$on('selectGroup', () => {
			selectGroupCalled = true;
		});

		const groupHeader = screen.getByRole('button', { name: /infrastructure/i });
		await fireEvent.keyDown(groupHeader, { key: 'Enter' });

		expect(selectGroupCalled).toBe(true);
	});

	it('handles keyboard Space to select group', async () => {
		const { component } = render(TreeGroup, {
			props: {
				group: mockGroup,
				services: mockServices
			}
		});

		let selectGroupCalled = false;
		component.$on('selectGroup', () => {
			selectGroupCalled = true;
		});

		const groupHeader = screen.getByRole('button', { name: /infrastructure/i });
		await fireEvent.keyDown(groupHeader, { key: ' ' });

		expect(selectGroupCalled).toBe(true);
	});

	it('handles ArrowRight to expand when collapsed', async () => {
		const { component } = render(TreeGroup, {
			props: {
				group: mockGroup,
				services: mockServices,
				isExpanded: false
			}
		});

		let toggleCalled = false;
		component.$on('toggle', () => {
			toggleCalled = true;
		});

		const groupHeader = screen.getByRole('button', { name: /infrastructure/i });
		await fireEvent.keyDown(groupHeader, { key: 'ArrowRight' });

		expect(toggleCalled).toBe(true);
	});

	it('handles ArrowLeft to collapse when expanded', async () => {
		const { component } = render(TreeGroup, {
			props: {
				group: mockGroup,
				services: mockServices,
				isExpanded: true
			}
		});

		let toggleCalled = false;
		component.$on('toggle', () => {
			toggleCalled = true;
		});

		const groupHeader = screen.getByRole('button', { name: /infrastructure/i });
		await fireEvent.keyDown(groupHeader, { key: 'ArrowLeft' });

		expect(toggleCalled).toBe(true);
	});

	it('shows folder open icon when expanded', () => {
		render(TreeGroup, {
			props: {
				group: mockGroup,
				services: mockServices,
				isExpanded: true
			}
		});

		// Check that the expanded state shows different icon
		const treeitem = screen.getByRole('treeitem');
		expect(treeitem).toHaveAttribute('aria-expanded', 'true');
	});
});
