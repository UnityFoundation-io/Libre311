<script lang='ts'>
	import messages from '$media/messages.json';
	import ServiceRequestPreview from '$lib/components/ServiceRequestPreview.svelte';
	import { serviceRequestsResponse} from './serviceRequestsResponse';	// replace this with the real response
</script>

<h1 class='text-3xl mx-2'>{messages['sidebar']['title']}</h1>

<ul>
	{#each serviceRequestsResponse as serviceRequest}
		<li class='m-3'>
			<ServiceRequestPreview {serviceRequest}/>
		</li>
	{/each}
</ul>
