<script lang="ts">
	import { onMount } from 'svelte';
	import messages from '$media/messages.json';
	import ServiceRequestPreview from '$lib/components/ServiceRequestPreview.svelte';
	import type { GetServiceRequestsResponse } from '$lib/services/Libre311/Libre311';

	let promise: any;

	onMount(async () => {
		// const promise: GetServiceRequestsResponse = await libre311Context.service.getServiceRequests();
	});

</script>
<h1 class="text-3xl mx-2">{messages['sidebar']['title']}</h1>

{#await promise then _}
	<ul>
		<li class="m-5">
			<ServiceRequestPreview />
			<hr class="my-4" style="height:2px;border:none;color:#D9D9D9;background-color:#D9D9D9;"/>
		</li>

		<li class="m-5">
			<ServiceRequestPreview />
			<hr class="my-4" style="height:2px;border:none;color:#D9D9D9;background-color:#D9D9D9;"/>
		</li>

		<li class="m-5">
			<ServiceRequestPreview />
			<hr class="my-4" style="height:2px;border:none;color:#D9D9D9;background-color:#D9D9D9;"/>
		</li>
	</ul>
{/await}
