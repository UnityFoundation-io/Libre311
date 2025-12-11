<script lang="ts">
	import { createEventDispatcher } from 'svelte';


	import ChevronRight from './Svg/outline/ChevronRight.svelte';
	import ChevronLeft from './Svg/outline/ChevronLeft.svelte';
	import type { Pagination } from '$lib/services/Libre311/Libre311';
    import {Button} from "$lib/components/ui/button";

	interface Props {
		pagination: Pagination;
		prevPage?: string | undefined;
		nextPage?: string | undefined;
	}

	let { pagination, prevPage = undefined, nextPage = undefined }: Props = $props();

	const dispatch = createEventDispatcher<{ pageChange: void }>();
	function scrollDispatch() {
		dispatch('pageChange');
	}

	let maxUpperBound = $derived((pagination.pageNumber + 1) * pagination.size);
	let upperBound = $derived(Math.min(maxUpperBound, pagination.totalSize));
	let lowerBound = $derived(maxUpperBound - pagination.size + 1);
</script>

<div class="text-base font-semibold text-slate-600">
	{lowerBound} - {upperBound} of {pagination?.totalSize}
	<span class="ml-1">
		<Button
			href={prevPage}
			variant={prevPage ? 'ghost' : 'ghost'}
			disabled={!prevPage}
			size="icon"
			on:click={scrollDispatch}
		>
			{#snippet icon()}
						<ChevronLeft  />
					{/snippet}
		</Button>
		<Button
			href={nextPage}
			variant={nextPage ? 'ghost' : 'ghost'}
			disabled={!nextPage}
			size="icon"
			on:click={scrollDispatch}
		>
			{#snippet icon()}
						<ChevronRight  />
					{/snippet}
		</Button>
	</span>
</div>
