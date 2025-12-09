<script lang="ts">
	import MarkdownRenderer from '$lib/components/MarkdownRenderer.svelte';
	import type { PageData } from './$types';

	export let data: PageData;
</script>

<svelte:head>
	<title>Terms of Use - {data.jurisdictionName}</title>
</svelte:head>

<div class="container mx-auto max-w-4xl px-4 py-8">
	<MarkdownRenderer markdown={data.policyContent} class="prose max-w-none" />
</div>
