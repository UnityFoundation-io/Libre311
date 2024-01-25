<script lang="ts">
	import { page } from '$app/stores';
	import ServiceRequestDetails from '$lib/components/ServiceRequestDetails.svelte';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { useSelectedServiceRequestStore } from '$lib/context/ServiceRequestsContext';

	const selectedServiceRequest = useSelectedServiceRequestStore();
	const linkResolver = useLibre311Context().linkResolver;
</script>

<a href={linkResolver.issuesMap($page.url)}>Back to map</a>
{#if $selectedServiceRequest}
	<ServiceRequestDetails serviceRequest={$selectedServiceRequest} />
{/if}
