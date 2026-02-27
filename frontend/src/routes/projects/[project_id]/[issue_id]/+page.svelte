<script lang="ts">
	import { page } from '$app/stores';
	import ServiceRequest from '$lib/components/ServiceRequest.svelte';
	import { useSelectedServiceRequestStore } from '$lib/context/ServiceRequestsContext';

	const selectedServiceRequest = useSelectedServiceRequestStore();
</script>

{#if $selectedServiceRequest}
	<ServiceRequest
		serviceRequest={$selectedServiceRequest}
		back={`/projects/${$page.params.project_id}`}
	/>
{/if}
