<script lang="ts">
	import { page } from '$app/state';
	import ServiceRequest from '$lib/components/ServiceRequest.svelte';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { useSelectedServiceRequestStore } from '$lib/context/ServiceRequestsContext';

	const selectedServiceRequest = useSelectedServiceRequestStore();
	const linkResolver = useLibre311Context().linkResolver;
</script>

{#if $selectedServiceRequest}
	<ServiceRequest
		serviceRequest={$selectedServiceRequest}
		back={linkResolver.issuesTable(page.url)}
	/>
{/if}
