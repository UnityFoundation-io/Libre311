<script lang="ts">
	import { onMount, onDestroy, createEventDispatcher } from 'svelte';
	import DOMPurify from 'dompurify';
	import { marked } from 'marked';
	import type EasyMDEType from 'easymde';

	export let value: string = '';
	export let placeholder: string = '';

	const dispatch = createEventDispatcher<{ change: string }>();

	let textareaElement: HTMLTextAreaElement;
	let easyMDE: EasyMDEType | null = null;

	onMount(async () => {
		const [{ default: EasyMDE }] = await Promise.all([
			import('easymde'),
			// @ts-ignore - CSS import for side effects
			import('easymde/dist/easymde.min.css')
		]);

		easyMDE = new EasyMDE({
			element: textareaElement,
			initialValue: value,
			placeholder,
			spellChecker: false,
			status: false,
			previewRender: (plainText: string) => {
				const rawHtml = marked.parse(plainText) as string;
				return DOMPurify.sanitize(rawHtml);
			},
			toolbar: [
				'bold',
				'italic',
				'heading',
				'|',
				'unordered-list',
				'ordered-list',
				'|',
				'link',
				'horizontal-rule',
				'|',
				'preview',
				'side-by-side',
				'guide'
			]
		});

		easyMDE.codemirror.on('change', () => {
			if (easyMDE) {
				const newValue = easyMDE.value();
				value = newValue;
				dispatch('change', newValue);
			}
		});
	});

	onDestroy(() => {
		if (easyMDE) {
			easyMDE.toTextArea();
			easyMDE = null;
		}
	});

	export function reset(newValue: string) {
		value = newValue;
		if (easyMDE) {
			easyMDE.value(newValue);
		}
	}
</script>

<div class="markdown-editor">
	<textarea bind:this={textareaElement}></textarea>
</div>

<style>
	.markdown-editor :global(.EasyMDEContainer) {
		border: 1px solid #d1d5db;
		border-radius: 0.5rem;
		overflow: hidden;
	}

	.markdown-editor :global(.EasyMDEContainer .CodeMirror) {
		border: none;
		border-radius: 0;
		min-height: 300px;
	}

	.markdown-editor :global(.editor-toolbar) {
		border: none;
		border-bottom: 1px solid #e5e7eb;
		border-radius: 0;
	}
</style>
