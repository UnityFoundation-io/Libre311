<script lang="ts">
	import messages from '$media/messages.json';
	import { Badge, Button, Card, Input, Select } from 'stwui';
	import type { ServiceRequest, ServiceRequestStatus } from '$lib/services/Libre311/Libre311';
	import Flag from '$lib/components/Svg/Flag.svelte';
	import clockIcon from '$lib/assets/Clock.svg';
	import { toTimeStamp } from '$lib/utils/functions';
	import type { SelectOption } from 'stwui/types';
	import {
		createInput,
		optionalCoalesceNameValidator,
		type FormInputValue,
		optionalCoalesceEmailValidator,
		optionalCoalescePhoneNumberValidator
	} from '$lib/utils/validation';
	import { mailIcon } from '$lib/components/Svg/outline/mailIcon.js';
	import { phoneIcon } from '$lib/components/Svg/outline/phoneIcon.js';
	import { calendarIcon } from '$lib/components/Svg/outline/calendarIcon.js';
	import { DatePicker } from 'stwui';

	export let serviceRequest: ServiceRequest;

	let agencyNameInput: FormInputValue<string | undefined | null> = createInput(
		serviceRequest.agency_responsible //.name
	);
	let agencyEmailInput: FormInputValue<string | undefined> = createInput(''); // TODO
	let agencyPhoneInput: FormInputValue<string | undefined> = createInput(''); // TODO

	const statusOptions: SelectOption[] = [
		{
			value: 'open',
			label: 'Open'
		},
		{
			value: 'assigned',
			label: 'Assigned'
		},
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

	function createName(serviceRequest: ServiceRequest) {
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

	function formatPhoneNumber(e: Event) {
		const target = e.target as HTMLInputElement;
		if (target.value) {
			let phoneNumber = target.value;
			phoneNumber = phoneNumber
				.replace(/\D/g, '') // Remove non-digit characters
				.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3'); // Format with hyphens
			return phoneNumber;
		}
	}

	function updateServiceRequest() {
		agencyNameInput = optionalCoalesceNameValidator(agencyNameInput);
		agencyEmailInput = optionalCoalesceEmailValidator(agencyEmailInput);
		agencyPhoneInput = optionalCoalescePhoneNumberValidator(agencyPhoneInput);

		const resultSet = new Set([agencyNameInput.type, agencyEmailInput.type, agencyPhoneInput.type]);
		if (resultSet.has('invalid')) return;

		serviceRequest = {
			...serviceRequest,
			agency_responsible: agencyNameInput.value,
			expected_datetime: expected_datetime.toISOString()
		};

		console.log(serviceRequest);
		const msg: string = `TODO: Update Service Request`;
		alert(msg);
	}

	$: name = createName(serviceRequest);
	$: expected_datetime = new Date();
</script>

<div class="flex h-full">
	<Card class="m-2">
		<div class="flex h-full w-full flex-col">
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

				<!-- TODO -->
				<!-- {#each serviceRequest.attributeMap.values() as attributes}
					<div class="mb-2">
						<strong class="text-base">{messages['serviceRequest']['attributes']}</strong>
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
				{/each} -->

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

				{#if serviceRequest.agency_responsible}
					<div class="mb-1">
						<strong class="text-base">{messages['serviceRequest']['agency_contact']}</strong>
						<p class="text-sm">{serviceRequest.agency_responsible ?? ''}</p>
					</div>
				{/if}

				{#if serviceRequest.service_notice}
					<div class="mb-1">
						<strong class="text-base">{messages['serviceRequest']['service_notice']}</strong>
						<p class="text-sm">{serviceRequest.service_notice ?? ''}</p>
					</div>
				{/if}

				{#if serviceRequest.status_notes}
					<div class="mb-1">
						<h2 class="text-base">{messages['serviceRequest']['status_notes']}</h2>
						<p class="text-sm">{toTimeStamp(serviceRequest.status_notes) ?? ''}</p>
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
					<Input
						allowClear
						type="text"
						name="phone"
						placeholder={messages['contact']['phone']['placeholder']}
						error={agencyPhoneInput.error}
						bind:value={agencyPhoneInput.value}
						on:input={formatPhoneNumber}
					>
						<Input.Leading slot="leading" data={phoneIcon} />
					</Input>
				</div>

				<div class="my-4">
					<DatePicker name="datetime" allowClear bind:value={expected_datetime}>
						<DatePicker.Label slot="label"
							>{messages['serviceRequest']['expected_datetime']}</DatePicker.Label
						>
						<DatePicker.Leading slot="leading" data={calendarIcon} />
					</DatePicker>
				</div>

				<div class="my-4">
					<Select
						name="select-priority"
						placeholder={serviceRequest.priority ?? 'Low'}
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
