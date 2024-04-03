<script lang="ts">
	import messages from '$media/messages.json';
	import { onMount } from 'svelte';
	import DisplayMultiAttribute from './DisplayServiceDefinitionAttributes/DisplayMultiAttribute.svelte';
	import DisplaySingleAttribute from './DisplayServiceDefinitionAttributes/DisplaySingleAttribute.svelte';
	import DisplayStringAttribute from './DisplayServiceDefinitionAttributes/DisplayStringAttribute.svelte';
	import DisplayNumberAttribute from './DisplayServiceDefinitionAttributes/DisplayNumberAttribute.svelte';
	import DisplayDateTimeAttribute from './DisplayServiceDefinitionAttributes/DisplayDateTimeAttribute.svelte';
	import DisplayTextAttribute from './DisplayServiceDefinitionAttributes/DisplayTextAttribute.svelte';
	import { Badge } from 'stwui';
	import StepControls from './StepControls.svelte';
	import { toCreateServiceRequestParams, type CreateServiceRequestUIParams } from './shared';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';
	import { goto } from '$app/navigation';
	import ServiceRequestStatusBadge from '../ServiceRequestStatusBadge.svelte';

	const libre311 = useLibre311Service();
	const alertError = useLibre311Context().alertError;
	const alert = useLibre311Context().alert;

	export let params: CreateServiceRequestUIParams;

	let imageData: string | undefined;
	let submittingServiceRequest: boolean = false;

	function createName(params: CreateServiceRequestUIParams) {
		if (params.first_name || params.last_name)
			return `${params.first_name ?? ''} ${params.last_name ?? ''}`;
	}

	function getTimeStamp(): string | null | undefined {
		let timeStamp = new Date().getTime();
		return timeStamp
			? `${new Date(timeStamp).toLocaleDateString()} ${new Date(timeStamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
			: '';
	}

	async function submitServiceReq() {
		try {
			let mediaUrl: string | undefined = undefined;

			submittingServiceRequest = true

			if (params.file) {
				mediaUrl = await libre311.uploadImage(params.file);
			}
			params.media_url = mediaUrl;

			await libre311.createServiceRequest(toCreateServiceRequestParams(params));

			submittingServiceRequest = false

			alert({
				type: 'success',
				title: 'Success',
				description: 'Your service request has been created'
			});
			goto('/issues/map');
		} catch (error) {
			alertError(error);
		}
	}

	onMount(() => {
		if (params.file) {
			let reader = new FileReader();
			reader.readAsDataURL(params.file);

			reader.onloadend = function () {
				const result: String = new String(reader.result);
				imageData = result.toString();
			};
		}
	});

	$: name = createName(params);
</script>

<div class="flex h-full items-center justify-center">
	<div class="flex h-full w-full flex-col">
		<div class="mt-4 flex-grow">
			<h1 class="text-lg">{messages['reviewServiceRequest']['title']}</h1>

			<div class="my-2">
				<div class="flow-root">
					<ServiceRequestStatusBadge class="float-right text-sm" status="open" />
				</div>

				<p class="my-1 text-sm font-extralight">{getTimeStamp()}</p>

				{#if imageData}
					<div class="image-container relative mx-auto my-4">
						<img class="rounded-lg" src={imageData} alt="preview" />
					</div>
				{/if}

				<div class="serviceTitle mt-2 flow-root">
					<h1 class="float-left text-lg">{params.service.service_name}</h1>
				</div>

				<div class="mb-2">
					<p class="text-sm">{params.address_string}</p>
				</div>

				{#each params.attributeMap.values() as attributes}
					<div class="mb-2">
						{#if attributes.datatype == 'multivaluelist'}
							<DisplayMultiAttribute {attributes} />
						{:else if attributes.datatype == 'datetime'}
							<DisplayDateTimeAttribute {attributes} />
						{:else if attributes.datatype == 'string'}
							<DisplayStringAttribute {attributes} />
						{:else if attributes.datatype == 'singlevaluelist'}
							<DisplaySingleAttribute {attributes} />
						{:else if attributes.datatype == 'number'}
							<DisplayNumberAttribute {attributes} />
						{:else if attributes.datatype == 'text'}
							<DisplayTextAttribute {attributes} />
						{/if}
					</div>
				{/each}

				{#if params.description}
					<div class="mb-1">
						<strong class="text-base">{messages['serviceRequest']['description']}</strong>
						<p class="text-sm">{params.description ?? ''}</p>
					</div>
				{/if}

				{#if name || params.email || params.phone}
					<div class="mb-1">
						<strong class="text-base">{messages['serviceRequest']['citizen_contact']}</strong>
						<p class="text-sm">{name ?? ''}</p>
						<p class="text-sm">{params.email ?? ''}</p>
						<p class="text-sm">{params.phone ?? ''}</p>
					</div>
				{/if}
			</div>
		</div>

		<StepControls on:click={submitServiceReq} loading={submittingServiceRequest}>
			<svelte:fragment slot="submit-text"
				>{messages['reviewServiceRequest']['button_submit']}</svelte:fragment
			>
		</StepControls>
	</div>
</div>

<style>
	.serviceTitle {
		color: hsl(var(--primary));
	}
	img {
		max-height: 15rem;
	}
	/* Desktop */
	@media only screen and (min-width: 769px) {
		img {
			max-height: 20rem;
		}
	}
</style>
