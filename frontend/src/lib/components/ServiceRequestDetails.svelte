<script lang="ts">
	import messages from '$media/messages.json';
	import { Badge, Card } from 'stwui';
	import { Dropdown } from 'stwui';
	import type { ServiceRequest } from '$lib/services/Libre311/Libre311';
	import dropDownIcon from '$lib/assets/ellipsis-vertical.svg';
	import clockIcon from '$lib/assets/Clock.svg';

	export let serviceRequest: ServiceRequest;

	let visible: boolean = false;

	function closeDropdown() {
		visible = false;
	}

	function toggleDropdown() {
		visible = !visible;
	}

	function getStatus(serviceRequest: ServiceRequest): string {
		switch (serviceRequest.status) {
			case 'Closed': {
				return 'success';
			}
			case 'Open': {
				return 'warn';
			}
			default:
				return 'error';
		}
	}

	function toTimeStamp(timeStamp: string | null | undefined) {
		return timeStamp
			? `${new Date(timeStamp).toLocaleDateString()} ${new Date(timeStamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
			: '';
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
			<div class="mx-auto flex overflow-y-hidden overflow-x-scroll rounded-md bg-[#D9D9D9]">
				<img
					class="h-[300px] w-full rounded-b-none"
					alt="service-request"
					src={serviceRequest.media_url}
				/>
			</div>
		{/if}

		<div class="mt-2 flow-root">
			<h1 class="float-left text-lg">{serviceRequest.service_name}</h1>

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

		{#if serviceRequest.detail}
			<div></div>

			<div class="mb-1">
				<strong class="text-base">{messages['serviceRequest']['detail']}</strong>

				<p class="text-sm">
					{#each serviceRequest.detail as detail, i}
						{detail}{#if i < serviceRequest.detail.length - 1}<span>, </span>{/if}
					{/each}
				</p>
			</div>
		{/if}

		<div class="mb-1">
			<strong class="text-base">{messages['serviceRequest']['description']}</strong>
			<p class="text-sm">{serviceRequest.description}</p>
		</div>

		{#if serviceRequest.citizen_contact}
			<div class="mb-1">
				<strong class="text-base">{messages['serviceRequest']['citizen_contact']}</strong>
				<p class="text-sm">{serviceRequest.citizen_contact}</p>
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
</style>
