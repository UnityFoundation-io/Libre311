<script lang="ts">
	import { Card, Button, Input, Alert } from 'stwui';
	import { createEventDispatcher } from 'svelte';
	import { dispatchEventFunctionFactory, type EventDispatchTypeMap } from './shared';
	import { emailValidator } from '$lib/utils/validation';
	import type { FormInputValue } from '$lib/utils/validation';

	export let emailInput: FormInputValue<string | undefined>;
	export let errorMessage: string | undefined;
	export let successMessage: string | undefined;
	export let loading = false;

	const dispatch = createEventDispatcher<EventDispatchTypeMap>();
	const { onForgotPassword } = dispatchEventFunctionFactory(dispatch);

	function forgotPassword() {
		const validatedEmail = emailValidator({ ...emailInput, value: emailInput.value || '' });
		if (validatedEmail.type === 'valid') {
			onForgotPassword(validatedEmail.value);
		}
	}
</script>

<Card class="w-full border-none shadow-none sm:w-1/3 sm:max-w-md sm:border-solid sm:shadow-md">
	<div class="m-4 flex flex-col items-center justify-center">
		<h1 class="text-lg">Forgot Password</h1>
	</div>

	<div class="m-4">
		{#if errorMessage}
			<Alert type="error" class="mb-4">
				<Alert.Title slot="title">Error</Alert.Title>
				<Alert.Description slot="description">{errorMessage}</Alert.Description>
			</Alert>
		{/if}

		{#if successMessage}
			<Alert type="success" class="mb-4">
				<Alert.Title slot="title">Success</Alert.Title>
				<Alert.Description slot="description">{successMessage}</Alert.Description>
			</Alert>
		{/if}

		<form on:submit|preventDefault={forgotPassword} class="flex flex-col gap-4">
			<p>Enter your email address and we will send you a link to reset your password.</p>
			<Input
				type="email"
				name="email"
				label="Email"
				placeholder="Enter your email"
				bind:value={emailInput.value}
				error={emailInput.error}
			>
				<Input.Label slot="label">Email</Input.Label>
			</Input>

			<Button type="primary" htmlType="submit" {loading} class="w-full">Send Reset Link</Button>
		</form>
	</div>

	<div class="m-4 text-center">
		<a href="/login" class="text-sm underline">Back to Login</a>
	</div>
</Card>
