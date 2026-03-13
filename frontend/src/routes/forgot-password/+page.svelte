<script lang="ts">
	import ForgotPasswordForm from '$lib/components/Login/ForgotPasswordForm.svelte';
	import type { EventDispatchTypeMap } from '$lib/components/Login/shared';
	import { createInput, emailValidator } from '$lib/utils/validation';
	import { useLibre311Service } from '$lib/context/Libre311Context';

	const authService = useLibre311Service();

	let emailInput = createInput('');
	let errorMessage: string | undefined;
	let successMessage: string | undefined;
	let loading = false;

	async function handleForgotPassword(e: CustomEvent<EventDispatchTypeMap['forgotPassword']>) {
		errorMessage = undefined;
		successMessage = undefined;

		const email = e.detail.email;
		emailInput = emailValidator(createInput(email || ''));

		if (emailInput.type === 'valid') {
			loading = true;
			try {
				await authService.forgotPassword(emailInput.value!);
				successMessage = 'If an account exists for that email, a reset link has been sent.';
			} catch (error: unknown) {
				errorMessage = 'An error occurred. Please try again later.';
			} finally {
				loading = false;
			}
		}
	}
</script>

<div class="min-h-full w-full sm:flex sm:items-center sm:justify-center sm:bg-primary">
	<ForgotPasswordForm
		{emailInput}
		bind:errorMessage
		bind:successMessage
		{loading}
		on:forgotPassword={handleForgotPassword}
	/>
</div>

<style>
	:global(.sm\:bg-primary) {
		background-color: hsl(var(--primary));
	}
</style>
