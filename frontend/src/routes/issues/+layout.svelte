<script lang="ts">
	import { page } from '$app/stores';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import ServiceRequestsContextProvider from '$lib/context/ServiceRequestsContextProvider.svelte';
	const linkResolver = useLibre311Context().linkResolver;

	let detailsLink = linkResolver.issuesList($page.url);
</script>

<ServiceRequestsContextProvider>
	<slot />
</ServiceRequestsContextProvider>
