import type {
	DatatypeUnion,
	Service,
	ServiceDefinitionAttribute,
	Group
} from '$lib/services/Libre311/Libre311';

/**
 * UI-friendly datatype labels displayed in the attribute type selector
 */
export type AttributeTypeLabel =
	| 'Short answer'
	| 'Paragraph'
	| 'Multiple choice'
	| 'Dropdown'
	| 'Number'
	| 'Date';

/**
 * Mapping between UI-friendly labels and backend datatype values
 */
export const DATATYPE_MAP: Record<AttributeTypeLabel, DatatypeUnion> = {
	'Short answer': 'string',
	Paragraph: 'text',
	'Multiple choice': 'multivaluelist',
	Dropdown: 'singlevaluelist',
	Number: 'number',
	Date: 'datetime'
};

/**
 * Reverse mapping from backend datatype to UI label
 */
export const DATATYPE_LABEL_MAP: Record<DatatypeUnion, AttributeTypeLabel> = {
	string: 'Short answer',
	text: 'Paragraph',
	multivaluelist: 'Multiple choice',
	singlevaluelist: 'Dropdown',
	number: 'Number',
	datetime: 'Date'
};

/**
 * All available attribute type labels for UI rendering
 */
export const ATTRIBUTE_TYPE_OPTIONS: AttributeTypeLabel[] = [
	'Short answer',
	'Paragraph',
	'Multiple choice',
	'Dropdown',
	'Number',
	'Date'
];

/**
 * Card display state for accordion behavior
 */
export type CardState = 'collapsed' | 'expanded';

/**
 * Extended attribute with UI-specific state for editor cards
 */
export type AttributeCardState = ServiceDefinitionAttribute & {
	cardState: CardState;
	isDirty: boolean;
	isSaving: boolean;
	error?: string;
};

/**
 * Main editor page state
 */
export interface EditorState {
	service: Service | null;
	attributes: AttributeCardState[];
	expandedCardIndex: number | null;
	isLoading: boolean;
	error: string | null;
}

/**
 * Save status for toast notifications
 */
export type SaveStatus =
	| { type: 'idle' }
	| { type: 'saving' }
	| { type: 'success'; message?: string }
	| { type: 'error'; message: string };

/**
 * Group with expansion state for list view
 */
export interface GroupListState extends Group {
	isExpanded: boolean;
	services: Service[];
	isLoading: boolean;
}

/**
 * List view page state for groups/services
 */
export interface ListViewState {
	groups: GroupListState[];
	isLoading: boolean;
	error: string | null;
	newGroupForm: {
		isVisible: boolean;
		name: string;
		error: string | null;
	};
}

/**
 * Check if a datatype supports list values (dropdown/multiple choice)
 */
export function isListDatatype(datatype: DatatypeUnion): boolean {
	return datatype === 'singlevaluelist' || datatype === 'multivaluelist';
}

/**
 * Get the icon name for a given datatype (for UI display)
 */
export function getDatatypeIcon(datatype: DatatypeUnion): string {
	const icons: Record<DatatypeUnion, string> = {
		string: 'short_text',
		text: 'notes',
		multivaluelist: 'checklist',
		singlevaluelist: 'arrow_drop_down_circle',
		number: 'tag',
		datetime: 'calendar_today'
	};
	return icons[datatype];
}
