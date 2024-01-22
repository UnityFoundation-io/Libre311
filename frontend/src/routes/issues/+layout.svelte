<script lang="ts">
	import ServiceRequestsContextProvider from '$lib/context/ServiceRequestsContextProvider.svelte';
</script>

<ServiceRequestsContextProvider>
	<slot />
</ServiceRequestsContextProvider>
