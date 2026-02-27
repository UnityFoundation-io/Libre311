<script lang="ts">
	import { Carta, CartaViewer } from 'carta-md';
	import DOMPurify from 'dompurify';

	export let markdown: string;
	let className: string = '';
	export { className as class };

	const carta = new Carta({
		sanitizer: DOMPurify.sanitize
	});
</script>

<div class="markdown-renderer {className}">
	{#key markdown}
		<CartaViewer {carta} value={markdown} />
	{/key}
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
	.markdown-renderer :global(h1) {
		@apply mb-4 mt-6 text-3xl font-bold;
	}

	.markdown-renderer :global(h2) {
		@apply mb-3 mt-5 text-2xl font-bold;
	}

	.markdown-renderer :global(h3) {
		@apply mb-2 mt-4 text-xl font-semibold;
	}

	.markdown-renderer :global(p) {
		@apply mb-4;
	}

	.markdown-renderer :global(ul),
	.markdown-renderer :global(ol) {
		@apply mb-4 ml-6;
	}

	.markdown-renderer :global(li) {
		@apply mb-2;
	}

	.markdown-renderer :global(a) {
		@apply text-blue-600 underline;
	}

	.markdown-renderer :global(a:hover) {
		@apply text-blue-800;
	}

	.markdown-renderer :global(strong) {
		@apply font-semibold;
	}

	.markdown-renderer :global(em) {
		@apply italic;
	}

	.markdown-renderer :global(hr) {
		@apply my-6 border-gray-300;
	}
</style>
