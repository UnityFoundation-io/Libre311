<script lang="ts">
	import messages from '$media/messages.json';
	import { Button, DatePicker, Input, Select, TextArea } from 'stwui';
	import {
		ServiceRequestPrioritySchema,
		type ServiceRequest,
		type ServiceRequestPriority,
		type ServiceRequestStatus,
		ServiceRequestStatusSchema
	} from '$lib/services/Libre311/Libre311';
	import { calendarIcon } from './Svg/outline/calendarIcon';

	import {
		createInput,
		optionalCoalesceEmailValidator,
		optionalCoalesceStringValidator,
		optionalPriorityValidator,
		statusValidator
	} from '$lib/utils/validation';
	import { wrenchScrewDriverIcon } from './Svg/outline/wrench-screwdriver';
	import { mailIcon } from './Svg/outline/mailIcon';
	import { user } from './Svg/outline/user';
	import type { UpdateSensitiveServiceRequestRequest } from '$lib/services/Libre311/types/UpdateSensitiveServiceRequest';
	import { createEventDispatcher } from 'svelte';
	import ServiceRequestButtonsContainer from './ServiceRequestButtonsContainer.svelte';
	import {
		serviceRequestPrioritySelectOptions,
		serviceRequestStatusSelectOptions
	} from '$lib/utils/functions';

	const dispatch = createEventDispatcher<{
		updateServiceRequest: UpdateSensitiveServiceRequestRequest;
		cancel: void;
	}>();

	export let serviceRequest: ServiceRequest;

	let statusInput = createInput<ServiceRequestStatus>(serviceRequest.status);
	let priorityInput = createInput<ServiceRequestPriority | undefined>(serviceRequest.priority);
	let expectedDateInput = createInput<Date | undefined>(
		serviceRequest.expected_datetime ? new Date(serviceRequest.expected_datetime) : undefined
	);
	let agencyNameInput = createInput<string | undefined>(serviceRequest.agency_responsible);
	let agencyEmailInput = createInput<string | undefined>(serviceRequest.agency_email);
	let serviceNoticeInput = createInput<string | undefined>(serviceRequest.service_notice);
	let statusNotesInput = createInput<string | undefined>(serviceRequest.status_notes);

	$: hasUserInput =
		statusInput.value != serviceRequest.status ||
		priorityInput.value != serviceRequest.priority ||
		userChangedDate(expectedDateInput.value) ||
		userChangedText(agencyNameInput.value, serviceRequest.agency_responsible) ||
		userChangedText(agencyEmailInput.value, serviceRequest.agency_email) ||
		userChangedText(serviceNoticeInput.value, serviceRequest.service_notice) ||
		userChangedText(statusNotesInput.value, serviceRequest.status_notes);

	function userChangedDate(expectedDateInputValue: Date | undefined) {
		const currentDate = serviceRequest.expected_datetime;
		const expectedDate = expectedDateInputValue?.toISOString().replace(/\.\d+/g, '');
		return currentDate != expectedDate;
	}

	function userChangedText(expectedText: string | undefined, currentText: string | undefined) {
		if (currentText == undefined) {
			return !((expectedText == '' || expectedText == undefined) && currentText == undefined);
		} else {
			return expectedText != currentText;
		}
	}

	function updateStatus(e: Event) {
		const target = e.target as HTMLInputElement;
		statusInput.value = ServiceRequestStatusSchema.parse(target.value);
	}

	function updatePriority(e: Event) {
		const target = e.target as HTMLInputElement;
		priorityInput.value = ServiceRequestPrioritySchema.parse(target.value);
	}

	async function updateServiceRequest(s: ServiceRequest) {
		statusInput = statusValidator(statusInput);
		priorityInput = optionalPriorityValidator(priorityInput);
		agencyNameInput = optionalCoalesceStringValidator(agencyNameInput);
		agencyEmailInput = optionalCoalesceEmailValidator(agencyEmailInput);
		serviceNoticeInput = optionalCoalesceStringValidator(serviceNoticeInput);
		statusNotesInput = optionalCoalesceStringValidator(statusNotesInput);

		if (statusInput.type == 'invalid') return;
		if (priorityInput.type == 'invalid') return;
		if (expectedDateInput.type == 'invalid') return;
		if (agencyNameInput.type == 'invalid') return;
		if (agencyEmailInput.type == 'invalid') return;
		if (serviceNoticeInput.type == 'invalid') return;
		if (statusNotesInput.type == 'invalid') return;

		const sensitiveServiceRequest: UpdateSensitiveServiceRequestRequest = {
			...s,
			status: statusInput.value,
			priority: priorityInput.value,
			expected_datetime: expectedDateInput.value?.toISOString(),
			agency_responsible: agencyNameInput.value,
			agency_email: agencyEmailInput.value,
			service_notice: serviceNoticeInput.value,
			status_notes: statusNotesInput.value
		};

		dispatch('updateServiceRequest', sensitiveServiceRequest);
	}
</script>

<form>
	<!-- UPDATE EXPECTED TIMESTAMP -->
	<DatePicker name="datetime" bind:value={expectedDateInput.value}>
		<DatePicker.Label slot="label">
			<strong class="text-base">
				{messages['serviceRequest']['expected_datetime']}
			</strong>
		</DatePicker.Label>
		<DatePicker.Leading slot="leading" data={calendarIcon} />
	</DatePicker>

	<!-- UPDATE STATUS -->
	<div class="mb-1">
		<Select
			name="select-status"
			placeholder={serviceRequest.status.charAt(0).toUpperCase() + serviceRequest.status.slice(1)}
			options={serviceRequestStatusSelectOptions}
			on:change={updateStatus}
		>
			<Select.Label slot="label">
				<strong class="text-base">{messages['serviceRequest']['status']}</strong>
			</Select.Label>
			<Select.Options slot="options">
				{#each serviceRequestStatusSelectOptions as option}
					<Select.Options.Option {option} />
				{/each}
			</Select.Options>
		</Select>
	</div>

	<!-- UPDATE PRIORITY -->
	<div class="mb-1">
		<Select
			name="select-priority"
			placeholder={serviceRequest.priority
				? `${serviceRequest.priority.charAt(0).toUpperCase()}${serviceRequest.priority.slice(1)}`
				: '--'}
			options={serviceRequestPrioritySelectOptions}
			on:change={updatePriority}
		>
			<Select.Label slot="label">
				<strong class="text-base">
					{messages['serviceRequest']['priority']}
				</strong>
			</Select.Label>
			<Select.Options slot="options">
				{#each serviceRequestPrioritySelectOptions as option}
					<Select.Options.Option {option} />
				{/each}
			</Select.Options>
		</Select>
	</div>

	<!-- UPDATE AGENCY -->
	<div class="mb-1">
		<Input
			type="text"
			name="firstName"
			placeholder={messages['serviceRequest']['agency_name']}
			error={agencyNameInput.error}
			bind:value={agencyNameInput.value}
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
			error={agencyEmailInput.error}
			bind:value={agencyEmailInput.value}
		>
			<Input.Label slot="label">
				<strong class="text-base">
					{messages['serviceRequest']['agency_contact_email']}
				</strong>
			</Input.Label>
			<Input.Leading slot="leading" data={mailIcon} />
		</Input>
	</div>

	<!-- UPDATE SERVICE NOTICE -->
	<div class="mb-1">
		<Input
			type="text"
			name="firstName"
			placeholder={messages['serviceRequest']['service_notice_placeholder']}
			bind:value={serviceNoticeInput.value}
		>
			<Input.Label slot="label">
				<strong class="text-base">{messages['serviceRequest']['service_notice']}</strong>
			</Input.Label>
			<Input.Leading slot="leading" data={wrenchScrewDriverIcon} />
		</Input>
	</div>

	<!-- UPDATE STATUS NOTES -->
	<div class="mb-1 flex flex-col">
		<strong class="text-base">{messages['serviceRequest']['status_notes']}</strong>
		<TextArea
			bind:value={statusNotesInput.value}
			name="comments"
			placeholder="notes"
			class="relative"
		/>
	</div>
</form>

<ServiceRequestButtonsContainer>
	<Button
		slot="left"
		on:click={() => {
			dispatch('cancel');
		}}
	>
		{messages['updateServiceRequest']['button_cancel']}
	</Button>
	<Button
		disabled={!hasUserInput}
		slot="right"
		type={!hasUserInput ? undefined : 'primary'}
		on:click={() => updateServiceRequest(serviceRequest)}
	>
		Submit
	</Button>
</ServiceRequestButtonsContainer>
