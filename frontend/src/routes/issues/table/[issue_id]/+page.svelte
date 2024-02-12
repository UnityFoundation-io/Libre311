<script lang="ts">
	import ServiceRequestUpdate from '$lib/components/ServiceRequestUpdate.svelte';
	import { useSelectedServiceRequestStore } from '$lib/context/ServiceRequestsContext';

	const selectedServiceRequest = useSelectedServiceRequestStore();
</script>

{#if $selectedServiceRequest}
	<ServiceRequestUpdate serviceRequest={$selectedServiceRequest} />
{/if}
