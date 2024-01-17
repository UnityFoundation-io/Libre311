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
			case 'closed': {
				return 'success';
			}
			case 'open': {
				return 'warn';
			}
		}
		return 'error';
	}

	function toTimeStamp (serviceRequest: ServiceRequest) {
		return `${new Date(serviceRequest.requested_datetime).toLocaleDateString()} ${new Date(serviceRequest.requested_datetime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}`
	}

	const gotoServiceRequest = () => {
		goto(`/issues/map/${serviceRequest.service_request_id}`);
	}
</script>

<Card>
	<div class="my-1 mx-4" slot="content">

		<div class='flow-root'>			
			<h2 class='text-base float-left tracking-wide'>
				#{serviceRequest.service_request_id}
			</h2>
			<Badge class='text-sm float-right' type={getStatus(serviceRequest)}>{serviceRequest.status}</Badge>
		</div>

		<p class='text-sm font-extralight my-1'>{toTimeStamp(serviceRequest)} </p>
		
		{#if serviceRequest.media_url}
			<div class='bg-[#D9D9D9] rounded-md'>
				<a class='flex overflow-y-hidden overflow-x-scroll mx-auto rounded-md' href={`/issues/map/${serviceRequest.service_request_id}`}>
					<img class='h-[300px] rounded-b-none w-full' alt='service-request' src={serviceRequest.media_url}/>
				</a>
			</div>
		{/if}

		<div class='mt-2 flow-root'>
			<a href={`/issues/map/${serviceRequest.service_request_id}`}>
				<h1 class='text-lg float-left'>{serviceRequest.service_name}</h1>
			</a>

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
		
		<div class="mb-1">
			<strong class="text-base">{messages['serviceRequest']['description']}</strong>
		</div>

		<div>
			<p class='w-82 overflow-hidden text-sm text-ellipsis whitespace-nowrap'>{serviceRequest.description}</p>
		</div>

	</div>
</Card>

<style>
	h1 {
		color: hsl(var(--primary));
	}
</style>
