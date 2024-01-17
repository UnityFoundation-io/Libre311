<script lang='ts'>
	import messages from '$media/messages.json'
	import { goto } from '$app/navigation';
	import { Badge, Card } from 'stwui';
	import { Dropdown } from 'stwui';
	import type { ServiceRequest } from '$lib/services/Libre311/Libre311';
	import dropDownIcon from '$lib/assets/ellipsis-vertical.svg';

	export let serviceRequest: ServiceRequest;

	let visible: boolean = false;

	function closeDropdown() {
		visible = false;
	}

	function toggleDropdown() {
		visible = !visible;
	}

	function getStatus (serviceRequest: ServiceRequest) : string  {
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

	function toTimeStamp (timeStamp: string | null | undefined) {
		return timeStamp ? `${new Date(timeStamp).toLocaleDateString()} ${new Date(timeStamp).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}` : '';
	}
</script>

<Card>
	<div class="my-2 mx-4" slot="content">

		<div class='flow-root'>			
			<h2 class='text-base float-left tracking-wide'>
				#{serviceRequest.service_request_id}
			</h2>
			<Badge class='text-sm float-right' type={getStatus(serviceRequest)}>{serviceRequest.status}</Badge>
		</div>

		<p class='text-sm font-extralight my-1'>{toTimeStamp(serviceRequest.requested_datetime)} </p>
		
		{#if serviceRequest.media_url}
			<div class='flex overflow-y-hidden overflow-x-scroll mx-auto rounded-md bg-[#D9D9D9]'>
				<img class='h-[300px] rounded-b-none w-full' alt='service-request' src={serviceRequest.media_url}/>
			</div>
		{/if}

		<div class='mt-2 flow-root'>
			<h1 class='text-lg float-left'>{serviceRequest.service_name}</h1>

			<Dropdown class='float-right' bind:visible={visible}>
				<button
					aria-label="dropdown toggle"
					slot="trigger"
					on:click={toggleDropdown}
					type="button"
				>
					<span class="sr-only">Open user menu</span>
					<img
						src={dropDownIcon}
						alt="drop-down-menu"
					/>
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
			<div>
				
			</div>

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
			<p class='text-sm'>{serviceRequest.description}</p>
		</div>

		<div class="mb-1">
			<strong class="text-base">{messages['serviceRequest']['citizen_contact']}</strong>
			<p class='text-sm'></p>
		</div>

		<div class="mb-1">
			<strong class="text-base">{messages['serviceRequest']['agency_contact']}</strong>
			<p class='text-sm'>{serviceRequest.agency_responsible}</p>
		</div>

		<div class="mb-1">
			<strong class="text-base">{messages['serviceRequest']['expected_datetime']}</strong>
			<p class='text-sm'>{toTimeStamp(serviceRequest.expected_datetime)}</p>
		</div>

		{#if serviceRequest.status_notes}
			<div class="mb-1">
				<h2 class="text-base">{messages['serviceRequest']['service_notes']}</h2>
				<p class='text-sm'>{toTimeStamp(serviceRequest.status_notes)}</p>
			</div>
		{/if}

	</div>
</Card>

<style>
	h1 {
		color: hsl(var(--primary));
	}
</style>
