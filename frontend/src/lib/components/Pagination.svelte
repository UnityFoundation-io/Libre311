<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import { Button } from 'stwui';

	import ChevronRight from './Svg/outline/ChevronRight.svelte';
	import ChevronLeft from './Svg/outline/ChevronLeft.svelte';
	import type { Pagination } from '$lib/services/Libre311/Libre311';

	export let pagination: Pagination;
	export let prevPage: string | undefined = undefined;
	export let nextPage: string | undefined = undefined;

	const dispatch = createEventDispatcher<{ pageChange: void }>();
	function scrollDispatch() {
		dispatch('pageChange');
	}

	$: maxUpperBound = (pagination.pageNumber + 1) * pagination.size;
	$: upperBound = Math.min(maxUpperBound, pagination.totalSize);
	$: lowerBound = maxUpperBound - pagination.size + 1;
</script>

<nav aria-label="Pagination" class="text-base font-semibold text-slate-600">
	<span aria-live="polite" aria-atomic="true"
		>{lowerBound} - {upperBound} of {pagination?.totalSize}</span
	>
	<span class="ml-1 whitespace-nowrap">
		<Button
			href={prevPage}
			type={prevPage ? 'text' : 'ghost'}
			disabled={!prevPage}
			shape="circle"
			title="Previous Set"
			aria-label="Previous Set"
			on:click={scrollDispatch}
		>
			<ChevronLeft slot="icon" />
		</Button>
		<Button
			href={nextPage}
			type={nextPage ? 'text' : 'ghost'}
			disabled={!nextPage}
			shape="circle"
			title="Next Set"
			aria-label="Next Set"
			on:click={scrollDispatch}
		>
			<ChevronRight slot="icon" />
		</Button>
	</span>
</nav>
