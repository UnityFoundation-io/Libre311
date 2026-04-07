<script lang="ts">
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import { createInput, passwordValidator } from '$lib/utils/validation';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { Card, Button, Input, Alert } from 'stwui';
	const authService = useLibre311Service();
	const token = $page.url.searchParams.get('token');

	let passwordInput = createInput('');
	let confirmPasswordInput = createInput('');
	let errorMessage: string | undefined;
	let successMessage: string | undefined;
	let loading = false;

	async function resetPassword() {
		errorMessage = undefined;
		successMessage = undefined;

		passwordInput = passwordValidator(passwordInput);
		if (passwordInput.value !== confirmPasswordInput.value) {
			confirmPasswordInput.type = 'invalid';
			confirmPasswordInput.error = 'Passwords do not match';
		} else {
			confirmPasswordInput.type = 'valid';
			confirmPasswordInput.error = undefined;
		}

		if (passwordInput.type === 'valid' && confirmPasswordInput.type === 'valid' && token) {
			loading = true;
			try {
				await authService.resetPassword(token, passwordInput.value!);
				successMessage = 'Your password has been reset successfully. You can now login.';
				setTimeout(() => {
					goto('/login');
				}, 3000);
			} catch (error: unknown) {
				errorMessage = 'Failed to reset password. The link may be expired or invalid.';
			} finally {
				loading = false;
			}
		}
	}
</script>

<div class="min-h-full w-full sm:flex sm:items-center sm:justify-center sm:bg-primary">
	<Card class="w-full border-none shadow-none sm:w-1/3 sm:max-w-md sm:border-solid sm:shadow-md">
		<div class="m-4 flex flex-col items-center justify-center">
			<h1 class="text-lg">Reset Password</h1>
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

			{#if !token}
				<Alert type="error" class="mb-4">
					<Alert.Title slot="title">Error</Alert.Title>
					<Alert.Description slot="description">Invalid or missing reset token.</Alert.Description>
				</Alert>
			{:else if !successMessage}
				<form on:submit|preventDefault={resetPassword} class="flex flex-col gap-4">
					<Input
						type="password"
						name="password"
						label="New Password"
						placeholder="Enter new password"
						bind:value={passwordInput.value}
						error={passwordInput.error}
					>
						<Input.Label slot="label">New Password</Input.Label>
					</Input>

					<Input
						type="password"
						name="confirmPassword"
						label="Confirm New Password"
						placeholder="Confirm new password"
						bind:value={confirmPasswordInput.value}
						error={confirmPasswordInput.error}
					>
						<Input.Label slot="label">Confirm New Password</Input.Label>
					</Input>

					<Button type="primary" htmlType="submit" {loading} class="w-full">Reset Password</Button>
				</form>
			{/if}
		</div>

		<div class="m-4 text-center">
			<a href="/login" class="text-sm underline">Back to Login</a>
		</div>
	</Card>
</div>

<style>
	:global(.sm\:bg-primary) {
		background-color: hsl(var(--primary));
	}
</style>
