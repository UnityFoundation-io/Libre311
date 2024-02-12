<script lang="ts">
	import AdminTable from '$lib/components/AdminTable.svelte';
	import { useLibre311Context } from '$lib/context/Libre311Context';

	import { useServiceRequestsContext } from '$lib/context/ServiceRequestsContext';

	const ctx = useServiceRequestsContext();
	const serviceRequestsRes = ctx.serviceRequestsResponse;

	const linkResolver = useLibre311Context().linkResolver;
</script>

{#if $serviceRequestsRes.type === 'success'}
	<AdminTable serviceRequests={$serviceRequestsRes.value.serviceRequests} />
{/if}
