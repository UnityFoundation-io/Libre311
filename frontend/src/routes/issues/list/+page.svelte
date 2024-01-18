<script lang="ts">
	import messages from '$media/messages.json';
	import ServiceRequestPreview from '$lib/components/ServiceRequestPreview.svelte';
	import { usePaginationStore, useServiceRequestsStore } from '$lib/context/ServiceRequestsContext';
	import Pagination from '$lib/components/Pagination.svelte';

	const serviceRequestsStore = useServiceRequestsStore();
	const paginationStore = usePaginationStore();
</script>

<div class='flex items-center justify-center'>

	<div class='max-w-lg w-full'>
		{#if $paginationStore}
			<div class="flex justify-center">
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
	</div>
</div>
