<script lang="ts">
	import messages from '$media/messages.json';
	import { Button, Input, Select, TextArea } from 'stwui';
	import {
		ServiceRequestPrioritySchema,
		type ServiceRequest,
		type ServiceRequestPriority,
		type ServiceRequestStatus,
		ServiceRequestStatusSchema
	} from '$lib/services/Libre311/Libre311';

	import {
		createInput,
		inputValidatorFactory,
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
		serviceRequestStatusSelectOptions,
		setUpAlertRole
	} from '$lib/utils/functions';
	import { z } from 'zod';

	const dispatch = createEventDispatcher<{
		updateServiceRequest: UpdateSensitiveServiceRequestRequest;
		cancel: void;
	}>();

	export let serviceRequest: ServiceRequest;

	let statusInput = createInput<ServiceRequestStatus>(serviceRequest.status);
	let priorityInput = createInput<ServiceRequestPriority | undefined>(serviceRequest.priority);
	let expectedDateInput = createInput<string | undefined>(
		serviceRequest.expected_datetime?.slice(0, 10)
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

	function userChangedDate(expectedDateInputValue: String | undefined) {
		return serviceRequest.expected_datetime != expectedDateInputValue;
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

	export const dateValidator = inputValidatorFactory(
		z.union([
			z.literal(''),
			z
				.string()
				.regex(/^\d{4}-\d{2}-\d{2}$/)
				.optional()
		])
	);

	async function updateServiceRequest(s: ServiceRequest) {
		expectedDateInput = dateValidator(expectedDateInput);
		statusInput = statusValidator(statusInput);
		priorityInput = optionalPriorityValidator(priorityInput);
		agencyNameInput = optionalCoalesceStringValidator(agencyNameInput);
		agencyEmailInput = optionalCoalesceEmailValidator(agencyEmailInput);
		serviceNoticeInput = optionalCoalesceStringValidator(serviceNoticeInput);
		statusNotesInput = optionalCoalesceStringValidator(statusNotesInput);

		if (expectedDateInput.type == 'invalid') return;
		if (statusInput.type == 'invalid') return;
		if (priorityInput.type == 'invalid') return;
		if (agencyNameInput.type == 'invalid') return;
		if (agencyEmailInput.type == 'invalid') return;
		if (serviceNoticeInput.type == 'invalid') return;
		if (statusNotesInput.type == 'invalid') return;

		let localDateString: string | null = null; // Null will clear in the backend
		if (expectedDateInput.value) {
			const [y, m, d] = expectedDateInput.value.split('-').map(Number);
			localDateString = new Date(y, m - 1, d).toISOString();
		}

		const sensitiveServiceRequest: UpdateSensitiveServiceRequestRequest = {
			...s,
			status: statusInput.value,
			priority: priorityInput.value,
			expected_datetime: localDateString,
			agency_responsible: agencyNameInput.value,
			agency_email: agencyEmailInput.value,
			service_notice: serviceNoticeInput.value,
			status_notes: statusNotesInput.value
		};

		dispatch('updateServiceRequest', sensitiveServiceRequest);
	}

	let agencyRoot: HTMLElement;

	$: setUpAlertRole(agencyEmailInput, agencyRoot, 'input#email', 'email-error');
</script>

<form>
	<!-- UPDATE EXPECTED TIMESTAMP -->
	<div class="mb-1">
		<label class="block py-2 text-sm font-medium">
			<strong class="text-base">{messages['serviceRequest']['expected_datetime']}</strong>
			<div class="mt-1 flex gap-2">
				<input
					type="date"
					name="expected-completion"
					bind:value={expectedDateInput.value}
					class="block w-full rounded-md border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none"
				/>
				<button
					type="button"
					class="stwui-button m-2 focus:outline-none"
					on:click={() => (expectedDateInput.value = '')}>Clear</button
				>
			</div>
		</label>
		{#if expectedDateInput.error}
			<p id="expected-completion-error" class="mt-1 text-sm text-danger">
				{expectedDateInput.error}
			</p>
		{/if}

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
		<div bind:this={agencyRoot} class="mb-1">
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
