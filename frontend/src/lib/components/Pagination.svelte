<script lang="ts">
	import { Button } from 'stwui';

	import { chevronRightSvg } from './Svg/outline/ChevronRight.svelte';
	import { chevronLeftSvg } from './Svg/outline/ChevronLeft.svelte';
	import type { Pagination } from '$lib/services/Libre311/Libre311';

	export let pagination: Pagination;
	export let prevPage: string | undefined = undefined;
	export let nextPage: string | undefined = undefined;
	export let element: HTMLElement;

	function scrollToTop() {
		element.scrollIntoView();
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
			on:click={scrollToTop}
		>
			<Button.Icon data={chevronLeftSvg} slot="icon" />
		</Button>
		<Button
			href={nextPage}
			type={nextPage ? 'text' : 'ghost'}
			disabled={!nextPage}
			shape="circle"
			on:click={scrollToTop}
		>
			<Button.Icon data={chevronRightSvg} slot="icon" />
		</Button>
	</span>
</div>
