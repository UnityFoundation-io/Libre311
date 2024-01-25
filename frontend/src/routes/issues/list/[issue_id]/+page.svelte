<script lang="ts">
	import { page } from '$app/stores';
	import ServiceRequestDetails from '$lib/components/ServiceRequestDetails.svelte';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { useSelectedServiceRequestStore } from '$lib/context/ServiceRequestsContext';

	const selectedServiceRequest = useSelectedServiceRequestStore();
	const linkResolver = useLibre311Context().linkResolver;
</script>

<a href={linkResolver.issuesList($page.url)}>Back to list</a>

{#if $selectedServiceRequest}
<div class="flex items-center justify-center">
	<div class="w-full md:w-3/4 lg:w-1/2">
		<ServiceRequestDetails serviceRequest={$selectedServiceRequest} />
	</div>
</div>
{/if}
