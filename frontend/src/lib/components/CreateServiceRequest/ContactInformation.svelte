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

	interface Props {
		params: Readonly<Partial<CreateServiceRequestUIParams>>;
	}

	let { params }: Props = $props();

	let firstNameInput: FormInputValue<string | undefined> = $state(createInput(params.first_name));
	let lastNameInput: FormInputValue<string | undefined> = $state(createInput(params.last_name));
	let emailInput: FormInputValue<string | undefined> = $state(createInput(params.email));
	let phoneInput: FormInputValue<string | undefined> = $state(createInput(params.phone));

	let btnText =
		$derived(firstNameInput.value || lastNameInput.value || emailInput.value || phoneInput.value
			? messages['contact']['button']['submit']
			: messages['contact']['button']['skip']);

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
					{#snippet label()}
										<Input.Label >{messages['contact']['name']['label']}</Input.Label>
									{/snippet}
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
					error={emailInput.error}
					bind:value={emailInput.value}
				>
					{#snippet label()}
										<Input.Label >{messages['contact']['email']['label']}</Input.Label>
									{/snippet}
					{#snippet leading()}
										<Input.Leading  data={mailIcon} />
									{/snippet}
				</Input>
			</div>

			<div class="my-2">
				<Input
					type="text"
					name="phone"
					placeholder={messages['contact']['phone']['placeholder']}
					error={phoneInput.error}
					bind:value={phoneInput.value}
					on:input={formatPhoneNumber}
				>
					{#snippet label()}
										<Input.Label >{messages['contact']['phone']['label']}</Input.Label>
									{/snippet}
					{#snippet leading()}
										<Input.Leading  data={phoneIcon} />
									{/snippet}
				</Input>
			</div>
		</div>

		<StepControls on:click={handleSubmit}>
			<!-- @migration-task: migrate this slot by hand, `submit-text` is an invalid identifier -->
	<svelte:fragment slot="submit-text">{btnText}</svelte:fragment>
		</StepControls>
	</div>
</form>
