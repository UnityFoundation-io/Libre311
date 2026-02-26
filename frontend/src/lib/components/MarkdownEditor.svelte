<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import DOMPurify from 'dompurify';
	import { Carta, CartaEditor } from 'carta-md';

	// Import styles
	import 'carta-md/default.css';

	export let value: string = '';
	export let placeholder: string = '';

	const dispatch = createEventDispatcher<{ change: string }>();

	const carta = new Carta({
		sanitizer: DOMPurify.sanitize
	});

	$: {
		dispatch('change', value);
	}

	export function reset(newValue: string) {
		value = newValue;
	}
</script>

<div class="markdown-editor">
	<CartaEditor {carta} bind:value mode="auto" {placeholder} />
</div>

<style lang="postcss">
	/* Required for consistent code rendering */
	:global(.carta-font-code) {
		font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono',
			'Courier New', monospace;
		font-size: 0.875rem;
		line-height: 1.5;
		letter-spacing: normal;
	}

	/* Basic markdown styling using Tailwind @apply */
	:global(.carta-renderer h1) {
		@apply mb-4 mt-6 text-3xl font-bold;
	}

	:global(.carta-renderer h2) {
		@apply mb-3 mt-5 text-2xl font-bold;
	}

	:global(.carta-renderer h3) {
		@apply mb-2 mt-4 text-xl font-semibold;
	}

	:global(.carta-renderer p) {
		@apply mb-4;
	}

	:global(.carta-renderer ul),
	:global(.carta-renderer ol) {
		@apply mb-4 ml-6;
	}

	:global(.carta-renderer li) {
		@apply mb-2;
	}

	:global(.carta-renderer a) {
		@apply text-blue-600 underline;
	}

	:global(.carta-renderer a:hover) {
		@apply text-blue-800;
	}

	:global(.carta-renderer strong) {
		@apply font-semibold;
	}

	:global(.carta-renderer em) {
		@apply italic;
	}

	:global(.carta-renderer hr) {
		@apply my-6 border-gray-300;
	}

	.markdown-editor :global(textarea.carta-font-code) {
		font-size: 0.875rem;
	}

	/* Show bold and italic in the editor input without syntax colors */
	.markdown-editor :global(.shj-syn-class) {
		font-weight: bold;
		color: inherit;
	}

	.markdown-editor :global(.shj-syn-kwd) {
		font-style: italic;
		color: inherit;
	}
	.markdown-editor :global(.carta-toolbar) {
		height: 2.5rem;
	}
</style>
