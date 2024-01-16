<script lang="ts">
	// import { page } from '$app/stores';
	import ServiceRequestDetails from '$lib/components/ServiceRequestDetails.svelte';
	import { serviceRequestsResponse } from '../serviceRequestsResponse';	// replace this with the real response
</script>

<!-- <h1>load and show issues detail for {+$page.params.issue_id}</h1> -->

<a href="/issues/map">
	<div class="m-2">
		<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-6 h-6">
			<path fill-rule="evenodd" d="M9.53 2.47a.75.75 0 0 1 0 1.06L4.81 8.25H15a6.75 6.75 0 0 1 0 13.5h-3a.75.75 0 0 1 0-1.5h3a5.25 5.25 0 1 0 0-10.5H4.81l4.72 4.72a.75.75 0 1 1-1.06 1.06l-6-6a.75.75 0 0 1 0-1.06l6-6a.75.75 0 0 1 1.06 0Z" clip-rule="evenodd" />
		</svg>
	</div>
</a>

<ServiceRequestDetails serviceRequest={serviceRequestsResponse[1]}/>
