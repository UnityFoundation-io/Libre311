<script lang="ts">
	import messages from '$media/messages.json';
	import { Badge, Button, Card, Input, Select, TextArea } from 'stwui';
	import clockIcon from '$lib/assets/Clock.svg';
	import type { ServiceRequestStatus } from '$lib/services/Libre311/Libre311';
	import Flag from '$lib/components/Svg/Flag.svelte';
	import { toTimeStamp } from '$lib/utils/functions';
	import type { SelectOption } from 'stwui/types';
	import {
		createInput,
		optionalCoalesceNameValidator,
		type FormInputValue,
		emailValidator,
		optionalCoalesceStringValidator,
		optionalCoalesceEmailValidator
	} from '$lib/utils/validation';
	import { mailIcon } from '$lib/components/Svg/outline/mailIcon.js';
	import { calendarIcon } from '$lib/components/Svg/outline/calendarIcon.js';
	import { wrenchScrewDriverIcon } from '$lib/components/Svg/outline/wrench-screwdriver';
	import { DatePicker } from 'stwui';
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import type { UpdateSensitiveServiceRequestRequest } from '$lib/services/Libre311/types/UpdateSensitiveServiceRequest';
	import SelectedValues from './SelectedValues.svelte';

	const libre311 = useLibre311Service();

	export let serviceRequest: UpdateSensitiveServiceRequestRequest;

	let agencyNameInput: FormInputValue<string | undefined> = createInput(
		serviceRequest.agency_responsible
	);
	let agencyEmailInput: FormInputValue<string | undefined> = createInput(
		serviceRequest.agency_email
	);

	let serviceNoticeInput: FormInputValue<string | undefined> = createInput(
		serviceRequest.service_notice
	);
	let statusNotesInput: FormInputValue<string | undefined> = createInput(
		serviceRequest.status_notes
	);

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

	function getStatus(serviceRequest: UpdateSensitiveServiceRequestRequest) {
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

	function createName(serviceRequest: UpdateSensitiveServiceRequestRequest) {
		if (serviceRequest.first_name || serviceRequest.last_name)
			return `${serviceRequest.first_name ?? ''} ${serviceRequest.last_name ?? ''}`;
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

	async function updateServiceRequest() {
		if (agencyNameInput.value !== null || agencyNameInput.value !== undefined) {
			let agency: string = new String(agencyNameInput.value).toString();
			if (agency) agencyNameInput = optionalCoalesceNameValidator(createInput(agency));
		}
		agencyEmailInput = optionalCoalesceEmailValidator(agencyEmailInput);
		serviceNoticeInput = optionalCoalesceStringValidator(serviceNoticeInput);
		statusNotesInput = optionalCoalesceStringValidator(statusNotesInput);

		const resultSet = new Set([
			agencyNameInput.type,
			agencyEmailInput.type,
			serviceNoticeInput.type,
			statusNotesInput.type
		]);
		if (resultSet.has('invalid')) {
			// Refresh Inputs
			agencyNameInput.value = '';
			return;
		}

		if (agencyNameInput.value !== undefined) {
			agencyNameInput.value = '';
		}

		const sensitiveServiceRequest: UpdateSensitiveServiceRequestRequest = {
			...serviceRequest,
			agency_responsible: agencyNameInput.value,
			agency_email: agencyEmailInput.value,
			expected_datetime: expected_datetime.toISOString(),
			service_notice: serviceNoticeInput.value,
			status_notes: statusNotesInput.value
		};

		const res = await libre311.updateServiceRequest(sensitiveServiceRequest);
	}

	$: name = createName(serviceRequest);
	$: expected_datetime = serviceRequest.expected_datetime
		? new Date(Date.parse(serviceRequest.expected_datetime))
		: new Date();
</script>

<div class="flex h-full">
	<Card class="m-2 w-full">
		<div class="flex h-full w-full flex-col" slot="content">
			<div class="m-2 flex-grow">
				<div class="flow-root">
					<h2 class="float-left text-base tracking-wide">
						#{serviceRequest.service_request_id}
					</h2>

					<Badge class="float-right text-sm" type={getStatus(serviceRequest)}
						>{serviceRequest.status}
					</Badge>
				</div>

				<p class="my-1 text-sm font-extralight">{toTimeStamp(serviceRequest.requested_datetime)}</p>

				{#if serviceRequest.media_url}
					<div
						class="serviceImage"
						style={`background-image: url('${serviceRequest.media_url}');`}
					/>
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

				{#if serviceRequest.selected_values}
					<div class="mb-1">
						<SelectedValues selectedValues={serviceRequest.selected_values} />
					</div>
				{/if}

				{#if serviceRequest.description}
					<div class="mb-1">
						<strong class="text-base">{messages['serviceRequest']['description']}</strong>
						<p class="text-sm">{serviceRequest.description ?? ''}</p>
					</div>
				{/if}

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

				{#if name}
					<div class="mb-1">
						<strong class="text-base">{messages['serviceRequest']['citizen_contact']}</strong>
						<p class="text-sm">{name ?? ''}</p>
						<p class="text-sm">{serviceRequest.email ?? ''}</p>
						<p class="text-sm">{serviceRequest.phone ?? ''}</p>
					</div>
				{/if}

				<div class="my-4">
					<hr />
				</div>

				<div class="my-4">
					<Select
						name="select-status"
						placeholder={serviceRequest.status.charAt(0).toUpperCase() +
							serviceRequest.status.slice(1)}
						options={statusOptions}
						on:change={updateStatus}
					>
						<Select.Label slot="label">{messages['serviceRequest']['status']}</Select.Label>
						<Select.Options slot="options">
							{#each statusOptions as option}
								<Select.Options.Option {option} />
							{/each}
						</Select.Options>
					</Select>
				</div>

				<div class="my-4">
					<Select
						name="select-priority"
						placeholder={serviceRequest.priority
							? `${serviceRequest.priority.charAt(0).toUpperCase()}${serviceRequest.priority.slice(1)}`
							: '--'}
						options={priorityOptions}
						on:change={updatePriority}
					>
						<Select.Label slot="label">{messages['serviceRequest']['priority']}</Select.Label>
						<Select.Options slot="options">
							{#each priorityOptions as option}
								<Select.Options.Option {option} />
							{/each}
						</Select.Options>
					</Select>
				</div>

				<div class="my-4">
					<DatePicker name="datetime" allowClear bind:value={expected_datetime}>
						<DatePicker.Label slot="label">
							{messages['serviceRequest']['expected_datetime']}
						</DatePicker.Label>
						<DatePicker.Leading slot="leading" data={calendarIcon} />
					</DatePicker>
				</div>

				<div class="my-4">
					<Input
						allowClear
						type="text"
						name="firstName"
						placeholder={messages['serviceRequest']['agency_name']}
						error={agencyNameInput.error}
						bind:value={agencyNameInput.value}
					>
						<Input.Label slot="label">{messages['serviceRequest']['agency_contact']}</Input.Label>
					</Input>
					<Input
						allowClear
						name="email"
						type="email"
						placeholder={messages['contact']['email']['placeholder']}
						error={agencyEmailInput.error}
						bind:value={agencyEmailInput.value}
					>
						<Input.Leading slot="leading" data={mailIcon} />
					</Input>
				</div>

				<div class="my-4">
					<Input
						allowClear
						type="text"
						name="firstName"
						placeholder={messages['serviceRequest']['service_notice_placeholder']}
						error={serviceNoticeInput.error}
						bind:value={serviceNoticeInput.value}
					>
						<Input.Label slot="label">{messages['serviceRequest']['service_notice']}</Input.Label>
						<Input.Leading slot="leading" data={wrenchScrewDriverIcon} />
					</Input>
				</div>

				<div class="my-4 flex flex-col">
					<p class="text-sm">{messages['serviceRequest']['status_notes']}</p>
					<TextArea
						bind:value={statusNotesInput.value}
						name="comments"
						placeholder="notes"
						class="relative"
					/>
				</div>
			</div>

			<div class="m-2 flex items-center justify-between">
				<Button href="/issues/table">
					{messages['updateServiceRequest']['button_back']}
				</Button>

				<Button type="primary" on:click={updateServiceRequest}>
					{messages['updateServiceRequest']['button_submit']}
				</Button>
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
