<script lang="ts">
	import { page } from '$app/stores';
	import ServiceRequestUpdate from '$lib/components/ServiceRequestUpdate.svelte';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { useSelectedServiceRequestStore } from '$lib/context/ServiceRequestsContext';

	const selectedServiceRequest = useSelectedServiceRequestStore();
	const linkResolver = useLibre311Context().linkResolver;
</script>

{#if $selectedServiceRequest}
	<ServiceRequestUpdate
		serviceRequest={$selectedServiceRequest}
		back={linkResolver.issuesTable($page.url)}
	/>
{/if}
