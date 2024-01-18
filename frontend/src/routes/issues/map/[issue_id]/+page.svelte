<script lang="ts">
	import ServiceRequestDetails from '$lib/components/ServiceRequestDetails.svelte';
	import {
		useIssuesLinkStore,
		useSelectedServiceRequestStore,
		useServiceRequestsStore
	} from '$lib/context/ServiceRequestsContext';

	const issuesLink = useIssuesLinkStore();
	const serviceRequestsStore = useServiceRequestsStore();
	const selectedServiceRequest = useSelectedServiceRequestStore();
</script>

<a href={$issuesLink}>Back to map</a>
{#if $selectedServiceRequest}
	<ServiceRequestDetails serviceRequest={$selectedServiceRequest} />
{/if}
