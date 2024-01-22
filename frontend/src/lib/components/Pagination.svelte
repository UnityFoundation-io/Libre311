<script lang="ts">
	import { Button } from 'stwui';

	import { chevronRightSvg } from './Svg/outline/ChevronRight.svelte';
	import { chevronLeftSvg } from './Svg/outline/ChevronLeft.svelte';
	import type { Pagination } from '$lib/services/Libre311/Libre311';

	export let pagination: Pagination;
	export let prevPage: string | undefined = undefined;
	export let nextPage: string | undefined = undefined;

	$: intermediate = (pagination.pageNumber + 1) * pagination.size;
	$: upperBound = Math.min(intermediate, pagination.totalSize);
	$: lowerBound = intermediate - pagination.size;
</script>

<div class="text-base font-semibold text-slate-600">
	{lowerBound} - {upperBound} of {pagination?.totalSize}
	<span class="ml-1">
		<Button href={prevPage} type={prevPage ? 'text' : 'ghost'} disabled={!prevPage} shape="circle">
			<Button.Icon data={chevronLeftSvg} slot="icon" />
		</Button>
		<Button href={nextPage} type={nextPage ? 'text' : 'ghost'} disabled={!nextPage} shape="circle">
			<Button.Icon data={chevronRightSvg} slot="icon" />
		</Button>
	</span>
</div>
