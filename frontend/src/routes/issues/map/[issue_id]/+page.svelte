<script lang="ts">
	import { page } from '$app/stores';
	import ServiceRequestDetails from '$lib/components/ServiceRequestDetails.svelte';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { useSelectedServiceRequestStore } from '$lib/context/ServiceRequestsContext';

	const selectedServiceRequest = useSelectedServiceRequestStore();
	const { linkResolver, networkStatus } = useLibre311Context();
	const isOnline = networkStatus.isOnline;
</script>

{#if $selectedServiceRequest}
	<ServiceRequestDetails
		serviceRequest={$selectedServiceRequest}
		back={linkResolver.issuesMap($page.url)}
	/>
{:else if !$isOnline}
	<div class="flex h-full flex-col items-center justify-center gap-4 p-6 text-center">
		<p class="text-base">Cannot show request details while offline.</p>
		<a href={linkResolver.issuesMap($page.url)} class="text-primary underline">Back to Map</a>
	</div>
{/if}
