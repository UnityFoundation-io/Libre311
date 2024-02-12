<script lang="ts">
	import messages from '$media/messages.json';
	import { Badge, Card } from 'stwui';
	import type { ServiceRequest } from '$lib/services/Libre311/Libre311';
	import Flag from '$lib/components/Svg/Flag.svelte';
	import clockIcon from '$lib/assets/Clock.svg';
	import { toTimeStamp } from '$lib/utils/functions';

	export let serviceRequest: ServiceRequest;

	function getStatus(serviceRequest: ServiceRequest) {
		switch (serviceRequest.status) {
			case 'closed': {
				return 'success';
			}
			case 'open': {
				return 'warn';
			}
			default:
				return 'error';
		}
	}

	function createName(serviceRequest: ServiceRequest) {
		console.log(serviceRequest);

		if (serviceRequest.first_name || serviceRequest.last_name)
			return `${serviceRequest.first_name ?? ''} ${serviceRequest.last_name ?? ''}`;
	}

	$: name = createName(serviceRequest);
</script>

<Card>
	<div class="mx-4 my-2">
		<div class="flow-root">
			<h2 class="float-left text-base tracking-wide">
				#{serviceRequest.service_request_id}
			</h2>
			<Badge class="float-right text-sm" type={getStatus(serviceRequest)}
				>{serviceRequest.status}</Badge
			>
		</div>

		<p class="my-1 text-sm font-extralight">{toTimeStamp(serviceRequest.requested_datetime)}</p>

		{#if serviceRequest.media_url}
			<div class="serviceImage" style={`background-image: url('${serviceRequest.media_url}');`} />
		{/if}

		<div class="mb-2 mt-2 flow-root">
			<h1 class="float-left text-lg">{serviceRequest.service_name}</h1>
			<div class="float-right">
				<Flag />
			</div>
		</div>

		<div class="mb-2">
			<p class="text-sm">{serviceRequest.address}</p>
		</div>

		<!-- {#if serviceRequest.detail}
			<div></div>

			<div class="mb-1">
				<strong class="text-base">{messages['serviceRequest']['detail']}</strong>

				<p class="text-sm">
					{#each serviceRequest.detail as detail, i}
						{detail}{#if i < serviceRequest.detail.length - 1}<span>, </span>{/if}
					{/each}
				</p>
			</div>
		{/if} -->

		<div class="mb-1">
			<strong class="text-base">{messages['serviceRequest']['description']}</strong>
			<p class="text-sm">{serviceRequest.description}</p>
		</div>

		{#if name}
			<div class="mb-1">
				<strong class="text-base">{messages['serviceRequest']['citizen_contact']}</strong>
				<p class="text-sm">{name ?? ''}</p>
				<p class="text-sm">{serviceRequest.email ?? ''}</p>
				<p class="text-sm">{serviceRequest.phone ?? ''}</p>
			</div>
		{/if}

		{#if serviceRequest.agency_responsible}
			<div class="mb-1">
				<strong class="text-base">{messages['serviceRequest']['agency_contact']}</strong>
				<p class="text-sm">{serviceRequest.agency_responsible}</p>
			</div>
		{/if}

		<div class="mb-1 flex">
			<img alt="clock" src={clockIcon} />
			<strong class="ml-1 text-base">{messages['serviceRequest']['expected_datetime']}</strong>
			<div class="ml-1 flex items-center">
				{#if serviceRequest.expected_datetime}
					<p class="text-sm">{toTimeStamp(serviceRequest.expected_datetime)}</p>
				{:else}
					<p class="text-sm">--</p>
				{/if}
			</div>
		</div>

		{#if serviceRequest.status_notes}
			<div class="mb-1">
				<h2 class="text-base">{messages['serviceRequest']['service_notes']}</h2>
				<p class="text-sm">{toTimeStamp(serviceRequest.status_notes)}</p>
			</div>
		{/if}
	</div>
</Card>

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
</style>
