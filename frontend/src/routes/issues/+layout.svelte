<script lang="ts">
	import ServiceRequestsContextProvider from '$lib/context/ServiceRequestsContextProvider.svelte';
	interface Props {
		children?: import('svelte').Snippet;
	}

	let { children }: Props = $props();
</script>

<ServiceRequestsContextProvider>
	{@render children?.()}
</ServiceRequestsContextProvider>
