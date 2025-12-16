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

<div class="text-base font-semibold text-slate-600">
	{lowerBound} - {upperBound} of {pagination?.totalSize}
	<span class="ml-1">
		<Button
			href={prevPage}
			type={prevPage ? 'text' : 'ghost'}
			disabled={!prevPage}
			shape="circle"
			aria-label="previous set"
			on:click={scrollDispatch}
		>
			<ChevronLeft slot="icon" />
		</Button>
		<Button
			href={nextPage}
			type={nextPage ? 'text' : 'ghost'}
			disabled={!nextPage}
			shape="circle"
			aria-label="next set"
			on:click={scrollDispatch}
		>
			<ChevronRight slot="icon" />
		</Button>
	</span>
</div>
