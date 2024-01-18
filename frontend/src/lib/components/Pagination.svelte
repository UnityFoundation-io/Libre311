<script lang="ts">
	import { Button } from 'stwui';

	import type { EnhancedPagination } from '$lib/context/ServiceRequestsContext';
	import { chevronRightSvg } from './Svg/outline/ChevronRight.svelte';
	import { chevronLeftSvg } from './Svg/outline/ChevronLeft.svelte';

	export let pagination: EnhancedPagination;

	$: prevPage = pagination.prevPage;
	$: nextPage = pagination.nextPage;
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
		<Button
			href={nextPage}
			type={nextPage ? 'text' : 'ghost'}
			disabled={!pagination.nextPage}
			shape="circle"
		>
			<Button.Icon data={chevronRightSvg} slot="icon" />
		</Button>
	</span>
</div>
