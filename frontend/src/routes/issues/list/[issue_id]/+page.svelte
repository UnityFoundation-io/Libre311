<script lang="ts">
	import ServiceRequestDetails from '$lib/components/ServiceRequestDetails.svelte';
	import { useSelectedServiceRequestStore } from '$lib/context/ServiceRequestsContext';

	const selectedServiceRequest = useSelectedServiceRequestStore();
</script>

{#if $selectedServiceRequest}
	<div class="flex h-full w-full items-center justify-center">
		<div class="h-full w-full md:w-3/4 lg:w-1/2">
			<ServiceRequestDetails serviceRequest={$selectedServiceRequest} />
		</div>
	</div>
{/if}
