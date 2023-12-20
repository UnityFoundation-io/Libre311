<script lang="ts">
	import Funnel from '$lib/components/Svg/outline/Funnel.svelte';
	import Bars3 from '$lib/components/Svg/Bars3.svelte';
	import Libre311ContextProvider from '$lib/context/Libre311ContextProvider.svelte';
	import type { Libre311ContextProps } from '$lib/context/Libre311Context';
	import { page } from '$app/stores';
	import type { Page } from '@sveltejs/kit';

	function getJurisdictionId(page: Page<Record<string, string>>) {
		const jurisdictionId = page.params.jurisdiction_id;
		if (!jurisdictionId)
			throw new Error('jurisdiction_id is required to initialize the Libre311Context');
		return jurisdictionId;
	}

	const props: Libre311ContextProps = {
		service: {
			baseUrl: 'http://localhost:8080',
			jurisdiction_id: getJurisdictionId($page)
		}
	};
</script>

<Libre311ContextProvider {props}>
	<div class="wrapper">
		<header>
			<h1>Fayetteville, AR</h1>
			<div class="controls">
				<Funnel />
				<Bars3 />
			</div>
		</header>
		<main>
			<slot />
		</main>
	</div>
</Libre311ContextProvider>

<style>
	.wrapper {
		--header-height: 4rem;
	}
	header {
		height: var(--header-height);
		background-color: hsl(var(--primary));
		color: hsl(var(--primary-content));
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 0 1rem;
	}
	.controls {
		display: flex;
		gap: 1rem;
	}

	main {
		height: calc(100vh - var(--header-height));
	}

	:global() {
		--header-height: 4rem;
	}
</style>
