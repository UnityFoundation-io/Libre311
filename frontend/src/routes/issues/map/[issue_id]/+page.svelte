<script lang="ts">
	import ServiceRequestDetails from '$lib/components/ServiceRequestDetails.svelte';
	import { serviceRequestsResponse } from '../serviceRequestsResponse';	// replace this with the real response
</script>

<ServiceRequestDetails serviceRequest={serviceRequestsResponse[1]}/>
