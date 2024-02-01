<script lang="ts">
	import messages from '$media/messages.json';
	import { Input } from 'stwui';
	import { mailIcon } from '$lib/assets/mailIcon.js';
	import { phoneIcon } from '$lib/assets/phoneIcon.js';
	import { checkPhoneNumber } from '$lib/utils/functions';
	import type { CreateServiceRequestParams } from '$lib/services/Libre311/Libre311';
	import { createUnvalidatedInput, nullishCoalesceEmailValidator } from '$lib/utils/validation';
	import { createEventDispatcher } from 'svelte';
	import type { StepChangeEvent } from './types';

	import StepControls from './StepControls.svelte';

	const dispatch = createEventDispatcher<StepChangeEvent>();

	export let params: Readonly<Partial<CreateServiceRequestParams>>;

	let firstNameError: string | undefined;
	let lastNameError: string | undefined;
	let emailError: string | undefined;
	let phoneError: string | undefined;

	$: btnText =
		params.first_name || params.last_name || params.email || params.phone
			? messages['contact']['button']['submit']
			: messages['contact']['button']['skip'];

	function handleSubmit() {
		let emailValidity = nullishCoalesceEmailValidator(createUnvalidatedInput(params.email));
		emailError = emailValidity.type == 'invalid' ? emailValidity.error : '';
		phoneError = checkPhoneNumber(params.phone);

		if (
			(firstNameError == '' || firstNameError == undefined) &&
			(lastNameError == '' || lastNameError == undefined) &&
			(emailError == '' || emailError == undefined) &&
			(phoneError == '' || phoneError == undefined)
		)
			dispatch('stepChange', params);
	}

	function formatPhoneNumber() {
		params.phone = params.phone
			.replace(/\D/g, '') // Remove non-digit characters
			.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3'); // Format with hyphens
	}
</script>

<div class="flex h-full items-center justify-center">
	<div class="flex h-full flex-col">
		<div class="mt-4 flex-grow">
			<h1 class="text-lg">{messages['contact']['header']}</h1>
			<p class="my-2 text-sm">{messages['contact']['body']}</p>

			<div class="my-2">
				<Input
					allowClear
					type="text"
					name="firstName"
					placeholder={messages['contact']['name']['first_name']['placeholder']}
					error={firstNameError}
					bind:value={params.first_name}
				>
					<Input.Label slot="label">{messages['contact']['name']['label']}</Input.Label>
				</Input>
				<Input
					allowClear
					type="text"
					name="lastName"
					placeholder={messages['contact']['name']['last_name']['placeholder']}
					error={lastNameError}
					bind:value={params.last_name}
				></Input>
			</div>

			<div class="my-2">
				<Input
					allowClear
					name="email"
					type="email"
					placeholder={messages['contact']['email']['placeholder']}
					error={emailError}
					bind:value={params.email}
				>
					<Input.Label slot="label">{messages['contact']['email']['label']}</Input.Label>
					<Input.Leading slot="leading" params={mailIcon} />
				</Input>
			</div>

			<div class="my-2">
				<Input
					allowClear
					type="text"
					name="phone"
					placeholder={messages['contact']['phone']['placeholder']}
					error={phoneError}
					bind:value={params.phone}
					on:input={formatPhoneNumber}
				>
					<Input.Label slot="label">{messages['contact']['phone']['label']}</Input.Label>
					<Input.Leading slot="leading" params={phoneIcon} />
				</Input>
			</div>
		</div>

		<StepControls on:click={handleSubmit}>
			<svelte:fragment slot="submit-text">{btnText}</svelte:fragment>
		</StepControls>
	</div>
</div>
