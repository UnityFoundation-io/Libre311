<script lang="ts">
	import messages from '$media/messages.json';
	import { Button, Card } from 'stwui';
	import type { ServiceRequest, Project } from '$lib/services/Libre311/Libre311';
	import { toTimeStamp } from '$lib/utils/functions';
	import SelectedValues from './SelectedValues.svelte';
	import ServiceRequestUpdate from './ServiceRequestUpdate.svelte';
	import type { UpdateSensitiveServiceRequestRequest } from '$lib/services/Libre311/types/UpdateSensitiveServiceRequest';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';
	import { useServiceRequestsContext } from '$lib/context/ServiceRequestsContext';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import ServiceRequestButtonsContainer from './ServiceRequestButtonsContainer.svelte';
	import ServiceRequestStatusBadge from './ServiceRequestStatusBadge.svelte';
	import AuthGuard from './AuthGuard.svelte';
	import ConfirmationModal from './ConfirmationModal.svelte';
	import RemovalSuggestionsList from './RemovalSuggestionsList.svelte';
	import { onMount } from 'svelte';
	import { useJurisdiction } from '$lib/context/JurisdictionContext';
	import { Modal, Portal } from 'stwui';

	const libre311 = useLibre311Service();
	const alertError = useLibre311Context().alertError;
	const alert = useLibre311Context().alert;
	const { refreshSelectedServiceRequest } = useServiceRequestsContext();
	const jurisdiction = useJurisdiction();
	const linkResolver = useLibre311Context().linkResolver;

	export let serviceRequest: ServiceRequest;
	export let back: string;

	let isUpdateButtonClicked: boolean = false;
	let showDeleteModal = false;
	let isDeleting = false;
	let isValidating = false;
	let showValidateModal = false;
	let showValidationInfoModal = false;
	let projects: Project[] = [];

	$: if ($page.url) {
		isUpdateButtonClicked = false;
	}

	$: name = createName(serviceRequest);

	onMount(async () => {
		try {
			projects = await libre311.getProjects();
		} catch (error) {
			console.error('Failed to load projects:', error);
		}
	});

	$: project = projects.find((p) => p.id === serviceRequest.project_id);

	function createName(serviceRequest: UpdateSensitiveServiceRequestRequest) {
		if (serviceRequest.first_name || serviceRequest.last_name)
			return `${serviceRequest.first_name ?? ''} ${serviceRequest.last_name ?? ''}`;
	}

	function deleteServiceReq() {
		showDeleteModal = true;
	}

	async function confirmDelete() {
		isDeleting = true;
		try {
			await libre311.deleteServiceRequest({
				service_request_id: serviceRequest.service_request_id
			});

			alert({
				type: 'success',
				title: 'Success',
				description: 'Service request has been deleted'
			});
			goto(linkResolver.issuesTable($page.url));
		} catch (error) {
			alertError(error);
		} finally {
			isDeleting = false;
			showDeleteModal = false;
		}
	}

	async function manuallyValidate() {
		isValidating = true;
		try {
			const updatedRequest = await libre311.updateServiceRequest({
				...serviceRequest,
				attribute_validation: 'APPROVED'
			});
			refreshSelectedServiceRequest(updatedRequest);
			showValidateModal = false;
			alert({ type: 'success', title: 'Success', description: 'Request marked as validated' });
		} catch (error) {
			alertError(error);
		} finally {
			isValidating = false;
		}
	}

	async function updateServiceRequest(e: CustomEvent<UpdateSensitiveServiceRequestRequest>) {
		try {
			// Use the response directly - it contains the full updated service request
			const updatedRequest = await libre311.updateServiceRequest(e.detail);

			refreshSelectedServiceRequest(updatedRequest);

			isUpdateButtonClicked = false;

			alert({
				type: 'success',
				title: 'Success',
				description: 'Your service request has been updated'
			});
		} catch (error) {
			alertError(error);
		}
	}
</script>

<div class="flex h-full w-full flex-col">
	<Card class="m-2 flex-grow overflow-y-auto">
		<div class="flex h-full w-full flex-col" slot="content">
			<h3 class="ml-4 text-base">
				{#if isUpdateButtonClicked}
					{messages['serviceRequest']['update']}
				{:else}
					{messages['serviceRequest']['detail']}
				{/if}
			</h3>
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

				<!-- ATTRIBUTE VALIDATION WARNING -->
				{#if serviceRequest.attribute_validation === 'NEEDS_REVIEW'}
					<div class="mb-2 flex items-center gap-2 rounded bg-yellow-100 px-3 py-2 text-sm text-yellow-800">
						<span class="flex-1">Questions / answers were unable to validate.</span>
						<button
							class="rounded-full p-0.5 hover:bg-yellow-200"
							title="Why might this happen?"
							on:click={() => (showValidationInfoModal = true)}
						>
							<svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
								<path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd" />
							</svg>
						</button>
						<Button type="default" on:click={() => (showValidateModal = true)}>
							Validate
						</Button>
					</div>
				{/if}

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
						<strong class="text-base">{messages['serviceRequest']['citizen_contact']}:</strong>
						<p class="text-sm">{name ?? ''}</p>
						<p class="text-sm">{serviceRequest.email ?? ''}</p>
						<p class="text-sm">{serviceRequest.phone ?? ''}</p>
					</div>
				{/if}

				{#if !isUpdateButtonClicked}
					<!-- EXPECTED TIMESTAMP -->
					{#if serviceRequest.expected_datetime}
						<div class="mb-1 flex flex-col">
							<strong class="text-base">{messages['serviceRequest']['expected_datetime']}:</strong>
							<div class="flex items-center">
								<p class="text-sm">{toTimeStamp(serviceRequest.expected_datetime) ?? ''}</p>
							</div>
						</div>
					{/if}

					<!-- PRIORITY -->
					{#if serviceRequest.priority}
						<div class="mb-1">
							<strong class="text-base">{messages['serviceRequest']['priority']}:</strong>
							<p class="text-sm">
								{serviceRequest.priority.charAt(0).toUpperCase() + serviceRequest.priority.slice(1)}
							</p>
						</div>
					{/if}

					<!-- PROJECT -->
					{#if $jurisdiction.project_feature !== 'DISABLED' && project}
						<div class="mb-1">
							<strong class="text-base">Project:</strong>
							<p class="text-sm">
								{project.name} ({project.status})
							</p>
						</div>
					{/if}

					<!-- AGENCY -->
					{#if serviceRequest.agency_responsible || serviceRequest.agency_email}
						<div class="mb-1">
							<strong class="text-base">{messages['serviceRequest']['agency_contact']}:</strong>
							<p class="text-sm">{serviceRequest.agency_responsible ?? ''}</p>
							<strong class="text-base"
								>{messages['serviceRequest']['agency_contact_email']}:</strong
							>
							<p class="text-sm">{serviceRequest.agency_email ?? ''}</p>
						</div>
					{/if}

					<!-- SERVICE NOTICE -->
					{#if serviceRequest.service_notice}
						<div class="mb-1">
							<strong class="text-base">{messages['serviceRequest']['service_notice']}:</strong>
							<p class="text-sm">{serviceRequest.service_notice}</p>
						</div>
					{/if}

					<!-- STATUS NOTES -->
					{#if serviceRequest.status_notes}
						<div class="mb-1">
							<h2 class="text-base">{messages['serviceRequest']['status_notes']}:</h2>
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
							slot="middle"
							requires={[
								'LIBRE311_REQUEST_EDIT-TENANT',
								'LIBRE311_REQUEST_EDIT-SYSTEM',
								'LIBRE311_REQUEST_EDIT-SUBTENANT'
							]}
						>
							<Button on:click={deleteServiceReq}>
								{messages['updateServiceRequest']['button_delete']}
							</Button>
						</AuthGuard>

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
	<RemovalSuggestionsList serviceRequestId={serviceRequest.service_request_id} />
</div>

<ConfirmationModal
	open={showDeleteModal}
	title="Delete Service Request"
	message="Are you sure you would like to delete this request?"
	handleClose={() => (showDeleteModal = false)}
	handleConfirm={confirmDelete}
	loading={isDeleting}
/>

<ConfirmationModal
	open={showValidateModal}
	title="Manually Validate Request"
	message="Mark this request as reviewed. Staff are responsible for verifying the submitted questions and answers are accurate."
	confirmationLabel="I have reviewed the questions / answers on this request"
	cancelLabel="Cancel"
	confirmLabel="Validate"
	loading={isValidating}
	handleClose={() => (showValidateModal = false)}
	handleConfirm={manuallyValidate}
/>

{#if showValidationInfoModal}
	<Portal>
		<Modal handleClose={() => (showValidationInfoModal = false)}>
			<Modal.Content slot="content">
				<Modal.Content.Header slot="header">Why can't we validate?</Modal.Content.Header>
				<Modal.Content.Body slot="body">
					<div class="p-4 text-sm">
						<p class="mb-3">
							The questions and answers on this request no longer match the current service
							definition. Common causes:
						</p>
						<ul class="mb-3 list-inside list-disc space-y-1">
							<li>The request was drafted, then service questions were changed before it was submitted.</li>
							<li>The request was submitted while offline and synced after the service definition changed.</li>
						</ul>
						<p class="font-medium">What to do:</p>
						<ol class="mt-1 list-inside list-decimal space-y-1">
							<li>Review the submitted answers below.</li>
							<li>Confirm they are accurate and complete enough to action.</li>
							<li>Click <strong>Validate</strong> and check the confirmation box.</li>
						</ol>
					</div>
				</Modal.Content.Body>
				<Modal.Content.Footer slot="footer">
					<div class="flex justify-end">
						<Button on:click={() => (showValidationInfoModal = false)} type="primary">Close</Button>
					</div>
				</Modal.Content.Footer>
			</Modal.Content>
		</Modal>
	</Portal>
{/if}

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
