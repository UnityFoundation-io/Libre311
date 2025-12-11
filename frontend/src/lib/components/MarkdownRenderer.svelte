<script lang="ts">
	import { marked } from 'marked';
	import DOMPurify from 'dompurify';

	export let markdown: string;
	let className: string = '';
	export { className as class };

	let html = '';

	// Configure marked options
	marked.setOptions({
		breaks: true, // GFM line breaks
		gfm: true // GitHub Flavored Markdown
	});

	// Render markdown to HTML and sanitize to prevent XSS
	$: {
		try {
			const rawHtml = marked.parse(markdown, { async: false });
			html = DOMPurify.sanitize(rawHtml);
		} catch (error) {
			console.error('Error parsing markdown:', error);
			html = '<p>Error rendering content</p>';
		}
	}
</script>

<div class="markdown-renderer {className}">
	<!-- eslint-disable-next-line svelte/no-at-html-tags -->
	{@html html}
</div>

<style lang="postcss">
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
