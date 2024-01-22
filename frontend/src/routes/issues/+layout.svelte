<script lang="ts">
	import { page } from '$app/stores';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import ServiceRequestsContextProvider from '$lib/context/ServiceRequestsContextProvider.svelte';
	const linkResolver = useLibre311Context().linkResolver;

	let detailsLink = linkResolver.issuesList($page.url);
</script>

<ServiceRequestsContextProvider>
	<div class="flex justify-center">
		<a href="/issues/map">map</a>
		|
		<a href={detailsLink}>list</a>
	</div>
	<slot />
</ServiceRequestsContextProvider>
