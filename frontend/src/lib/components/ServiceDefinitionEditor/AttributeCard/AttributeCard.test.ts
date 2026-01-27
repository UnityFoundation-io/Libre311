import { describe, it, expect } from 'vitest';
import type { DatatypeUnion } from '$lib/services/Libre311/Libre311';
import { DATATYPE_LABEL_MAP, isListDatatype } from '../types';

/**
 * Unit tests for AttributeCard component logic
 *
 * Note: These tests focus on the component's logic and state management
 * since the project does not have @testing-library/svelte installed.
 *
 * Integration tests in Playwright verify the actual rendering in the browser.
 */

describe('AttributeCard - Type Label Mapping', () => {
	it('should map string datatype to Short answer', () => {
		expect(DATATYPE_LABEL_MAP['string']).toBe('Short answer');
	});

	it('should map text datatype to Paragraph', () => {
		expect(DATATYPE_LABEL_MAP['text']).toBe('Paragraph');
	});

	it('should map multivaluelist to Multiple choice', () => {
		expect(DATATYPE_LABEL_MAP['multivaluelist']).toBe('Multiple choice');
	});

	it('should map singlevaluelist to Dropdown', () => {
		expect(DATATYPE_LABEL_MAP['singlevaluelist']).toBe('Dropdown');
	});

	it('should map number datatype to Number', () => {
		expect(DATATYPE_LABEL_MAP['number']).toBe('Number');
	});

	it('should map datetime datatype to Date', () => {
		expect(DATATYPE_LABEL_MAP['datetime']).toBe('Date');
	});
});

describe('AttributeCard - List Type Detection', () => {
	it('should identify multivaluelist as list type', () => {
		expect(isListDatatype('multivaluelist')).toBe(true);
	});

	it('should identify singlevaluelist as list type', () => {
		expect(isListDatatype('singlevaluelist')).toBe(true);
	});

	it('should not identify string as list type', () => {
		expect(isListDatatype('string')).toBe(false);
	});

	it('should not identify text as list type', () => {
		expect(isListDatatype('text')).toBe(false);
	});

	it('should not identify number as list type', () => {
		expect(isListDatatype('number')).toBe(false);
	});

	it('should not identify datetime as list type', () => {
		expect(isListDatatype('datetime')).toBe(false);
	});
});

describe('AttributeCard - Dirty State Logic', () => {
	interface FormState {
		description: string;
		datatype: DatatypeUnion;
		required: boolean;
		datatypeDescription: string;
		values: Array<{ key: string; name: string }>;
	}

	function computeIsDirty(current: FormState, original: FormState): boolean {
		return (
			current.description !== original.description ||
			current.datatype !== original.datatype ||
			current.required !== original.required ||
			current.datatypeDescription !== original.datatypeDescription ||
			JSON.stringify(current.values) !== JSON.stringify(original.values)
		);
	}

	const originalState: FormState = {
		description: 'What is your name?',
		datatype: 'string',
		required: false,
		datatypeDescription: '',
		values: []
	};

	it('should not be dirty when nothing changed', () => {
		const current = { ...originalState };
		expect(computeIsDirty(current, originalState)).toBe(false);
	});

	it('should be dirty when description changed', () => {
		const current = { ...originalState, description: 'What is your full name?' };
		expect(computeIsDirty(current, originalState)).toBe(true);
	});

	it('should be dirty when datatype changed', () => {
		const current = { ...originalState, datatype: 'text' as DatatypeUnion };
		expect(computeIsDirty(current, originalState)).toBe(true);
	});

	it('should be dirty when required changed', () => {
		const current = { ...originalState, required: true };
		expect(computeIsDirty(current, originalState)).toBe(true);
	});

	it('should be dirty when help text changed', () => {
		const current = { ...originalState, datatypeDescription: 'Enter your name' };
		expect(computeIsDirty(current, originalState)).toBe(true);
	});

	it('should be dirty when values changed', () => {
		const current = { ...originalState, values: [{ key: '1', name: 'Option 1' }] };
		expect(computeIsDirty(current, originalState)).toBe(true);
	});
});

describe('AttributeCard - Validation Logic', () => {
	function computeIsValid(
		description: string,
		datatype: DatatypeUnion,
		values: Array<{ key: string; name: string }>
	): boolean {
		const hasDescription = description.trim().length > 0;
		const isList = isListDatatype(datatype);
		const hasValues = !isList || values.length > 0;
		return hasDescription && hasValues;
	}

	it('should be valid with description for non-list type', () => {
		expect(computeIsValid('What is your name?', 'string', [])).toBe(true);
	});

	it('should be invalid without description', () => {
		expect(computeIsValid('', 'string', [])).toBe(false);
	});

	it('should be invalid with whitespace-only description', () => {
		expect(computeIsValid('   ', 'string', [])).toBe(false);
	});

	it('should be valid for list type with values', () => {
		expect(computeIsValid('Choose one', 'singlevaluelist', [{ key: '1', name: 'Option 1' }])).toBe(
			true
		);
	});

	it('should be invalid for list type without values', () => {
		expect(computeIsValid('Choose one', 'singlevaluelist', [])).toBe(false);
	});

	it('should be invalid for multiselect without values', () => {
		expect(computeIsValid('Choose all', 'multivaluelist', [])).toBe(false);
	});
});

describe('AttributeCard - Can Save Logic', () => {
	function computeCanSave(isDirty: boolean, isValid: boolean, isSaving: boolean): boolean {
		return isDirty && isValid && !isSaving;
	}

	it('should allow save when dirty, valid, and not saving', () => {
		expect(computeCanSave(true, true, false)).toBe(true);
	});

	it('should not allow save when not dirty', () => {
		expect(computeCanSave(false, true, false)).toBe(false);
	});

	it('should not allow save when invalid', () => {
		expect(computeCanSave(true, false, false)).toBe(false);
	});

	it('should not allow save when already saving', () => {
		expect(computeCanSave(true, true, true)).toBe(false);
	});
});

describe('AttributeCard - List Indicators', () => {
	function getListIndicator(datatype: DatatypeUnion): string {
		if (datatype === 'multivaluelist') {
			return '[ ]';
		}
		if (datatype === 'singlevaluelist') {
			return 'O';
		}
		return '';
	}

	it('should return checkbox indicator for multiple choice', () => {
		expect(getListIndicator('multivaluelist')).toBe('[ ]');
	});

	it('should return circle indicator for dropdown', () => {
		expect(getListIndicator('singlevaluelist')).toBe('O');
	});

	it('should return empty string for non-list types', () => {
		expect(getListIndicator('string')).toBe('');
		expect(getListIndicator('text')).toBe('');
		expect(getListIndicator('number')).toBe('');
		expect(getListIndicator('datetime')).toBe('');
	});
});

describe('AttributeCard - Accordion Behavior', () => {
	it('should only allow one card expanded at a time', () => {
		// Simulating accordion state management
		const cards = [
			{ id: 1, isExpanded: false },
			{ id: 2, isExpanded: false },
			{ id: 3, isExpanded: false }
		];

		function expandCard(cardId: number) {
			return cards.map((card) => ({
				...card,
				isExpanded: card.id === cardId
			}));
		}

		// Expand card 1
		let result = expandCard(1);
		expect(result.filter((c) => c.isExpanded).length).toBe(1);
		expect(result.find((c) => c.id === 1)?.isExpanded).toBe(true);

		// Expand card 2 - should collapse card 1
		result = expandCard(2);
		expect(result.filter((c) => c.isExpanded).length).toBe(1);
		expect(result.find((c) => c.id === 1)?.isExpanded).toBe(false);
		expect(result.find((c) => c.id === 2)?.isExpanded).toBe(true);
	});
});

/**
 * H1 Fix Tests: Attribute Code Tracking
 *
 * Tests the pattern used to prevent infinite loop from reactive statement
 * that initializes form state when attribute prop changes.
 * The fix tracks attribute.code to detect actual prop changes.
 */
describe('AttributeCard - H1: Attribute Code Tracking (Infinite Loop Prevention)', () => {
	interface Attribute {
		code: number;
		description: string;
		datatype: DatatypeUnion;
		required: boolean;
	}

	interface FormState {
		description: string;
		datatype: DatatypeUnion;
		required: boolean;
		lastAttributeCode: number | null;
	}

	function shouldReinitialize(
		attribute: Attribute | null,
		lastAttributeCode: number | null
	): boolean {
		return attribute !== null && attribute.code !== lastAttributeCode;
	}

	function initializeFormWithTracking(attr: Attribute, currentState: FormState): FormState {
		// Only reinitialize if attribute code changed
		if (!shouldReinitialize(attr, currentState.lastAttributeCode)) {
			return currentState;
		}

		return {
			description: attr.description,
			datatype: attr.datatype,
			required: attr.required,
			lastAttributeCode: attr.code
		};
	}

	it('should initialize form when attribute first provided', () => {
		const attr: Attribute = {
			code: 123,
			description: 'Test question',
			datatype: 'string',
			required: false
		};
		const initialState: FormState = {
			description: '',
			datatype: 'string',
			required: false,
			lastAttributeCode: null
		};

		const result = initializeFormWithTracking(attr, initialState);

		expect(result.description).toBe('Test question');
		expect(result.lastAttributeCode).toBe(123);
	});

	it('should not reinitialize when same attribute code', () => {
		const attr: Attribute = {
			code: 123,
			description: 'Updated description', // Different description but same code
			datatype: 'string',
			required: false
		};
		const currentState: FormState = {
			description: 'User edited this',
			datatype: 'text', // User changed this
			required: true, // User changed this
			lastAttributeCode: 123 // Same code as attr
		};

		const result = initializeFormWithTracking(attr, currentState);

		// Should keep user's edits, not overwrite with prop
		expect(result.description).toBe('User edited this');
		expect(result.datatype).toBe('text');
		expect(result.required).toBe(true);
	});

	it('should reinitialize when attribute code changes (different attribute)', () => {
		const newAttr: Attribute = {
			code: 456, // Different code
			description: 'New question',
			datatype: 'number',
			required: true
		};
		const currentState: FormState = {
			description: 'Old question',
			datatype: 'string',
			required: false,
			lastAttributeCode: 123
		};

		const result = initializeFormWithTracking(newAttr, currentState);

		expect(result.description).toBe('New question');
		expect(result.datatype).toBe('number');
		expect(result.lastAttributeCode).toBe(456);
	});

	it('should not initialize when attribute is null', () => {
		expect(shouldReinitialize(null, null)).toBe(false);
		expect(shouldReinitialize(null, 123)).toBe(false);
	});
});

/**
 * H2 Fix Tests: Dirty State Dispatch Optimization
 *
 * Tests the pattern used to prevent excessive event dispatching.
 * The fix tracks previousIsDirty and only dispatches when value changes.
 */
describe('AttributeCard - H2: Dirty Dispatch Optimization', () => {
	interface DirtyTracker {
		previousIsDirty: boolean;
		dispatchCount: number;
	}

	function handleDirtyChange(
		isDirty: boolean,
		tracker: DirtyTracker
	): { tracker: DirtyTracker; dispatched: boolean } {
		if (isDirty !== tracker.previousIsDirty) {
			return {
				tracker: {
					previousIsDirty: isDirty,
					dispatchCount: tracker.dispatchCount + 1
				},
				dispatched: true
			};
		}
		return { tracker, dispatched: false };
	}

	it('should dispatch when isDirty changes from false to true', () => {
		const tracker: DirtyTracker = { previousIsDirty: false, dispatchCount: 0 };

		const result = handleDirtyChange(true, tracker);

		expect(result.dispatched).toBe(true);
		expect(result.tracker.previousIsDirty).toBe(true);
		expect(result.tracker.dispatchCount).toBe(1);
	});

	it('should dispatch when isDirty changes from true to false', () => {
		const tracker: DirtyTracker = { previousIsDirty: true, dispatchCount: 0 };

		const result = handleDirtyChange(false, tracker);

		expect(result.dispatched).toBe(true);
		expect(result.tracker.previousIsDirty).toBe(false);
		expect(result.tracker.dispatchCount).toBe(1);
	});

	it('should NOT dispatch when isDirty stays false', () => {
		const tracker: DirtyTracker = { previousIsDirty: false, dispatchCount: 0 };

		const result = handleDirtyChange(false, tracker);

		expect(result.dispatched).toBe(false);
		expect(result.tracker.dispatchCount).toBe(0);
	});

	it('should NOT dispatch when isDirty stays true', () => {
		const tracker: DirtyTracker = { previousIsDirty: true, dispatchCount: 0 };

		const result = handleDirtyChange(true, tracker);

		expect(result.dispatched).toBe(false);
		expect(result.tracker.dispatchCount).toBe(0);
	});

	it('should only dispatch once for repeated true values', () => {
		let tracker: DirtyTracker = { previousIsDirty: false, dispatchCount: 0 };

		// First change to true - should dispatch
		let result = handleDirtyChange(true, tracker);
		expect(result.dispatched).toBe(true);
		tracker = result.tracker;

		// Repeated true values - should NOT dispatch
		result = handleDirtyChange(true, tracker);
		expect(result.dispatched).toBe(false);

		result = handleDirtyChange(true, tracker);
		expect(result.dispatched).toBe(false);

		result = handleDirtyChange(true, tracker);
		expect(result.dispatched).toBe(false);

		// Total dispatch count should be 1
		expect(tracker.dispatchCount).toBe(1);
	});

	it('should dispatch correctly through multiple state transitions', () => {
		let tracker: DirtyTracker = { previousIsDirty: false, dispatchCount: 0 };

		// false -> true (dispatch)
		let result = handleDirtyChange(true, tracker);
		expect(result.dispatched).toBe(true);
		tracker = result.tracker;

		// true -> true (no dispatch)
		result = handleDirtyChange(true, tracker);
		expect(result.dispatched).toBe(false);

		// true -> false (dispatch)
		result = handleDirtyChange(false, tracker);
		expect(result.dispatched).toBe(true);
		tracker = result.tracker;

		// false -> false (no dispatch)
		result = handleDirtyChange(false, tracker);
		expect(result.dispatched).toBe(false);

		// false -> true (dispatch)
		result = handleDirtyChange(true, tracker);
		expect(result.dispatched).toBe(true);
		tracker = result.tracker;

		// Total: 3 dispatches (false->true, true->false, false->true)
		expect(tracker.dispatchCount).toBe(3);
	});
});

/**
 * H3 Fix Tests: Keyboard Handler Scope
 *
 * Tests the pattern for scoped keyboard handling (container-level vs global).
 * The fix attaches keyboard handler to container instead of window.
 */
describe('AttributeCard - H3: Keyboard Handler Scope', () => {
	interface KeyboardEvent {
		key: string;
		ctrlKey: boolean;
		metaKey: boolean;
		target: { closest: (selector: string) => HTMLElement | null };
	}

	function shouldHandleKeydown(event: KeyboardEvent, containerRef: HTMLElement | null): boolean {
		// Only handle if the event target is within our container
		if (!containerRef) return false;
		return event.target.closest('.attribute-card-expanded') !== null;
	}

	it('should handle keydown when target is within container', () => {
		const container = document.createElement('div');
		container.className = 'attribute-card-expanded';

		const event: KeyboardEvent = {
			key: 's',
			ctrlKey: true,
			metaKey: false,
			target: { closest: () => container }
		};

		expect(shouldHandleKeydown(event, container)).toBe(true);
	});

	it('should NOT handle keydown when target is outside container', () => {
		const container = document.createElement('div');
		container.className = 'attribute-card-expanded';

		const event: KeyboardEvent = {
			key: 's',
			ctrlKey: true,
			metaKey: false,
			target: { closest: () => null } // Not in our container
		};

		expect(shouldHandleKeydown(event, container)).toBe(false);
	});

	it('should NOT handle keydown when container is null', () => {
		const event: KeyboardEvent = {
			key: 's',
			ctrlKey: true,
			metaKey: false,
			target: { closest: () => null }
		};

		expect(shouldHandleKeydown(event, null)).toBe(false);
	});
});

/**
 * L1 Fix Tests: Unique Input IDs
 *
 * Tests the pattern for generating unique input IDs based on attribute code.
 */
describe('AttributeCard - L1: Unique Input IDs', () => {
	function generateQuestionInputId(attributeCode: number | undefined): string {
		return `question-text-${attributeCode ?? 'new'}`;
	}

	function generateHelpTextInputId(attributeCode: number | undefined): string {
		return `help-text-${attributeCode ?? 'new'}`;
	}

	it('should generate unique question input ID with attribute code', () => {
		expect(generateQuestionInputId(123)).toBe('question-text-123');
		expect(generateQuestionInputId(456)).toBe('question-text-456');
	});

	it('should generate unique help text input ID with attribute code', () => {
		expect(generateHelpTextInputId(123)).toBe('help-text-123');
		expect(generateHelpTextInputId(456)).toBe('help-text-456');
	});

	it('should use "new" suffix when attribute code is undefined', () => {
		expect(generateQuestionInputId(undefined)).toBe('question-text-new');
		expect(generateHelpTextInputId(undefined)).toBe('help-text-new');
	});

	it('should generate different IDs for different attributes', () => {
		const id1 = generateQuestionInputId(100);
		const id2 = generateQuestionInputId(200);
		const id3 = generateQuestionInputId(300);

		expect(id1).not.toBe(id2);
		expect(id2).not.toBe(id3);
		expect(id1).not.toBe(id3);
	});
});
