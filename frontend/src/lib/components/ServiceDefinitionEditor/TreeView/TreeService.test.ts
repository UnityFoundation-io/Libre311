import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/svelte';
import TreeService from './TreeService.svelte';
import type { Service } from '$lib/services/Libre311/Libre311';

const mockService: Service = {
	service_code: 101,
	service_name: 'Pothole Repair',
	description: 'Report potholes in roads',
	metadata: true,
	type: 'realtime',
	group_id: 1
};

describe('TreeService', () => {
	beforeEach(() => {
		vi.clearAllMocks();
	});

	it('renders service name', () => {
		render(TreeService, {
			props: {
				service: mockService
			}
		});

		expect(screen.getByText('Pothole Repair')).toBeInTheDocument();
	});

	it('has proper ARIA attributes', () => {
		render(TreeService, {
			props: {
				service: mockService,
				level: 2
			}
		});

		const treeitem = screen.getByRole('treeitem');
		expect(treeitem).toHaveAttribute('aria-level', '2');
		expect(treeitem).toHaveAttribute('aria-selected', 'false');
	});

	it('shows selected state via aria-selected', () => {
		render(TreeService, {
			props: {
				service: mockService,
				isSelected: true
			}
		});

		const treeitem = screen.getByRole('treeitem');
		expect(treeitem).toHaveAttribute('aria-selected', 'true');
	});

	it('has selection highlight styles when selected', () => {
		render(TreeService, {
			props: {
				service: mockService,
				isSelected: true
			}
		});

		const treeitem = screen.getByRole('treeitem');
		expect(treeitem.classList.contains('bg-blue-50')).toBe(true);
	});

	it('dispatches select event when clicked', async () => {
		const { component } = render(TreeService, {
			props: {
				service: mockService
			}
		});

		let selectCalled = false;
		component.$on('select', () => {
			selectCalled = true;
		});

		const treeitem = screen.getByRole('treeitem');
		await fireEvent.click(treeitem);

		expect(selectCalled).toBe(true);
	});

	it('dispatches select event on Enter key', async () => {
		const { component } = render(TreeService, {
			props: {
				service: mockService
			}
		});

		let selectCalled = false;
		component.$on('select', () => {
			selectCalled = true;
		});

		const treeitem = screen.getByRole('treeitem');
		await fireEvent.keyDown(treeitem, { key: 'Enter' });

		expect(selectCalled).toBe(true);
	});

	it('dispatches select event on Space key', async () => {
		const { component } = render(TreeService, {
			props: {
				service: mockService
			}
		});

		let selectCalled = false;
		component.$on('select', () => {
			selectCalled = true;
		});

		const treeitem = screen.getByRole('treeitem');
		await fireEvent.keyDown(treeitem, { key: ' ' });

		expect(selectCalled).toBe(true);
	});

	it('does not show drag handle when draggable is false', () => {
		render(TreeService, {
			props: {
				service: mockService,
				draggable: false
			}
		});

		// Drag handle should not be rendered
		expect(screen.queryByRole('button', { name: /drag/i })).not.toBeInTheDocument();
	});

	it('shows drag handle when draggable is true', () => {
		render(TreeService, {
			props: {
				service: mockService,
				draggable: true
			}
		});

		// Drag handle should be present (hidden by default, visible on hover)
		const dragHandle = screen.getByRole('button', { name: /drag to reorder/i });
		expect(dragHandle).toBeInTheDocument();
	});

	it('applies opacity when isDragging is true', () => {
		render(TreeService, {
			props: {
				service: mockService,
				isDragging: true
			}
		});

		const treeitem = screen.getByRole('treeitem');
		expect(treeitem.classList.contains('opacity-50')).toBe(true);
	});

	it('dispatches dragstart event when drag handle mousedown', async () => {
		const { component } = render(TreeService, {
			props: {
				service: mockService,
				draggable: true
			}
		});

		let dragstartCalled = false;
		component.$on('dragstart', () => {
			dragstartCalled = true;
		});

		const dragHandle = screen.getByRole('button', { name: /drag to reorder/i });
		await fireEvent.mouseDown(dragHandle);

		expect(dragstartCalled).toBe(true);
	});

	it('is focusable via tabindex', () => {
		render(TreeService, {
			props: {
				service: mockService
			}
		});

		const treeitem = screen.getByRole('treeitem');
		expect(treeitem).toHaveAttribute('tabindex', '0');
	});

	it('shows document icon', () => {
		render(TreeService, {
			props: {
				service: mockService
			}
		});

		// Check for SVG presence (document icon)
		const svg = document.querySelector('svg');
		expect(svg).toBeInTheDocument();
	});

	it('truncates long service names', () => {
		const longNameService = {
			...mockService,
			service_name: 'This is a very long service name that should be truncated in the UI'
		};

		render(TreeService, {
			props: {
				service: longNameService
			}
		});

		const nameSpan = screen.getByText(longNameService.service_name);
		expect(nameSpan.classList.contains('truncate')).toBe(true);
	});
});
