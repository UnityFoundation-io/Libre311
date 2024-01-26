<script lang="ts">
	import messages from '$media/messages.json';
	import type { ServiceRequest } from '$lib/services/Libre311/Libre311';
	import ServiceRequestDetails from './ServiceRequestDetails.svelte';

	export let data: ServiceRequest;
</script>

<div class="flex h-full items-center justify-center">
	<div class="mx-4 flex flex-col" style="height: calc(100vh - var(--header-height));">
		<div class="mt-4 flex-grow">
			<h1 class="text-xlg">{messages['reviewServiceRequest']['title']}</h1>
			<p class="my-2 text-sm">
				{messages['reviewServiceRequest']['sub_heading']}
			</p>

			<div class="my-4">
				<ServiceRequestDetails serviceRequest={data} />
			</div>
		</div>

		<div class="mb-4">
			<div class="flex items-center justify-between">
				<button class="my-2 text-sm" type="button" on:click|preventDefault={() => {}}>
					{messages['reviewServiceRequest']['button_edit']}
				</button>
				<button class="submit my-2 text-sm" type="button" on:click|preventDefault={() => {}}>
					{messages['reviewServiceRequest']['button_submit']}
				</button>
			</div>
		</div>
	</div>
</div>

<style>
	.submit {
		padding: 0.5rem 2rem;
		background-color: hsl(var(--primary));
		color: hsl(var(--primary-content));
		border: 1px solid hsl(var(--primary));
		border-radius: 10px;
	}
	.submit:hover {
		--tw-surface-opacity: 0.1;
		border: 1px solid hsl(var(--primary));
		color: hsl(var(--content));
		background-color: hsl(var(--surface) / var(--tw-surface-opacity));
	}
</style>
