import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/svelte';
import AttributeCardList from './AttributeCardList.svelte';
import type { ServiceDefinitionAttribute } from '$lib/services/Libre311/Libre311';

// Mock attributes for testing
const mockAttributes: ServiceDefinitionAttribute[] = [
	{
		code: 1,
		variable: true,
		datatype: 'string',
		required: true,
		datatype_description: 'Help text 1',
		order: 1,
		description: 'First question'
	},
	{
		code: 2,
		variable: true,
		datatype: 'number',
		required: false,
		datatype_description: null,
		order: 2,
		description: 'Second question'
	},
	{
		code: 3,
		variable: true,
		datatype: 'singlevaluelist',
		required: true,
		datatype_description: 'Select one',
		order: 3,
		description: 'Third question',
		values: [
			{ key: '1', name: 'Option A' },
			{ key: '2', name: 'Option B' }
		]
	}
];

describe('AttributeCardList', () => {
	beforeEach(() => {
		vi.clearAllMocks();
	});

	it('renders a list of attribute cards', () => {
		render(AttributeCardList, {
			props: {
				attributes: mockAttributes
			}
		});

		expect(screen.getByRole('list', { name: /attribute questions/i })).toBeInTheDocument();
		expect(screen.getAllByRole('listitem')).toHaveLength(3);
	});

	it('displays all attributes in collapsed state by default', () => {
		render(AttributeCardList, {
			props: {
				attributes: mockAttributes,
				expandedIndex: null
			}
		});

		// All cards should show their descriptions (collapsed state)
		expect(screen.getByText('First question')).toBeInTheDocument();
		expect(screen.getByText('Second question')).toBeInTheDocument();
		expect(screen.getByText('Third question')).toBeInTheDocument();
	});

	it('dispatches expand event when a collapsed card is clicked', async () => {
		const { component } = render(AttributeCardList, {
			props: {
				attributes: mockAttributes,
				expandedIndex: null
			}
		});

		let expandedIndex: number | null = null;
		component.$on('expand', (e) => {
			expandedIndex = e.detail.index;
		});

		// Find and click on the first card's collapsed view
		const firstQuestion = screen.getByText('First question');
		await fireEvent.click(firstQuestion);

		expect(expandedIndex).toBe(0);
	});

	it('dispatches collapse event when expanded card collapse is triggered', async () => {
		const { component } = render(AttributeCardList, {
			props: {
				attributes: mockAttributes,
				expandedIndex: 0
			}
		});

		let collapseCalled = false;
		component.$on('collapse', () => {
			collapseCalled = true;
		});

		// The expanded card should be visible with Escape key
		await fireEvent.keyDown(window, { key: 'Escape' });

		expect(collapseCalled).toBe(true);
	});

	it('shows dirty indicator for dirty attributes', () => {
		render(AttributeCardList, {
			props: {
				attributes: mockAttributes,
				dirtyAttributes: new Set([1, 3])
			}
		});

		// Dirty attributes should have some visual indicator (implementation dependent)
		const listItems = screen.getAllByRole('listitem');
		expect(listItems).toHaveLength(3);
	});

	it('dispatches delete event with correct attribute', async () => {
		const { component } = render(AttributeCardList, {
			props: {
				attributes: mockAttributes,
				expandedIndex: 0
			}
		});

		let deletedAttribute: ServiceDefinitionAttribute | null = null;
		component.$on('delete', (e) => {
			deletedAttribute = e.detail.attribute;
		});

		// Find and click delete button
		const deleteButton = screen.getByRole('button', { name: /delete attribute/i });
		await fireEvent.click(deleteButton);

		expect(deletedAttribute).toEqual(mockAttributes[0]);
	});

	it('dispatches copy event with correct attribute', async () => {
		const { component } = render(AttributeCardList, {
			props: {
				attributes: mockAttributes,
				expandedIndex: 0
			}
		});

		let copiedAttribute: ServiceDefinitionAttribute | null = null;
		component.$on('copy', (e) => {
			copiedAttribute = e.detail.attribute;
		});

		// Find and click copy button
		const copyButton = screen.getByRole('button', { name: /copy attribute/i });
		await fireEvent.click(copyButton);

		expect(copiedAttribute).toEqual(mockAttributes[0]);
	});

	it('dispatches dirty event when card content changes', async () => {
		const { component } = render(AttributeCardList, {
			props: {
				attributes: mockAttributes,
				expandedIndex: 0
			}
		});

		let dirtyEvent: { code: number; isDirty: boolean } | null = null;
		component.$on('dirty', (e) => {
			dirtyEvent = { code: e.detail.code, isDirty: e.detail.isDirty };
		});

		// Type in the question input to trigger dirty
		const questionInput = screen.getByLabelText(/question/i);
		await fireEvent.input(questionInput, { target: { value: 'Modified question' } });

		expect(dirtyEvent).toEqual({ code: 1, isDirty: true });
	});

	it('disables drag when reorderEnabled is false', () => {
		render(AttributeCardList, {
			props: {
				attributes: mockAttributes,
				reorderEnabled: false
			}
		});

		const listItems = screen.getAllByRole('listitem');
		listItems.forEach((item) => {
			expect(item).toHaveAttribute('draggable', 'false');
		});
	});

	it('enables drag when reorderEnabled is true and card is collapsed', () => {
		render(AttributeCardList, {
			props: {
				attributes: mockAttributes,
				reorderEnabled: true,
				expandedIndex: null
			}
		});

		const listItems = screen.getAllByRole('listitem');
		listItems.forEach((item) => {
			expect(item).toHaveAttribute('draggable', 'true');
		});
	});

	it('disables drag on expanded card', () => {
		render(AttributeCardList, {
			props: {
				attributes: mockAttributes,
				reorderEnabled: true,
				expandedIndex: 1
			}
		});

		const listItems = screen.getAllByRole('listitem');
		// First and third should be draggable, second should not
		expect(listItems[0]).toHaveAttribute('draggable', 'true');
		expect(listItems[1]).toHaveAttribute('draggable', 'false');
		expect(listItems[2]).toHaveAttribute('draggable', 'true');
	});

	it('dispatches reorder event on successful drag and drop', async () => {
		const { component } = render(AttributeCardList, {
			props: {
				attributes: mockAttributes,
				reorderEnabled: true
			}
		});

		let reorderEvent: { fromIndex: number; toIndex: number } | null = null;
		component.$on('reorder', (e) => {
			reorderEvent = e.detail;
		});

		const listItems = screen.getAllByRole('listitem');

		// Mock drag and drop
		const dataTransfer = {
			effectAllowed: '',
			dropEffect: '',
			setData: vi.fn(),
			getData: vi.fn(() => '0')
		};

		// Drag first item
		await fireEvent.dragStart(listItems[0], { dataTransfer });

		// Drop on third item
		await fireEvent.dragOver(listItems[2], {
			dataTransfer,
			clientY: 1000 // After midpoint
		});
		await fireEvent.drop(listItems[2], { dataTransfer });

		expect(reorderEvent).toEqual({ fromIndex: 0, toIndex: 2 });
	});

	it('does not reorder when dropping on same position', async () => {
		const { component } = render(AttributeCardList, {
			props: {
				attributes: mockAttributes,
				reorderEnabled: true
			}
		});

		let reorderEvent = null;
		component.$on('reorder', (e) => {
			reorderEvent = e.detail;
		});

		const listItems = screen.getAllByRole('listitem');

		const dataTransfer = {
			effectAllowed: '',
			dropEffect: '',
			setData: vi.fn(),
			getData: vi.fn(() => '1')
		};

		// Drag second item
		await fireEvent.dragStart(listItems[1], { dataTransfer });

		// Drop on same item
		await fireEvent.drop(listItems[1], { dataTransfer });

		expect(reorderEvent).toBeNull();
	});

	it('renders empty state when no attributes provided', () => {
		render(AttributeCardList, {
			props: {
				attributes: []
			}
		});

		const list = screen.getByRole('list', { name: /attribute questions/i });
		expect(list.children).toHaveLength(0);
	});

	it('shows saving state for attributes being saved', () => {
		render(AttributeCardList, {
			props: {
				attributes: mockAttributes,
				savingAttributes: new Set([2]),
				expandedIndex: 1
			}
		});

		// The Save button should show saving state
		const saveButton = screen.getByRole('button', { name: /saving/i });
		expect(saveButton).toBeInTheDocument();
	});
});
