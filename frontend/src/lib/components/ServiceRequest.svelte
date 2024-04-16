<script lang="ts">
	import messages from '$media/messages.json';
	import { Button, Card } from 'stwui';
	import type { ServiceRequest } from '$lib/services/Libre311/Libre311';
	import { toTimeStamp } from '$lib/utils/functions';
	import SelectedValues from './SelectedValues.svelte';
	import ServiceRequestUpdate from './ServiceRequestUpdate.svelte';
	import type { UpdateSensitiveServiceRequestRequest } from '$lib/services/Libre311/types/UpdateSensitiveServiceRequest';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import ServiceRequestButtonsContainer from './ServiceRequestButtonsContainer.svelte';
	import ServiceRequestStatusBadge from './ServiceRequestStatusBadge.svelte';
	import AuthGuard from './AuthGuard.svelte';

	const libre311 = useLibre311Service();
	const alertError = useLibre311Context().alertError;
	const alert = useLibre311Context().alert;

	export let serviceRequest: ServiceRequest;
	export let back: string;

	let isUpdateButtonClicked: boolean = false;

	$: if ($page.url) {
		isUpdateButtonClicked = false;
	}

	$: name = createName(serviceRequest);

	function createName(serviceRequest: UpdateSensitiveServiceRequestRequest) {
		if (serviceRequest.first_name || serviceRequest.last_name)
			return `${serviceRequest.first_name ?? ''} ${serviceRequest.last_name ?? ''}`;
	}

	async function updateServiceRequest(e: CustomEvent<UpdateSensitiveServiceRequestRequest>) {
		try {
			await libre311.updateServiceRequest(e.detail);

			isUpdateButtonClicked = false;

			alert({
				type: 'success',
				title: 'Success',
				description: 'Your service request has been updated'
			});

			goto(back);
		} catch (error) {
			alertError(error);
		}
	}
</script>

<div class="flex h-full">
	<Card class="m-2 w-full overflow-y-auto">
		<div class="flex h-full w-full flex-col" slot="content">
			<div class="m-2 flex-grow">
				<!-- ID & STATUS -->
				<div class="flow-root">
					<h2 class="float-left text-base tracking-wide">
						#{serviceRequest.service_request_id}
					</h2>

					<ServiceRequestStatusBadge class="float-right text-sm" status={serviceRequest.status} />
				</div>

				<!-- REQUESTED TIMESTAMP -->
				<p class="my-1 text-sm font-extralight">{toTimeStamp(serviceRequest.requested_datetime)}</p>

				<!-- MEDIA -->
				{#if serviceRequest.media_url}
					<div
						class="serviceImage"
						style={`background-image: url('${serviceRequest.media_url}');`}
					/>
				{/if}

				<!-- SERVICE NAME -->
				<div class="mb-2 mt-2 flow-root">
					<h1 class="float-left text-lg">{serviceRequest.service_name}</h1>
					<div class="float-right">
						<!-- <Flag /> -->
					</div>
				</div>

				<!-- ADDRESS -->
				<div class="mb-2">
					<p class="text-sm">{serviceRequest.address}</p>
				</div>

				{#if serviceRequest.selected_values}
					<div class="mb-1">
						<SelectedValues selectedValues={serviceRequest.selected_values} />
					</div>
				{/if}

				<!-- DESCRIPTION -->
				{#if serviceRequest.description}
					<div class="mb-1">
						<strong class="text-base">{messages['serviceRequest']['description']}</strong>
						<p class="text-sm">{serviceRequest.description ?? ''}</p>
					</div>
				{/if}

				<!-- NAME & EMAIL & PHONE (CITIZEN) -->
				{#if name}
					<div class="mb-1">
						<strong class="text-base">{messages['serviceRequest']['citizen_contact']}</strong>
						<p class="text-sm">{name ?? ''}</p>
						<p class="text-sm">{serviceRequest.email ?? ''}</p>
						<p class="text-sm">{serviceRequest.phone ?? ''}</p>
					</div>
				{/if}

				{#if !isUpdateButtonClicked}
					<!-- EXPECTED TIMESTAMP -->
					{#if serviceRequest.expected_datetime}
						<div class="mb-1 flex flex-col">
							<strong class="text-base">{messages['serviceRequest']['expected_datetime']}</strong>
							<div class="flex items-center">
								<p class="text-sm">{toTimeStamp(serviceRequest.expected_datetime) ?? ''}</p>
							</div>
						</div>
					{/if}

					<!-- PRIORITY -->
					{#if serviceRequest.priority}
						<div class="mb-1">
							<strong class="text-base">{messages['serviceRequest']['priority']}</strong>
							<p class="text-sm">
								{serviceRequest.priority.charAt(0).toUpperCase() +
									serviceRequest.priority.slice(1) ?? ''}
							</p>
						</div>
					{/if}

					<!-- AGENCY -->
					{#if serviceRequest.agency_responsible || serviceRequest.agency_email}
						<div class="mb-1">
							<strong class="text-base">{messages['serviceRequest']['agency_contact']}</strong>
							<p class="text-sm">{serviceRequest.agency_responsible ?? ''}</p>
							<p class="text-sm">{serviceRequest.agency_email ?? ''}</p>
						</div>
					{/if}

					<!-- SERVICE NOTICE -->
					{#if serviceRequest.service_notice}
						<div class="mb-1">
							<strong class="text-base">{messages['serviceRequest']['service_notice']}</strong>
							<p class="text-sm">{serviceRequest.service_notice}</p>
						</div>
					{/if}

					<!-- STATUS NOTES -->
					{#if serviceRequest.status_notes}
						<div class="mb-1">
							<h2 class="text-base">{messages['serviceRequest']['status_notes']}</h2>
							<p class="text-sm">{serviceRequest.status_notes}</p>
						</div>
					{/if}
				{/if}
			</div>

			<!-- UPDATE -->
			{#if isUpdateButtonClicked}
				<div class="mx-2">
					<ServiceRequestUpdate
						{serviceRequest}
						on:cancel={() => {
							isUpdateButtonClicked = false;
						}}
						on:updateServiceRequest={updateServiceRequest}
					/>
				</div>
			{:else}
				<div class="mx-2">
					<ServiceRequestButtonsContainer>
						<Button slot="left" href={back}>
							{messages['updateServiceRequest']['button_back']}
						</Button>

						<AuthGuard
							slot="right"
							requires={[
								'LIBRE311_REQUEST_EDIT-TENANT',
								'LIBRE311_REQUEST_EDIT-SYSTEM',
								'LIBRE311_REQUEST_EDIT-SUBTENANT'
							]}
						>
							<Button on:click={() => (isUpdateButtonClicked = true)}>
								{messages['updateServiceRequest']['button_update']}
							</Button>
						</AuthGuard>
					</ServiceRequestButtonsContainer>
				</div>
			{/if}
		</div>
	</Card>
</div>

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
