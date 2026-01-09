<script lang="ts">
	import messages from '$media/messages.json';
	import { Button, Card } from 'stwui';
	import type { ServiceRequest } from '$lib/services/Libre311/Libre311';
	import { toTimeStamp } from '$lib/utils/functions';
	import SelectedValues from './SelectedValues.svelte';
	import ServiceRequestStatusBadge from './ServiceRequestStatusBadge.svelte';
	import SuggestionModal from './SuggestionModal.svelte';

	export let serviceRequest: ServiceRequest;
	export let back: string | undefined = undefined;

	let showSuggestionModal = false;
</script>

<div class="flex h-full">
	<Card class="m-2 w-full overflow-y-auto">
		<div class="flex h-full w-full flex-col" slot="content">
			<h3 class="ml-4 text-base">{messages['serviceRequest']['detail']}</h3>
			<div class="m-2 flex-grow">
				<div class="flow-root">
					<h2 class="float-left text-base tracking-wide">
						#{serviceRequest.service_request_id}
					</h2>

					<ServiceRequestStatusBadge class="float-right text-sm" status={serviceRequest.status} />
				</div>

				<p class="my-1 text-sm font-extralight">{toTimeStamp(serviceRequest.requested_datetime)}</p>

				{#if serviceRequest.media_url}
					<div
						class="serviceImage"
						style={`background-image: url('${serviceRequest.media_url}');`}
					/>
				{/if}

				<div class="mb-2 mt-2 flow-root">
					<h1 class="float-left text-lg">{serviceRequest.service_name}</h1>
					<div class="float-right">
						<Button on:click={() => (showSuggestionModal = true)} size="sm" type="ghost">
							Suggest Removal
						</Button>
					</div>
				</div>

				<div class="mb-2">
					<p class="text-sm">{serviceRequest.address}</p>
				</div>

				{#if serviceRequest.selected_values}
					<div class="mb-1">
						<SelectedValues selectedValues={serviceRequest.selected_values} />
					</div>
				{/if}
				{#if serviceRequest.description}
					<div class="mb-1">
						<strong class="text-base">{messages['serviceRequest']['description']}</strong>
						<p class="text-sm">{serviceRequest.description ?? ''}</p>
					</div>
				{/if}

				{#if serviceRequest.expected_datetime}
					<div class="mb-1 flex flex-col">
						<strong class="text-base">{messages['serviceRequest']['expected_datetime']}</strong>
						<div class="flex items-center">
							<p class="text-sm">{toTimeStamp(serviceRequest.expected_datetime) ?? ''}</p>
						</div>
					</div>
				{/if}

				{#if serviceRequest.priority}
					<div class="mb-1">
						<strong class="text-base">{messages['serviceRequest']['priority']}</strong>
						<p class="text-sm">
							{serviceRequest.priority.charAt(0).toUpperCase() + serviceRequest.priority.slice(1) ??
								''}
						</p>
					</div>
				{/if}

				{#if serviceRequest.agency_responsible}
					<div class="mb-1">
						<strong class="text-base">{messages['serviceRequest']['agency_contact']}</strong>
						<p class="text-sm">{serviceRequest.agency_responsible ?? ''}</p>
						<strong class="text-base">{messages['serviceRequest']['agency_contact_email']}</strong>
						<p class="text-sm">{serviceRequest.agency_email ?? ''}</p>
					</div>
				{/if}

				{#if serviceRequest.service_notice}
					<div class="mb-1">
						<strong class="text-base">{messages['serviceRequest']['service_notice']}</strong>
						<p class="text-sm">{serviceRequest.service_notice ?? ''}</p>
					</div>
				{/if}

				{#if serviceRequest.status_notes}
					<div class="mb-1">
						<h2 class="text-base">{messages['serviceRequest']['status_notes']}</h2>
						<p class="text-sm">{serviceRequest.status_notes ?? ''}</p>
					</div>
				{/if}
			</div>

			<div class="m-2">
				{#if back}
					<Button href={back}>
						{messages['updateServiceRequest']['button_back']}
					</Button>
				{:else}
					<Button on:click={() => window.history.back()}>
						{messages['updateServiceRequest']['button_back']}
					</Button>
				{/if}
			</div>
		</div>
	</Card>
</div>

<SuggestionModal
	bind:open={showSuggestionModal}
	serviceRequestId={serviceRequest.service_request_id}
	handleClose={() => (showSuggestionModal = false)}
/>

<style>
	h1 {
		color: hsl(var(--primary));
	}
	.serviceImage {
		height: 0;
		padding-top: 56.25%;
		overflow-x: hidden;
		overflow-y: hidden;
		background-position: center;
		background-size: cover;
		border-radius: 10px;
	}
	.mb-1 p {
		text-indent: 0.5rem;
	}
</style>
