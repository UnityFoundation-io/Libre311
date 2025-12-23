/**
 * TableWithDetailPane Component Contract
 *
 * Feature: 002-admin-table-detail-pane
 *
 * A layout component that displays a data table with an optional
 * slide-in detail pane on the left side.
 */

/**
 * Component Props
 */
export interface TableWithDetailPaneProps {
	/**
	 * Whether the detail pane is currently visible.
	 * When true, the layout splits into detail pane (left) + table (right).
	 * When false, the table takes full width.
	 */
	detailPaneOpen: boolean;

	/**
	 * Fixed width of the detail pane when open.
	 * @default '400px'
	 */
	detailPaneWidth?: string;

	/**
	 * Duration of the slide animation in milliseconds.
	 * @default 300
	 */
	animationDuration?: number;

	/**
	 * Breakpoint (in pixels) below which mobile behavior activates.
	 * On mobile, detail pane replaces table entirely instead of split view.
	 * @default 769
	 */
	mobileBreakpoint?: number;
}

/**
 * Component Slots
 */
export interface TableWithDetailPaneSlots {
	/**
	 * Main table content area.
	 * Receives the data table, filters, and CSV download button.
	 */
	table: {};

	/**
	 * Detail pane content area.
	 * Receives the ServiceRequest component for viewing/editing selected request.
	 * Only rendered when detailPaneOpen is true.
	 */
	'detail-pane': {};
}

/**
 * Component Events
 */
export interface TableWithDetailPaneEvents {
	/**
	 * Emitted when the detail pane close button is clicked.
	 * Parent should handle navigation to close the pane.
	 */
	close: CustomEvent<void>;
}

/**
 * CSS Custom Properties
 *
 * The component exposes these CSS custom properties for theming:
 *
 * --detail-pane-bg: Background color of detail pane (default: surface color)
 * --detail-pane-shadow: Box shadow for detail pane (default: subtle shadow)
 * --animation-easing: Easing function for slide animation (default: ease-out)
 */

/**
 * Responsive Behavior
 *
 * Desktop (>= mobileBreakpoint):
 * - Split view: detail pane (left, fixed width) + table (right, flexible)
 * - Both visible simultaneously
 *
 * Mobile (< mobileBreakpoint):
 * - When detailPaneOpen=false: Table full screen
 * - When detailPaneOpen=true: Detail pane full screen, table hidden
 * - Back button navigates to table view
 */

/**
 * Accessibility
 *
 * - Detail pane uses <aside> element with role="complementary"
 * - Focus management: Focus moves to detail pane when opened
 * - Escape key closes detail pane (optional enhancement)
 * - Screen reader announces "Detail view opened/closed"
 */
