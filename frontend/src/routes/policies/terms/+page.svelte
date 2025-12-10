<script lang="ts">
	import MarkdownRenderer from '$lib/components/MarkdownRenderer.svelte';
	import type { PageData } from './$types';

	export let data: PageData;
</script>

<svelte:head>
	<title>Terms of Use - {data.jurisdictionName}</title>
</svelte:head>

<div class="container mx-auto max-w-4xl px-4 py-8">
	{#if data.loadError}
		<div class="mb-4 rounded-md border border-yellow-300 bg-yellow-50 p-4 text-yellow-800">
			{data.loadError}
		</div>
	{/if}
	<MarkdownRenderer markdown={data.policyContent} class="prose max-w-none" />
</div>
