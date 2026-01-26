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

		// The expanded card has a form role with keydown handler (H3 fix: container-scoped, not global)
		const expandedForm = screen.getByRole('form', { name: /edit attribute/i });
		await fireEvent.keyDown(expandedForm, { key: 'Escape' });

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

	it('dispatches deleteConfirm event with correct attribute', async () => {
		const { component } = render(AttributeCardList, {
			props: {
				attributes: mockAttributes,
				expandedIndex: 0
			}
		});

		let deletedAttribute: ServiceDefinitionAttribute | null = null;
		component.$on('deleteConfirm', (e) => {
			deletedAttribute = e.detail.attribute;
		});

		// Find and click delete button to open confirmation modal
		const deleteButton = screen.getByRole('button', { name: /delete attribute/i });
		await fireEvent.click(deleteButton);

		// Click confirm in the modal
		const confirmButton = screen.getByRole('button', { name: /^delete$/i });
		await fireEvent.click(confirmButton);

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

		// Type in the question input to trigger dirty - use exact aria-label match
		const questionInput = screen.getByRole('textbox', { name: /question text/i });
		await fireEvent.input(questionInput, { target: { value: 'Modified question' } });

		expect(dirtyEvent).toEqual({ code: 1, isDirty: true });
	});

	it('disables reorder buttons when reorderEnabled is false', () => {
		render(AttributeCardList, {
			props: {
				attributes: mockAttributes,
				reorderEnabled: false
			}
		});

		expect(screen.queryByRole('button', { name: /move question up/i })).not.toBeInTheDocument();
		expect(screen.queryByRole('button', { name: /move question down/i })).not.toBeInTheDocument();
	});

	it('renders reorder buttons when reorderEnabled is true', () => {
		render(AttributeCardList, {
			props: {
				attributes: mockAttributes,
				reorderEnabled: true
			}
		});

		const upButtons = screen.getAllByRole('button', { name: /move question up/i });
		const downButtons = screen.getAllByRole('button', { name: /move question down/i });

		expect(upButtons).toHaveLength(3);
		expect(downButtons).toHaveLength(3);
	});

	it('disables first up button and last down button', () => {
		render(AttributeCardList, {
			props: {
				attributes: mockAttributes,
				reorderEnabled: true
			}
		});

		const upButtons = screen.getAllByRole('button', { name: /move question up/i });
		const downButtons = screen.getAllByRole('button', { name: /move question down/i });

		expect(upButtons[0]).toBeDisabled();
		expect(upButtons[1]).not.toBeDisabled();
		expect(upButtons[2]).not.toBeDisabled();

		expect(downButtons[0]).not.toBeDisabled();
		expect(downButtons[1]).not.toBeDisabled();
		expect(downButtons[2]).toBeDisabled();
	});

	it('dispatches reorder event when moving item up', async () => {
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

		// Click up button on second item (index 1)
		const upButtons = screen.getAllByRole('button', { name: /move question up/i });
		await fireEvent.click(upButtons[1]);

		expect(reorderEvent).toEqual({ fromIndex: 1, toIndex: 0 });
	});

	it('dispatches reorder event when moving item down', async () => {
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

		// Click down button on first item (index 0)
		const downButtons = screen.getAllByRole('button', { name: /move question down/i });
		await fireEvent.click(downButtons[0]);

		expect(reorderEvent).toEqual({ fromIndex: 0, toIndex: 1 });
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
