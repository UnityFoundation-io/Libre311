<script lang="ts">
	import { page } from '$app/stores';
	import ServiceRequestUpdate from '$lib/components/ServiceRequestUpdate.svelte';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { useSelectedServiceRequestStore } from '$lib/context/ServiceRequestsContext';

	const selectedServiceRequest = useSelectedServiceRequestStore();
	const linkResolver = useLibre311Context().linkResolver;
</script>

<a href={linkResolver.issuesTable($page.url)}>Back to table</a>

{#if $selectedServiceRequest}
	<div class="flex items-center justify-center">
		<div class="w-full">
			<ServiceRequestUpdate serviceRequest={$selectedServiceRequest} />
		</div>
	</div>
{/if}
