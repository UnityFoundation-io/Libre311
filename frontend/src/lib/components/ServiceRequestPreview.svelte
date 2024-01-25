<script lang="ts">
	import messages from '$media/messages.json';
	import { goto } from '$app/navigation';
	import { Badge, Card } from 'stwui';
	import { Dropdown } from 'stwui';
	import type { ServiceRequest } from '$lib/services/Libre311/Libre311';
	import dropDownIcon from '$lib/assets/ellipsis-vertical.svg';

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
			case 'Closed': {
				return 'success';
			}
			case 'Open': {
				return 'warn';
			}
		}
		return 'error';
	}

	function toTimeStamp(serviceRequest: ServiceRequest) {
		return `${new Date(serviceRequest.requested_datetime).toLocaleDateString()} ${new Date(serviceRequest.requested_datetime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
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

		<p class="my-1 text-sm font-extralight">{toTimeStamp(serviceRequest)}</p>

		{#if serviceRequest.media_url}
			<div class="rounded-md bg-[#D9D9D9]">
				<img
					class="h-[300px] w-full rounded-md"
					alt="service-request"
					src={serviceRequest.media_url}
				/>
			</div>
		{/if}

		<div class="mt-2 flow-root">
			<a href={detailsLink}>
				<h1 class="float-left text-lg">{serviceRequest.service_name}</h1>
			</a>

			<Dropdown class="float-right" bind:visible>
				<button aria-label="dropdown toggle" slot="trigger" on:click={toggleDropdown} type="button">
					<span class="sr-only">Open user menu</span>
					<img src={dropDownIcon} alt="drop-down-menu" />
				</button>
				<Dropdown.Items slot="items">
					<Dropdown.Items.Item on:click={closeDropdown} label="Item 1" />
					<Dropdown.Items.Item on:click={closeDropdown} label="Item 2" />
					<Dropdown.Items.Item on:click={closeDropdown} label="Item 3" />
				</Dropdown.Items>
			</Dropdown>
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
