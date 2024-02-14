<script lang="ts">
	import messages from '$media/messages.json';
	import { goto } from '$app/navigation';
	import { Badge, Card } from 'stwui';
	import { Dropdown } from 'stwui';
	import type { ServiceRequest } from '$lib/services/Libre311/Libre311';
	import dropDownIcon from '$lib/assets/ellipsis-vertical.svg';
	import { toTimeStamp } from '$lib/utils/functions';
	import Flag from './Svg/Flag.svelte';

	export let serviceRequest: ServiceRequest;
	export let detailsLink: string;

	let visible: boolean = false;

	function closeDropdown() {
		visible = false;
	}

	function toggleDropdown() {
		visible = !visible;
	}

	function getStatus(serviceRequest: ServiceRequest) {
		switch (serviceRequest.status) {
			case 'closed': {
				return 'success';
			}
			case 'open': {
				return 'warn';
			}
		}
		return 'error';
	}
</script>

<Card>
	<div class="mx-4 my-2" slot="content">
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
			<div class="rounded-md bg-[#D9D9D9]">
				<img
					class="h-[300px] w-full rounded-md"
					alt="service-request"
					src={serviceRequest.media_url}
				/>
			</div>
		{/if}

		<div class="mb-2 mt-2 flow-root">
			<a href={detailsLink}>
				<h1 class="float-left text-lg">{serviceRequest.service_name}</h1>
			</a>
			<div class="float-right">
				<Flag />
			</div>
		</div>

		<div class="mb-2">
			<p class="text-sm">{serviceRequest.address}</p>
		</div>

		<div class="mb-1">
			<strong class="text-base">{messages['serviceRequest']['description']}</strong>
		</div>

		<div>
			<p class="w-82 overflow-hidden text-ellipsis whitespace-nowrap text-sm">
				{serviceRequest.description}
			</p>
		</div>
	</div>
</Card>

<style>
	h1 {
		color: hsl(var(--primary));
	}
</style>
