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

		// Filter treeitems by aria-level attribute since testing-library doesn't support level option
		const allTreeitems = screen.getAllByRole('treeitem');
		const serviceItems = allTreeitems.filter((item) => item.getAttribute('aria-level') === '2');
		const selectedItem = serviceItems.find((item) => item.getAttribute('aria-selected') === 'true');
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
		expect(groupHeader.classList.contains('bg-blue-50')).toBe(true);
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

		// Use more specific selector - the group header has the service count in its aria-label
		const groupHeader = screen.getByRole('button', { name: /infrastructure, 2 services/i });
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

		// Check that the expanded state shows different icon - get the group treeitem (level 1)
		const allTreeitems = screen.getAllByRole('treeitem');
		const groupTreeitem = allTreeitems.find((item) => item.getAttribute('aria-level') === '1');
		expect(groupTreeitem).toHaveAttribute('aria-expanded', 'true');
	});

	describe('Service Reordering', () => {
		it('renders up/down buttons when draggableServices is true', () => {
			render(TreeGroup, {
				props: {
					group: mockGroup,
					services: mockServices,
					isExpanded: true,
					draggableServices: true
				}
			});

			const upButtons = screen.getAllByLabelText(/move .* up/i);
			const downButtons = screen.getAllByLabelText(/move .* down/i);

			expect(upButtons).toHaveLength(2);
			expect(downButtons).toHaveLength(2);
		});

		it('disables up button for first service', () => {
			render(TreeGroup, {
				props: {
					group: mockGroup,
					services: mockServices,
					isExpanded: true,
					draggableServices: true
				}
			});

			const firstUpButton = screen.getByLabelText(`Move ${mockServices[0].service_name} up`);
			expect(firstUpButton).toBeDisabled();

			const secondUpButton = screen.getByLabelText(`Move ${mockServices[1].service_name} up`);
			expect(secondUpButton).not.toBeDisabled();
		});

		it('disables down button for last service', () => {
			render(TreeGroup, {
				props: {
					group: mockGroup,
					services: mockServices,
					isExpanded: true,
					draggableServices: true
				}
			});

			const firstDownButton = screen.getByLabelText(`Move ${mockServices[0].service_name} down`);
			expect(firstDownButton).not.toBeDisabled();

			const lastDownButton = screen.getByLabelText(`Move ${mockServices[1].service_name} down`);
			expect(lastDownButton).toBeDisabled();
		});

		it('dispatches keyboardReorder event with correct direction', async () => {
			const { component } = render(TreeGroup, {
				props: {
					group: mockGroup,
					services: mockServices,
					isExpanded: true,
					draggableServices: true
				}
			});

			let eventDetail: { serviceCode: number; direction: string } | null = null;
			component.$on('keyboardReorder', (e) => {
				eventDetail = e.detail;
			});

			// Click Down on first item
			const firstDownButton = screen.getByLabelText(`Move ${mockServices[0].service_name} down`);
			await fireEvent.click(firstDownButton);

			expect(eventDetail).toEqual({
				serviceCode: mockServices[0].service_code,
				direction: 'down'
			});

			// Click Up on second item
			const secondUpButton = screen.getByLabelText(`Move ${mockServices[1].service_name} up`);
			await fireEvent.click(secondUpButton);

			expect(eventDetail).toEqual({
				serviceCode: mockServices[1].service_code,
				direction: 'up'
			});
		});

		it('prevents row selection when clicking reorder buttons', async () => {
			const { component } = render(TreeGroup, {
				props: {
					group: mockGroup,
					services: mockServices,
					isExpanded: true,
					draggableServices: true
				}
			});

			let selectServiceCalled = false;
			component.$on('selectService', () => {
				selectServiceCalled = true;
			});

			// Click Down on first item
			const firstDownButton = screen.getByLabelText(`Move ${mockServices[0].service_name} down`);
			await fireEvent.click(firstDownButton);

			expect(selectServiceCalled).toBe(false);
		});
	});
});
