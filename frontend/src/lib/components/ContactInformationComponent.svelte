<script lang="ts">
	import messages from '$media/messages.json';
	import { Input } from 'stwui';
	import { mailIcon } from '$lib/assets/mailIcon.js';
	import { phoneIcon } from '$lib/assets/phoneIcon.js';
	import { emailValidator, checkValid, checkPhoneNumber } from '$lib/utils/functions';
	import type { ContactInformation } from '$lib/services/Libre311/Libre311';

	export let data: ContactInformation;

	let firstNameError: string | undefined;
	let lastNameError: string | undefined;
	let emailError: string | undefined;
	let phoneError: string | undefined;

	function handleBack() {
		console.log('TODO: back not implemented');
		data.first_name = '';
		data.last_name = '';
		data.email = '';
		data.phone = '';
	}

	function handleSubmit() {
		if (data.first_name == '') {
			firstNameError = 'First name required';
		} else {
			firstNameError = '';
		}
		if (data.last_name == '') {
			lastNameError = 'Last name required';
		} else {
			lastNameError = '';
		}

		emailError = checkValid(emailValidator, data.email);
		phoneError = checkPhoneNumber(data.phone);
	}
</script>

<div class="flex h-full items-center justify-center">
	<div class="mx-4 flex flex-col" style="height: calc(100vh - var(--header-height));">
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
					bind:value={data.first_name}
				>
					<Input.Label slot="label">{messages['contact']['name']['label']}</Input.Label>
				</Input>
				<Input
					allowClear
					type="text"
					name="lastName"
					placeholder={messages['contact']['name']['last_name']['placeholder']}
					error={lastNameError}
					bind:value={data.last_name}
				></Input>
			</div>

			<div class="my-2">
				<Input
					allowClear
					name="email"
					type="email"
					placeholder={messages['contact']['email']['placeholder']}
					error={emailError}
					bind:value={data.email}
				>
					<Input.Label slot="label">{messages['contact']['email']['label']}</Input.Label>
					<Input.Leading slot="leading" data={mailIcon} />
				</Input>
			</div>

			<div class="my-2">
				<Input
					allowClear
					type="text"
					name="phone"
					placeholder={messages['contact']['phone']['placeholder']}
					error={phoneError}
					bind:value={data.phone}
				>
					<Input.Label slot="label">{messages['contact']['phone']['label']}</Input.Label>
					<Input.Leading slot="leading" data={phoneIcon} />
				</Input>
			</div>
		</div>

		<div class="mb-4">
			<div class="flex items-center justify-between">
				<button class="my-2 text-sm" type="submit" on:click|preventDefault={handleBack}>
					{messages['contact']['button']['back']}
				</button>
				<button class="submit my-2 text-sm" type="submit" on:click|preventDefault={handleSubmit}>
					{messages['contact']['button']['submit']}
				</button>
			</div>
		</div>
	</div>
</div>

<style>
	.submit {
		padding: 0.5rem 2rem;
		background-color: hsl(var(--primary));
		color: hsl(var(--primary-content));
		border: 1px solid hsl(var(--primary));
		border-radius: 10px;
	}

	.submit:hover {
		--tw-surface-opacity: 0.1;
		border: 1px solid hsl(var(--primary));
		color: hsl(var(--primary));
		background-color: hsl(var(--primary) / var(--tw-surface-opacity));
	}
</style>
