<script lang="ts">
	import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';
	import { useServiceRequestsContext } from '$lib/context/ServiceRequestsContext';

	const ctx = useServiceRequestsContext();
	const resStore = ctx.serviceRequestsResponse;

	$: console.log($resStore);
</script>

<SideBarMainContentLayout>
	<slot slot="side-bar" />
	<div slot="main-content">map goes here</div>
</SideBarMainContentLayout>
