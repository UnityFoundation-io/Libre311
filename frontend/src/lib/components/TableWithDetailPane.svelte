<script lang="ts">
	import { slide } from 'svelte/transition';
	import { createEventDispatcher, tick, onMount } from 'svelte';
	import { browser } from '$app/environment';

	/**
	 * Whether the detail pane is currently visible.
	 * When true, the layout splits into detail pane (left) + table (right).
	 * When false, the table takes full width.
	 */
	export let detailPaneOpen = false;

	/**
	 * Fixed width of the detail pane when open.
	 * @default '400px'
	 */
	export let detailPaneWidth = '400px';

	/**
	 * Duration of the slide animation in milliseconds.
	 * @default 300
	 */
	export let animationDuration = 300;

	/**
	 * Breakpoint (in pixels) below which mobile behavior activates.
	 * On mobile, detail pane replaces table entirely instead of split view.
	 * @default 769
	 */
	export let mobileBreakpoint = 769;

	const dispatch = createEventDispatcher<{ close: void }>();

	// Reference to detail pane element for focus management
	let detailPaneElement: HTMLElement | undefined;

	// Track reduced motion preference (WCAG 2.3.3)
	let prefersReducedMotion = false;

	onMount(() => {
		if (browser) {
			const mediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
			prefersReducedMotion = mediaQuery.matches;

			// Listen for changes to the preference
			const handleChange = (e: MediaQueryListEvent) => {
				prefersReducedMotion = e.matches;
			};
			mediaQuery.addEventListener('change', handleChange);

			return () => mediaQuery.removeEventListener('change', handleChange);
		}
	});

	// Effective animation duration respecting reduced motion preference
	$: effectiveDuration = prefersReducedMotion ? 0 : animationDuration;

	function handleClose() {
		dispatch('close');
	}

	// Focus management: move focus to detail pane when opened (WCAG 2.4.3)
	$: if (detailPaneOpen && browser) {
		tick().then(() => {
			detailPaneElement?.focus();
		});
	}

	// Reactive CSS variable for detail pane width
	$: gridColumns = detailPaneOpen ? `${detailPaneWidth} 1fr` : '1fr';
</script>

<div
	class="layout-container"
	style:--detail-pane-width={detailPaneWidth}
	style:--mobile-breakpoint="{mobileBreakpoint}px"
	style:grid-template-columns={gridColumns}
>
	{#if detailPaneOpen}
		<aside
			bind:this={detailPaneElement}
			class="detail-pane"
			aria-label="Detail view"
			tabindex="-1"
			transition:slide={{ duration: effectiveDuration, axis: 'x' }}
		>
			<slot name="detail-pane" {handleClose} />
		</aside>
	{/if}

	<main class="table-area" class:hidden-on-mobile={detailPaneOpen}>
		<slot name="table" />
	</main>
</div>

<style>
	.layout-container {
		display: grid;
		height: 100%;
		width: 100%;
		overflow: hidden;
	}

	.detail-pane {
		height: 100%;
		overflow-y: auto;
		background-color: var(--detail-pane-bg, hsl(var(--surface)));
		box-shadow: var(--detail-pane-shadow, 2px 0 8px rgba(0, 0, 0, 0.1));
		border-right: 1px solid hsl(var(--border));
	}

	.table-area {
		height: 100%;
		overflow: hidden;
		display: flex;
		flex-direction: column;
	}

	/* Mobile styles: detail pane replaces table entirely */
	@media (max-width: 768px) {
		.layout-container {
			grid-template-columns: 1fr !important;
		}

		.detail-pane {
			position: absolute;
			top: 0;
			left: 0;
			right: 0;
			bottom: 0;
			width: 100%;
			z-index: 10;
			box-shadow: none;
			border-right: none;
		}

		.hidden-on-mobile {
			display: none;
		}
	}
</style>
