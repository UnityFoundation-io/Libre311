<script lang="ts">
	import messages from '$media/messages.json';
	import ServiceRequestPreview from '$lib/components/ServiceRequestPreview.svelte';
	import { usePaginationStore, useServiceRequestsStore } from '$lib/context/ServiceRequestsContext';
	import Pagination from '$lib/components/Pagination.svelte';

	const serviceRequestsStore = useServiceRequestsStore();
	const paginationStore = usePaginationStore();
</script>

<h1 class="mx-2 text-3xl">{messages['sidebar']['title']}</h1>

{#if $paginationStore}
	<div class="flex justify-end">
		<Pagination pagination={$paginationStore} />
	</div>
{/if}

<ul>
	{#if $serviceRequestsStore.type === 'success'}
		{#each $serviceRequestsStore.value as serviceRequest}
			<li class="m-3">
				<ServiceRequestPreview {serviceRequest} />
			</li>
		{/each}
	{/if}
</ul>
