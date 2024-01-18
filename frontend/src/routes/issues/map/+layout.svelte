<script lang="ts">
	import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';
	import ServiceRequestsContextProvider from '$lib/context/ServiceRequestsContextProvider.svelte';
</script>

<ServiceRequestsContextProvider>
	<SideBarMainContentLayout>
		<slot slot="side-bar" />
		<div slot="main-content">map goes here</div>
	</SideBarMainContentLayout>
</ServiceRequestsContextProvider>
