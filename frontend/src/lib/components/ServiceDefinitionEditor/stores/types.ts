import type {
	DatatypeUnion,
	Service,
	ServiceDefinitionAttribute,
	Group,
	AttributeValue
} from '$lib/services/Libre311/Libre311';

/**
 * Type of item selected in the tree panel
 */
export type SelectionType = 'group' | 'service' | null;

/**
 * Unique identifier for a card (for dirty tracking)
 */
export type CardId = 'group' | 'header' | `attr-${number}`;

/**
 * Editor selection state
 */
export interface EditorSelection {
	type: SelectionType;
	groupId: number | null;
	serviceCode: number | null;
}

/**
 * Unsaved changes state for a single card
 */
export interface CardDirtyState {
	isDirty: boolean;
	originalValue: unknown;
	currentValue: unknown;
}

/**
 * Complete editor state for the split-pane interface
 */
export interface SplitPaneEditorState {
	// Selection
	selection: EditorSelection;

	// Tree expansion state (which groups are expanded)
	expandedGroupIds: Set<number>;

	// Which attribute card is currently expanded (only one at a time)
	expandedAttributeId: number | null;

	// Per-card dirty state tracking
	cardStates: Map<CardId, CardDirtyState>;

	// Loading states
	isLoading: boolean;
	isSaving: Map<CardId, boolean>;

	// Error state
	error: string | null;

	// Unsaved changes modal state
	pendingNavigation: (() => void) | null;
	showUnsavedModal: boolean;
}

/**
 * Tree panel data structure (groups with their services)
 */
export interface TreeData {
	groups: GroupWithServices[];
}

/**
 * Group with its services for tree display
 */
export interface GroupWithServices extends Group {
	services: Service[];
	serviceCount: number;
}

/**
 * Service with full definition (including attributes)
 */
export interface ServiceWithDefinition extends Service {
	attributes: ServiceDefinitionAttribute[];
}

/**
 * Form data for creating/updating a group
 */
export interface GroupFormData {
	name: string;
}

/**
 * Form data for creating/updating a service header
 */
export interface ServiceHeaderFormData {
	serviceName: string;
	description: string;
}

/**
 * Form data for creating/updating an attribute
 */
export interface AttributeFormData {
	description: string;
	datatype: DatatypeUnion;
	required: boolean;
	datatypeDescription: string;
	values?: AttributeValue[];
}

/**
 * Drag operation state
 */
export interface DragState {
	isDragging: boolean;
	dragType: 'service' | 'attribute' | null;
	draggedId: number | null;
	dropTargetId: number | null;
	dropPosition: 'before' | 'after' | null;
}

/**
 * Props for attribute editing components
 */
export interface AttributeEditProps {
	attribute: ServiceDefinitionAttribute;
	isExpanded: boolean;
	isDirty: boolean;
	isSaving: boolean;
	onExpand: () => void;
	onCollapse: () => void;
	onSave: (data: AttributeFormData) => Promise<void>;
	onCancel: () => void;
	onCopy: () => void;
	onDelete: () => void;
}
