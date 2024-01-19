<script lang="ts">
	import ServiceRequestsContextProvider from '$lib/context/ServiceRequestsContextProvider.svelte';
</script>

<ServiceRequestsContextProvider>
	<div class="flex justify-center">
		<a href="/issues/map">map</a>
		|
		<a href="/issues/list">list</a>
	</div>
	<slot />
</ServiceRequestsContextProvider>
