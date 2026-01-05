<script lang="ts">
	import messages from '$media/messages.json';
	import { Input } from 'stwui';
	import { mailIcon } from '$lib/components/Svg/outline/mailIcon.js';
	import { phoneIcon } from '$lib/components/Svg/outline/phoneIcon.js';

	import {
		createInput,
		optionalCoalesceNameValidator,
		type FormInputValue,
		optionalCoalesceEmailValidator,
		optionalCoalescePhoneNumberValidator
	} from '$lib/utils/validation';
	import { createEventDispatcher } from 'svelte';
	import type { CreateServiceRequestUIParams, StepChangeEvent } from './shared';

	import StepControls from './StepControls.svelte';

	const dispatch = createEventDispatcher<StepChangeEvent>();

	export let params: Readonly<Partial<CreateServiceRequestUIParams>>;

	let firstNameInput: FormInputValue<string | undefined> = createInput(params.first_name);
	let lastNameInput: FormInputValue<string | undefined> = createInput(params.last_name);
	let emailInput: FormInputValue<string | undefined> = createInput(params.email);
	let phoneInput: FormInputValue<string | undefined> = createInput(params.phone);

	$: btnText =
		firstNameInput.value || lastNameInput.value || emailInput.value || phoneInput.value
			? messages['contact']['button']['submit']
			: messages['contact']['button']['skip'];

	function handleSubmit() {
		firstNameInput = optionalCoalesceNameValidator(firstNameInput);
		lastNameInput = optionalCoalesceNameValidator(lastNameInput);
		emailInput = optionalCoalesceEmailValidator(emailInput);
		phoneInput = optionalCoalescePhoneNumberValidator(phoneInput);

		const resultSet = new Set([
			firstNameInput.type,
			lastNameInput.type,
			emailInput.type,
			phoneInput.type
		]);
		if (resultSet.has('invalid')) return;

		const updatedParams = {
			first_name: firstNameInput.value,
			last_name: lastNameInput.value,
			phone: phoneInput.value,
			email: emailInput.value
		};

		dispatch('stepChange', updatedParams);
	}

	function formatPhoneNumber() {
		if (!phoneInput.value) return;
		phoneInput.value = phoneInput.value
			.replace(/\D/g, '') // Remove non-digit characters
			.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3'); // Format with hyphens
		phoneInput = phoneInput;
	}

	// pass svelte checks
	// eslint-disable-next-line @typescript-eslint/no-explicit-any
	const telAutocomplete = 'tel' as any;
	// eslint-disable-next-line @typescript-eslint/no-explicit-any
	const emailAutocomplete = 'email' as any;
</script>

<form class="flex h-full items-center justify-center">
	<div class="flex h-full flex-col">
		<div class="mt-4 flex-grow">
			<h1 class="text-lg">{messages['contact']['header']}</h1>
			<p class="my-2 text-sm">{messages['contact']['body']}</p>

			<div class="my-2">
				<Input
					type="text"
					name="firstName"
					placeholder={messages['contact']['name']['first_name']['placeholder']}
					error={firstNameInput.error}
					bind:value={firstNameInput.value}
				>
					<Input.Label slot="label">{messages['contact']['name']['label']}</Input.Label>
				</Input>
				<Input
					type="text"
					name="lastName"
					placeholder={messages['contact']['name']['last_name']['placeholder']}
					error={lastNameInput.error}
					bind:value={lastNameInput.value}
				></Input>
			</div>

			<div class="my-2">
				<Input
					name="email"
					type="email"
					placeholder={messages['contact']['email']['placeholder']}
					autocomplete={emailAutocomplete}
					error={emailInput.error}
					bind:value={emailInput.value}
				>
					<Input.Label slot="label">{messages['contact']['email']['label']}</Input.Label>
					<Input.Leading slot="leading" data={mailIcon} />
				</Input>
			</div>

			<div class="my-2">
				<Input
					type="text"
					name="phone"
					placeholder={messages['contact']['phone']['placeholder']}
					autocomplete={telAutocomplete}
					error={phoneInput.error}
					bind:value={phoneInput.value}
					on:input={formatPhoneNumber}
				>
					<Input.Label slot="label">{messages['contact']['phone']['label']}</Input.Label>
					<Input.Leading slot="leading" data={phoneIcon} />
				</Input>
			</div>
		</div>

		<StepControls on:click={handleSubmit}>
			<svelte:fragment slot="submit-text">{btnText}</svelte:fragment>
		</StepControls>
	</div>
</form>
