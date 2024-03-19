<script lang="ts">
	import messages from '$media/messages.json';
	import { Badge, Button, Card, DatePicker, Input, Select, TextArea } from 'stwui';
	import type { ServiceRequest, ServiceRequestStatus } from '$lib/services/Libre311/Libre311';
	import clockIcon from '$lib/assets/Clock.svg';
	import { toTimeStamp } from '$lib/utils/functions';
	import SelectedValues from './SelectedValues.svelte';
	import Flag from './Svg/Flag.svelte';
	import type { UpdateSensitiveServiceRequestRequest } from '$lib/services/Libre311/types/UpdateSensitiveServiceRequest';
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import { wrenchScrewDriverIcon } from './Svg/outline/wrench-screwdriver';
	import { mailIcon } from './Svg/outline/mailIcon';
	import type { SelectOption } from 'stwui/types';
	import { calendarIcon } from './Svg/outline/calendarIcon';
	import { user } from './Svg/outline/user';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';

	const libre311 = useLibre311Service();

	export let serviceRequest: ServiceRequest;
	export let back: string;

	let isUpdateButtonClicked: boolean = false;

	$: agencyResponsible = serviceRequest.agency_responsible;
	$: agencyEmail = serviceRequest.agency_email;
	$: serviceNotice = serviceRequest.service_notice;
	$: statusNotes = serviceRequest.status_notes;

	const statusOptions: SelectOption[] = [
		{
			value: 'open',
			label: 'Open'
		},
		// TODO
		// {
		// 	value: 'assigned',
		// 	label: 'Assigned'
		// },
		{
			value: 'closed',
			label: 'Closed'
		}
	];

	const priorityOptions: SelectOption[] = [
		{
			value: 'low',
			label: 'Low'
		},
		{
			value: 'medium',
			label: 'Medium'
		},
		{
			value: 'high',
			label: 'High'
		}
	];

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

	function updateStatus(e: Event) {
		const target = e.target as HTMLInputElement;
		if (target.value) {
			const status = target.value as ServiceRequestStatus;
			serviceRequest.status = status;
		}
	}

	function updatePriority(e: Event) {
		const target = e.target as HTMLInputElement;
		if (target.value) {
			const priority = target.value;
			serviceRequest.priority = priority;
		}
	}

	function createName(serviceRequest: UpdateSensitiveServiceRequestRequest) {
		if (serviceRequest.first_name || serviceRequest.last_name)
			return `${serviceRequest.first_name ?? ''} ${serviceRequest.last_name ?? ''}`;
	}

	function handleUpdateButtonClick(serviceRequest: ServiceRequest) {
		isUpdateButtonClicked = true;
	}

	async function updateServiceRequest(s: ServiceRequest) {
		const sensitiveServiceRequest: UpdateSensitiveServiceRequestRequest = {
			...s,
			status: serviceRequest.status,
			agency_responsible: serviceRequest.agency_responsible,
			agency_email: serviceRequest.agency_email,
			service_notice: serviceRequest.service_notice,
			status_notes: serviceRequest.status_notes
		};

		const res = await libre311.updateServiceRequest(sensitiveServiceRequest);

		isUpdateButtonClicked = false;

		goto(`/issues/table`);
	}

	$: if ($page.url) {
		isUpdateButtonClicked = false;
	}

	$: name = createName(serviceRequest);
	let expected_datetime = serviceRequest.expected_datetime
		? new Date(Date.parse(serviceRequest.expected_datetime))
		: null;
</script>

<div class="flex h-full">
	<Card class="m-2 overflow-y-auto">
		<div class="flex h-full w-full flex-col" slot="content">
			<div class="m-2 flex-grow">
				<!-- ID & STATUS -->
				<div class="flow-root">
					<h2 class="float-left text-base tracking-wide">
						#{serviceRequest.service_request_id}
					</h2>

					<Badge class="float-right text-sm" type={getStatus(serviceRequest)}
						>{serviceRequest.status}
					</Badge>
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
						<Flag />
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

				<!-- EXPECTED TIMESTAMP -->
				{#if isUpdateButtonClicked}
					<DatePicker name="datetime" bind:value={expected_datetime}>
						<DatePicker.Label slot="label">
							<strong class="text-base">
								{messages['serviceRequest']['expected_datetime']}
							</strong>
						</DatePicker.Label>
						<DatePicker.Leading slot="leading" data={calendarIcon} />
					</DatePicker>
				{:else}
					<div class="mb-1 flex flex-col">
						<strong class="text-base">{messages['serviceRequest']['expected_datetime']}</strong>
						<div class="flex items-center">
							{#if serviceRequest.expected_datetime}
								<p class="text-sm">{toTimeStamp(serviceRequest.expected_datetime) ?? ''}</p>
							{:else}
								<p class="text-sm">--</p>
							{/if}
							<img alt="clock" src={clockIcon} />
						</div>
					</div>
				{/if}

				{#if isUpdateButtonClicked}
					<!-- STATUS SELECT -->
					<div class="mb-1">
						<Select
							name="select-status"
							placeholder={serviceRequest.status.charAt(0).toUpperCase() +
								serviceRequest.status.slice(1)}
							options={statusOptions}
							on:change={updateStatus}
						>
							<Select.Label slot="label">
								<strong class="text-base">{messages['serviceRequest']['status']}</strong>
							</Select.Label>
							<Select.Options slot="options">
								{#each statusOptions as option}
									<Select.Options.Option {option} />
								{/each}
							</Select.Options>
						</Select>
					</div>

					<!-- PRIORITY SELECT -->
					<div class="mb-1">
						<Select
							name="select-priority"
							placeholder={serviceRequest.priority
								? `${serviceRequest.priority.charAt(0).toUpperCase()}${serviceRequest.priority.slice(1)}`
								: '--'}
							options={priorityOptions}
							on:change={updatePriority}
						>
							<Select.Label slot="label">
								<strong class="text-base">
									{messages['serviceRequest']['priority']}
								</strong>
							</Select.Label>
							<Select.Options slot="options">
								{#each priorityOptions as option}
									<Select.Options.Option {option} />
								{/each}
							</Select.Options>
						</Select>
					</div>
				{/if}

				<!-- AGENCY NAME & AGENCY EMAIL -->
				{#if (agencyResponsible || agencyEmail) && !isUpdateButtonClicked}
					<div class="mb-1">
						<strong class="text-base">{messages['serviceRequest']['agency_contact']}</strong>
						<p class="text-sm">{agencyResponsible}</p>
						<p class="text-sm">{agencyEmail}</p>
					</div>
				{:else if isUpdateButtonClicked}
					<div class="mb-1">
						<Input
							type="text"
							name="firstName"
							placeholder={messages['serviceRequest']['agency_name']}
							bind:value={serviceRequest.agency_responsible}
						>
							<Input.Label slot="label">
								<strong class="text-base">
									{messages['serviceRequest']['agency_contact']}
								</strong>
							</Input.Label>
							<Input.Leading slot="leading" data={user} />
						</Input>

						<Input
							name="email"
							type="email"
							placeholder={messages['contact']['email']['placeholder']}
							bind:value={serviceRequest.agency_email}
						>
							<Input.Leading slot="leading" data={mailIcon} />
						</Input>
					</div>
				{/if}

				<!-- SERVICE NOTICE -->
				{#if serviceNotice && !isUpdateButtonClicked}
					<div class="mb-1">
						<strong class="text-base">{messages['serviceRequest']['service_notice']}</strong>
						<p class="text-sm">{serviceNotice}</p>
					</div>
				{:else if isUpdateButtonClicked}
					<div class="mb-1">
						<Input
							type="text"
							name="firstName"
							placeholder={messages['serviceRequest']['service_notice_placeholder']}
							bind:value={serviceRequest.service_notice}
						>
							<Input.Label slot="label">
								<strong class="text-base">{messages['serviceRequest']['service_notice']}</strong>
							</Input.Label>
							<Input.Leading slot="leading" data={wrenchScrewDriverIcon} />
						</Input>
					</div>
				{/if}

				<!-- STATUS NOTES -->
				{#if statusNotes && !isUpdateButtonClicked}
					<div class="mb-1">
						<h2 class="text-base">{messages['serviceRequest']['status_notes']}</h2>
						<p class="text-sm">{statusNotes}</p>
					</div>
				{:else if isUpdateButtonClicked}
					<div class="mb-1 flex flex-col">
						<strong class="text-base">{messages['serviceRequest']['status_notes']}</strong>
						<TextArea
							bind:value={serviceRequest.status_notes}
							name="comments"
							placeholder="notes"
							class="relative"
						/>
					</div>
				{/if}
			</div>

			<div class="mx-2 flex items-center justify-between pb-4 pt-4">
				{#if isUpdateButtonClicked}
					<Button
						on:click={() => {
							isUpdateButtonClicked = false;
						}}
					>
						{messages['updateServiceRequest']['button_cancel']}
					</Button>

					<Button type="primary" on:click={() => updateServiceRequest(serviceRequest)}>
						Submit
					</Button>
				{:else}
					<Button href={back}>
						{messages['updateServiceRequest']['button_back']}
					</Button>

					<Button on:click={() => handleUpdateButtonClick(serviceRequest)}>
						{messages['updateServiceRequest']['button_update']}
					</Button>
				{/if}
			</div>
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
</style>
